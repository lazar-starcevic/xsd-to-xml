package com.xsdtoxml.main;

import org.apache.catalina.Context;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.startup.Tomcat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.support.XmlWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;

public class Main {

   private static final Logger log = LoggerFactory.getLogger(Main.class);

   public static void main(String[] args) throws LifecycleException {
      XmlWebApplicationContext appContext = new XmlWebApplicationContext();
      appContext.setConfigLocation("classpath:applicationContext.xml");
      appContext.refresh();

      Tomcat tomcat = new Tomcat();
      tomcat.setPort(8080);
      tomcat.getConnector();

      Context ctx = tomcat.addContext("", System.getProperty("java.io.tmpdir"));

      DispatcherServlet dispatcher = new DispatcherServlet(appContext);
      Tomcat.addServlet(ctx, "dispatcher", dispatcher);
      ctx.addServletMappingDecoded("/", "dispatcher");

      tomcat.start();
      log.info("Server started @ http://localhost:8080");
      log.info("Try http://localhost:8080/api/object");
      tomcat.getServer().await();
   }
}
