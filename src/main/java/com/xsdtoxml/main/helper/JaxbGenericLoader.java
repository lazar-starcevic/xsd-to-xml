package com.xsdtoxml.main.helper;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.SchemaFactory;

import org.xml.sax.SAXException;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBElement;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.JAXBIntrospector;
import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.Unmarshaller;

public final class JaxbGenericLoader {
   private final JAXBContext ctx;

   public JaxbGenericLoader(String contextPath) throws JAXBException {
      this.ctx = JAXBContext.newInstance(contextPath);
   }

   public JaxbGenericLoader(Class<?>... boundTypes) throws JAXBException {
      this.ctx = JAXBContext.newInstance(boundTypes);
   }

   public <T> T load(Path xmlPath, Class<T> type) throws IOException, JAXBException {
      try (InputStream inputStream = Files.newInputStream(xmlPath)) {
         Unmarshaller unmarshaller = ctx.createUnmarshaller();
         Object root = unmarshaller.unmarshal(new StreamSource(inputStream));
         Object unwrappedObject = unwrap(root);
         return coerce(unwrappedObject, type);
      }
   }

   public <T> T loadValidated(Path xmlPath, Path xsdPath, Class<T> type) throws IOException, JAXBException, SAXException {
      try (InputStream inputStream = Files.newInputStream(xmlPath)) {
         Unmarshaller unmarshaller = ctx.createUnmarshaller();
         SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
         unmarshaller.setSchema(schemaFactory.newSchema(xsdPath.toFile()));
         Object root = unmarshaller.unmarshal(new StreamSource(inputStream));
         Object unwrappedObject = unwrap(root);
         return coerce(unwrappedObject, type);
      }
   }

   public Object unwrap(Object object) {
      if (object instanceof JAXBElement<?> element) {
         return element.getValue();
      }
      return object;
   }

   public <T> QName getRootQName(T root) {
      JAXBIntrospector introspector = ctx.createJAXBIntrospector();
      QName name = introspector.getElementName(root);

      if (name == null) {
         JAXBElement<T> element = new JAXBElement<>(new QName("unknown"),
           (Class<T>) root.getClass(), root);
         name = introspector.getElementName(element);
      }
      return name;
   }

   public <T> String prettyPrint(T root) {
      if (root == null) {
         return "null";
      }

      try {
         Marshaller marshaller = ctx.createMarshaller();
         marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
         StringWriter stringWriter = new StringWriter();
         JAXBIntrospector introspector = ctx.createJAXBIntrospector();

         if (introspector.isElement(root)) {
            marshaller.marshal(root, stringWriter);
         } else {
            QName qName = getRootQName(root);
            if (qName == null) {
               qName = new QName("root");
            }

            JAXBElement<T> element = new JAXBElement<>(qName,
              (Class<T>) root.getClass(), root);
            marshaller.marshal(element, stringWriter);
         }
         return stringWriter.toString();
      } catch (Exception e) {
         throw new IllegalStateException("Failed to marshal " + root.getClass().getName(), e);
      }
   }

   private <T> T coerce(Object value, Class<T> type) throws JAXBException {
      if (value == null) {
         return null;
      }

      if (!type.isInstance(value)) {
         throw new JAXBException("Unmarshalled type " + value.getClass().getName()
           + " is not assignable to " + type.getName());
      }

      return type.cast(value);
   }
}
