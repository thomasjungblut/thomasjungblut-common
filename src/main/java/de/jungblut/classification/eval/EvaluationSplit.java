package de.jungblut.classification.eval;

import com.google.common.base.Preconditions;
import de.jungblut.datastructure.ArrayUtils;
import de.jungblut.math.DoubleVector;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Arrays;
import java.util.Deque;
import java.util.LinkedList;

/**
 * Split data class that contains the division of train/test vectors. If no
 * division is yet there, you can use the
 * {@link #create(DoubleVector[], DoubleVector[], float, boolean)} method.
 *
 * @author thomas.jungblut
 */
public class EvaluationSplit {

    private static final Logger LOG = LogManager.getLogger(EvaluationSplit.class);

    private final DoubleVector[] trainFeatures;
    private final DoubleVector[] trainOutcome;
    private final DoubleVector[] testFeatures;
    private final DoubleVector[] testOutcome;

    /**
     * Sets a split internally.
     */
    public EvaluationSplit(DoubleVector[] trainFeatures,
                           DoubleVector[] trainOutcome, DoubleVector[] testFeatures,
                           DoubleVector[] testOutcome) {
        this.trainFeatures = trainFeatures;
        this.trainOutcome = trainOutcome;
        this.testFeatures = testFeatures;
        this.testOutcome = testOutcome;
    }

    public DoubleVector[] getTrainFeatures() {
        return this.trainFeatures;
    }

    public DoubleVector[] getTrainOutcome() {
        return this.trainOutcome;
    }

    public DoubleVector[] getTestFeatures() {
        return this.testFeatures;
    }

    public DoubleVector[] getTestOutcome() {
        return this.testOutcome;
    }

    /**
     * Creates a new evaluation split.
     *
     * @param features      the features of your classifier.
     * @param outcome       the target variables of the classifier.
     * @param splitFraction a value between 0f and 1f that sets the size of the
     *                      trainingset. With 1k items, a splitPercentage of 0.9f will result
     *                      in 900 items to train and 100 to evaluate.
     * @param random        true if data needs shuffling before.
     * @return a new {@link EvaluationSplit}.
     */
    public static EvaluationSplit create(DoubleVector[] features,
                                         DoubleVector[] outcome, float splitFraction, boolean random) {
        Preconditions.checkArgument(features.length == outcome.length,
                "Feature vector and outcome vector must match in length!");
        Preconditions.checkArgument(splitFraction >= 0f && splitFraction <= 1f,
                "splitFraction must be between 0 and 1! Given: " + splitFraction);

        if (random) {
            ArrayUtils.multiShuffle(features, outcome);
        }

        final int splitIndex = (int) (features.length * splitFraction);
        DoubleVector[] trainFeatures = ArrayUtils
                .subArray(features, splitIndex - 1);
        DoubleVector[] trainOutcome = ArrayUtils.subArray(outcome, splitIndex - 1);
        DoubleVector[] testFeatures = ArrayUtils.subArray(features, splitIndex,
                features.length - 1);
        DoubleVector[] testOutcome = ArrayUtils.subArray(outcome, splitIndex,
                outcome.length - 1);
        return new EvaluationSplit(trainFeatures, trainOutcome, testFeatures,
                testOutcome);
    }

    /**
     * Creates a new stratified evaluation split. Sampling is done based on the
     * max index of the outcome classes (assumes one-hot encoding, or zero/one
     * encoding for binary classes). This class does not keep the relation of the
     * original outcome vectors to their features, thus every mutual information
     * stored for both should be included in the feature vector.
     *
     * @param features      the features of your classifier.
     * @param outcome       the target variables of the classifier.
     * @param splitFraction a value between 0f and 1f that sets the size of the
     *                      trainingset. With 1k items, a splitPercentage of 0.9f will result
     *                      in 900 items to train and 100 to evaluate.
     * @param random        true if data needs shuffling before.
     * @return a new {@link EvaluationSplit}.
     */
    public static EvaluationSplit createStratified(DoubleVector[] features,
                                                   DoubleVector[] outcome, float splitFraction, boolean random) {
        Preconditions.checkArgument(features.length == outcome.length,
                "Feature vector and outcome vector must match in length!");
        Preconditions.checkArgument(splitFraction >= 0f && splitFraction <= 1f,
                "splitFraction must be between 0 and 1! Given: " + splitFraction);

        // we group features based on their class

        @SuppressWarnings("unchecked")
        Deque<DoubleVector>[] multiQueues = new Deque[Math.max(2,
                outcome[0].getDimension())];

        DoubleVector[] sampleOutcome = new DoubleVector[multiQueues.length];

        for (int i = 0; i < features.length; i++) {
            int key = multiQueues.length == 2 ? (int) outcome[i].get(0) : outcome[i]
                    .maxIndex();
            Deque<DoubleVector> deque = multiQueues[key];
            if (deque == null) {
                deque = new LinkedList<>();
                multiQueues[key] = deque;
            }
            deque.addLast(features[i]);
            sampleOutcome[key] = outcome[i];
        }

        for (int i = 0; i < multiQueues.length; i++) {
            Preconditions
                    .checkNotNull(
                            multiQueues[i],
                            "Queue for class "
                                    + i
                                    + " couldn't be found. This happens when the mentioned class label doesn't exists in the given set of vectors.");
        }

        int splitSize = (int) (features.length * splitFraction);
        double[] samplingProbabilities = new double[multiQueues.length];
        int[] splitIndices = new int[multiQueues.length];
        int sum = 0;
        for (int i = 0; i < multiQueues.length; i++) {
            samplingProbabilities[i] = ((double) multiQueues[i].size())
                    / features.length;
            splitIndices[i] = (int) (splitSize * samplingProbabilities[i]);
            Preconditions.checkArgument(splitIndices[i] > 0,
                    "Can't stratify the class " + i + " because the split size was "
                            + "too small to satisfy the sampling requirement.");
            sum += splitIndices[i];
        }

        if (sum != splitSize) {
            // correct the difference that arises from float arithmetic
            LOG.warn("Correcting the split size from " + splitSize + " to " + sum
                    + ", to satisfy the sampling target.");
            splitSize = sum;
        }

        DoubleVector[] trainFeatures = new DoubleVector[splitSize];
        DoubleVector[] trainOutcomes = new DoubleVector[splitSize];

        LOG.info("Sampling probabilities by class: "
                + Arrays.toString(samplingProbabilities));

        int offset = 0;
        for (int s = 0; s < splitIndices.length; s++) {
            for (int i = 0; i < splitIndices[s]; i++) {
                trainFeatures[offset] = multiQueues[s].poll();
                trainOutcomes[offset] = sampleOutcome[s];
                offset++;
            }
        }

        Preconditions.checkArgument(offset == trainFeatures.length,
                "Didn't fill up the targeted split size of " + splitSize
                        + " vectors in the training set!");

        // poll the rest out of the queues for the test set
        DoubleVector[] testFeatures = new DoubleVector[features.length - splitSize];
        DoubleVector[] testOutcomes = new DoubleVector[features.length - splitSize];

        offset = 0;
        for (int i = 0; i < multiQueues.length; i++) {
            while (!multiQueues[i].isEmpty()) {

                Preconditions
                        .checkArgument(offset < testFeatures.length,
                                "Features are overflowing the calculated testset size, stratifying failed.");

                testFeatures[offset] = multiQueues[i].poll();
                testOutcomes[offset] = sampleOutcome[i];
                offset++;
            }
        }

        if (random) {
            // radomize both sets again
            ArrayUtils.multiShuffle(trainFeatures, trainOutcomes);
            ArrayUtils.multiShuffle(testFeatures, testOutcomes);
        }

        return new EvaluationSplit(trainFeatures, trainOutcomes, testFeatures,
                testOutcomes);
    }
}
