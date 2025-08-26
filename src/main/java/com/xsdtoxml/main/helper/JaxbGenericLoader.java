package com.xsdtoxml.main.helper;

import jakarta.xml.bind.*;

import javax.xml.transform.stream.StreamSource;
import javax.xml.namespace.QName;
import javax.xml.validation.SchemaFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import org.xml.sax.SAXException;

public final class JaxbGenericLoader {
   private final JAXBContext ctx;

   public JaxbGenericLoader(String contextPath) throws JAXBException {
      this.ctx = JAXBContext.newInstance(contextPath);
   }

   public Object load(Path xmlPath) throws IOException, JAXBException {
      try (InputStream in = Files.newInputStream(xmlPath)) {
         Unmarshaller u = ctx.createUnmarshaller();
         Object root = u.unmarshal(new StreamSource(in));
         return unwrap(root);
      }
   }

   public Object loadValidated(Path xmlPath, Path xsdPath) throws IOException, JAXBException, SAXException {
      try (InputStream in = Files.newInputStream(xmlPath)) {
         Unmarshaller u = ctx.createUnmarshaller();
         SchemaFactory sf = SchemaFactory.newInstance(javax.xml.XMLConstants.W3C_XML_SCHEMA_NS_URI);
         u.setSchema(sf.newSchema(xsdPath.toFile()));
         Object root = u.unmarshal(new StreamSource(in));
         return unwrap(root);
      }
   }

   public static Object unwrap(Object o) {
      if (o instanceof JAXBElement<?> je) {
         return je.getValue();
      }
      return o;
   }

   public QName getRootQName(Object root) {
      JAXBIntrospector ji = ctx.createJAXBIntrospector();
      QName name = ji.getElementName(root);
      if (name == null) {
         @SuppressWarnings("unchecked")
         JAXBElement<Object> el = new JAXBElement<>(new QName("unknown"), (Class<Object>) root.getClass(), root);
         name = ji.getElementName(el);
      }
      return name;
   }

   public String prettyPrint(Object root) {
      try {
         Marshaller m = ctx.createMarshaller();
         m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
         java.io.StringWriter sw = new java.io.StringWriter();

         JAXBIntrospector ji = ctx.createJAXBIntrospector();
         if (ji.isElement(root)) {
            m.marshal(root, sw);
         } else {
            QName qn = getRootQName(root);
            @SuppressWarnings("unchecked")
            JAXBElement<Object> el = new JAXBElement<>(qn != null ? qn : new QName("root"),
              (Class<Object>) root.getClass(), root);
            m.marshal(el, sw);
         }
         return sw.toString();
      } catch (Exception e) {
         return root == null ? "null" : root.toString();
      }
   }
}
