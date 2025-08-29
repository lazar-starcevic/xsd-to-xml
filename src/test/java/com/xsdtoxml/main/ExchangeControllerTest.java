package com.xsdtoxml.main;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import java.io.IOException;
import java.nio.file.Files;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.xml.sax.SAXException;

import com.xsdtoxml.main.api.ExchangeController;
import com.xsdtoxml.main.generated.Exchange;
import com.xsdtoxml.main.helper.JaxbGenericLoader;

import jakarta.xml.bind.JAXBException;

class ExchangeControllerTest {

   private MockMvc mockMvc;

   @BeforeEach
   void setup() throws JAXBException, IOException, SAXException {
      Resource xmlResource = new FileSystemResource(Files.createTempFile("xml-", ".xml"));
      Resource xsdResource = new FileSystemResource(Files.createTempFile("xsd-", ".xsd"));

      JaxbGenericLoader loader = Mockito.mock(JaxbGenericLoader.class);
      Exchange exchange = new Exchange();

      Mockito.when(loader.loadValidated(any(), any(), eq(Exchange.class))).thenReturn(exchange);
      Mockito.when(loader.prettyPrint(exchange)).thenReturn("<Exchange/>");

      ExchangeController controller = new ExchangeController(
        loader,
        xmlResource,
        xsdResource
      );

      mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
   }

   @Test
   void getExchange() throws Exception {
      mockMvc.perform(get("/api/exchange").accept(MediaType.APPLICATION_XML_VALUE))
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_XML_VALUE))
        .andExpect(MockMvcResultMatchers.xpath("/Exchange").exists());
   }
}
