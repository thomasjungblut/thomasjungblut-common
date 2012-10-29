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

  @Test
  public void testVoting() {

    Voting voter = new Voting(CombiningType.MAX, new ClassifierFactory() {
      @Override
      public Classifier newInstance() {
        return new LogisticRegression(1.0d, new Fmincg(), 100, false);
      }
    }, 8, true);

    Tuple<DoubleVector[], DenseDoubleVector[]> data = MushroomReader
        .readMushroomDataset();
    voter.train(data.getFirst(), data.getSecond());

  }
}
