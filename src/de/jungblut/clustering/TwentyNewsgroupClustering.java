package de.jungblut.clustering;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.SequenceFile;
import org.apache.hadoop.io.SequenceFile.CompressionType;
import org.apache.hama.bsp.BSPJob;

import com.google.common.collect.HashMultiset;

import de.jungblut.classification.bayes.TwentyNewsgroupReader;
import de.jungblut.clustering.model.ClusterCenter;
import de.jungblut.clustering.model.VectorWritable;
import de.jungblut.distance.CosineDistance;
import de.jungblut.math.DoubleVector;
import de.jungblut.math.dense.DenseIntVector;
import de.jungblut.math.tuple.Tuple;
import de.jungblut.math.tuple.Tuple3;
import de.jungblut.nlp.Vectorizer;

public final class TwentyNewsgroupClustering {

  public static void main(String[] args) throws IOException,
      ClassNotFoundException, InterruptedException {
//    Tuple3<List<String[]>, DenseIntVector, String[]> readTwentyNewsgroups = TwentyNewsgroupReader
//        .readTwentyNewsgroups(new File("files/20news-bydate"));
//    List<String[]> documents = readTwentyNewsgroups.getFirst();
//    DenseIntVector predictedClass = readTwentyNewsgroups.getSecond();
//    String[] classNames = readTwentyNewsgroups.getThird();
//
//    Tuple<HashMultiset<String>[], String[]> wordCountTokenTuple = Vectorizer
//        .prepareWordCountToken(documents);
//    String[] bagOfWords = wordCountTokenTuple.getSecond();
//    List<DoubleVector> tfIdfVectorized = Vectorizer.tfIdfVectorize(documents,
//        wordCountTokenTuple);

    System.out.println("Finished vectorization!");

    Configuration conf = new Configuration();

    // TODO play arround with it and make some accuracy to iterations estimation
    // chart
    conf.set("k.means.max.iterations", "1000");
    // TODO try different measurements to see that cosine is the best
    conf.set("distance.measure.class", CosineDistance.class.getCanonicalName());
    conf.set("bsp.local.tasks.maximum", "2");
    FileSystem fs = FileSystem.get(conf);
    Path in = new Path("files/vectorized-in/input.seq");
    Path center = new Path("files/centers/centers.seq");
    conf.set("centroid.path", center.toString());
    Path out = new Path("files/newgroup-out/");
    BSPJob job = KMeansBSP.createJob(conf, in, out);

//    prepareInput(tfIdfVectorized, conf, fs, in, center, out);

    job.waitForCompletion(true);

  }

  /**
   * @param tfIdfVectorized
   * @param conf
   * @param fs
   * @param in
   * @param center
   * @param out
   * @throws IOException
   */
  public static void prepareInput(List<DoubleVector> tfIdfVectorized,
      Configuration conf, FileSystem fs, Path in, Path center, Path out)
      throws IOException {
    if (fs.exists(out))
      fs.delete(out, true);

    if (fs.exists(center))
      fs.delete(out, true);

    if (fs.exists(in))
      fs.delete(in, true);

    final SequenceFile.Writer centerWriter = SequenceFile.createWriter(fs,
        conf, center, ClusterCenter.class, NullWritable.class,
        CompressionType.NONE);
    final NullWritable value = NullWritable.get();

    final SequenceFile.Writer dataWriter = SequenceFile.createWriter(fs, conf,
        in, VectorWritable.class, NullWritable.class, CompressionType.NONE);

    int i = 0;
    final int k = 6;
    for (DoubleVector vec : tfIdfVectorized) {
      VectorWritable vector = new VectorWritable(vec);
      dataWriter.append(vector, value);
      if (k > i) {
        centerWriter.append(new ClusterCenter(vector), value);
      } else if (k == i) {
        centerWriter.close();
      }
      i++;
      System.out.println(i + "/" + tfIdfVectorized.size());
    }
    dataWriter.close();
  }
}
