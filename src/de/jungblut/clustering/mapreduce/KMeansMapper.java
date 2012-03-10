package de.jungblut.clustering.mapreduce;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.SequenceFile;
import org.apache.hadoop.mapreduce.Mapper;

import de.jungblut.clustering.model.ClusterCenter;
import de.jungblut.clustering.model.Vector;
import de.jungblut.distance.DistanceMeasurer;
import de.jungblut.distance.EuclidianDistance;

// first iteration, k-random centers, in every follow-up iteration we have new calculated centers
class KMeansMapper extends Mapper<ClusterCenter, Vector, ClusterCenter, Vector> {

  private final List<ClusterCenter> centers = new LinkedList<>();
  private DistanceMeasurer distanceMeasurer;

  @Override
  protected void setup(Context context) throws IOException,
      InterruptedException {
    super.setup(context);
    Configuration conf = context.getConfiguration();
    Path centroids = new Path(conf.get("centroid.path"));
    FileSystem fs = FileSystem.get(conf);

    SequenceFile.Reader reader = new SequenceFile.Reader(fs, centroids, conf);
    ClusterCenter key = new ClusterCenter();
    IntWritable value = new IntWritable();
    while (reader.next(key, value)) {
      centers.add(new ClusterCenter(key));
    }
    reader.close();

    distanceMeasurer = new EuclidianDistance();
  }

  @Override
  protected void map(ClusterCenter key, Vector value, Context context)
      throws IOException, InterruptedException {

    ClusterCenter nearest = null;
    double nearestDistance = Double.MAX_VALUE;
    for (ClusterCenter c : centers) {
      double dist = distanceMeasurer.measureDistance(c.getCenter().getVector(),
          value.getVector());
      if (nearest == null) {
        nearest = c;
        nearestDistance = dist;
      } else {
        if (nearestDistance > dist) {
          nearest = c;
          nearestDistance = dist;
        }
      }
    }
    context.write(nearest, value);
  }

}
