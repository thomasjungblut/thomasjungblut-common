package de.jungblut.datastructure;

import java.util.Collection;

/**
 * Provider for generic collections to read.
 *
 * @author thomas.jungblut
 */
public class CollectionInputProvider<T> extends InputProvider<T> {

    private Collection<T> collection;

    public CollectionInputProvider(Collection<T> col) {
        this.collection = col;
    }

    @Override
    public Iterable<T> iterate() {
        return collection;
    }

}
