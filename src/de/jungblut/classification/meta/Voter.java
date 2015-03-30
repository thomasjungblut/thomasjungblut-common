package de.jungblut.classification.meta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.base.Preconditions;

import de.jungblut.classification.AbstractClassifier;
import de.jungblut.classification.Classifier;
import de.jungblut.classification.ClassifierFactory;
import de.jungblut.datastructure.ArrayUtils;
import de.jungblut.math.DoubleVector;
import de.jungblut.math.dense.DenseDoubleVector;
import de.jungblut.partition.BlockPartitioner;
import de.jungblut.partition.Boundaries.Range;

/**
 * Implementation of vote ensembling. This features multithreading, different
 * combination and selection techniques.
 * 
 * @author thomas.jungblut
 * 
 */
public final class Voter<A extends Classifier> extends AbstractClassifier {

  private static final Logger LOG = LogManager.getLogger(Voter.class);

  public static enum CombiningType {
    MAJORITY, AVERAGE, PROBABILITY
  }

  public static enum SelectionType {
    NONE, SHUFFLE, BAGGING
  }

  private final Classifier[] classifier;

  private CombiningType type;
  private SelectionType selection = SelectionType.NONE;
  private int threads = 1;
  private boolean verbose;

  private Voter(CombiningType type, int numClassifiers,
      ClassifierFactory<A> classifierFactory) {
    this.type = type;
    this.classifier = new Classifier[numClassifiers];
    for (int i = 0; i < numClassifiers; i++) {
      this.classifier[i] = classifierFactory.newInstance();
    }
  }

  private Voter(List<A> classifierCollection) {
    classifier = new Classifier[classifierCollection.size()];
    for (int i = 0; i < classifier.length; i++) {
      classifier[i] = Preconditions.checkNotNull(classifierCollection.get(i));
    }
  }

  @Override
  public void train(DoubleVector[] features, DoubleVector[] outcome) {
    ExecutorService pool = Executors.newFixedThreadPool(threads);
    try {
      ExecutorCompletionService<Boolean> completionService = new ExecutorCompletionService<>(
          pool);

      // do the selection of the data
      List<TrainingSplit> splits = null;
      switch (selection) {
        case BAGGING:
          splits = bag(features, outcome);
          break;
        case SHUFFLE:
          splits = partition(features, outcome, true);
          break;
        default:
          splits = partition(features, outcome, false);
          break;
      }

      // submit to the threadpool
      for (int i = 0; i < classifier.length; i++) {
        completionService.submit(new TrainingWorker(classifier[i], splits
            .get(i)));
      }

      // let the training happen meanwhile wait for the result
      for (int i = 0; i < classifier.length; i++) {
        completionService.take();
        if (verbose) {
          LOG.info("Finished with training classifier " + (i + 1) + " of "
              + classifier.length);
        }

      }
    } catch (InterruptedException e) {
      e.printStackTrace();
    } finally {
      pool.shutdownNow();
    }
    if (verbose) {
      LOG.info("Successfully finished training!");
    }

  }

  @Override
  public DoubleVector predict(DoubleVector features) {
    // predict
    DoubleVector[] result = new DoubleVector[classifier.length];
    for (int i = 0; i < classifier.length; i++) {
      result[i] = classifier[i].predict(features);
    }
    int numPossibleOutcomes = result[0].getDimension() == 1 ? 2 : result[0]
        .getDimension();
    DoubleVector toReturn = new DenseDoubleVector(
        result[0].getDimension() == 1 ? 1 : numPossibleOutcomes);
    // now combine the results based on the rule
    switch (type) {
      case MAJORITY:
        double[] histogram = createPredictionHistogram(result,
            numPossibleOutcomes);
        if (numPossibleOutcomes == 2) {
          toReturn.set(0, ArrayUtils.maxIndex(histogram));
        } else {
          toReturn.set(ArrayUtils.maxIndex(histogram), 1d);
        }
        break;
      case PROBABILITY:
        DoubleVector v = result[0];
        for (int i = 1; i < result.length; i++) {
          v = v.add(result[i]);
        }
        toReturn = v.divide(v.sum());
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

  public Classifier[] getClassifier() {
    return classifier;
  }

  /**
   * @return sets this instance to verbose and returns it.
   */
  public Voter<A> verbose() {
    return verbose(true);
  }

  /**
   * @return sets this instance to verbose and returns it.
   */
  public Voter<A> verbose(boolean verb) {
    this.verbose = verb;
    return this;
  }

  /**
   * @return sets the selection type and returns this instance.
   */
  public Voter<A> selectionType(SelectionType type) {
    this.selection = type;
    return this;
  }

  /**
   * @return sets the number of threads and returns this instance.
   */
  public Voter<A> numThreads(int threads) {
    this.threads = threads;
    return this;
  }

  /**
   * @return sets the used combination type in this instance and returns it.
   */
  public Voter<A> setCombiningType(CombiningType type) {
    this.type = type;
    return this;
  }

  private double[] createPredictionHistogram(DoubleVector[] result,
      int possibleOutcomes) {
    double[] histogram = new double[possibleOutcomes];
    for (int i = 0; i < classifier.length; i++) {
      int clz = classifier[i].extractPredictedClass(result[i]);
      histogram[clz]++;
    }
    return histogram;
  }

  private List<TrainingSplit> bag(DoubleVector[] features,
      DoubleVector[] outcome) {
    List<TrainingSplit> splits = new ArrayList<>(classifier.length);
    Random rand = new Random();
    for (int i = 0; i < classifier.length; i++) {

      DoubleVector[] featureBag = new DoubleVector[features.length];
      DoubleVector[] outcomeBag = new DoubleVector[features.length];
      // bagging is basically filling random items into these arrays
      for (int n = 0; n < features.length; n++) {
        int nextInt = rand.nextInt(features.length);
        featureBag[n] = features[nextInt];
        outcomeBag[n] = outcome[nextInt];
      }
      splits.add(new TrainingSplit(featureBag, outcomeBag));
    }

    return splits;
  }

  private List<TrainingSplit> partition(DoubleVector[] features,
      DoubleVector[] outcome, boolean shuffle) {

    List<TrainingSplit> splits = new ArrayList<>(classifier.length);
    if (shuffle) {
      ArrayUtils.multiShuffle(features, outcome);
    }

    List<Range> partitions = new ArrayList<>(new BlockPartitioner().partition(
        classifier.length, features.length).getBoundaries());

    final int[] splitRanges = new int[classifier.length + 1];
    for (int i = 1; i < classifier.length; i++) {
      splitRanges[i] = partitions.get(i).getStart();
    }
    splitRanges[classifier.length] = features.length - 1;

    if (verbose) {
      LOG.info("Computed split ranges for 0-" + features.length + ": "
          + Arrays.toString(splitRanges) + "\n");
    }

    for (int i = 0; i < classifier.length; i++) {
      DoubleVector[] featureSplit = ArrayUtils.subArray(features,
          splitRanges[i], splitRanges[i + 1]);
      DoubleVector[] outcomeSplit = ArrayUtils.subArray(outcome,
          splitRanges[i], splitRanges[i + 1]);
      splits.add(new TrainingSplit(featureSplit, outcomeSplit));
    }

    return splits;
  }

  /**
   * Creates a new voting classificator. The training is single threaded, no
   * shuffling or bagging takes place.
   * 
   * @param numClassifiers the number of base classifiers to use.
   * @param type the combining type to use.
   * @param classifierFactory the classifier factory to create base classifiers.
   * @return a new Voter.
   */
  public static <K extends Classifier> Voter<K> create(int numClassifiers,
      CombiningType type, ClassifierFactory<K> classifierFactory) {
    return new Voter<>(type, numClassifiers, classifierFactory);
  }

  /**
   * Creates a voter from the given trained models for prediction purposes.
   * 
   * @param classifier
   * @return
   */
  public static <K extends Classifier> Voter<K> fromTrainedModels(
      List<K> classifier) {
    return new Voter<>(classifier);
  }

  final class TrainingWorker implements Callable<Boolean> {

    private final Classifier cls;
    private final TrainingSplit split;

    TrainingWorker(Classifier classifier, TrainingSplit split) {
      this.cls = classifier;
      this.split = split;
    }

    @Override
    public Boolean call() throws Exception {

      cls.train(split.getTrainFeatures(), split.getTrainOutcome());

      return true;
    }
  }

}
