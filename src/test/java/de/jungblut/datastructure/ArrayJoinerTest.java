package de.jungblut.datastructure;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static org.junit.Assert.assertEquals;

@RunWith(JUnit4.class)
public class ArrayJoinerTest {

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Test
    public void testIntJoin() throws Exception {
        String out = ArrayJoiner.on(',').join(new int[]{1, 2, 3, 4});
        assertEquals("1,2,3,4", out);
    }

    @Test
    public void testCharJoin() throws Exception {
        String out = ArrayJoiner.on(',').join(new char[]{'1', '2', '3', '4'});
        assertEquals("1,2,3,4", out);
    }

    @Test
    public void testByteJoin() throws Exception {
        String out = ArrayJoiner.on(',').join(new byte[]{1, 2, 3, 4});
        assertEquals("1,2,3,4", out);
    }

    @Test
    public void testShortJoin() throws Exception {
        String out = ArrayJoiner.on(',').join(new short[]{1, 2, 3, 4});
        assertEquals("1,2,3,4", out);
    }

    @Test
    public void testLongJoin() throws Exception {
        String out = ArrayJoiner.on(',').join(new long[]{1, 2, 3, 4});
        assertEquals("1,2,3,4", out);
    }

    @Test
    public void testFloatJoin() throws Exception {
        String out = ArrayJoiner.on(',').join(new float[]{1, 2, 3, 4});
        assertEquals("1.0,2.0,3.0,4.0", out);
    }

    @Test
    public void testDoubleJoin() throws Exception {
        String out = ArrayJoiner.on(',').join(new double[]{1, 2, 3, 4});
        assertEquals("1.0,2.0,3.0,4.0", out);
    }

    @Test
    public void testStringJoin() throws Exception {
        String out = ArrayJoiner.on(",.").join(new int[]{1, 2, 3, 4});
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
        String join = ArrayJoiner.on(".").join(new int[]{14});
        assertEquals("14", join);
    }

    @Test
    public void testTwoElement() throws Exception {
        String join = ArrayJoiner.on(".").join(new int[]{14, 1336});
        assertEquals("14.1336", join);
    }

}
