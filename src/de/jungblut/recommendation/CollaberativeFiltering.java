package de.jungblut.recommendation;

import java.util.Random;

import de.jungblut.math.DenseBooleanMatrix;
import de.jungblut.math.DenseDoubleMatrix;

public class CollaberativeFiltering {

  public static void main(String[] args) {
    final DenseDoubleMatrix userMovieRatings = MovieLensReader
        .getUserMovieRatings();
    final DenseBooleanMatrix ratingMatrix = userMovieRatings
        .getNonDefaultBooleanMatrix();
    final DenseDoubleMatrix normalizedRatings = Normalizer
        .meanNormalize(userMovieRatings);

    final int numUsers = normalizedRatings.getColumnCount();
    final int numMovies = normalizedRatings.getRowCount();
    final int numFeatures = 10;

    final double lambda = 10.0d;

    DenseDoubleMatrix x = new DenseDoubleMatrix(numMovies, numFeatures,
        new Random());
    DenseDoubleMatrix theta = new DenseDoubleMatrix(numUsers, numFeatures,
        new Random());

  }

}
