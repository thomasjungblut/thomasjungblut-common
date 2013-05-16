package de.jungblut.datastructure;

import junit.framework.TestCase;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class ArrayJoinerTest extends TestCase {

  @Rule
  public ExpectedException exception = ExpectedException.none();

  @Test
  public void testCharJoin() throws Exception {

    String out = ArrayJoiner.on(',').join(new int[] { 1, 2, 3, 4 });

    assertEquals("1,2,3,4", out);

  }

  @Test
  public void testStringJoin() throws Exception {

    String out = ArrayJoiner.on(",.").join(new int[] { 1, 2, 3, 4 });

    assertEquals("1,.2,.3,.4", out);

  }

  @Test
  public void testNullJoin() {
    exception.expect(NullPointerException.class);
    ArrayJoiner.on(null);
  }

  @Test
  public void testNullElements() {
    exception.expect(NullPointerException.class);
    ArrayJoiner.on(".").join((byte[]) null);
  }

  @Test
  public void testZeroElements() throws Exception {
    String join = ArrayJoiner.on(".").join(new int[0]);
    assertEquals("", join);
  }

  @Test
  public void testSingleElement() throws Exception {
    String join = ArrayJoiner.on(".").join(new int[] { 14 });
    assertEquals("14", join);
  }

  @Test
  public void testTwoElement() throws Exception {
    String join = ArrayJoiner.on(".").join(new int[] { 14, 1336 });
    assertEquals("14.1336", join);
  }

}
