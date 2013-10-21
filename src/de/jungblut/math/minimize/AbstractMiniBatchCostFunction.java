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
import de.jungblut.math.sparse.SparseDoubleRowMatrix;
import de.jungblut.math.tuple.Tuple;
import de.jungblut.partition.Boundaries.Range;

/**
 * Mini Batch cost function. It features parallel calculation of mini batches
 * and averaging of the error and gradient. In addition you can calculate
 * mini-batches in a stochastic fashion, that means that every called iteration
 * a new mini-batch will be evaluated, its gradient will immediately be
 * returned. <br/>
 * <br/>
 * The resulting costfunction can be minimized with every normal
 * {@link Minimizer}. The extending cost functions should be as stateless as
 * possible.
 * 
 * @author thomas.jungblut
 * 
 */
public abstract class AbstractMiniBatchCostFunction implements CostFunction {

  private final Executor pool;
  private final List<Tuple<DoubleMatrix, DoubleMatrix>> batches;
  private final boolean stochastic;
  // offset for minibatches in stochastic mode
  private int batchOffset = 0;

  class CallableMiniBatch implements Callable<CostGradientTuple> {

    private final DoubleVector parameters;
    private final Tuple<DoubleMatrix, DoubleMatrix> featureOutcome;

    public CallableMiniBatch(Tuple<DoubleMatrix, DoubleMatrix> featureOutcome,
        DoubleVector parameters) {
      super();
      this.featureOutcome = featureOutcome;
      this.parameters = parameters;
    }

    @Override
    public CostGradientTuple call() throws Exception {
      return evaluateBatch(parameters, featureOutcome.getFirst(),
          featureOutcome.getSecond());
    }
  }

  /**
   * An abstract minibatch costfunction. This trains on all batches and averages
   * their results (gradient&cost).
   * 
   * @param inputMatrix the data to crunch, bias will be added while calculating
   *          the batches.
   * @param outcomeMatrix the data that denotes the outcome of the features-
   *          while supervised learning. Can be null for unsupervised methods.
   * @param batchSize the batch size to use, 0 denotes full batch learning by
   *          default.
   * @param numThreads the number of threads to use to calculate the batches.
   */
  public AbstractMiniBatchCostFunction(DoubleVector[] inputMatrix,
      DoubleVector[] outcomeMatrix, int batchSize, int numThreads) {
    this(inputMatrix, outcomeMatrix, batchSize, numThreads, false);
  }

  /**
   * An abstract minibatch costfunction.
   * 
   * @param inputMatrix the data to crunch, bias will be added while calculating
   *          the batches.
   * @param outcomeMatrix the data that denotes the outcome of the features-
   *          while supervised learning. Can be null for unsupervised methods.
   * @param batchSize the batch size to use, 0 denotes full batch learning by
   *          default.
   * @param numThreads the number of threads to use to calculate the batches.
   * @param stochastic if true the batches will be evaluated stochastically,
   *          that means that each iteration the next batch is chosen to
   *          evaluate and calculate gradient and cost.
   */
  public AbstractMiniBatchCostFunction(DoubleVector[] inputMatrix,
      DoubleVector[] outcomeMatrix, int batchSize, int numThreads,
      boolean stochastic) {
    Preconditions.checkArgument(batchSize >= 0
        && batchSize <= inputMatrix.length, "Batchsize wasn't in range of 0-"
        + inputMatrix.length);
    Preconditions.checkArgument(numThreads >= 1,
        "#Threads need to be at least > 0");
    this.stochastic = stochastic;

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
    pool = Executors.newFixedThreadPool(stochastic ? 1 : numThreads, factory);

    batches = new ArrayList<>();
    for (Range r : partitions) {
      int start = r.getStart();
      int end = r.getEnd(); // inclusive
      DoubleVector[] featureSubArray = ArrayUtils.subArray(inputMatrix, start,
          end);
      boolean sparse = featureSubArray[0].isSparse();
      DoubleMatrix outcomeMat = null;
      if (outcomeMatrix != null) {
        DoubleVector[] outcomeSubArray = ArrayUtils.subArray(outcomeMatrix,
            start, end);
        outcomeMat = new DenseDoubleMatrix(outcomeSubArray);
      }
      DenseDoubleVector bias = DenseDoubleVector.ones(featureSubArray.length);
      DoubleMatrix featureMatrix = sparse ? new SparseDoubleRowMatrix(
          featureSubArray) : new DenseDoubleMatrix(featureSubArray);
      DoubleMatrix featuresWithBias = sparse ? new SparseDoubleRowMatrix(bias,
          featureMatrix) : new DenseDoubleMatrix(bias, featureMatrix);
      batches.add(new Tuple<>(featuresWithBias, outcomeMat));
    }
  }

  @Override
  public final CostGradientTuple evaluateCost(DoubleVector input) {

    ExecutorCompletionService<CostGradientTuple> completionService = new ExecutorCompletionService<>(
        pool);

    int submittedBatches = 0;
    // submit all batches to the service, pool will take care of the parallelism
    for (int i = batchOffset; i < batches.size(); i++) {
      completionService.submit(new CallableMiniBatch(batches.get(i), input));
      submittedBatches++;
      if (stochastic) {
        // if stochastic, we will increase the batch
        batchOffset++;
        // check for wrap-ups
        if (batchOffset >= batches.size()) {
          batchOffset = 0;
        }
        break;
      }
    }
    double costSum = 0d;
    DoubleVector gradientSum = new DenseDoubleVector(input.getLength());
    try {
      // now collect the results
      for (int i = 0; i < submittedBatches; i++) {
        CostGradientTuple result = completionService.take().get();
        costSum += result.getCost();
        gradientSum = gradientSum.add(result.getGradient());
      }
    } catch (InterruptedException | ExecutionException e) {
      e.printStackTrace();
      // return null so minimizers should fast-fail
      return null;
    }
    if (submittedBatches != 1) {
      // just return an average over the batches
      return new CostGradientTuple(costSum / submittedBatches,
          gradientSum.divide(submittedBatches));
    } else {
      return new CostGradientTuple(costSum, gradientSum);
    }
  }

  /**
   * Evaluate the batch.
   * 
   * @param theta the parameters to use.
   * @param featureBatch the batch matrix as input (already contains a bias!).
   * @param outcomeBatch the batch matrix denoting the output.
   * @return the cost/gradient tuple usually used when using
   *         {@link #evaluateCost(DoubleVector)}.
   */
  protected abstract CostGradientTuple evaluateBatch(DoubleVector theta,
      DoubleMatrix featureBatch, DoubleMatrix outcomeBatch);

}
