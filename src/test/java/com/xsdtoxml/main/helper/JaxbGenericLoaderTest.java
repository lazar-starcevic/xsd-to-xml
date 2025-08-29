package com.xsdtoxml.main.helper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.xml.namespace.QName;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mockito;

import com.xsdtoxml.main.generated.Address;
import com.xsdtoxml.main.generated.Person;

import jakarta.xml.bind.JAXBElement;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.UnmarshalException;

class JaxbGenericLoaderTest {

   @TempDir
   private Path tmp;

   private JaxbGenericLoader loader;

   @BeforeEach
   void setup() throws JAXBException {
      this.loader = new JaxbGenericLoader(Person.class, Address.class);
   }

   @Test
   void givenSetupWhenLoaderIsConstructedThenLoaderNotNull() {
      // THEN
      assertNotNull(loader);
   }

   @Test
   void givenInvalidPathWhenLoaderIsConstructedThenThrowException() {
      // GIVEN
      String nonExistingPackage = "non.existing.package";

      // THEN
      assertThrows(JAXBException.class, () -> new JaxbGenericLoader(nonExistingPackage));
   }

   @Test
   void givenValidXmlWhenResultIsLoadedThenAssertResult() throws Exception {
      // GIVEN
      Path xml = write("person-valid.xml", getValidXml());
      Person result;

      // WHEN
      result = loader.load(xml, Person.class);

      // THEN
      assertNotNull(result);
      assertEquals("Jane", result.getFirstName());
      assertEquals(30, result.getAge());
   }

   @Test
   void givenMalformedXmlWhenLoadThenThrowException() throws Exception {
      // GIVEN
      String xmlContent = """
        <person>
            <firstName>Jane</firstName>
        """;
      Path incompleteXml;

      // WHEN
      incompleteXml = write("incomplete.xml", xmlContent);

      // THEN
      assertThrows(JAXBException.class, () -> loader.load(incompleteXml, Person.class));
   }

   @Test
   void givenNonExistingPathWhenLoadThenThrowException() {
      // GIVEN
      Path nonExistingPath = tmp.resolve("non-existing.xml");

      // THEN
      assertThrows(IOException.class, () -> loader.load(nonExistingPath, Class.class));
   }

   @Test
   void givenXmlAndXsdWhenValidateThenNoExceptionIsThrown() throws Exception {
      // GIVEN
      Path xml = write("person-valid.xml", getValidXml());
      Path xsd = write("person.xsd", getValidXsd());
      Object result;

      // WHEN
      result = loader.loadValidated(xml, xsd, Person.class);

      // THEN
      assertNotNull(result);
   }

   @Test
   void givenInvalidXmlWhenValidateThenThrowException() throws Exception {
      // GIVEN
      String invalidXml = """
        <person>
          <firstName>Jane</firstName>
          <lastName>Doe</lastName>
          <age>thirty</age>
        </person>
        """;
      Path xml = write("person-invalid.xml", invalidXml);
      Path xsd = write("person.xsd", getValidXsd());

      // THEN
      assertThrows(UnmarshalException.class, () -> loader.loadValidated(xml, xsd, Person.class));
   }

   @Test
   void givenElementWhenUnwrapThenElementIsUnchanged() {
      // GIVEN
      String value = "value";
      JAXBElement<String> element = new JAXBElement<>(new QName("qname"), String.class, value);
      Object out;

      // WHEN
      out = loader.unwrap(element);

      // THEN
      assertEquals(value, out);
   }

   @Test
   void givenObjectWhenQNameReadThenNamesMatch() {
      // GIVEN
      Person person = getPerson();

      // WHEN
      QName qName = loader.getRootQName(person);

      // THEN
      assertEquals("Person", qName.getLocalPart());
   }

   @Test
   void givenObjectWithoutRootElementAnnotationWhenReadQNameThenNameIsUnknown() {
      // GIVEN
      Address a = getAddress();

      // WHEN
      QName q = loader.getRootQName(a);

      // THEN
      assertEquals("unknown", q.getLocalPart());
   }

   @Test
   void givenNullAsObjectWhenPrettyPrintThenNullStringIsReturned() {
      // GIVEN
      String prettyPrint;

      // WHEN
      prettyPrint = loader.prettyPrint(isNull());

      // THEN
      assertEquals("null", prettyPrint);
   }

   @Test
   void givenObjectWhenPrettyPrintThenXmlContainsTags() {
      // GIVEN
      Person person = getPerson();
      String xml;

      // WHEN
      xml = loader.prettyPrint(person);

      // THEN
      assertTrue(xml.contains("<Person>"));
      assertTrue(xml.contains("</Person>"));
      assertTrue(xml.contains("\n"));
   }

   @Test
   void givenObjectWithoutRootElementAnnotationWhenPrettyPrintThenXmlContainsUnknown() {
      // GIVEN
      Address address = getAddress();
      String xml;

      // WHEN
      xml = loader.prettyPrint(address);

      // THEN
      assertTrue(xml.contains("<unknown>"));
      assertTrue(xml.contains("</unknown>"));
   }

   @Test
   void givenClassNotInContextWhenPrettyPrintThenThrowException() {
      // GIVEN
      class NotInContext {
      }

      NotInContext not = new NotInContext();

      // WHEN
      IllegalStateException exception = assertThrows(
        IllegalStateException.class,
        () -> loader.prettyPrint(not)
      );

      // THEN
      assertTrue(exception.getMessage().contains("Failed to marshal"));
      assertNotNull(exception.getCause());
      assertTrue(exception.getCause() instanceof JAXBException);
   }

   @Test
   void givenLoaderWithNullRootQNameReturnWhenPrettyPrintThenXmlContainsRoot() {
      // GIVEN
      JaxbGenericLoader spiedLoader = Mockito.spy(loader);
      doReturn(null).when(spiedLoader).getRootQName(any());

      // WHEN
      String xml = spiedLoader.prettyPrint(getAddress());

      // THEN
      assertTrue(xml.contains("<root>"));
      assertTrue(xml.contains("</root>"));
   }

   @Test
   void givenNullOnUnwrapWhenLoadThenReturnNull() throws IOException, JAXBException {
      // GIVEN
      JaxbGenericLoader spiedLoader = spy(loader);
      doReturn(null).when(spiedLoader).unwrap(any());
      Path path = write("person-valid.xml", getValidXml());
      Person person;

      // WHEN
      person = spiedLoader.load(path, Person.class);

      // THEN
      assertNull(person);
   }

   @Test
   void givenWrongTypeOnUnwrapWhenLoadThenReturnNull() throws IOException {
      // GIVEN
      JaxbGenericLoader spiedLoader = spy(loader);
      doReturn(getAddress()).when(spiedLoader).unwrap(any());
      Path path = write("person-valid.xml", getValidXml());
      JAXBException exception;

      // WHEN
      exception = assertThrows(
        JAXBException.class,
        () -> spiedLoader.load(path, Person.class)
      );

      // THEN
      assertTrue(exception.getMessage().contains("is not assignable"));
   }

   private Path write(String name, String content) throws IOException {
      Path path = tmp.resolve(name);
      Files.writeString(path, content);
      return path;
   }

   private String getValidXml() {
      return """
          <Person>
              <firstName>Jane</firstName>
              <lastName>Doe</lastName>
              <age>30</age>
          </Person>
        """;
   }

   private String getValidXsd() {
      return """
        <xsd:schema xmlns:xsd="http://www.w3.org/2001/XMLSchema" elementFormDefault="qualified">
          <xsd:element name="Person" type="PersonType"/>
          <xsd:complexType name="PersonType">
            <xsd:sequence>
              <xsd:element name="firstName" type="xsd:string"/>
              <xsd:element name="lastName" type="xsd:string"/>
              <xsd:element name="age" type="xsd:int"/>
            </xsd:sequence>
          </xsd:complexType>
        </xsd:schema>
        """;
   }

   private Person getPerson() {
      return new Person("Jane", "Doe", 30);
   }

   private Address getAddress() {
      return new Address("Street", "City");
   }
}
