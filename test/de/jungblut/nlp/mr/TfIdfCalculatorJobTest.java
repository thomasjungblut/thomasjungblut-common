package de.jungblut.nlp.mr;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mrunit.mapreduce.ReduceDriver;
import org.apache.hadoop.mrunit.types.Pair;
import org.junit.BeforeClass;
import org.junit.Test;

import de.jungblut.nlp.mr.TfIdfCalculatorJob.DocumentVectorizerReducer;
import de.jungblut.writable.VectorWritable;

public class TfIdfCalculatorJobTest {

  static ReduceDriver<Text, TextIntIntIntWritable, Text, VectorWritable> reduceDriver;

  @BeforeClass
  public static void setUp() {
    DocumentVectorizerReducer reducer = new DocumentVectorizerReducer();
    reduceDriver = ReduceDriver.newReduceDriver(reducer);
    reduceDriver.getConfiguration().setInt(
        TfIdfCalculatorJob.NUMBER_OF_DOCUMENTS_KEY, 20);
    reduceDriver.getConfiguration().setInt(
        TfIdfCalculatorJob.NUMBER_OF_TOKENS_KEY, 3);
  }

  @Test
  public void testReducer() throws IOException {

    reduceDriver.setInput(new Text("ID2012"), Arrays
        .asList(new TextIntIntIntWritable(new Text("this"), new IntWritable(4),
            new IntWritable(2), new IntWritable(1))));

    List<Pair<Text, VectorWritable>> res = reduceDriver.run();
    assertEquals(1, res.size());

    Pair<Text, VectorWritable> pair = res.get(0);
    assertEquals("ID2012", pair.getFirst().toString());
    assertEquals(3.2188758248682006d, pair.getSecond().getVector().get(1), 1e-4);
  }
}
