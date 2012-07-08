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

    Iterator<Range> iterator = boundaries.iterator();

    Range next = iterator.next();
    assertEquals(0, next.getStart());
    assertEquals(5, next.getEnd());
    next = iterator.next();
    assertEquals(6, next.getStart());
    assertEquals(10, next.getEnd());
  }
}
