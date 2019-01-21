package de.jungblut.partition;

import com.google.common.base.Preconditions;

/**
 * This partitioner partitions connected ranges from 0 to numberOfRows into
 * sizeOfCluster buckets.
 * 
 * @author thomas.jungblut
 * 
 */
public class BlockPartitioner implements Partitioner {

  @Override
  public Boundaries partition(int sizeOfCluster, int numberOfRows) {
    Preconditions.checkArgument(sizeOfCluster != 0,
        "Size of Cluster should not be 0! Given: " + sizeOfCluster);
    Boundaries bounds = new Boundaries();

    int avg = Math.round(numberOfRows / sizeOfCluster);
    int neededSplits = sizeOfCluster;
    int leftPieces;
    int start = 0;
    while (neededSplits > 0) {
      bounds.addRange(start, start += (avg - 1));
      start++;
      leftPieces = numberOfRows - start;
      neededSplits--;
      if (neededSplits > 0) {
        avg = leftPieces / neededSplits;
      }
    }

    return bounds;
  }
}
