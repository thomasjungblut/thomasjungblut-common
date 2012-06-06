package de.jungblut.partition;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Set;
import java.util.TreeSet;

import org.apache.hadoop.io.WritableComparable;

public class Boundaries {

  private Set<Range> boundaries = new TreeSet<Range>();

  public Boundaries() {
    super();
  }

  public void addRange(int start, int end) {
    boundaries.add(new Range(start, end));
  }

  /**
   * Split going from<br>
   * start -> split - 1<br>
   * split + 1 -> end <br>
   * 
   * @param start
   * @param split
   * @param end
   */
  public void splitRange(int start, int split, int end) {
    // removing the old one
    Range old = new Range(start, end);
    boundaries.remove(old);
    // adding new ones
    boundaries.add(new Range(start, split - 1));
    boundaries.add(new Range(split + 1, end));
  }

  public void removeRow(int row) {
    Range found = searchContainingBound(row);
    if (found.start == row) {
      boundaries.add(new Range(found.start + 1, found.end));
      boundaries.remove(found);
    } else if (found.end == row) {
      boundaries.add(new Range(found.start, found.end - 1));
      boundaries.remove(found);
    } else {
      splitRange(found.start, row, found.end);
    }
  }

  private Range searchContainingBound(int row) {
    for (Range r : boundaries) {
      if (r.start <= row && r.end >= row) {
        return r;
      }
    }
    return null;
  }

  public static class Range implements WritableComparable<Range> {
    private int start;
    private int end;

    public Range() {
      super();
    }

    public Range(int start, int end) {
      super();
      this.start = start;
      this.end = end;
    }

    public int getStart() {
      return start;
    }

    public int getEnd() {
      return end;
    }

    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + end;
      result = prime * result + start;
      return result;
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj)
        return true;
      if (obj == null)
        return false;
      if (getClass() != obj.getClass())
        return false;
      Range other = (Range) obj;
      if (end != other.end)
        return false;
      if (start != other.start)
        return false;
      return true;
    }

    @Override
    public void write(DataOutput out) throws IOException {
      out.writeInt(start);
      out.writeInt(end);
    }

    @Override
    public void readFields(DataInput in) throws IOException {
      start = in.readInt();
      end = in.readInt();
    }

    @Override
    public String toString() {
      return "Range [start=" + start + ", end=" + end + "]";
    }

    @Override
    public int compareTo(Range o) {
      return (start < o.start ? -1 : (start == o.start ? 0 : 1));
    }
  }

  public Set<Range> getBoundaries() {
    return boundaries;
  }

  @Override
  public String toString() {
    return "Boundaries [boundaries=" + boundaries + "]";
  }

}
