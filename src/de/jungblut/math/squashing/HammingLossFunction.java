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
      double sum = 0d;
      DoubleVector yRow = y.getRowVector(row);
      TIntHashSet visitedColumns = new TIntHashSet(yRow.getLength());
      DoubleVector hypRow = hypothesis.getRowVector(row);
      Iterator<DoubleVectorElement> iterateNonZero = hypRow.iterateNonZero();
      while (iterateNonZero.hasNext()) {
        DoubleVectorElement next = iterateNonZero.next();
        visitedColumns.add(next.getIndex());
        if (next.getValue() > activationThreshold
            ^ yRow.get(next.getIndex()) == 1d) {
          sum += 1d;
        }
      }

      iterateNonZero = yRow.iterateNonZero();
      while (iterateNonZero.hasNext()) {
        DoubleVectorElement next = iterateNonZero.next();
        if (!visitedColumns.contains(next.getIndex())) {
          visitedColumns.add(next.getIndex());
          if (hypRow.get(next.getIndex()) > activationThreshold
              ^ next.getValue() == 1d) {
            sum += 1d;
          }
        }
      }

      hammingSum += (sum / visitedColumns.size());

    }

    return hammingSum / y.getRowCount();
  }

}
