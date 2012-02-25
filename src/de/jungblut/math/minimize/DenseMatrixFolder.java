package de.jungblut.math.minimize;

import de.jungblut.math.DenseDoubleMatrix;
import de.jungblut.math.DenseDoubleVector;

public class DenseMatrixFolder {

  public static DenseDoubleVector foldMatrices(DenseDoubleMatrix... matrices) {
    int length = 0;
    for (DenseDoubleMatrix matrix : matrices) {
      length += matrix.getRowCount() * matrix.getColumnCount();
    }

    DenseDoubleVector v = new DenseDoubleVector(length);
    int index = 0;
    for (DenseDoubleMatrix matrix : matrices) {
      for (int j = 0; j < matrix.getColumnCount(); j++) {
        for (int i = 0; i < matrix.getRowCount(); i++) {
          v.set(index++, matrix.get(i, j));
        }
      }
    }

    return v;
  }

  public static DenseDoubleMatrix[] unfoldMatrices(DenseDoubleVector vector,
      int[][] sizeArray) {
    DenseDoubleMatrix[] arr = new DenseDoubleMatrix[sizeArray.length];
    for (int i = 0; i < sizeArray.length; i++) {
      arr[i] = new DenseDoubleMatrix(sizeArray[i][0], sizeArray[i][1]);
    }

    int currentVectorIndex = 0;
    for (int i = 0; i < arr.length; i++) {
      final int numRows = sizeArray[i][0];
      final int numColumns = sizeArray[i][1];
      for (int col = 0; col < numColumns; col++) {
        for (int row = 0; row < numRows; row++) {
          arr[i].set(row, col, vector.get(currentVectorIndex++));
        }
      }
    }

    return arr;
  }

  public static void main(String[] args) {
    DenseDoubleMatrix a = new DenseDoubleMatrix(new double[][] { { 1, 2, 3 },
        { 4, 5, 6 } });
    DenseDoubleMatrix b = new DenseDoubleMatrix(new double[][] { { 7, 8, 9 },
        { 10, 11, 12 } });
    DenseDoubleVector foldMatrices = foldMatrices(a, b);
    System.out.println(foldMatrices);
    DenseDoubleMatrix[] unfoldMatrices = unfoldMatrices(foldMatrices,
        new int[][] { { 2, 3 }, { 2, 3 } });

    for (DenseDoubleMatrix m : unfoldMatrices) {
      System.out.println(m);
    }

  }

}
