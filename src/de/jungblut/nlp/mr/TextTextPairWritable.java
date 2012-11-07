package de.jungblut.nlp.mr;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.WritableComparable;

import com.google.common.collect.ComparisonChain;

public final class TextTextPairWritable implements
    WritableComparable<TextTextPairWritable> {

  private Text first;
  private Text second;

  public TextTextPairWritable() {
  }

  public TextTextPairWritable(Text first, Text second) {
    this.first = first;
    this.second = second;
  }

  @Override
  public void readFields(DataInput in) throws IOException {
    first = new Text();
    first.readFields(in);
    second = new Text();
    second.readFields(in);
  }

  @Override
  public void write(DataOutput out) throws IOException {
    first.write(out);
    second.write(out);
  }

  @Override
  public int compareTo(TextTextPairWritable o) {
    return ComparisonChain.start().compare(first, o.first)
        .compare(second, o.second).result();
  }

  public Text getFirst() {
    return first;
  }

  public Text getSecond() {
    return second;
  }

}
