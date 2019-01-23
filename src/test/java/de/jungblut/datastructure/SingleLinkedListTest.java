package de.jungblut.datastructure;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class SingleLinkedListTest {

    @Test
    public void testInserts() throws Exception {
        SingleLinkedList<Integer> list = new SingleLinkedList<>();
        fill(list);

        for (int i = 0; i < 10; i++)
            assertEquals(i, list.get(i).intValue());

        int index = 0;
        for (int i : list) {
            assertEquals(i, index);
            index++;
        }

        // random access
        list.add(3, 24);
        list.add(8, 22);
        list.add(0, 88);

        int[] arr = new int[]{88, 0, 1, 2, 24, 3, 4, 5, 6, 22, 7, 8, 9};
        index = 0;
        for (int i : list) {
            assertEquals(arr[index++], i);
        }

    }

    @Test
    public void testRemoval() throws Exception {
        SingleLinkedList<Integer> list = new SingleLinkedList<>();
        fill(list);

        Integer remove = list.remove(0);
        assertEquals(0, remove.intValue());

        Integer remove2 = list.remove(1);
        assertEquals(2, remove2.intValue());

        Integer remove3 = list.remove(list.size() - 1);
        assertEquals(9, remove3.intValue());

        assertEquals(7, list.size());

        int[] arr = new int[]{1, 3, 4, 5, 6, 7, 8};
        int index = 0;
        for (int i : list) {
            assertEquals(arr[index++], i);
        }

        // now add again and see if it worked
        fill(list);
        assertEquals(17, list.size());
        arr = new int[]{1, 3, 4, 5, 6, 7, 8, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9};
        index = 0;
        for (int i : list) {
            assertEquals(arr[index++], i);
        }
    }

    @Test
    public void testSet() throws Exception {
        SingleLinkedList<Integer> list = new SingleLinkedList<>();
        fill(list);

        Integer set = list.set(5, 15);
        assertEquals(5, set.intValue());

        assertEquals(15, list.get(5).intValue());

    }

    private void fill(SingleLinkedList<Integer> list) {
        for (int i = 0; i < 10; i++)
            list.add(i);
    }

}
