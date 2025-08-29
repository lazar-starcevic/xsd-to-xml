package com.xsdtoxml.main.generated;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Address")
public class Address {
   private String street;
   private String city;

   public Address() {
   }

   public Address(String street, String city) {
      this.street = street;
      this.city = city;
   }

   public String getStreet() {
      return street;
   }

   public void setStreet(String street) {
      this.street = street;
   }

   public String getCity() {
      return city;
   }

   public void setCity(String city) {
      this.city = city;
   }
}
