package com.xsdtoxml.main.api;

import java.nio.file.Path;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.xsdtoxml.main.helper.JaxbGenericLoader;

@Controller
@RequestMapping("/api")
public class ObjectController {

   private final JaxbGenericLoader loader;

   public ObjectController(JaxbGenericLoader loader) {
      this.loader = loader;
   }

   @GetMapping(value = "/object", produces = MediaType.APPLICATION_XML_VALUE)
   @ResponseBody
   public ResponseEntity<String> xml() throws Exception {
      Object root = loader.loadValidated(
        Path.of("src/main/resources/example.xml"),
        Path.of("src/main/resources/generated-xsd/schema0.xsd"));
      return ResponseEntity.ok(loader.prettyPrint(root));
   }
}
