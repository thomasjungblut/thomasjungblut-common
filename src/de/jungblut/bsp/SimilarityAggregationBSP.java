package de.jungblut.bsp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Writable;
import org.apache.hama.HamaConfiguration;
import org.apache.hama.bsp.BSP;
import org.apache.hama.bsp.BSPJob;
import org.apache.hama.bsp.BSPPeer;
import org.apache.hama.bsp.sync.SyncException;

import de.jungblut.ner.IterativeSimilarityAggregation;
import de.jungblut.writable.VectorWritable;

/**
 * Similarity aggregation like in {@link IterativeSimilarityAggregation} just
 * parallelized with Apache Hama's BSP. The input key is the terms matrix, split
 * by single vectors (usually row vectors of this matrix). The output is a
 * {@link VectorWritable} that represents the indices of your dictionary of the
 * words that were expanded.
 * 
 * @author thomas.jungblut
 * 
 */
public class SimilarityAggregationBSP extends
    BSP<VectorWritable, NullWritable, VectorWritable, NullWritable, Writable> {

  public static final String SEED_IN_PATH = "hama.seed.in.path";

  ArrayList<Integer> seedList = new ArrayList<>();

  @Override
  public void setup(
      BSPPeer<VectorWritable, NullWritable, VectorWritable, NullWritable, Writable> peer)
      throws IOException, SyncException, InterruptedException {

    Configuration conf = peer.getConfiguration();
    FileSystem fs = FileSystem.get(conf);
    Path seedPath = new Path(conf.get(SEED_IN_PATH));
    try (BufferedReader br = new BufferedReader(new InputStreamReader(
        fs.open(seedPath)))) {
      seedList.add(Integer.parseInt(br.readLine()));
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  @Override
  public void bsp(
      BSPPeer<VectorWritable, NullWritable, VectorWritable, NullWritable, Writable> peer)
      throws IOException, SyncException, InterruptedException {

    /*
     * Basically a single peer is just reading a chunk of the similarity matrix,
     * calculating its part of the similarity vector to the seed indices and
     * sending it to the master task. The master task is accumulating the sums
     * over all peers and calculating the loss as well as sending the new
     * candidate terms back to each peer.
     */

  }

  /**
   * Creates a basic job with sequencefiles as in and output.
   */
  public static BSPJob createJob(Configuration cnf, Path in, Path out)
      throws IOException {
    HamaConfiguration conf = new HamaConfiguration(cnf);
    BSPJob job = new BSPJob(conf, SimilarityAggregationBSP.class);
    job.setJobName("Similarity Aggregation");
    job.setJarByClass(SimilarityAggregationBSP.class);
    job.setBspClass(SimilarityAggregationBSP.class);
    job.setInputPath(in);
    job.setOutputPath(out);
    job.setInputFormat(org.apache.hama.bsp.SequenceFileInputFormat.class);
    job.setOutputFormat(org.apache.hama.bsp.SequenceFileOutputFormat.class);
    job.setOutputKeyClass(VectorWritable.class);
    job.setOutputValueClass(NullWritable.class);
    return job;
  }

  public static void main(String[] args) throws Exception {

    Path in = new Path("files/simagg/in/data.seq");
    // this seed path contains the indices of the seeds in each line
    Path seed = new Path("files/simagg/in/seeds/seed.txt");
    Path out = new Path("files/simagg/out");

    Configuration conf = new Configuration();
    conf.set(SEED_IN_PATH, seed.toString());
    // if you're in local mode, you can increase this to match your core sizes
    conf.set("bsp.local.tasks.maximum", "2");

    BSPJob job = createJob(conf, in, out);

    FileSystem fs = FileSystem.get(conf);
    // TODO write the seeds based on the given dictionary

    if (job.waitForCompletion(true)) {

    }
  }

}
