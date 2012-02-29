package de.jungblut.recommendation;

import static de.jungblut.math.MatrixUtils.sum;
import static de.jungblut.math.MatrixUtils.sumWhenTrue;
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

    // do the magic...
    DenseDoubleMatrix tmp = theta.multiply(x.transpose())
        .subtract(userMovieRatings.transpose()).pow(2).transpose();
    // code=((Theta*X'-Y').^2)';

    j = sumWhenTrue(tmp, ratingMatrix) / 2.0d
        + (lambda * sum(theta.pow(2)) / 2.0d)
        + (lambda * sum((x.pow(2))) / 2.0d);
    // J = (sum(code(R==1)))/2 + (lambda * (sum(sum(Theta.^2))) /2) +
    // (lambda *
    // sum((sum(X.^2))) /2);

    xGradient = ((x.multiply(theta.transpose()).subtract(userMovieRatings)
        .multiplyElementWise(ratingMatrix)).multiply(theta).add(x
        .multiply(lambda)));
    // X_grad = ((X*Theta'-Y).*R)*Theta + lambda*X;

    thetaGradient = ((theta.multiply(x.transpose()).subtract(userMovieRatings
        .transpose())).multiplyElementWise(ratingMatrix.transpose())).multiply(
        x).add(theta.multiply(lambda));
    // Theta_grad = ((Theta*X'-Y').*R')*X + lambda*Theta;

    return new Tuple<Double, DenseDoubleVector>(j,
        DenseMatrixFolder.foldMatrices(xGradient, thetaGradient));
  }

  public static void main(String[] args) {
    DenseDoubleMatrix x = new DenseDoubleMatrix(new double[][] {
        { 1.048686, -0.400232, 1.194119 }, { 0.780851, -0.385626, 0.521198 },
        { 0.641509, -0.547854, -0.083796 }, { 0.453618, -0.800218, 0.680481 },
        { 0.937538, 0.106090, 0.361953 }, });

    DenseDoubleMatrix y = new DenseDoubleMatrix(new double[][] {
        { 5, 4, 0, 0 }, { 3, 0, 0, 0 }, { 4, 0, 0, 0 }, { 3, 0, 0, 0 },
        { 3, 0, 0, 0 } });

    DenseBooleanMatrix r = new DenseBooleanMatrix(new boolean[][] {
        { true, true, false, false }, { true, false, false, false },
        { true, false, false, false }, { true, false, false, false },
        { true, false, false, false } });

    int numUsers = 4;
    int numMovies = 5;
    int numFeatures = 3;
    double lambda = 0.0d;

    DenseDoubleMatrix theta = new DenseDoubleMatrix(new double[][] {
        { 0.28544, -1.68427, 0.26294 }, { 0.50501, -0.45465, 0.31746 },
        { -0.43192, -0.47880, 0.84671 }, { 0.72860, -0.27189, 0.32684 } });

    // fold x and theta so they are ready to be passed to fmincg
    DenseDoubleVector initialParameters = DenseMatrixFolder.foldMatrices(x,
        theta);
    CoFiCostFunction cost = new CoFiCostFunction(y, r, numUsers, numMovies,
        numFeatures, lambda);

    Tuple<Double, DenseDoubleVector> evaluateCost = cost
        .evaluateCost(initialParameters);
    // should be arround 22.22
    System.out.println(evaluateCost.getFirst());
  }
}
