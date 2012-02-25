package de.jungblut.recommendation;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
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

    DenseDoubleMatrix p = x.multiply(computedTheta);

    DenseDoubleVector myPredictions = p.getColumnVector(0).add(
        movieRatingMeanVector);

    HashMap<Integer, String> movieLookupTable = MovieLensReader
        .getMovieLookupTable();
    
    List<Tuple<Double, Integer>> sort = DenseDoubleVector.sort(myPredictions,
        Collections.reverseOrder(new Comparator<Double>() {
          @Override
          public int compare(Double o1, Double o2) {
            return Double.compare(o1, o2);
          }
        }));
    
    System.out.println("Predictions for me: ");
    for (int i = 0; i < 10; i++) {
      Tuple<Double, Integer> tuple = sort.get(i);
      double score = tuple.getFirst();
      int index = tuple.getSecond();
      System.out.println(movieLookupTable.get(index) + " | " + score);
    }

  }
}
