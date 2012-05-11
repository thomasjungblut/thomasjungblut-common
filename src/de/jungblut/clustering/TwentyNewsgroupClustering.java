package de.jungblut.clustering;

import gnu.trove.map.hash.TIntObjectHashMap;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileStatus;
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
import de.jungblut.math.DoubleVector.DoubleVectorElement;
import de.jungblut.math.dense.DenseIntVector;
import de.jungblut.math.tuple.Tuple;
import de.jungblut.math.tuple.Tuple3;
import de.jungblut.nlp.Vectorizer;

/**
 * One ugly piece of code, fastly hacked for my study.
 * 
 */
public final class TwentyNewsgroupClustering {

  private static final int K_MEANS_NUM = 30 + 1;

  public static void main(String[] args) throws IOException,
      ClassNotFoundException, InterruptedException {
    Tuple3<List<String[]>, DenseIntVector, String[]> readTwentyNewsgroups = TwentyNewsgroupReader
        .readTwentyNewsgroups(new File("files/20news-bydate"));
    List<String[]> documents = readTwentyNewsgroups.getFirst();
    DenseIntVector predictedClass = readTwentyNewsgroups.getSecond();
    String[] classNames = readTwentyNewsgroups.getThird();

    Tuple<HashMultiset<String>[], String[]> wordCountTokenTuple = Vectorizer
        .prepareWordCountToken(documents);
    String[] bagOfWords = wordCountTokenTuple.getSecond();
    List<DoubleVector> tfIdfVectorized = Vectorizer.tfIdfVectorize(documents,
        wordCountTokenTuple);

    for (int i = 0; i < tfIdfVectorized.size(); i++) {
      DoubleVector doubleVector = tfIdfVectorized.get(i).multiply(100.0d);
      tfIdfVectorized.set(i, new IdentifiableDoubleVector(
          predictedClass.get(i), doubleVector));
    }

    System.out.println("Finished vectorization!");

    Configuration conf = new Configuration();

    // TODO play arround with it and make some accuracy to iterations
    conf.set("k.means.max.iterations", "10");
    conf.set("distance.measure.class", CosineDistance.class.getCanonicalName());
    conf.set("bsp.local.tasks.maximum", "4");
    FileSystem fs = FileSystem.get(conf);
    Path in = new Path("files/vectorized-in/input.seq");
    Path center = new Path("files/centers/centers.seq");
    conf.set("centroid.path", center.toString());
    Path out = new Path("files/newgroup-out/");
    BSPJob job = KMeansBSP.createJob(conf, in, out);

    prepareInput(tfIdfVectorized, conf, fs, in, center, out);
    job.waitForCompletion(true);

    readOutput(classNames, conf, fs, out, bagOfWords);
    // readOutput(null, conf, fs, out, null);
  }

  private static void readOutput(String[] classNames, Configuration conf,
      FileSystem fs, Path out, String[] tokens) throws IOException {
    // read!
    FileStatus[] stati = fs.listStatus(out);
    HashSet<DoubleVector> centers = new HashSet<>();
    HashMap<Integer, Integer> centerMap = new HashMap<>();
    TIntObjectHashMap<List<DoubleVector>> map = new TIntObjectHashMap<>();
    int clusterIds = 0;
    // TODO euclidian?
    CosineDistance dist = new CosineDistance();
    double errorPercent = 0.0d;
    int num = 0;
    for (FileStatus status : stati) {
      if (!status.isDir()) {
        Path path = status.getPath();
        SequenceFile.Reader reader = new SequenceFile.Reader(fs, path, conf);
        ClusterCenter key = new ClusterCenter();
        VectorWritable v = new VectorWritable();
        while (reader.next(key, v)) {
          if (v.getVector().getLength() == 0)
            continue;
          DoubleVector centerVector = key.getCenterVector();
          double measureDistance = dist.measureDistance(centerVector,
              v.getVector());
          num++;
          errorPercent += measureDistance;
          centers.add(centerVector);
          Integer integer = centerMap.get(key.clusterIndex);
          if (integer == null) {
            integer = clusterIds++;
            centerMap.put(key.clusterIndex, integer);
          }
          List<DoubleVector> list = map.get(integer.intValue());
          if (list == null) {
            list = new ArrayList<DoubleVector>();
            map.put(integer.intValue(), list);
          }
          list.add(v.getVector().deepCopy());
        }
        reader.close();
      }
    }
    System.out.println("Error with " + (K_MEANS_NUM - 1) + " is "
        + (errorPercent / num));

    // for (DoubleVector v : centers) {
    // System.out.println(getSignificantWords(v, tokens));
    // }
    //
    // TIntObjectIterator<List<DoubleVector>> iterator = map.iterator();
    // while (iterator.hasNext()) {
    // iterator.advance();
    // int clusterKey = iterator.key();
    // int[] assignments = new int[classNames.length];
    // for (DoubleVector v : iterator.value()) {
    // assignments[((IdentifiableDoubleVector) v).getId()]++;
    // }
    // int maxI = 0;
    // int maxVal = Integer.MIN_VALUE;
    // for (int i = 0; i < assignments.length; i++) {
    // if (assignments[i] > maxVal) {
    // maxVal = assignments[i];
    // maxI = i;
    // }
    // }
    // System.out.println(clusterKey + " has following assignments: "
    // + Arrays.toString(assignments) + " probably class " + maxI);
    // }
  }

  public static String getSignificantWords(DoubleVector vector, String[] tokens) {
    List<Tuple<Double, Integer>> list = new ArrayList<Tuple<Double, Integer>>(
        vector.getLength());
    Iterator<DoubleVectorElement> iterateNonZero = vector.iterateNonZero();
    while (iterateNonZero.hasNext()) {
      DoubleVectorElement next = iterateNonZero.next();
      list.add(new Tuple<Double, Integer>(next.getValue(), next.getIndex()));
    }
    Collections.sort(list, new Comparator<Tuple<Double, Integer>>() {
      @Override
      public int compare(Tuple<Double, Integer> o1, Tuple<Double, Integer> o2) {
        return Double.compare(o2.getFirst(), o1.getFirst());
      }
    });
    String toReturn = "";
    int i = 0;
    for (Tuple<Double, Integer> tpl : list) {
      toReturn += tokens[tpl.getSecond()] + " ; ";
      i++;
      if (i > 10)
        break;
    }
    return toReturn;
  }

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

    int k = 0;
    for (DoubleVector vec : tfIdfVectorized) {
      // int id = ((IdentifiableDoubleVector) vec).getId();
      VectorWritable vector = new VectorWritable(vec);
      dataWriter.append(vector, value);
      if (k < K_MEANS_NUM) {
        centerWriter.append(new ClusterCenter(vector), value);
        k++;
      } else if (k == K_MEANS_NUM) {
        centerWriter.close();
      }
    }
    dataWriter.close();
  }
}
