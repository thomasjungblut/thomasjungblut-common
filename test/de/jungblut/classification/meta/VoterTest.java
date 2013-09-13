package de.jungblut.classification.meta;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;

import org.junit.BeforeClass;
import org.junit.Test;

import de.jungblut.classification.Classifier;
import de.jungblut.classification.ClassifierFactory;
import de.jungblut.classification.meta.Voter.CombiningType;
import de.jungblut.classification.regression.LogisticRegression;
import de.jungblut.math.DoubleVector;
import de.jungblut.math.minimize.Fmincg;
import de.jungblut.reader.Dataset;
import de.jungblut.reader.MushroomReader;

public class VoterTest {

  private static ClassifierFactory<LogisticRegression> factory = new ClassifierFactory<LogisticRegression>() {
    @Override
    public LogisticRegression newInstance() {
      return new LogisticRegression(0.0d, new Fmincg(), 1000, false);
    }
  };
  private static double logisticTrainingError;

  private static DoubleVector[] features;
  private static DoubleVector[] outcomes;

  @BeforeClass
  public static void setUp() throws Exception {
    Dataset dataset = MushroomReader
        .readMushroomDataset("files/mushroom/mushroom_dataset.csv");
    features = dataset.getFeatures();
    features = Arrays.copyOf(features, 500);
    outcomes = dataset.getOutcomes();
    outcomes = Arrays.copyOf(outcomes, 500);

    logisticTrainingError = trainInternal(factory.newInstance());
  }

  @Test
  public void testVoting() {
    Voter<LogisticRegression> voter = Voter.create(20, CombiningType.MAJORITY,
        factory);
    double trainingError = trainInternal(voter);
    assertEquals(0.002, trainingError, 0.1);
  }

  @Test
  public void testAverageVoting() {
    Voter<LogisticRegression> voter = Voter.create(20, CombiningType.AVERAGE,
        factory);
    double trainingError = trainInternal(voter);
    assertEquals("Error of single logistic regression: "
        + logisticTrainingError + " and voted regression was higher: "
        + trainingError, logisticTrainingError, trainingError, 0.2d);
  }

  // returns the trainingset error
  public static double trainInternal(Classifier classifier) {

    classifier.train(features, outcomes);

    double err = 0d;
    for (int i = 0; i < features.length; i++) {
      DoubleVector features = VoterTest.features[i];
      DoubleVector outcome = VoterTest.outcomes[i];
      DoubleVector predict = classifier.predict(features);
      err += outcome.subtract(predict).abs().sum();
    }
    return err / VoterTest.features.length;
  }
}
