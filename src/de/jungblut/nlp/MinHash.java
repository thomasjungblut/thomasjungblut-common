package de.jungblut.nlp;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Random;

import com.google.common.base.Preconditions;

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
    this.numHashes = numHashes;
    this.seed1 = new int[numHashes];
    this.seed2 = new int[numHashes];

    Random r = new Random();
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
    double identicalMinHashes = 0.0d;
    for (int i = 0; i < left.length; i++) {
      if (left[i] == right[i]) {
        identicalMinHashes++;
      }
    }
    return identicalMinHashes / left.length;
  }

  /**
   * Creates a {@link MinHash} instance with the given number of hash functions.
   */
  public static MinHash create(int numHashes) {
    return new MinHash(numHashes);
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
