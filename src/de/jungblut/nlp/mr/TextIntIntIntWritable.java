package de.jungblut.nlp.mr;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.WritableComparable;

import com.google.common.collect.ComparisonChain;

public final class TextIntIntIntWritable implements
    WritableComparable<TextIntIntIntWritable> {

  private Text first;
  private IntWritable second;
  private IntWritable third;
  private IntWritable fourth;

  public TextIntIntIntWritable() {
  }

  public TextIntIntIntWritable(Text first, IntWritable second,
      IntWritable third, IntWritable fourth) {
    this.first = first;
    this.second = second;
    this.third = third;
    this.fourth = fourth;
  }

  @Override
  public void readFields(DataInput in) throws IOException {
    first = new Text();
    first.readFields(in);
    second = new IntWritable();
    second.readFields(in);
    third = new IntWritable();
    third.readFields(in);
    fourth = new IntWritable();
    fourth.readFields(in);
  }

  @Override
  public void write(DataOutput out) throws IOException {
    first.write(out);
    second.write(out);
    third.write(out);
    fourth.write(out);
  }

  @Override
  public int compareTo(TextIntIntIntWritable o) {
    return ComparisonChain.start().compare(first, o.first)
        .compare(second, o.second).compare(third, o.third)
        .compare(fourth, o.fourth).result();
  }

  public Text getFirst() {
    return first;
  }

  public IntWritable getSecond() {
    return second;
  }

  public IntWritable getThird() {
    return third;
  }

  public IntWritable getFourth() {
    return fourth;
  }

  @Override
  public String toString() {
    return "Pair [" + (first != null ? "first=" + first + ", " : "")
        + (second != null ? "second=" + second + ", " : "")
        + (third != null ? "third=" + third + ", " : "")
        + (fourth != null ? "fourth=" + fourth : "") + "]";
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((first == null) ? 0 : first.hashCode());
    result = prime * result + ((fourth == null) ? 0 : fourth.hashCode());
    result = prime * result + ((second == null) ? 0 : second.hashCode());
    result = prime * result + ((third == null) ? 0 : third.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    TextIntIntIntWritable other = (TextIntIntIntWritable) obj;
    if (first == null) {
      if (other.first != null) {
        return false;
      }
    } else if (!first.equals(other.first)) {
      return false;
    }
    if (fourth == null) {
      if (other.fourth != null) {
        return false;
      }
    } else if (!fourth.equals(other.fourth)) {
      return false;
    }
    if (second == null) {
      if (other.second != null) {
        return false;
      }
    } else if (!second.equals(other.second)) {
      return false;
    }
    if (third == null) {
      if (other.third != null) {
        return false;
      }
    } else if (!third.equals(other.third)) {
      return false;
    }
    return true;
  }

}
