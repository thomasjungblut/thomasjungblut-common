package de.jungblut.datastructure;

import java.util.ArrayList;

import junit.framework.TestCase;

import org.junit.Test;

public class CollectionInputProviderTest extends TestCase {

  @Test
  public void testCollectionInputProvider() {
    ArrayList<String> list = new ArrayList<>();
    list.add("lol");
    list.add("omg!");
    CollectionInputProvider<String> prov = new CollectionInputProvider<>(list);
    Iterable<String> from = prov.iterate();
    int index = 0;
    for (String s : from) {
      assertEquals(list.get(index++), s);
    }
  }

}
