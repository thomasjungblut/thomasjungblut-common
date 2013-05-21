package de.jungblut.partition;

import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

import junit.framework.TestCase;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import de.jungblut.partition.Boundaries.Range;

@RunWith(JUnit4.class)
public class BlockPartitionerTest extends TestCase {

  @Rule
  public ExpectedException exception = ExpectedException.none();

  @Test
  public void testPartitioner() {
    Boundaries partition = new BlockPartitioner().partition(2, 10);
    Set<Range> boundaries = new TreeSet<>(partition.getBoundaries());
    assertEquals(2, boundaries.size());
    Iterator<Range> iterator = boundaries.iterator();

    Range next = iterator.next();
    assertEquals(0, next.getStart());
    assertEquals(4, next.getEnd());
    next = iterator.next();
    assertEquals(5, next.getStart());
    assertEquals(9, next.getEnd());
  }

  @Test
  public void testPartitionerException() throws Exception {
    exception.expect(IllegalArgumentException.class);
    new BlockPartitioner().partition(0, 10);
  }
}
