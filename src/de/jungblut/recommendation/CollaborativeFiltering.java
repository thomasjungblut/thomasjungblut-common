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
import de.jungblut.normalize.Normalizer;
import de.jungblut.util.Tuple;

public final class CollaborativeFiltering {

  private final DenseDoubleMatrix userMovieRatings;
  final DenseBooleanMatrix ratingMatrix;
  private Tuple<DenseDoubleMatrix, DenseDoubleVector> normalizedTuple;
  private DenseDoubleVector movieRatingMeanVector;
  private DenseDoubleMatrix p;

  public CollaborativeFiltering(DenseDoubleMatrix userMovieRatings) {
    super();
    this.userMovieRatings = userMovieRatings;
    this.ratingMatrix = userMovieRatings.getNonDefaultBooleanMatrix();
    normalizedTuple = Normalizer.meanNormalize(userMovieRatings);
  }

  public DenseDoubleMatrix train() {
    final DenseDoubleMatrix normalizedRatings = normalizedTuple.getFirst();
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
    DenseDoubleVector minimizeFunction = Fmincg.minimizeFunction(cost,
        initialParameters, 100, true);

    DenseDoubleMatrix[] unfoldMatrices = DenseMatrixFolder.unfoldMatrices(
        minimizeFunction, new int[][] {
            { x.getRowCount(), x.getColumnCount() },
            { theta.getRowCount(), theta.getColumnCount() } });

    DenseDoubleMatrix computedTheta = unfoldMatrices[1];
    p = x.multiply(computedTheta.transpose());
    return p;
  }

  public DenseDoubleVector predict(int userColumn) {
    return p.getColumnVector(0).add(movieRatingMeanVector);
  }

  public static void main(String[] args) {
    // take 100 users and all movies
    final DenseDoubleMatrix userMovieRatings = MovieLensReader
        .getUserMovieRatings().slice(100, 6040);
    // set my preferences
    userMovieRatings.set(0, 260, 5); // star wars IV
    userMovieRatings.set(0, 1196, 5); // star wars V
    userMovieRatings.set(0, 1210, 5); // star wars VI

    CollaborativeFiltering collaborativeFiltering = new CollaborativeFiltering(
        userMovieRatings);

    collaborativeFiltering.train();
    DenseDoubleVector myPredictions = collaborativeFiltering.predict(0);

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
