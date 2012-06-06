package de.jungblut.bsp;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.io.WritableUtils;
import org.apache.hadoop.mapreduce.InputFormat;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.JobContext;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.SequenceFileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.SequenceFileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.apache.hadoop.mapreduce.lib.partition.HashPartitioner;

/**
 * Random graph generator for Apache Hama BSP graph module (target version
 * 0.5.0), used to create large input for the SSSP example.
 * 
 * @author thomas.jungblut
 * 
 */
public class RandomGraphGenerator {

  /**
   * An input format that assigns ranges of longs to each mapper.
   */
  static class RangeInputFormat extends InputFormat<LongWritable, NullWritable> {

    /**
     * An input split consisting of a range on numbers.
     */
    static class RangeInputSplit extends InputSplit implements Writable {
      long firstRow;
      long rowCount;

      public RangeInputSplit() {
      }

      public RangeInputSplit(long offset, long length) {
        firstRow = offset;
        rowCount = length;
      }

      @Override
      public long getLength() throws IOException {
        return 0;
      }

      @Override
      public String[] getLocations() throws IOException {
        return new String[] {};
      }

      @Override
      public void readFields(DataInput in) throws IOException {
        firstRow = WritableUtils.readVLong(in);
        rowCount = WritableUtils.readVLong(in);
      }

      @Override
      public void write(DataOutput out) throws IOException {
        WritableUtils.writeVLong(out, firstRow);
        WritableUtils.writeVLong(out, rowCount);
      }
    }

    /**
     * A record reader that will generate a range of numbers.
     */
    static class RangeRecordReader extends
        RecordReader<LongWritable, NullWritable> {
      long startRow;
      long finishedRows;
      long totalRows;
      private LongWritable key;

      @Override
      public void close() throws IOException {
        // NOTHING
      }

      @Override
      public float getProgress() throws IOException {
        return finishedRows / (float) totalRows;
      }

      @Override
      public void initialize(InputSplit pSplit, TaskAttemptContext context)
          throws IOException, InterruptedException {
        RangeInputSplit split = (RangeInputSplit) pSplit;
        startRow = split.firstRow;
        finishedRows = 0;
        totalRows = split.rowCount;
        key = new LongWritable();
      }

      @Override
      public boolean nextKeyValue() throws IOException, InterruptedException {
        if (finishedRows < totalRows) {
          key.set(startRow + finishedRows);
          finishedRows += 1;
          return true;
        } else {
          return false;
        }
      }

      @Override
      public LongWritable getCurrentKey() throws IOException,
          InterruptedException {
        return key;
      }

      @Override
      public NullWritable getCurrentValue() throws IOException,
          InterruptedException {
        return null;
      }

    }

    /**
     * Create the desired number of splits, dividing the number of rows between
     * the mappers.
     */
    @Override
    public List<InputSplit> getSplits(JobContext job) {
      long totalRows = getNumberOfRows(job);
      int numSplits = (int) (totalRows / 100000);
      if (numSplits == 0) {
        numSplits = 1;
      }
      long rowsPerSplit = totalRows / numSplits;
      System.out.println("Generating " + totalRows + " using " + numSplits
          + " maps with step of " + rowsPerSplit);
      InputSplit[] splits = new InputSplit[numSplits];
      long currentRow = 0;
      for (int split = 0; split < numSplits - 1; ++split) {
        splits[split] = new RangeInputSplit(currentRow, rowsPerSplit);
        currentRow += rowsPerSplit;
      }
      splits[numSplits - 1] = new RangeInputSplit(currentRow, totalRows
          - currentRow);
      List<InputSplit> arrayList = new ArrayList<InputSplit>(splits.length);
      for (InputSplit s : splits) {
        arrayList.add(s);
      }
      return arrayList;
    }

    @Override
    public RecordReader<LongWritable, NullWritable> createRecordReader(
        InputSplit split, TaskAttemptContext context) throws IOException,
        InterruptedException {
      return new RangeRecordReader();
    }

  }

  static long getNumberOfRows(JobContext job) {
    return job.getConfiguration().getLong("hama.num.vertices", 0);
  }

  public static class SortGenMapper extends
      Mapper<LongWritable, NullWritable, Text, NullWritable> {

    private NullWritable value = NullWritable.get();

    @Override
    protected void map(LongWritable k, NullWritable v, Context context)
        throws IOException, InterruptedException {
      long rowId = k.get();
      context.write(new Text(Long.toString(rowId)), value);
    }

  }

  public static void main(String[] args) throws Exception {
    if (args.length != 4) {
      System.out
          .println("USAGE: <Number of vertices> <Number of edges per vertex> <Number of partitions> <Outpath>");
      return;
    }
    System.out.println(Arrays.toString(args));
    Configuration conf = new Configuration();
    conf.setInt("hama.num.vertices", Integer.parseInt(args[0]));
    conf.setInt("hama.num.partitions", Integer.parseInt(args[2]));
    conf.setInt("number.edges", Integer.parseInt(args[1]));
    Job job = new Job(conf);

    Path generated = new Path(new Path(args[3]).getParent(), "generated");
    FileOutputFormat.setOutputPath(job, generated);
    FileSystem.get(conf).delete(generated, true);
    job.setJobName("RangeWriter");
    job.setMapperClass(SortGenMapper.class);
    job.setNumReduceTasks(0);
    job.setOutputKeyClass(Text.class);
    job.setOutputValueClass(NullWritable.class);
    job.setInputFormatClass(RangeInputFormat.class);
    job.setOutputFormatClass(SequenceFileOutputFormat.class);

    job.waitForCompletion(true);
    conf.setInt("max.id", Integer.valueOf(args[0]));
    job = new Job(conf);

    FileOutputFormat.setOutputPath(job, new Path(args[3]));
    FileSystem.get(conf).delete(new Path(args[3]), true);
    job.setJobName("Random Vertex Writer");
    FileInputFormat.addInputPath(job, generated);
    job.setMapperClass(RandomMapper.class);
    job.setReducerClass(Reducer.class);
    job.setMapOutputKeyClass(Text.class);
    job.setMapOutputValueClass(Text.class);
    job.setNumReduceTasks(conf.getInt("hama.num.partitions", 2));
    job.setPartitionerClass(HashPartitioner.class);
    job.setOutputKeyClass(Text.class);
    job.setOutputValueClass(Text.class);
    job.setInputFormatClass(SequenceFileInputFormat.class);
    job.setOutputFormatClass(TextOutputFormat.class);
    job.waitForCompletion(true);
  }

  public static class RandomMapper extends
      Mapper<Text, NullWritable, Text, Text> {

    enum GraphCounter {
      VERTICES, EDGES
    }

    Random rand = new Random();
    int maxId;
    int edges;

    @Override
    protected void setup(Context context) throws IOException,
        InterruptedException {
      maxId = context.getConfiguration().getInt("max.id", 1);
      edges = context.getConfiguration().getInt("number.edges", 1);
    }

    @Override
    protected void map(Text key, NullWritable value, Context context)
        throws IOException, InterruptedException {
      // for SSSP we need: VERTEX_ID\t(n-tab separated VERTEX_ID:VERTEX_VALUE
      // pairs)

      String s = "";
      for (int i = 0; i < edges; i++) {
        int rowId = rand.nextInt(maxId);
        s += Long.toString(rowId) + ":" + rand.nextInt(100) + "\t";
      }

      context.getCounter(GraphCounter.VERTICES).increment(1L);
      context.getCounter(GraphCounter.EDGES).increment(edges);
      context.write(key, new Text(s));
    }

  }

}
