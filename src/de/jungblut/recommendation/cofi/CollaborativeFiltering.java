package de.jungblut.recommendation.cofi;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import de.jungblut.math.BooleanMatrix;
import de.jungblut.math.DoubleMatrix;
import de.jungblut.math.DoubleVector;
import de.jungblut.math.dense.DenseDoubleMatrix;
import de.jungblut.math.dense.DenseDoubleVector;
import de.jungblut.math.minimize.DenseMatrixFolder;
import de.jungblut.math.minimize.Fmincg;
import de.jungblut.normalize.Normalizer;
import de.jungblut.recommendation.MovieLensReader;
import de.jungblut.util.Tuple;

public final class CollaborativeFiltering {

  private final DoubleMatrix userMovieRatings;
  final BooleanMatrix ratingMatrix;
  private Tuple<DoubleMatrix, DoubleVector> normalizedTuple;
  private DoubleVector movieRatingMeanVector;
  private DoubleMatrix p;

  public CollaborativeFiltering(DoubleMatrix userMovieRatings) {
    super();
    this.userMovieRatings = userMovieRatings;
    this.ratingMatrix = userMovieRatings.getNonDefaultBooleanMatrix();
    normalizedTuple = Normalizer.meanNormalizeRows(userMovieRatings);
  }

  public DoubleMatrix train() {
    final DoubleMatrix normalizedRatings = normalizedTuple.getFirst();
    movieRatingMeanVector = normalizedTuple.getSecond();

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
    DoubleVector minimizeFunction = Fmincg.minimizeFunction(cost,
        initialParameters, 100, true);

    DenseDoubleMatrix[] unfoldMatrices = DenseMatrixFolder.unfoldMatrices(
        minimizeFunction, new int[][] {
            { x.getRowCount(), x.getColumnCount() },
            { theta.getRowCount(), theta.getColumnCount() } });

    DenseDoubleMatrix computedTheta = unfoldMatrices[1];
    p = (DenseDoubleMatrix) x.multiply(computedTheta.transpose());
    return p;
  }

  public DoubleVector predict(int userColumn) {
    return p.getColumnVector(0).add(movieRatingMeanVector);
  }

  public static void main(String[] args) {
    final DoubleMatrix userMovieRatings = MovieLensReader.getUserMovieRatings()
        .slice(100, 6041);
    // set my preferences
    userMovieRatings.set(0, 260, 5); // star wars IV
    userMovieRatings.set(0, 1196, 5); // star wars V
    userMovieRatings.set(0, 1210, 5); // star wars VI

    CollaborativeFiltering collaborativeFiltering = new CollaborativeFiltering(
        userMovieRatings);

    collaborativeFiltering.train();
    DoubleVector myPredictions = collaborativeFiltering.predict(0);

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
      if (index > 0)
        System.out.println(movieLookupTable.get(index) + " | " + score);
    }

  }
}
