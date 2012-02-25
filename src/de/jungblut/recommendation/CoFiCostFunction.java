package de.jungblut.recommendation;

import de.jungblut.math.DenseBooleanMatrix;
import de.jungblut.math.DenseDoubleMatrix;
import de.jungblut.math.DenseDoubleVector;
import de.jungblut.math.minimize.CostFunction;
import de.jungblut.math.minimize.DenseMatrixFolder;
import de.jungblut.util.Tuple;

public final class CoFiCostFunction implements CostFunction {

  // Y in octave code
  private final DenseDoubleMatrix userMovieRatings;
  // R in octave code
  private final DenseBooleanMatrix ratingMatrix;
  private final double lambda;
  private int[][] foldArrays;

  public CoFiCostFunction(DenseDoubleMatrix userMovieRatings,
      DenseBooleanMatrix ratingMatrix, int numUsers, int numMovies,
      int numFeatures, double lambda) {
    this.userMovieRatings = userMovieRatings;
    this.ratingMatrix = ratingMatrix;
    this.lambda = lambda;

    foldArrays = new int[][] { { numMovies, numFeatures },
        { numUsers, numFeatures } };
  }

  @Override
  public Tuple<Double, DenseDoubleVector> evaluateCost(DenseDoubleVector input) {
    DenseDoubleMatrix[] unfoldMatrices = DenseMatrixFolder.unfoldMatrices(
        input, foldArrays);
    DenseDoubleMatrix x = unfoldMatrices[0];
    DenseDoubleMatrix theta = unfoldMatrices[1];

    double j = 0.0d;
    DenseDoubleMatrix thetaGradient = new DenseDoubleMatrix(
        theta.getRowCount(), theta.getColumnCount());
    DenseDoubleMatrix xGradient = new DenseDoubleMatrix(x.getRowCount(),
        x.getColumnCount());

    // do the magic... TODO I guess there are some wrong transposes!

    DenseDoubleMatrix tmp = theta.multiply(x)
        .subtract(userMovieRatings).pow(2).transpose();
    // code=((Theta*X'-Y').^2)';

    j = sumWhenTrue(tmp, ratingMatrix) / 2.0d
        + (lambda * sum(theta.pow(2)) / 2.0d)
        + (lambda * sum((x.pow(2))) / 2.0d);
    // J = (sum(code(R==1)))/2 + (lambda * (sum(sum(Theta.^2))) /2) + (lambda *
    // sum((sum(X.^2))) /2);

    xGradient = ((x.multiply(theta).subtract(userMovieRatings.transpose())
        .multiplyElementWise(ratingMatrix)).multiply(theta).add(x.multiply(
        lambda).transpose()));
    // X_grad = ((X*Theta'-Y).*R)*Theta + lambda*X;

    thetaGradient = ((theta.multiply(x).subtract(userMovieRatings).transpose()
        .multiplyElementWise(ratingMatrix)).transpose().multiply(x).add(theta.multiply(
        lambda).transpose()));
    // Theta_grad = ((Theta*X'-Y').*R')*X + lambda*Theta;

    return new Tuple<Double, DenseDoubleVector>(j,
        DenseMatrixFolder.foldMatrices(xGradient, thetaGradient));
  }

  private double sum(DenseDoubleMatrix toSum) {
    double totalSum = 0.0d;
    for (int col = 0; col < toSum.getColumnCount(); col++) {
      double colSum = 0.0d;
      for (int row = 0; row < toSum.getRowCount(); row++) {
        colSum += toSum.get(row, col);
      }
      totalSum += colSum;
    }
    return totalSum;
  }

  private double sumWhenTrue(DenseDoubleMatrix toSum, DenseBooleanMatrix blocker) {
    double totalSum = 0.0d;
    for (int col = 0; col < toSum.getColumnCount(); col++) {
      double colSum = 0.0d;
      for (int row = 0; row < toSum.getRowCount(); row++) {
        if (blocker.get(row, col))
          colSum += toSum.get(row, col);
      }
      totalSum += colSum;
    }
    return totalSum;
  }
  
  
  public static void main(String[] args) {
    
  }
  
}
