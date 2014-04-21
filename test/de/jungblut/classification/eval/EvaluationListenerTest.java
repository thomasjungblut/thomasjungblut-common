package de.jungblut.classification.eval;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import de.jungblut.classification.ClassifierFactory;
import de.jungblut.classification.eval.Evaluator.EvaluationResult;
import de.jungblut.classification.nn.MultilayerPerceptron;
import de.jungblut.classification.nn.MultilayerPerceptronWeightMapper;
import de.jungblut.datastructure.ArrayUtils;
import de.jungblut.math.DoubleVector;
import de.jungblut.math.activation.ActivationFunction;
import de.jungblut.math.activation.ActivationFunctionSelector;
import de.jungblut.math.minimize.GradientDescent;
import de.jungblut.math.squashing.LogisticErrorFunction;
import de.jungblut.reader.Dataset;
import de.jungblut.reader.MushroomReader;

public class EvaluationListenerTest {

  static int calls = 0;

  @Test
  public void testEvaluationCalls() throws Exception {

    Dataset dataset = MushroomReader
        .readMushroomDataset("files/mushroom/mushroom_dataset.csv");

    final GradientDescent minimizer = new GradientDescent(0.1, 0);
    DoubleVector[] subsetFeature = ArrayUtils.subArray(dataset.getFeatures(),
        10);
    DoubleVector[] subsetOutcome = ArrayUtils.subArray(dataset.getOutcomes(),
        10);

    final EvaluationSplit split = EvaluationSplit.create(subsetFeature,
        subsetOutcome, 0.9f, false);

    ClassifierFactory<MultilayerPerceptron> factory = new ClassifierFactory<MultilayerPerceptron>() {
      @Override
      public MultilayerPerceptron newInstance() {
        return MultilayerPerceptron.MultilayerPerceptronBuilder
            .create(
                new int[] { split.getTrainFeatures()[0].getDimension(), 50, 1 },
                new ActivationFunction[] {
                    ActivationFunctionSelector.LINEAR.get(),
                    ActivationFunctionSelector.SIGMOID.get(),
                    ActivationFunctionSelector.SIGMOID.get() },
                new LogisticErrorFunction(), minimizer, 10).verbose().build();
      }
    };

    EvaluationListener<MultilayerPerceptron> cb = new EvaluationListener<MultilayerPerceptron>(
        new MultilayerPerceptronWeightMapper(factory), 2, split) {
      @Override
      protected void onResult(int iteration, double cost,
          EvaluationResult trainEval, EvaluationResult testEval) {
        // increment a static since method variables need final modifier
        calls++;
      }
    };
    minimizer.addIterationCompletionCallback(cb);
    MultilayerPerceptron network = factory.newInstance();
    network.train(split.getTrainFeatures(), split.getTrainOutcome());
    // assert it was called exactly ten times
    assertEquals(10, calls);

  }
}
