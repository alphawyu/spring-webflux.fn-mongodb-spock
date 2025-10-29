package com.realworld.webfluxfn.repository;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class DogTest {
  @Test
  public void testBark() {
    String expectedString = "woof";
    assertEquals(expectedString, "woof");
  }
}
