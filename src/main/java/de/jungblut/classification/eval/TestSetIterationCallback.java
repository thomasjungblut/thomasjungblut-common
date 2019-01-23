package de.jungblut.classification.eval;

import com.google.common.base.Preconditions;
import de.jungblut.classification.Classifier;
import de.jungblut.classification.eval.Evaluator.EvaluationResult;
import de.jungblut.math.DoubleVector;
import de.jungblut.math.minimize.IterationCompletionListener;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Comparator;

/**
 * This callback is used to evaluate the performance on a held-out test set. It
 * saves the best performing parameter inside to be retrieved later.
 *
 * @author thomas.jungblut
 */
public class TestSetIterationCallback<T extends Classifier> implements
        IterationCompletionListener {

    private static final Logger LOG = LogManager
            .getLogger(TestSetIterationCallback.class);

    private final EvaluationSplit split;
    private final WeightMapper<T> mapper;
    private final Comparator<EvaluationResult> resultComparison;

    private EvaluationResult bestResult;
    private DoubleVector bestWeights;
    private int evaluationInterval;

    /**
     * Creates a new test set iteration callback instance.
     *
     * @param split              the split to test on
     * @param mapper             the weight mapper to transform a weight to a classifier.
     * @param resultComparison   the comparator to compare what result is better and
     *                           should be kept.
     * @param evaluationInterval how often the result should be evaluated.
     */
    public TestSetIterationCallback(EvaluationSplit split,
                                    WeightMapper<T> mapper, Comparator<EvaluationResult> resultComparison,
                                    int evaluationInterval) {
        this.evaluationInterval = evaluationInterval;
        this.resultComparison = Preconditions.checkNotNull(resultComparison,
                "resultComparison");
        this.split = Preconditions.checkNotNull(split, "split");
        this.mapper = Preconditions.checkNotNull(mapper, "mapper");
    }

    /**
     * Creates a new test set iteration callback instance. The callback executes
     * every 10 iterations.
     *
     * @param split            the split to test on
     * @param mapper           the weight mapper to transform a weight to a classifier.
     * @param resultComparison the comparator to compare what result is better and
     *                         should be kept.
     */
    public TestSetIterationCallback(EvaluationSplit split,
                                    WeightMapper<T> mapper, Comparator<EvaluationResult> resultComparison) {
        this(split, mapper, resultComparison, 10);
    }

    @Override
    public void onIterationFinished(int iteration, double cost,
                                    DoubleVector currentWeights) {

        if (iteration % evaluationInterval == 0) {
            T newClassifier = mapper.mapWeights(currentWeights);
            EvaluationResult result = Evaluator.testClassifier(newClassifier,
                    split.getTestFeatures(), split.getTestOutcome());

            if (bestResult == null) {
                bestResult = result;
                bestWeights = currentWeights.deepCopy();
            } else {
                if (resultComparison.compare(bestResult, result) > 0) {
                    LOG.info("Found better weights with result:");
                    result.print(LOG);
                    bestResult = result;
                    bestWeights = currentWeights.deepCopy();
                }
            }
        }
    }

    public EvaluationResult getBestResult() {
        return bestResult;
    }

    public DoubleVector getBestWeights() {
        return bestWeights;
    }
}
