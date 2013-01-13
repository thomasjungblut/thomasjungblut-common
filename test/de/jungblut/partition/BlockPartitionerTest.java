package de.jungblut.partition;

import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

import junit.framework.TestCase;

import org.junit.Test;

import de.jungblut.partition.Boundaries.Range;

public class BlockPartitionerTest extends TestCase {

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
}
