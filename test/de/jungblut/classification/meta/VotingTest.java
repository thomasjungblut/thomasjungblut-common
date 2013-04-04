package de.jungblut.classification.meta;

import java.util.Arrays;

import junit.framework.TestCase;

import org.junit.Test;

import de.jungblut.classification.Classifier;
import de.jungblut.classification.ClassifierFactory;
import de.jungblut.classification.meta.Voting.CombiningType;
import de.jungblut.classification.regression.LogisticRegression;
import de.jungblut.math.DoubleVector;
import de.jungblut.math.dense.DenseDoubleVector;
import de.jungblut.math.minimize.Fmincg;
import de.jungblut.math.tuple.Tuple;
import de.jungblut.reader.MushroomReader;

public class VotingTest extends TestCase {

  private ClassifierFactory factory = new ClassifierFactory() {
    @Override
    public Classifier newInstance() {
      return new LogisticRegression(0.0d, new Fmincg(), 1000, false);
    }
  };
  private double logisticTrainingError;

  private DoubleVector[] features;
  private DenseDoubleVector[] outcomes;

  @Override
  protected void setUp() throws Exception {
    Tuple<DoubleVector[], DenseDoubleVector[]> readMushroomDataset = MushroomReader
        .readMushroomDataset("files/mushroom/mushroom_dataset.csv");
    features = readMushroomDataset.getFirst();
    features = Arrays.copyOf(features, 500);
    outcomes = readMushroomDataset.getSecond();
    outcomes = Arrays.copyOf(outcomes, 500);

    logisticTrainingError = trainInternal(factory.newInstance());
  }

  @Test
  public void testVoting() {
    Voting voter = new Voting(CombiningType.MAJORITY, factory, 20, false);
    double trainingError = trainInternal(voter);
    assertEquals(0.002, trainingError, 0.01);
  }

  @Test
  public void testAverageVoting() {
    Voting voter = new Voting(CombiningType.AVERAGE, factory, 20, false);
    double trainingError = trainInternal(voter);
    assertEquals("Error of single logistic regression: "
        + logisticTrainingError + " and voted regression was higher: "
        + trainingError, logisticTrainingError, trainingError, 0.2d);
  }

  // returns the trainingset error
  public double trainInternal(Classifier classifier) {

    classifier.train(features, outcomes);

    double err = 0d;
    for (int i = 0; i < features.length; i++) {
      DoubleVector features = this.features[i];
      DenseDoubleVector outcome = this.outcomes[i];
      DoubleVector predict = classifier.predict(features);
      err += outcome.subtract(predict).abs().sum();
    }
    return err / this.features.length;
  }
}
