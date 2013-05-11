package de.jungblut.nlp.mr;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import junit.framework.TestCase;

import org.apache.hadoop.io.IntWritable;
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
import de.jungblut.nlp.mr.WordCorpusFrequencyJob.DocumentSumReducer;
import de.jungblut.nlp.mr.WordCorpusFrequencyJob.TokenMapper;

public class WordCorpusFrequencyJobTest extends TestCase {

  MapDriver<LongWritable, Text, Text, TextIntPairWritable> mapDriver;
  ReduceDriver<Text, TextIntPairWritable, Text, TextIntIntIntWritable> reduceDriver;
  MapReduceDriver<LongWritable, Text, Text, TextIntPairWritable, Text, TextIntIntIntWritable> mapReduceDriver;

  String toDedup = "this this is a text about how i used lower case and and duplicate words words";
  HashMultiset<String> tokenFrequency = HashMultiset.create(Arrays
      .asList(new StandardTokenizer().tokenize(toDedup)));

  @Override
  @Before
  public void setUp() {
    toDedup = "ID123\t" + toDedup;
    TokenMapper mapper = new TokenMapper();
    DocumentSumReducer reducer = new DocumentSumReducer();
    mapDriver = MapDriver.newMapDriver(mapper);
    reduceDriver = ReduceDriver.newReduceDriver(reducer);
    mapReduceDriver = MapReduceDriver.newMapReduceDriver(mapper, reducer);
  }

  @Test
  public void testMapper() throws IOException {
    mapDriver.withInput(new LongWritable(), new Text(toDedup));

    for (Entry<String> entry : tokenFrequency.entrySet()) {
      mapDriver.addOutput(
          new Text(entry.getElement()),
          new TextIntPairWritable(new Text("ID123"), new IntWritable(entry
              .getCount())));
    }

    mapDriver.runTest();
  }

  @SuppressWarnings("deprecation")
  @Test
  public void testReducer() throws Exception {
    String p = "/tmp/dict.txt";
    reduceDriver.getConfiguration().set(
        WordCorpusFrequencyJob.DICT_OUT_PATH_KEY, p);
    reduceDriver.setInputKey(new Text("this"));

    reduceDriver.setInputValues(Arrays.asList(new TextIntPairWritable(new Text(
        "ID123"), new IntWritable(4))));

    reduceDriver.addOutput(new Text("ID123"), new TextIntIntIntWritable(
        new Text("this"), new IntWritable(1), new IntWritable(4),
        new IntWritable(0)));

    reduceDriver.runTest();

    // now check the dictionary
    Path path = FileSystems.getDefault().getPath(p);
    List<String> lines = Files.readAllLines(path, Charset.defaultCharset());
    assertEquals(1, lines.size());
    assertEquals("0\tthis", lines.get(0));
    FileSystems.getDefault().provider().delete(path);
    FileSystems.getDefault().provider()
        .delete(FileSystems.getDefault().getPath("/tmp/.dict.txt.crc"));
  }
}
