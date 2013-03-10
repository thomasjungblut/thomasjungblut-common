package de.jungblut.math.activation;

import java.util.Iterator;

import de.jungblut.math.DoubleMatrix;
import de.jungblut.math.DoubleVector;
import de.jungblut.math.DoubleVector.DoubleVectorElement;
import de.jungblut.math.dense.DenseDoubleMatrix;
import de.jungblut.math.dense.DenseDoubleVector;
import de.jungblut.math.sparse.SparseDoubleColumnMatrix;
import de.jungblut.math.sparse.SparseDoubleVector;

/**
 * Implements the boiler plate code for applying functions on container classes
 * like vectors and matrices by applying the function on every element. This
 * implementation is aware of the type of the vector and matrix, so it is also
 * optimized for sparse as well as dense types.
 * 
 * @author thomas.jungblut
 * 
 */
public abstract class AbstractActivationFunction implements ActivationFunction {

  @Override
  public DoubleVector apply(DoubleVector vector) {
    DoubleVector newInstance = newInstance(vector);
    if (vector.isSparse()) {
      Iterator<DoubleVectorElement> iterateNonZero = newInstance
          .iterateNonZero();
      while (iterateNonZero.hasNext()) {
        DoubleVectorElement next = iterateNonZero.next();
        newInstance.set(next.getIndex(), apply(next.getValue()));
      }
    } else {
      for (int i = 0; i < vector.getDimension(); i++) {
        newInstance.set(i, apply(vector.get(i)));
      }
    }
    return newInstance;
  }

  @Override
  public DoubleMatrix apply(DoubleMatrix matrix) {
    DoubleMatrix newInstance = newInstance(matrix);
    if (matrix.isSparse()) {
      // if we have a sparse matrix, it is more efficient to loop over the
      // sparse column vectors
      int[] columnIndices = newInstance.columnIndices();
      for (int col : columnIndices) {
        newInstance.setColumnVector(col, apply(matrix.getColumnVector(col)));
      }
    } else {
      // on dense matrices we can be faster by directly looping over the items
      for (int i = 0; i < matrix.getRowCount(); i++) {
        for (int j = 0; j < matrix.getColumnCount(); j++) {
          newInstance.set(i, j, apply(matrix.get(i, j)));
        }
      }
    }
    return newInstance;
  }

  @Override
  public DoubleVector gradient(DoubleVector vector) {
    DoubleVector newInstance = newInstance(vector);
    if (vector.isSparse()) {
      Iterator<DoubleVectorElement> iterateNonZero = newInstance
          .iterateNonZero();
      while (iterateNonZero.hasNext()) {
        DoubleVectorElement next = iterateNonZero.next();
        newInstance.set(next.getIndex(), gradient(next.getValue()));
      }
    } else {
      for (int i = 0; i < vector.getDimension(); i++) {
        newInstance.set(i, gradient(vector.get(i)));
      }
    }
    return newInstance;
  }

  @Override
  public DoubleMatrix gradient(DoubleMatrix matrix) {
    DoubleMatrix newInstance = newInstance(matrix);
    if (matrix.isSparse()) {
      // if we have a sparse matrix, it is more efficient to loop over the
      // sparse column vectors
      int[] columnIndices = newInstance.columnIndices();
      for (int col : columnIndices) {
        newInstance.setColumnVector(col, gradient(matrix.getColumnVector(col)));
      }
    } else {
      // on dense matrices we can be faster by directly looping over the items
      for (int i = 0; i < matrix.getRowCount(); i++) {
        for (int j = 0; j < matrix.getColumnCount(); j++) {
          newInstance.set(i, j, gradient(matrix.get(i, j)));
        }
      }
    }
    return newInstance;
  }

  protected DoubleMatrix newInstance(DoubleMatrix mat) {
    if (mat.isSparse()) {
      return new SparseDoubleColumnMatrix(mat.getRowCount(),
          mat.getColumnCount());
    } else {
      return new DenseDoubleMatrix(mat.getRowCount(), mat.getColumnCount());
    }

  }

  protected DoubleVector newInstance(DoubleVector v) {
    if (v.isSparse()) {
      return new SparseDoubleVector(v.getDimension());
    } else {
      return new DenseDoubleVector(v.getDimension());
    }
  }

  @Override
  public String toString() {
    return getClass().getSimpleName();
  }

}
