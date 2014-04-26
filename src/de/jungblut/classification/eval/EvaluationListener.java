package de.jungblut.classification.eval;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import de.jungblut.classification.Classifier;
import de.jungblut.classification.eval.Evaluator.EvaluationResult;
import de.jungblut.math.DoubleVector;
import de.jungblut.math.minimize.CostFunction;
import de.jungblut.math.minimize.IterationCompletionListener;
import de.jungblut.math.minimize.Minimizer;

/**
 * The evaluation listener is majorly used to track the overfitting of a
 * classifier while training. This is usually hooked into the {@link Minimizer}
 * of choice and will be triggered at a configurable interval of iterations
 * (through {@link #setRunIntervall(int)}). This class is designed to be
 * subclasses and enhanced with other print statements or functionality to save
 * the best performing parameters.
 * 
 * @author thomas.jungblut
 * 
 * @param <A> the type of the classifier.
 */
public class EvaluationListener<A extends Classifier> implements
    IterationCompletionListener {

  private static final Log LOG = LogFactory.getLog(EvaluationListener.class);

  protected final int numLabels;
  protected final EvaluationSplit split;
  protected final WeightMapper<A> mapper;

  protected int runInterval = 1;

  /**
   * Initializes this listener.
   * 
   * @param mapper the mapper that converts the {@link DoubleVector} from the
   *          minimizable {@link CostFunction} to a classifier.
   * @param numLabels the number of labels the classifier can predict.
   * @param split the train/test split.
   */
  public EvaluationListener(WeightMapper<A> mapper, int numLabels,
      EvaluationSplit split) {
    this.mapper = mapper;
    this.numLabels = numLabels;
    this.split = split;
  }

  @Override
  public void onIterationFinished(int iteration, double cost,
      DoubleVector currentWeights) {
    if (iteration % runInterval == 0) {
      A classifier = mapper.mapWeights(currentWeights);
      EvaluationResult testEval = Evaluator.testClassifier(classifier,
          split.getTestFeatures(), split.getTestOutcome());
      EvaluationResult trainEval = Evaluator.testClassifier(classifier,
          split.getTrainFeatures(), split.getTrainOutcome());
      onResult(iteration, cost, trainEval, testEval);
      print(iteration, cost, trainEval, testEval);
    }
  }

  /**
   * Sets the run intervall of this listener. For example: if set to 5, the
   * evaluator will run only every five iterations.
   */
  public final void setRunIntervall(int runIntervall) {
    this.runInterval = runIntervall;
  }

  /**
   * Will be called on a result of the evaluation. This method does nothing, is
   * designed to be overriden though.
   * 
   * @param iteration the current number of iteration.
   * @param cost the identified cost of the costfunction.
   * @param trainEval the evaluation on the trainingset.
   * @param testEval the evaluation on the testset.
   */
  protected void onResult(int iteration, double cost,
      EvaluationResult trainEval, EvaluationResult testEval) {

  }

  /**
   * Prints information about the accuraccy. This is designed to be overridden
   * by subclasses to e.g. log to a file or print something else.
   */
  protected void print(int iteration, double cost,
      EvaluationResult trainEvaluation, EvaluationResult testEvaluation) {
    LOG.info("Iteration " + iteration + " | Validation accuracy: "
        + testEvaluation.getAccuracy() + " | Training accuracy: "
        + trainEvaluation.getAccuracy());
  }

}
