package de.jungblut.classification.meta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import de.jungblut.classification.AbstractClassifier;
import de.jungblut.classification.Classifier;
import de.jungblut.classification.ClassifierFactory;
import de.jungblut.datastructure.ArrayUtils;
import de.jungblut.math.DoubleVector;
import de.jungblut.math.dense.DenseDoubleVector;
import de.jungblut.partition.BlockPartitioner;
import de.jungblut.partition.Boundaries.Range;

/**
 * Implementation of vote ensembling.
 * 
 * @author thomas.jungblut
 * 
 */
public final class Voting extends AbstractClassifier {

  public static enum CombiningType {
    MAJORITY, AVERAGE
  }

  private final CombiningType type;
  private final Classifier[] classifier;
  private final int threads;
  private final boolean verbose;

  public Voting(CombiningType type, ClassifierFactory factory, int models,
      int threads, boolean verbose) {
    this.threads = threads;
    this.verbose = verbose;
    this.type = type;
    this.classifier = new Classifier[models];
    for (int i = 0; i < models; i++) {
      this.classifier[i] = factory.newInstance();
    }
  }

  public Voting(CombiningType type, ClassifierFactory factory, int models,
      boolean verbose) {
    this(type, factory, models, Runtime.getRuntime().availableProcessors(),
        verbose);
  }

  public Voting(CombiningType type, Classifier[] classifier) {
    this.type = type;
    this.classifier = classifier;
    this.threads = Runtime.getRuntime().availableProcessors();
    this.verbose = false;
  }

  @Override
  public void train(DoubleVector[] features, DoubleVector[] outcome) {
    ExecutorService pool = Executors.newFixedThreadPool(threads);
    ExecutorCompletionService<Boolean> completionService = new ExecutorCompletionService<>(
        pool);
    // each of the classifiers get a randomized chunk of the training set
    ArrayUtils.multiShuffle(features, outcome);

    List<Range> partitions = new ArrayList<>(new BlockPartitioner().partition(
        classifier.length, features.length).getBoundaries());

    final int[] splitRanges = new int[classifier.length + 1];
    for (int i = 1; i < classifier.length; i++) {
      splitRanges[i] = partitions.get(i).getStart();
    }
    splitRanges[classifier.length] = features.length - 1;

    if (verbose) {
      System.out.println("Computed split ranges for 0-" + features.length
          + ": " + Arrays.toString(splitRanges) + "\n");
    }
    for (int i = 0; i < classifier.length; i++) {
      completionService.submit(new TrainingWorker(classifier[i], ArrayUtils
          .subArray(features, splitRanges[i], splitRanges[i + 1]), ArrayUtils
          .subArray(outcome, splitRanges[i], splitRanges[i + 1])));
    }

    try {
      // let the training happen meanwhile wait for the result
      for (int i = 0; i < classifier.length; i++) {
        completionService.take();
        if (verbose) {
          System.out.println("Finished with training classifier " + (i + 1)
              + " of " + classifier.length);
        }

      }
    } catch (InterruptedException e) {
      e.printStackTrace();
    } finally {
      pool.shutdownNow();
    }
    if (verbose) {
      System.out.println("Successfully finished training!");
    }

  }

  public Classifier[] getClassifier() {
    return classifier;
  }

  @Override
  public DoubleVector predict(DoubleVector features) {
    // predict
    DoubleVector[] result = new DoubleVector[classifier.length];
    for (int i = 0; i < classifier.length; i++) {
      result[i] = classifier[i].predict(features);
    }
    int possibleOutcomes = result[0].getDimension() == 1 ? 2 : result[0]
        .getDimension();
    DoubleVector toReturn = new DenseDoubleVector(
        result[0].getDimension() == 1 ? 1 : possibleOutcomes);
    // now combine the results based on the rule
    switch (type) {
      case MAJORITY:
        double[] histogram = new double[possibleOutcomes];
        for (int i = 0; i < classifier.length; i++) {
          histogram[classifier[i].predictClassInternal(result[i])]++;
        }
        if (possibleOutcomes == 2) {
          toReturn.set(0, ArrayUtils.maxIndex(histogram));
        } else {
          toReturn.set(ArrayUtils.maxIndex(histogram), 1d);
        }
        break;
      case AVERAGE:
        for (int i = 0; i < result.length; i++) {
          toReturn = toReturn.add(result[i]);
        }
        toReturn = toReturn.divide(classifier.length);
        break;
      default:
        throw new UnsupportedOperationException("Type " + type
            + " isn't supported yet!");
    }
    return toReturn;
  }

  final class TrainingWorker implements Callable<Boolean> {

    private final Classifier cls;
    private final DoubleVector[] features;
    private final DoubleVector[] outcome;

    TrainingWorker(Classifier classifier, DoubleVector[] features,
        DoubleVector[] outcome) {
      cls = classifier;
      this.features = features;
      this.outcome = outcome;
    }

    @Override
    public Boolean call() throws Exception {

      cls.train(features, outcome);

      return true;
    }
  }

}
