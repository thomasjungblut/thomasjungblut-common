package de.jungblut.classification.eval;

import de.jungblut.classification.Classifier;
import de.jungblut.classification.eval.Evaluator.EvaluationResult;
import de.jungblut.math.DoubleVector;
import de.jungblut.math.minimize.CostFunction;
import de.jungblut.math.minimize.IterationCompletionListener;
import de.jungblut.math.minimize.Minimizer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * The evaluation listener is majorly used to track the overfitting of a
 * classifier while training. This is usually hooked into the {@link Minimizer}
 * of choice and will be triggered at a configurable interval of iterations
 * (through {@link #setRunInterval(int)}). This class is designed to be
 * subclasses and enhanced with other print statements or functionality to save
 * the best performing parameters.
 *
 * @param <A> the type of the classifier.
 * @author thomas.jungblut
 */
public class EvaluationListener<A extends Classifier> implements
        IterationCompletionListener {

    private static final Logger LOG = LogManager
            .getLogger(EvaluationListener.class);

    protected final EvaluationSplit split;
    protected final WeightMapper<A> mapper;

    protected int runInterval = 1;

    /**
     * Initializes this listener.
     *
     * @param mapper the mapper that converts the {@link DoubleVector} from the
     *               minimizable {@link CostFunction} to a classifier.
     * @param split  the train/test split.
     */
    public EvaluationListener(WeightMapper<A> mapper, EvaluationSplit split) {
        this(mapper, split, 1);
    }

    /**
     * Initializes this listener.
     *
     * @param mapper      the mapper that converts the {@link DoubleVector} from the
     *                    minimizable {@link CostFunction} to a classifier.
     * @param split       the train/test split.
     * @param runInterval test interval.
     */
    public EvaluationListener(WeightMapper<A> mapper, EvaluationSplit split,
                              int runInterval) {
        this.mapper = mapper;
        this.split = split;
        this.runInterval = runInterval;
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
        }
    }

    /**
     * Sets the run intervall of this listener. For example: if set to 5, the
     * evaluator will run only every five iterations.
     */
    public final void setRunInterval(int runInterval) {
        this.runInterval = runInterval;
    }

    /**
     * Will be called on a result of the evaluation. This method does nothing, is
     * designed to be overridden though.
     *
     * @param iteration the current number of iteration.
     * @param cost      the identified cost of the costfunction.
     * @param trainEval the evaluation on the trainingset.
     * @param testEval  the evaluation on the testset.
     */
    protected void onResult(int iteration, double cost,
                            EvaluationResult trainEval, EvaluationResult testEval) {
        LOG.info("Iteration " + iteration + " | Validation accuracy: "
                + trainEval.getAccuracy() + " | Training accuracy: "
                + testEval.getAccuracy());
    }

}
