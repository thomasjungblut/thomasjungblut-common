package de.jungblut.recommendation.cosine;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.HashSet;
import java.util.Random;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.ArrayWritable;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.SequenceFile;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.SequenceFileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.SequenceFileOutputFormat;

import com.google.common.base.Preconditions;

import de.jungblut.distance.CosineDistance;
import de.jungblut.math.DoubleVector;
import de.jungblut.math.sparse.SparseDoubleVector;
import de.jungblut.writable.VectorWritable;

/**
 * Mapper gets product ID and a column vector of users that also have seen other
 * products. It calculates distances and emits a pair of the recommended product
 * and its score. Reducer is just colllecting, deduplicating and reducing.
 * 
 * @author thomas.jungblut
 * 
 */
public class CosineMapReduceJob {

  private static final String COSINE_DISTANCE_THRESHOLD_KEY = "recommendation.distance.threshold";
  private static final String INPUT_PATH_FILE_KEY = "recommendation.file.input";

  public static class CosineMapper extends
      Mapper<IntWritable, VectorWritable, IntWritable, PairWritable> {

    double threshold = 0.5d;
    SequenceFile.Reader reader;
    private Path path;
    private CosineDistance distance;

    @Override
    protected void setup(Context context) throws IOException,
        InterruptedException {
      Configuration configuration = context.getConfiguration();
      String thresholdString = configuration.get(COSINE_DISTANCE_THRESHOLD_KEY);
      if (thresholdString != null) {
        threshold = Double.parseDouble(thresholdString);
      }
      String pathString = configuration.get(INPUT_PATH_FILE_KEY);
      Preconditions.checkNotNull(pathString);
      path = new Path(pathString);
      reader = reopen(configuration, path);
      distance = new CosineDistance();
    }

    @Override
    protected void map(IntWritable key, VectorWritable value, Context context)
        throws IOException, InterruptedException {

      IntWritable k = new IntWritable();
      VectorWritable v = new VectorWritable();
      while (reader.next(k, v)) {
        double measuredDistance = distance.measureDistance(v.getVector(),
            value.getVector());
        if (measuredDistance < threshold) {
          context.write(key, new PairWritable(k.get(), measuredDistance));
        }
      }
      // reader here can't be null so we don't have to check this
      reader.close();
      reader = reopen(context.getConfiguration(), path);
    }

    public SequenceFile.Reader reopen(Configuration conf, Path p)
        throws IOException {
      return new SequenceFile.Reader(FileSystem.get(conf), p, conf);
    }

  }

  public static class CosineReducer extends
      Reducer<IntWritable, PairWritable, IntWritable, PairArrayWritable> {

    @Override
    protected void reduce(IntWritable key,
        java.lang.Iterable<PairWritable> values, Context context)
        throws IOException, InterruptedException {
      HashSet<PairWritable> set = new HashSet<>();
      for (PairWritable value : values) {
        set.add(value);
      }

      PairArrayWritable val = new PairArrayWritable();
      PairWritable[] array = new PairWritable[set.size()];
      int index = 0;
      for (PairWritable i : set) {
        array[index++] = i;
      }
      val.set(array);
      context.write(key, val);
    }

  }

  public static class PairWritable implements Writable {

    int productID;
    double score;

    public PairWritable() {
    }

    public PairWritable(int id, double score) {
      this.productID = id;
      this.score = score;
    }

    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + productID;
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
      PairWritable other = (PairWritable) obj;
      return productID == other.productID;
    }

    @Override
    public void readFields(DataInput in) throws IOException {
      productID = in.readInt();
      score = in.readDouble();
    }

    @Override
    public void write(DataOutput out) throws IOException {
      out.writeInt(productID);
      out.writeDouble(score);
    }

  }

  public static class PairArrayWritable extends ArrayWritable {
    public PairArrayWritable() {
      super(PairWritable.class);
    }
  }

  public static void main(String[] args) throws Exception {
    Configuration conf = new Configuration();

    Path input = new Path("files/recommendation/input/in.seq");
    generateInput(conf, input);

    conf.set(INPUT_PATH_FILE_KEY, input.toString());
    Path output = new Path("files/recommendation/output/");
    FileSystem fs = FileSystem.get(conf);
    if (fs.exists(output)) {
      fs.delete(output, true);
    }

    Job job = new Job(conf);
    job.setJobName("Recommender");
    job.setInputFormatClass(SequenceFileInputFormat.class);
    job.setOutputFormatClass(SequenceFileOutputFormat.class);
    job.setMapperClass(CosineMapper.class);
    job.setReducerClass(CosineReducer.class);

    FileInputFormat.setInputPaths(job, input);
    FileOutputFormat.setOutputPath(job, output);
    job.setNumReduceTasks(1);

    job.setMapOutputKeyClass(IntWritable.class);
    job.setMapOutputValueClass(PairWritable.class);
    job.setOutputKeyClass(IntWritable.class);
    job.setOutputValueClass(PairArrayWritable.class);

    job.waitForCompletion(true);

  }

  private static void generateInput(Configuration conf, Path input)
      throws IOException {

    FileSystem fs = FileSystem.get(conf);
    if (fs.exists(input)) {
      fs.delete(input, true);
    }

    SequenceFile.Writer writer = new SequenceFile.Writer(fs, conf, input,
        IntWritable.class, VectorWritable.class);

    final int vectorLength = 100;
    final int numSamples = 1000;
    final double sparsity = 0.1d;
    Random random = new Random();
    final int length = (int) (sparsity * vectorLength);

    for (int i = 0; i < numSamples; i++) {
      DoubleVector v = new SparseDoubleVector(vectorLength);
      for (int j = 0; j < length; j++) {
        int index = random.nextInt(vectorLength);
        v.set(index, 1.0d);
      }
      writer.append(new IntWritable(i), new VectorWritable(v));
    }
    writer.close();
  }
}
