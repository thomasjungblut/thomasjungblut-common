package de.jungblut.math.normalize;

import de.jungblut.math.DoubleMatrix;
import de.jungblut.math.DoubleVector;

/**
 * Calculates the principal components of a given matrix.
 * 
 * @author thomas.jungblut
 * 
 */
public final class PrincipalComponents {

  private final DoubleMatrix matrix;

  public PrincipalComponents(DoubleMatrix matrix, boolean normalize) {
    if (normalize) {
      this.matrix = Normalizer.featureNormalize(matrix).getFirst();
    } else {
      this.matrix = matrix;
    }
  }

  public DoubleVector[] compute() {
    // compute the covariance matrix first
    DoubleMatrix cov = (matrix.transpose().multiply(matrix)).divide(matrix
        .getRowCount());

    /**
     * now we decompose this covariance matrix into three multiplied matrices: <br/>
     * - orthogonal matrix U<br/>
     * - diagonal matrix S<br/>
     * - another tranposed orthogonal matrix V<br/>
     */

    // TODO return an array of eigenvectors sorted descending by eigenvalue
    return null;

  }

}
