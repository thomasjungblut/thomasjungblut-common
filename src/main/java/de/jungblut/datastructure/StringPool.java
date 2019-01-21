package de.jungblut.datastructure;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Simple map based StringPool that is considered faster than using
 * {@link String#intern()}, but uses a bit more memory.
 *
 * @author thomas.jungblut
 */
public final class StringPool {

    private final Map<String, String> pool;

    private StringPool(Map<String, String> map) {
        this.pool = map;
    }

    /**
     * Pools the given string and returns a reference to an existing string (if
     * exists).
     *
     * @param s the string
     * @return a pooled string instance.
     */
    public final String pool(String s) {
        if (s == null) {
            return null;
        }

        String toReturn;
        if ((toReturn = pool.get(s)) == null) {
            pool.put(s, s);
            return s;
        }
        return toReturn;
    }

    /**
     * @return a not synchronized string pool based on a {@link HashMap}.
     */
    public static StringPool getPool() {
        return new StringPool(new HashMap<String, String>());
    }

    /**
     * @return a synchronized string pool based on a {@link ConcurrentHashMap}.
     */
    public static StringPool getSynchronizedPool() {
        return new StringPool(new ConcurrentHashMap<String, String>());
    }

}
