package com.xsdtoxml.main.api;

import java.io.IOException;
import java.nio.file.Path;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.xml.sax.SAXException;

import com.xsdtoxml.main.generated.Exchange;
import com.xsdtoxml.main.helper.JaxbGenericLoader;

import jakarta.xml.bind.JAXBException;

@RestController
@RequestMapping("/api/exchange")
public class ExchangeController {
   private final JaxbGenericLoader loader;
   private final Path xml;
   private final Path xsd;

   public ExchangeController(JaxbGenericLoader loader,
     @Qualifier("inputXml") Resource xmlResource,
     @Qualifier("schemaXsd") Resource xsdResource) throws IOException {
      this.loader = loader;
      this.xml = xmlResource.getFile().toPath();
      this.xsd = xsdResource.getFile().toPath();
   }

   @GetMapping(produces = MediaType.APPLICATION_XML_VALUE)
   public ResponseEntity<String> xml() throws JAXBException, IOException, SAXException {
      Object root = loader.loadValidated(xml, xsd, Exchange.class);
      return ResponseEntity.ok(loader.prettyPrint(root));
   }
}

