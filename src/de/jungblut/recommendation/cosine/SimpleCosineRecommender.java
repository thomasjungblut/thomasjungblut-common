package de.jungblut.recommendation.cosine;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import de.jungblut.distance.CosineDistance;
import de.jungblut.distance.DistanceMeasurer;
import de.jungblut.math.DoubleMatrix;
import de.jungblut.math.DoubleVector;
import de.jungblut.math.dense.DenseDoubleMatrix;
import de.jungblut.math.dense.DenseDoubleVector;
import de.jungblut.math.sparse.SparseDoubleColumnMatrix;
import de.jungblut.recommendation.MovieLensReader;
import de.jungblut.util.Tuple;

public class SimpleCosineRecommender {

  private final DoubleMatrix input;
  private final DistanceMeasurer measure;
  private final double distanceThreshold;

  private DoubleMatrix output;

  public SimpleCosineRecommender(DoubleMatrix input, double distanceThreshold) {
    this.input = input;
    this.distanceThreshold = distanceThreshold;
    measure = new CosineDistance();
  }

  public DoubleMatrix train() {

    output = input.isSparse() ? new SparseDoubleColumnMatrix(
        input.getRowCount(), input.getColumnCount()) : new DenseDoubleMatrix(
        input.getRowCount(), input.getColumnCount());

    int[] columnIndices = input.columnIndices();
    int processedCols = 0;
    for (int col : columnIndices) {
      DoubleVector colVec = input.getColumnVector(col);
      for (int otherCol : columnIndices) {
        DoubleVector otherColVec = input.getColumnVector(otherCol);
        if (col != otherCol) {
          double measureDistance = measure.measureDistance(colVec, otherColVec);
          if (measureDistance > -1 && measureDistance < distanceThreshold)
            output.set(col, otherCol, measureDistance);
        }
      }
      processedCols++;
      if (processedCols % 100 == 0)
        System.out.println("Processed " + processedCols + " of "
            + input.getColumnCount());
    }

    return output;
  }

  public DoubleVector predict(int userColumn) {
    return output.getColumnVector(userColumn);
  }

  public static void main(String[] args) {
    final DoubleMatrix userMovieRatings = MovieLensReader.getUserMovieRatings()
        .slice(1000, 6041);
    // set my preferences
    userMovieRatings.set(0, 260, 5); // star wars IV
    userMovieRatings.set(0, 1196, 5); // star wars V
    userMovieRatings.set(0, 1210, 5); // star wars VI

    SimpleCosineRecommender recommendation = new SimpleCosineRecommender(
        userMovieRatings, 0.2d);
    recommendation.train();

    HashMap<Integer, String> movieLookupTable = MovieLensReader
        .getMovieLookupTable();

    DoubleVector myPredictions = recommendation.predict(0);

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
