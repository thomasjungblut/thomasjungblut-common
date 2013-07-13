package de.jungblut.datastructure;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;

import org.junit.Test;

public class IterablesTest {

  @Test
  public void testFrom() {
    ArrayList<String> list = new ArrayList<>();
    list.add("lol");
    list.add("omg!");
    Iterable<String> from = Iterables.from(list.iterator());

    int index = 0;
    for (String s : from) {
      assertEquals(list.get(index++), s);
    }
  }
}
