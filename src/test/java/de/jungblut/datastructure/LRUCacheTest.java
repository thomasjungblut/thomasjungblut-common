package de.jungblut.datastructure;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class LRUCacheTest {

    @Test
    public void testCaching() throws Exception {

        LRUCache<Integer, String> cache = new LRUCache<>(3);

        cache.put(1, "1");
        cache.put(2, "2");
        cache.put(3, "3");
        cache.put(4, "4");

        assertNull(cache.get(1));
        assertEquals("2", cache.get(2));
        assertEquals("3", cache.get(3));
        assertEquals("4", cache.get(4));

    }

}
