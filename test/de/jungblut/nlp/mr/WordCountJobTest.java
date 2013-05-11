package de.jungblut.nlp.mr;

import java.io.IOException;
import java.util.Arrays;

import junit.framework.TestCase;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mrunit.mapreduce.MapDriver;
import org.apache.hadoop.mrunit.mapreduce.MapReduceDriver;
import org.apache.hadoop.mrunit.mapreduce.ReduceDriver;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset.Entry;

import de.jungblut.nlp.StandardTokenizer;
import de.jungblut.nlp.mr.WordCountJob.WordFrequencyMapper;
import de.jungblut.nlp.mr.WordCountJob.WordFrequencyReducer;

public class WordCountJobTest extends TestCase {

  MapDriver<LongWritable, Text, Text, LongWritable> mapDriver;
  ReduceDriver<Text, LongWritable, Text, LongWritable> reduceDriver;
  MapReduceDriver<LongWritable, Text, Text, LongWritable, Text, LongWritable> mapReduceDriver;

  String toDedup = "this this is a text about how i used lower case and and duplicate words words";
  HashMultiset<String> tokenFrequency = HashMultiset.create(Arrays
      .asList(new StandardTokenizer().tokenize(toDedup)));

  @Override
  @Before
  public void setUp() {
    WordFrequencyMapper mapper = new WordFrequencyMapper();
    WordFrequencyReducer reducer = new WordFrequencyReducer();
    mapDriver = MapDriver.newMapDriver(mapper);
    reduceDriver = ReduceDriver.newReduceDriver(reducer);
    mapReduceDriver = MapReduceDriver.newMapReduceDriver(mapper, reducer);
  }

  @Test
  public void testMapper() throws IOException {
    mapDriver.withInput(new LongWritable(), new Text(toDedup));

    for (Entry<String> entry : tokenFrequency.entrySet()) {
      mapDriver.addOutput(new Text(entry.getElement()),
          new LongWritable(entry.getCount()));
    }

    mapDriver.runTest();
  }

  @Test
  public void testReducer() throws IOException {

    reduceDriver.setInput(new Text("this"), Arrays.asList(new LongWritable(2l),
        new LongWritable(4l), new LongWritable(1l), new LongWritable(25l)));

    reduceDriver.addOutput(new Text("this"), new LongWritable(32l));

    reduceDriver.runTest();
  }
}
