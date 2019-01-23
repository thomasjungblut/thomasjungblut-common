package de.jungblut.datastructure;

import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.assertEquals;

public class CollectionInputProviderTest {

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
