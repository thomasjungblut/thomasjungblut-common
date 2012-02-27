package de.jungblut.clustering.mapreduce;

import de.jungblut.clustering.model.ClusterCenter;
import de.jungblut.clustering.model.Vector;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.SequenceFile;
import org.apache.hadoop.mapreduce.Reducer;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

// calculate a new clustercenter for these vertices
public class KMeansReducer extends
    Reducer<ClusterCenter, Vector, ClusterCenter, Vector> {

  public static enum Counter {
    CONVERGED
  }

  private final List<ClusterCenter> centers = new LinkedList<>();

  @Override
  protected void reduce(ClusterCenter key, Iterable<Vector> values,
      Context context) throws IOException, InterruptedException {

    Vector newCenter = new Vector();
    List<Vector> vectorList = new LinkedList<>();
    int vectorSize = key.getCenter().getVector().length;
    newCenter.setVector(new double[vectorSize]);
    for (Vector value : values) {
      vectorList.add(new Vector(value));
      for (int i = 0; i < value.getVector().length; i++) {
        newCenter.getVector()[i] += value.getVector()[i];
      }
    }

    for (int i = 0; i < newCenter.getVector().length; i++) {
      newCenter.getVector()[i] = newCenter.getVector()[i] / vectorList.size();
    }

    ClusterCenter center = new ClusterCenter(newCenter);
    centers.add(center);
    for (Vector vector : vectorList) {
      context.write(center, vector);
    }

    if (center.converged(key))
      context.getCounter(Counter.CONVERGED).increment(1);

  }

  @Override
  protected void cleanup(Context context) throws IOException,
      InterruptedException {
    super.cleanup(context);
    Configuration conf = context.getConfiguration();
    Path outPath = new Path(conf.get("centroid.path"));
    FileSystem fs = FileSystem.get(conf);
    fs.delete(outPath, true);
    final SequenceFile.Writer out = SequenceFile.createWriter(fs,
        context.getConfiguration(), outPath, ClusterCenter.class,
        IntWritable.class);
    final IntWritable value = new IntWritable(0);
    for (ClusterCenter center : centers) {
      out.append(center, value);
    }
    out.close();
  }
}
