package de.jungblut.math.squashing;

import gnu.trove.set.hash.TIntHashSet;

import java.util.Iterator;

import de.jungblut.math.DoubleMatrix;
import de.jungblut.math.DoubleVector;
import de.jungblut.math.DoubleVector.DoubleVectorElement;

/**
 * The HammingLoss is the fraction of labels that are incorrectly predicted.
 * 
 * @author thomasjungblut
 * 
 */
public final class HammingLossFunction implements ErrorFunction {

  private final double activationThreshold;

  /**
   * @param activationThreshold the threshold which denotes from where on up a
   *          numerical value "turns a unit of activation on". Turning on the
   *          activation means that it is interpreted as value 1.
   */
  public HammingLossFunction(double activationThreshold) {
    this.activationThreshold = activationThreshold;
  }

  @Override
  public double calculateError(DoubleMatrix y, DoubleMatrix hypothesis) {
    double hammingSum = 0d;
    // we now loop row-wise over the matrices
    for (int row = 0; row < y.getRowCount(); row++) {
      DoubleVector yRow = y.getRowVector(row);
      DoubleVector hypRow = hypothesis.getRowVector(row);
      hammingSum += calculateError(yRow, hypRow);
    }

    return hammingSum / y.getRowCount();
  }

  @Override
  public double calculateError(DoubleVector y, DoubleVector hypothesis) {
    double sum = 0d;
    TIntHashSet visitedColumns = new TIntHashSet(y.getLength());
    Iterator<DoubleVectorElement> iterateNonZero = hypothesis.iterateNonZero();
    while (iterateNonZero.hasNext()) {
      DoubleVectorElement next = iterateNonZero.next();
      visitedColumns.add(next.getIndex());
      if (next.getValue() > activationThreshold ^ y.get(next.getIndex()) == 1d) {
        sum += 1d;
      }
    }

    iterateNonZero = y.iterateNonZero();
    while (iterateNonZero.hasNext()) {
      DoubleVectorElement next = iterateNonZero.next();
      if (!visitedColumns.contains(next.getIndex())) {
        visitedColumns.add(next.getIndex());
        if (hypothesis.get(next.getIndex()) > activationThreshold
            ^ next.getValue() == 1d) {
          sum += 1d;
        }
      }
    }
    return sum / visitedColumns.size();
  }

}
