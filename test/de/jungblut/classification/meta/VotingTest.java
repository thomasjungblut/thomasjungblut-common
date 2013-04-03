package de.jungblut.classification.meta;

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

  private Tuple<DoubleVector[], DenseDoubleVector[]> data;
  private ClassifierFactory factory = new ClassifierFactory() {
    @Override
    public Classifier newInstance() {
      return new LogisticRegression(0.0d, new Fmincg(), 1000, false);
    }
  };
  private double logisticTrainingError;

  @Override
  protected void setUp() throws Exception {
    data = MushroomReader
        .readMushroomDataset("files/mushroom/mushroom_dataset.csv");
    logisticTrainingError = trainInternal(factory.newInstance());
  }

  @Test
  public void testVoting() {
    Voting voter = new Voting(CombiningType.MAJORITY, factory, 20, false);
    double trainingError = trainInternal(voter);
    assertTrue("Error of single logistic regression: " + logisticTrainingError
        + " and voted regression was higher: " + trainingError,
        logisticTrainingError > trainingError);
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

    classifier.train(data.getFirst(), data.getSecond());

    double err = 0d;
    for (int i = 0; i < data.getFirst().length; i++) {
      DoubleVector features = data.getFirst()[i];
      DenseDoubleVector outcome = data.getSecond()[i];
      DoubleVector predict = classifier.predict(features);
      err += outcome.subtract(predict).abs().sum();
    }
    return err / data.getFirst().length;
  }
}
