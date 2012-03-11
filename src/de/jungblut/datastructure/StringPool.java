package de.jungblut.datastructure;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class StringPool {

  private final Map<String, String> pool;

  private StringPool(Map<String, String> map) {
    this.pool = map;
  }

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

  public static StringPool getPool() {
    return new StringPool(new HashMap<String, String>());
  }

  public static StringPool getSynchronizedPool() {
    return new StringPool(new ConcurrentHashMap<String, String>());
  }

}
