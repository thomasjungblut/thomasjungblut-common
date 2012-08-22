package de.jungblut.bsp;

import java.io.IOException;

import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Writable;
import org.apache.hama.bsp.BSP;
import org.apache.hama.bsp.BSPPeer;
import org.apache.hama.bsp.sync.SyncException;

import de.jungblut.math.DoubleVector;
import de.jungblut.math.dense.DenseIntVector;
import de.jungblut.ner.IterativeSimilarityAggregation;

/**
 * Similarity aggregation like in {@link IterativeSimilarityAggregation} just
 * parallelized with Apache Hama's BSP. The input key is the terms matrix, split
 * by single vectors (usually row vectors of this matrix). The output is a
 * {@link DenseIntVector} that represents the indices of your dictionary of the
 * words that were expanded.
 * 
 * @author thomas.jungblut
 * 
 */
public class SimilarityAggregationBSP extends
    BSP<DoubleVector, NullWritable, DenseIntVector, NullWritable, Writable> {

  @Override
  public void bsp(
      BSPPeer<DoubleVector, NullWritable, DenseIntVector, NullWritable, Writable> peer)
      throws IOException, SyncException, InterruptedException {

  }

}
