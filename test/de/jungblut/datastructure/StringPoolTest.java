package de.jungblut.datastructure;

import junit.framework.TestCase;

import org.junit.Test;

public class StringPoolTest extends TestCase {

  @Test
  public void testStringPooling() {
    StringPool sp = StringPool.getPool();

    String ab = "ab";
    String a = "a";
    String b = "b";

    String pooled = sp.pool(ab);
    // no pooled instances yet, so it should be the same string reference
    assertTrue(ab == pooled);

    // that is a new unseen instance of "ab"
    pooled = sp.pool(a + b);
    // still it should return the first instance
    assertTrue(ab == pooled);

  }

}
