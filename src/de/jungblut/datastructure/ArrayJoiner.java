package de.jungblut.datastructure;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;

/**
 * A {@link Joiner} utility that works for primitive arrays which Guava's Joiner
 * can't deal with.
 * 
 * @author thomas.jungblut
 * 
 */
public final class ArrayJoiner {

  private final String separator;

  private ArrayJoiner(String separator) {
    Preconditions.checkNotNull(separator);
    this.separator = separator;
  }

  /**
   * Joins the given array with the separator and returns the resulting string.
   * So an array of [1,2,3,4] turns into "1,2,3,4".
   * 
   * @param array a not null array of byte.
   * @return a string containing the results.
   */
  public String join(byte[] array) {
    Preconditions.checkNotNull(array);
    if (array.length == 0) {
      return "";
    }
    StringBuilder sb = new StringBuilder();
    final int lastIndex = array.length - 1;
    for (int i = 0; i < lastIndex; i++) {
      sb.append(array[i]);
      sb.append(separator);
    }
    sb.append(array[lastIndex]);
    return sb.toString();
  }

  /**
   * Joins the given array with the separator and returns the resulting string.
   * So an array of [1,2,3,4] turns into "1,2,3,4".
   * 
   * @param array a not null array of short.
   * @return a string containing the results.
   */
  public String join(short[] array) {
    Preconditions.checkNotNull(array);
    if (array.length == 0) {
      return "";
    }
    StringBuilder sb = new StringBuilder();
    final int lastIndex = array.length - 1;
    for (int i = 0; i < lastIndex; i++) {
      sb.append(array[i]);
      sb.append(separator);
    }
    sb.append(array[lastIndex]);
    return sb.toString();
  }

  /**
   * Joins the given array with the separator and returns the resulting string.
   * So an array of [1,2,3,4] turns into "1,2,3,4".
   * 
   * @param array a not null array of int.
   * @return a string containing the results.
   */
  public String join(int[] array) {
    Preconditions.checkNotNull(array);
    if (array.length == 0) {
      return "";
    }
    StringBuilder sb = new StringBuilder();
    final int lastIndex = array.length - 1;
    for (int i = 0; i < lastIndex; i++) {
      sb.append(array[i]);
      sb.append(separator);
    }
    sb.append(array[lastIndex]);
    return sb.toString();
  }

  /**
   * Joins the given array with the separator and returns the resulting string.
   * So an array of [1,2,3,4] turns into "1,2,3,4".
   * 
   * @param array a not null array of long.
   * @return a string containing the results.
   */
  public String join(long[] array) {
    Preconditions.checkNotNull(array);
    if (array.length == 0) {
      return "";
    }
    StringBuilder sb = new StringBuilder();
    final int lastIndex = array.length - 1;
    for (int i = 0; i < lastIndex; i++) {
      sb.append(array[i]);
      sb.append(separator);
    }
    sb.append(array[lastIndex]);
    return sb.toString();
  }

  /**
   * Joins the given array with the separator and returns the resulting string.
   * So an array of [1,2,3,4] turns into "1,2,3,4".
   * 
   * @param array a not null array of float.
   * @return a string containing the results.
   */
  public String join(float[] array) {
    Preconditions.checkNotNull(array);
    if (array.length == 0) {
      return "";
    }
    StringBuilder sb = new StringBuilder();
    final int lastIndex = array.length - 1;
    for (int i = 0; i < lastIndex; i++) {
      sb.append(array[i]);
      sb.append(separator);
    }
    sb.append(array[lastIndex]);
    return sb.toString();
  }

  /**
   * Joins the given array with the separator and returns the resulting string.
   * So an array of [1,2,3,4] turns into "1,2,3,4".
   * 
   * @param array a not null array of double.
   * @return a string containing the results.
   */
  public String join(double[] array) {
    Preconditions.checkNotNull(array);
    if (array.length == 0) {
      return "";
    }
    StringBuilder sb = new StringBuilder();
    final int lastIndex = array.length - 1;
    for (int i = 0; i < lastIndex; i++) {
      sb.append(array[i]);
      sb.append(separator);
    }
    sb.append(array[lastIndex]);
    return sb.toString();
  }

  /**
   * Joins the given array with the separator and returns the resulting string.
   * So an array of [1,2,3,4] turns into "1,2,3,4".
   * 
   * @param array a not null array of char.
   * @return a string containing the results.
   */
  public String join(char[] array) {
    Preconditions.checkNotNull(array);
    if (array.length == 0) {
      return "";
    }
    StringBuilder sb = new StringBuilder();
    final int lastIndex = array.length - 1;
    for (int i = 0; i < lastIndex; i++) {
      sb.append(array[i]);
      sb.append(separator);
    }
    sb.append(array[lastIndex]);
    return sb.toString();
  }

  /**
   * @return a new joiner on that separator.
   */
  public static ArrayJoiner on(char separator) {
    return new ArrayJoiner(String.valueOf(separator));
  }

  /**
   * @return a new joiner on that separator.
   */
  public static ArrayJoiner on(String separator) {
    return new ArrayJoiner(separator);
  }

}
