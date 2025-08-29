package com.xsdtoxml.main.generated;

import java.util.Objects;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "Person")
public class Person {
   private String firstName;
   private String lastName;
   private int age;

   public Person() {
   }

   public Person(String firstName, String lastName, int age) {
      this.firstName = firstName;
      this.lastName = lastName;
      this.age = age;
   }

   public String getFirstName() {
      return firstName;
   }

   public void setFirstName(String firstName) {
      this.firstName = firstName;
   }

   public String getLastName() {
      return lastName;
   }

   public void setLastName(String lastName) {
      this.lastName = lastName;
   }

   public int getAge() {
      return age;
   }

   public void setAge(int age) {
      this.age = age;
   }

   @Override
   public boolean equals(Object object) {
      if (this == object)
         return true;
      if (object == null || getClass() != object.getClass())
         return false;
      Person person = (Person) object;
      return age == person.age && Objects.equals(firstName, person.firstName) && Objects.equals(lastName, person.lastName);
   }

   @Override
   public int hashCode() {
      return Objects.hash(firstName, lastName, age);
   }
}
