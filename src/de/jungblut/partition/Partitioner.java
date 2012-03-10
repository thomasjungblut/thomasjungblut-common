package de.jungblut.partition;

/**
 * Used to partition a list/matrix-like structure to a number of cores /
 * buckets.
 * 
 * @author thomas.jungblut
 * 
 */
public interface Partitioner {

  public Boundaries partition(int sizeOfCluster, int numberOfRows);

}
