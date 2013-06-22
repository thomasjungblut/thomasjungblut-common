package de.jungblut.math.minimize;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.ThreadFactoryBuilder;

import de.jungblut.datastructure.ArrayUtils;
import de.jungblut.math.DoubleMatrix;
import de.jungblut.math.DoubleVector;
import de.jungblut.math.dense.DenseDoubleMatrix;
import de.jungblut.math.dense.DenseDoubleVector;
import de.jungblut.math.tuple.Tuple;
import de.jungblut.partition.Boundaries.Range;

/**
 * Mini Batch cost function. It features parallel calculation of mini batches
 * and averaging of the error and gradient. The resulting costfunction can be
 * minimized with every normal {@link Minimizer}. The extending cost functions
 * should be as stateless as possible.
 * 
 * @author thomas.jungblut
 * 
 */
public abstract class AbstractMiniBatchCostFunction implements CostFunction {

  private final Executor pool;
  private final List<DoubleMatrix> batches;

  class CallableMiniBatch implements Callable<Tuple<Double, DoubleVector>> {

    private final DoubleMatrix inputMatrix;
    private final DoubleVector parameters;

    public CallableMiniBatch(DoubleMatrix inputMatrix, DoubleVector parameters) {
      super();
      this.inputMatrix = inputMatrix;
      this.parameters = parameters;
    }

    @Override
    public Tuple<Double, DoubleVector> call() throws Exception {
      return evaluateBatch(parameters, inputMatrix);
    }
  }

  /**
   * An abstract minibatch costfunction.
   * 
   * @param inputMatrix the data to crunch, bias will be added while calculating
   *          the batches.
   * @param batchSize the batch size to use, 0 denotes full batch learning by
   *          default.
   * @param numThreads the number of threads to use to calculate the batches.
   */
  public AbstractMiniBatchCostFunction(DoubleVector[] inputMatrix,
      int batchSize, int numThreads) {
    Preconditions.checkArgument(batchSize >= 0
        && batchSize <= inputMatrix.length, "Batchsize wasn't in range of 0-"
        + inputMatrix.length);
    Preconditions.checkArgument(numThreads >= 1,
        "#Threads need to be at least > 0");

    ThreadFactory factory = new ThreadFactoryBuilder().setDaemon(true)
        .setNameFormat("MiniBatch Worker %d").build();
    // if we configured batchsize 0, we know that this is the default for full
    // batching, thus only use a single thread.
    Set<Range> partitions = new HashSet<>();
    if (batchSize == 0) {
      numThreads = 1;
      batchSize = inputMatrix.length;
      partitions.add(new Range(0, batchSize - 1));
    } else {
      int offset = 0;
      while (offset < inputMatrix.length) {
        partitions.add(new Range(offset, Math.min(inputMatrix.length - 1,
            offset + (batchSize - 1))));
        offset += batchSize;
      }
    }
    pool = Executors.newFixedThreadPool(numThreads, factory);

    batches = new ArrayList<>();
    for (Range r : partitions) {
      int start = r.getStart();
      int end = r.getEnd(); // inclusive
      DoubleVector[] subArray = ArrayUtils.subArray(inputMatrix, start, end);
      DoubleMatrix mat = new DenseDoubleMatrix(subArray);
      // add the bias
      DenseDoubleVector ones = DenseDoubleVector.ones(subArray.length);
      mat = new DenseDoubleMatrix(ones, mat);
      batches.add(mat);
    }
  }

  @Override
  public final Tuple<Double, DoubleVector> evaluateCost(DoubleVector input) {

    ExecutorCompletionService<Tuple<Double, DoubleVector>> completionService = new ExecutorCompletionService<>(
        pool);
    // submit all batches to the service, pool will take care of the parallelism
    for (int i = 0; i < batches.size(); i++) {
      completionService.submit(new CallableMiniBatch(batches.get(i), input));
    }
    double costSum = 0d;
    DoubleVector gradientSum = new DenseDoubleVector(input.getLength());
    try {
      for (int i = 0; i < batches.size(); i++) {
        Tuple<Double, DoubleVector> result = completionService.take().get();
        costSum += result.getFirst().doubleValue();
        gradientSum = gradientSum.add(result.getSecond());
      }
    } catch (InterruptedException | ExecutionException e) {
      e.printStackTrace();
      // return null so minimizers should fast-fail
      return null;
    }

    // just return an average over the batches
    return new Tuple<>(costSum / batches.size(), gradientSum.divide(batches
        .size()));
  }

  /**
   * Evaluate the batch.
   * 
   * @param theta the parameters to use.
   * @param batch the batch matrix as input (already contains a bias!).
   * @return the cost/gradient tuple usually used when using
   *         {@link #evaluateCost(DoubleVector)}.
   */
  protected abstract Tuple<Double, DoubleVector> evaluateBatch(
      DoubleVector theta, DoubleMatrix batch);

}
