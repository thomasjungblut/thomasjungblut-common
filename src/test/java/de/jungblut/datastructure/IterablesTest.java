package de.jungblut.datastructure;

import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.assertEquals;

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
