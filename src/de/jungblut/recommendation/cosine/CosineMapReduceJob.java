package de.jungblut.recommendation.cosine;

import gnu.trove.set.hash.TIntHashSet;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.ArrayWritable;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.SequenceFile;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;

import com.google.common.base.Preconditions;

import de.jungblut.distance.CosineDistance;
import de.jungblut.writable.VectorWritable;

public class CosineMapReduceJob {

  private static final String COSINE_DISTANCE_THRESHOLD_KEY = "recommendation.distance.threshold";
  private static final String INPUT_PATH_FILE_KEY = "recommendation.file.input";

  public static class CosineMapper extends
      Mapper<IntWritable, VectorWritable, IntWritable, IntWritable> {

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
        if (measuredDistance > threshold) {
          context.write(key, k);
        }
      }

      if (reader != null) {
        reader.close();
        reader = reopen(context.getConfiguration(), path);
      }
    }

    public SequenceFile.Reader reopen(Configuration conf, Path p)
        throws IOException {
      return new SequenceFile.Reader(FileSystem.get(conf), p, conf);
    }

  }

  public static class CosineReducer extends
      Reducer<IntWritable, IntWritable, IntWritable, ArrayWritable> {

    @Override
    protected void reduce(IntWritable key,
        java.lang.Iterable<IntWritable> values, Context context)
        throws IOException, InterruptedException {

      TIntHashSet set = new TIntHashSet();
      for (IntWritable value : values) {
        set.add(value.get());
      }

      // TODO this actually needs a subclass to work correctly
      ArrayWritable val = new ArrayWritable(IntWritable.class);
      IntWritable[] array = new IntWritable[set.size()];
      int index = 0;
      for (int i : set.toArray()) {
        array[index++] = new IntWritable(i);
      }
      val.set(array);

      context.write(key, val);

    }

  }

  public static void main(String[] args) {
    
  }

}
