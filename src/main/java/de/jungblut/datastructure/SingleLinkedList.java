package de.jungblut.datastructure;

import com.google.common.collect.AbstractIterator;

import java.util.AbstractList;
import java.util.Collection;
import java.util.Iterator;

/**
 * Single Linked list with less overhead in memory than the double linked list
 * of Java utils.
 *
 * @author thomas.jungblut
 */
public final class SingleLinkedList<T> extends AbstractList<T> {

    private Entry head;
    private Entry tail;

    private int size;

    public SingleLinkedList() {
    }

    public SingleLinkedList(Collection<? extends T> c) {
        addAll(c);
    }

    @Override
    public final boolean add(T element) {
        final Entry l = tail;
        final Entry newNode = new Entry(element);
        tail = newNode;
        if (l == null)
            head = newNode;
        else
            l.next = newNode;
        size++;
        return true;
    }

    @Override
    public final void add(int index, T element) {
        if (index != 0) {
            // append
            if (index == size) {
                add(element);
            } else {
                // random access
                Entry seek = seek(index - 1);
                Entry tmp = seek.next;
                seek.next = new Entry(element, tmp);
            }
        } else {
            // initial case
            if (size == 0) {
                Entry e = new Entry(element);
                head = e;
                tail = e;
            } else {
                // insert at head
                Entry tmp = head;
                head = new Entry(element, tmp);
            }
        }
        size++;
    }

    @Override
    public final T set(int index, T element) {
        Entry seek = seek(index);
        T tmp = seek.value;
        seek.value = element;
        return tmp;
    }

    @Override
    public final T remove(int index) {
        // special case, removing head
        if (index == 0) {
            T val = head.value;
            head = head.next;
            size--;
            return val;
        }
        Entry seek = seek(index - 1);
        T val = seek.next.value;
        // special case, removing tail
        if (index == size - 1) {
            tail = seek;
            tail.next = null;
        } else {
            seek.next = seek.next.next;
        }

        size--;

        return val;
    }

    @Override
    public final Iterator<T> iterator() {
        return new DefaultIterator();
    }

    @Override
    public final T get(int index) {
        return seek(index).value;
    }

    public final Entry seek(int index) {
        if (index == 0)
            return head;
        Entry ptr = head;
        for (int i = 0; i < index; i++) {
            ptr = ptr.next;
        }
        return ptr;
    }

    @Override
    public final int size() {
        return size;
    }

    final class DefaultIterator extends AbstractIterator<T> {

        Entry current;

        @Override
        protected final T computeNext() {
            if (current == null) {
                current = head;
            } else {
                current = current.next;
            }
            if (current == null) {
                return endOfData();
            }
            return current.value;
        }

    }

    final class Entry {
        T value;
        Entry next;

        public Entry(T value) {
            super();
            this.value = value;
        }

        public Entry(T value, Entry next) {
            super();
            this.value = value;
            this.next = next;
        }

    }

}
