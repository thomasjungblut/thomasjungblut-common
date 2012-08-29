package de.jungblut.nlp;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;
import java.util.Set;

import com.google.common.base.Preconditions;

import de.jungblut.datastructure.ArrayUtils;
import de.jungblut.math.DoubleVector;
import de.jungblut.math.DoubleVector.DoubleVectorElement;

/**
 * Linear MinHash algorithm to find near duplicates faster or to speedup nearest
 * neighbour searches.
 * 
 * @author thomas.jungblut
 * 
 */
public final class MinHash {

  private final int numHashes;

  private final int[] seed1;
  private final int[] seed2;

  private MinHash(int numHashes) {
    this(numHashes, System.currentTimeMillis());
  }

  private MinHash(int numHashes, long seed) {
    this.numHashes = numHashes;
    this.seed1 = new int[numHashes];
    this.seed2 = new int[numHashes];

    Random r = new Random(seed);
    for (int i = 0; i < numHashes; i++) {
      this.seed1[i] = r.nextInt();
      this.seed2[i] = r.nextInt();
    }
  }

  /**
   * Minhashes the given vector by iterating over all non zero items and hashing
   * each byte in its value (as an integer). So it will end up with 4 bytes to
   * be hashed into a single integer by a linear hash function.
   * 
   * @param vector a arbitrary vector.
   * @return a int array of min hashes based on how many hashes were configured.
   */
  public int[] minHashVector(DoubleVector vector) {
    int[] minHashes = new int[numHashes];
    byte[] bytesToHash = new byte[4];
    Arrays.fill(minHashes, Integer.MAX_VALUE);

    for (int i = 0; i < numHashes; i++) {
      Iterator<DoubleVectorElement> iterateNonZero = vector.iterateNonZero();
      while (iterateNonZero.hasNext()) {
        DoubleVectorElement next = iterateNonZero.next();
        int value = (int) next.getValue();
        bytesToHash[0] = (byte) (value >> 24);
        bytesToHash[1] = (byte) (value >> 16);
        bytesToHash[2] = (byte) (value >> 8);
        bytesToHash[3] = (byte) value;
        int hash = hash(bytesToHash, seed1[i], seed2[i]);
        if (minHashes[i] > hash) {
          minHashes[i] = hash;
        }
      }
    }

    return minHashes;
  }

  /**
   * Measures the similarity between two min hash arrays by comparing the hashes
   * at the same index. This is assuming that both arrays having the same size.
   * 
   * @return a similarity between 0 and 1, where 1 is very similar.
   */
  public double measureSimilarity(int[] left, int[] right) {
    Preconditions.checkArgument(left.length == right.length,
        "Left length was not equal to right length! " + left.length + " != "
            + right.length);

    if (left.length + right.length == 0)
      return 0d;

    int[] union = ArrayUtils.union(left, right);
    // copy and sort to not mutate left and right
    int[] lcp = Arrays.copyOf(left, left.length);
    int[] rcp = Arrays.copyOf(right, right.length);
    Arrays.sort(lcp);
    Arrays.sort(rcp);
    // to compute the intersection
    int[] intersection = ArrayUtils.intersection(lcp, rcp);

    return intersection.length / (double) union.length;
  }

  /**
   * Generates cluster keys from the minhashes. Make sure that if you are going
   * to lookup the ids in a hashtable, sort out these that don't have a specific
   * minimum occurence. Also make sure that if you're using this in parallel,
   * you have to make sure that the seeds of the minhash should be consistent
   * across each task. Otherwise this key will be completely random.
   * 
   * @param keyGroups how many keygroups there should be, normally it's just a
   *          single per hash.
   * @return a set of string IDs that can refer as cluster identifiers.
   */
  public Set<String> createClusterKeys(int[] minHashes, int keyGroups) {
    HashSet<String> set = new HashSet<>();

    for (int i = 0; i < numHashes; i++) {
      StringBuilder clusterIdBuilder = new StringBuilder();
      for (int j = 0; j < keyGroups; j++) {
        clusterIdBuilder.append(minHashes[(i + j) % minHashes.length]).append(
            '_');
      }
      String clusterId = clusterIdBuilder.toString();
      clusterId = clusterId.substring(0, clusterId.lastIndexOf('_'));
      set.add(clusterId);
    }

    return set;
  }

  /**
   * Creates a {@link MinHash} instance with the given number of hash functions.
   */
  public static MinHash create(int numHashes) {
    return new MinHash(numHashes);
  }

  /**
   * Creates a {@link MinHash} instance with the given number of hash functions
   * and a seed to be used in parallel systems.
   */
  public static MinHash create(int numHashes, long seed) {
    return new MinHash(numHashes, seed);
  }

  /**
   * Linear hashfunction with two random seeds.
   */
  private static int hash(byte[] bytes, int seed1, int seed2) {
    long hashValue = 31;
    for (byte byteVal : bytes) {
      hashValue *= seed1 * byteVal;
      hashValue += seed2;
    }
    return Math.abs((int) (hashValue % 2147482949));
  }

}
