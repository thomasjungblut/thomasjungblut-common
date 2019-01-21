package de.jungblut.datastructure;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class ArrayIteratorTest {

    @Test
    public void testArrayIterator() {

        Integer[] array = new Integer[]{0, 1, 2, 3, 4, 5};

        ArrayIterator<Integer> iterator = new ArrayIterator<>(array);

        while (iterator.hasNext()) {
            Integer next = iterator.next();
            assertEquals(next.intValue(), iterator.getIndex());
            iterator.remove();
            assertNull(array[iterator.getIndex()]);
        }

    }

}
