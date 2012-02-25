package de.jungblut.recommendation;

import java.util.Random;

import de.jungblut.math.DenseBooleanMatrix;
import de.jungblut.math.DenseDoubleMatrix;
import de.jungblut.math.DenseDoubleVector;
import de.jungblut.math.minimize.DenseMatrixFolder;
import de.jungblut.math.minimize.Fmincg;
import de.jungblut.util.Tuple;

public class CollaberativeFiltering {

  public static void main(String[] args) {
    final DenseDoubleMatrix userMovieRatings = MovieLensReader
        .getUserMovieRatings();
    final DenseBooleanMatrix ratingMatrix = userMovieRatings
        .getNonDefaultBooleanMatrix();
    final Tuple<DenseDoubleMatrix, DenseDoubleVector> normalizedTuple = Normalizer
        .meanNormalize(userMovieRatings);

    final DenseDoubleMatrix normalizedRatings = normalizedTuple.getFirst();
    final DenseDoubleVector movieRatingMeanVector = normalizedTuple.getSecond();

    final int numUsers = normalizedRatings.getColumnCount();
    final int numMovies = normalizedRatings.getRowCount();
    final int numFeatures = 10;

    final double lambda = 10.0d;

    DenseDoubleMatrix x = new DenseDoubleMatrix(numMovies, numFeatures,
        new Random());
    DenseDoubleMatrix theta = new DenseDoubleMatrix(numUsers, numFeatures,
        new Random());

    // fold x and theta so they are ready to be passed to fmincg
    DenseDoubleVector initialParameters = DenseMatrixFolder.foldMatrices(x,
        theta);
    CoFiCostFunction cost = new CoFiCostFunction(userMovieRatings,
        ratingMatrix, numUsers, numMovies, numFeatures, lambda);

    DenseDoubleVector minimizeFunction = Fmincg.minimizeFunction(cost,
        initialParameters, 100);

    DenseDoubleMatrix[] unfoldMatrices = DenseMatrixFolder.unfoldMatrices(
        minimizeFunction, new int[][] {
            { x.getRowCount(), x.getColumnCount() },
            { theta.getRowCount(), theta.getColumnCount() } });

    DenseDoubleMatrix computedTheta = unfoldMatrices[1];

    DenseDoubleMatrix p = x.multiply(computedTheta.transpose());

    DenseDoubleVector myPredictions = p.getColumnVector(0).add(
        movieRatingMeanVector);

    System.out.println("Predictions for me: " + myPredictions);

  }
}
