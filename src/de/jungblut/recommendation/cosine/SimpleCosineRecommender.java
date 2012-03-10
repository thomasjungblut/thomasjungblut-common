package de.jungblut.recommendation.cosine;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import de.jungblut.distance.CosineDistance;
import de.jungblut.distance.DistanceMeasurer;
import de.jungblut.math.DoubleMatrix;
import de.jungblut.math.DoubleVector;
import de.jungblut.math.dense.DenseDoubleMatrix;
import de.jungblut.math.dense.DenseDoubleVector;
import de.jungblut.math.sparse.SparseDoubleColumnMatrix;
import de.jungblut.partition.BlockPartitioner;
import de.jungblut.partition.Boundaries.Range;
import de.jungblut.recommendation.MovieLensReader;
import de.jungblut.util.Tuple;

public class SimpleCosineRecommender {

  private ExecutorService threadPool;

  private final DoubleMatrix input;
  private final DistanceMeasurer measure;
  private final double distanceThreshold;
  private final int numCores;

  private DoubleMatrix output;

  public SimpleCosineRecommender(DoubleMatrix input, double distanceThreshold) {
    this.input = input;
    this.distanceThreshold = distanceThreshold;
    measure = new CosineDistance();
    numCores = Runtime.getRuntime().availableProcessors();
  }

  public DoubleMatrix train() {
    output = input.isSparse() ? new SparseDoubleColumnMatrix(
        input.getRowCount(), input.getColumnCount()) : new DenseDoubleMatrix(
        input.getRowCount(), input.getColumnCount());

    threadPool = Executors.newFixedThreadPool(numCores);
    ExecutorCompletionService<Integer> completionService = new ExecutorCompletionService<>(
        threadPool);

    int columnCount = input.getColumnCount();
    Set<Range> parts = new BlockPartitioner().partition(numCores, columnCount)
        .getBoundaries();

    for (Range r : parts) {
      completionService.submit(new CosineCalculator(r, input));
    }

    for (int i = 0; i < parts.size(); i++) {
      try {
        completionService.take();
        System.out.println("Finished " + i + " task of " + parts.size());
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }

    threadPool.shutdownNow();
    return output;
  }

  class CosineCalculator implements Callable<Integer> {

    final Range assignedRange;
    final DoubleMatrix input;

    public CosineCalculator(Range assignedRange, DoubleMatrix input) {
      super();
      this.assignedRange = assignedRange;
      this.input = input;
    }

    @Override
    public Integer call() throws Exception {
      int start = assignedRange.getStart();
      int end = assignedRange.getEnd();
      int[] columnIndices = input.columnIndices();
      for (int col = start; col < end; col++) {
        DoubleVector colVec = input.getColumnVector(col);
        if (colVec != null) {
          for (int otherCol : columnIndices) {
            if (col != otherCol) {
              DoubleVector otherColVec = input.getColumnVector(otherCol);
              double measureDistance = measure.measureDistance(colVec,
                  otherColVec);
              if (measureDistance > -1 && measureDistance < distanceThreshold)
                output.set(otherCol, col, measureDistance);
            }
          }
        }
        if (col % 100 == 0)
          System.out.println(Thread.currentThread().getName() + " : "
              + (end - col) + " items need to be processed!");
      }
      return 0;
    }
  }

  public DoubleVector predict(int userColumn) {
    return output.getColumnVector(userColumn);
  }

  public static void main(String[] args) {
    final DoubleMatrix userMovieRatings = MovieLensReader.getUserMovieRatings();
    // set my preferences
    userMovieRatings.set(260, 0, 5); // star wars IV
    userMovieRatings.set(1196, 0, 5); // star wars V
    userMovieRatings.set(1210, 0, 5); // star wars VI

    SimpleCosineRecommender recommendation = new SimpleCosineRecommender(
        userMovieRatings, 0.2d);
    recommendation.train();

    HashMap<Integer, String> movieLookupTable = MovieLensReader
        .getMovieLookupTable();

    for (int j = 0; j < 20; j++) {
      DoubleVector myPredictions = recommendation.predict(j);

      List<Tuple<Double, Integer>> sort = DenseDoubleVector.sort(myPredictions,
          Collections.reverseOrder(new Comparator<Double>() {
            @Override
            public int compare(Double o1, Double o2) {
              return Double.compare(o1, o2);
            }
          }));

      System.out.println("\nPredictions for user " + j);
      for (int i = 0; i < 10; i++) {
        if (i >= sort.size())
          break;
        Tuple<Double, Integer> tuple = sort.get(i);
        double score = tuple.getFirst();
        int index = tuple.getSecond();
        if (index > 0)
          System.out.println(movieLookupTable.get(index) + " | " + score);
      }
    }
  }
}
