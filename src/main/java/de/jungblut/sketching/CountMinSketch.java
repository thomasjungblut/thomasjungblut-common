package de.jungblut.sketching;

import com.google.common.base.Preconditions;
import com.google.common.hash.Funnel;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;

public class CountMinSketch<T> {

  private final int[][] countTable;
  private final HashFunction[] hashers;
  private Funnel<T> funnel;

  /**
   * Creates a new CountMinSketch. This will allocate a 2D array of (tableHeight
   * * tableWidth) integers, which is the whole memory constraint of the
   * counting sketch. The height of the table tells how many hash functions to
   * use.
   * 
   * @param tableWidth the width of the counter table.
   * @param tableHeight the height of the counter table (as in how many hashing
   *          functions to use).
   * @param funnel the funnel to hash an object.
   */
  public CountMinSketch(int tableWidth, int tableHeight, Funnel<T> funnel) {
    this.funnel = Preconditions.checkNotNull(funnel, "funnel");
    Preconditions.checkArgument(tableWidth > 0,
        "tableWidth must at least be 1!");
    Preconditions.checkArgument(tableHeight > 0,
        "tableHeight must be greater than 0!");
    countTable = new int[tableHeight][tableWidth];
    hashers = new HashFunction[tableHeight];
    for (int i = 0; i < tableHeight; i++) {
      hashers[i] = Hashing.murmur3_128((int) (i * System.currentTimeMillis()));
    }
  }

  /**
   * Adds this object to the min sketch.
   */
  public void add(T obj) {
    Preconditions.checkNotNull(obj);
    for (int i = 0; i < countTable.length; i++) {
      int hash = hashers[i].newHasher().putObject(obj, funnel).hash().asInt();
      countTable[i][Math.abs(hash) % countTable[i].length]++;
    }
  }

  /**
   * Gives an approximate count of how often this element was seen through
   * {@link #add(Object)}.
   * 
   * @return 0 if it was never seen, an approximate count otherwise.
   */
  public int approximateCount(T obj) {
    Preconditions.checkNotNull(obj);
    int min = Integer.MAX_VALUE;
    for (int i = 0; i < countTable.length; i++) {
      int hash = hashers[i].newHasher().putObject(obj, funnel).hash().asInt();
      int valueAt = countTable[i][Math.abs(hash) % countTable[i].length];
      if (valueAt != 0) {
        min = Math.min(min, valueAt);
      }
    }

    return min == Integer.MAX_VALUE ? 0 : min;
  }

}
