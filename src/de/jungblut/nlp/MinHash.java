package de.jungblut.nlp;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;
import java.util.Set;

import com.google.common.base.Preconditions;
import com.google.common.hash.Hashing;

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

  /*
   * define some hashfunctions
   */

  public static enum HashType {
    LINEAR, MURMUR128, MD5
  }

  abstract class HashFunction {

    protected int seed;

    public HashFunction(int seed) {
      this.seed = seed;
    }

    abstract int hash(byte[] bytes);

  }

  class LinearHashFunction extends HashFunction {

    private int seed2;

    public LinearHashFunction(int seed, int seed2) {
      super(seed);
      this.seed2 = seed2;
    }

    @Override
    int hash(byte[] bytes) {
      long hashValue = 31;
      for (byte byteVal : bytes) {
        hashValue *= seed * byteVal;
        hashValue += seed2;
      }
      return Math.abs((int) (hashValue % 2147482949));
    }

  }

  class Murmur128HashFunction extends HashFunction {

    com.google.common.hash.HashFunction murmur;

    public Murmur128HashFunction(int seed) {
      super(seed);
      this.murmur = Hashing.murmur3_128(seed);
    }

    @Override
    int hash(byte[] bytes) {
      return murmur.hashBytes(bytes).asInt();
    }

  }

  class MD5HashFunction extends HashFunction {

    com.google.common.hash.HashFunction md5;

    public MD5HashFunction(int seed) {
      super(seed);
      this.md5 = Hashing.md5();
    }

    @Override
    int hash(byte[] bytes) {
      return md5.hashBytes(bytes).asInt();
    }

  }

  /*
   * Hashfunction end
   */

  private final int numHashes;

  private final HashFunction[] functions;

  private MinHash(int numHashes) {
    this(numHashes, HashType.LINEAR, System.currentTimeMillis());
  }

  private MinHash(int numHashes, HashType type) {
    this(numHashes, type, System.currentTimeMillis());
  }

  private MinHash(int numHashes, HashType type, long seed) {
    this.numHashes = numHashes;
    this.functions = new HashFunction[numHashes];
    Random r = new Random(seed);
    for (int i = 0; i < numHashes; i++) {
      switch (type) {
        case LINEAR:
          functions[i] = new LinearHashFunction(r.nextInt(), r.nextInt());
          break;
        case MURMUR128:
          functions[i] = new Murmur128HashFunction(r.nextInt());
          break;
        case MD5:
          functions[i] = new MD5HashFunction(r.nextInt());
          break;
        default:
          throw new IllegalArgumentException(
              "Don't know the equivalent hashfunction to: " + type);
      }
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
        int hash = functions[i].hash(bytesToHash);
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
  @SuppressWarnings("static-method")
  public double measureSimilarity(int[] left, int[] right) {
    Preconditions.checkArgument(left.length == right.length,
        "Left length was not equal to right length! " + left.length + " != "
            + right.length);

    if (left.length + right.length == 0)
      return 0d;

    int[] union = ArrayUtils.union(left, right);
    int[] intersection = ArrayUtils.intersectionUnsorted(left, right);

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
   * Creates a {@link MinHash} instance with the given number of hash functions
   * with a linear hashing function.
   */
  public static MinHash create(int numHashes) {
    return new MinHash(numHashes);
  }

  /**
   * Creates a {@link MinHash} instance with the given number of hash functions
   * and a seed to be used in parallel systems. This method uses a linear
   * hashfunction.
   */
  public static MinHash create(int numHashes, long seed) {
    return new MinHash(numHashes, HashType.LINEAR, seed);
  }

  /**
   * Creates a {@link MinHash} instance with the given number of hash functions.
   */
  public static MinHash create(int numHashes, HashType type) {
    return new MinHash(numHashes, type);
  }

  /**
   * Creates a {@link MinHash} instance with the given number of hash functions
   * and a seed to be used in parallel systems.
   */
  public static MinHash create(int numHashes, HashType type, long seed) {
    return new MinHash(numHashes, type, seed);
  }

}
