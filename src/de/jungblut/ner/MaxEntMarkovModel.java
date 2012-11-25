package de.jungblut.ner;

import java.util.Collections;

import com.google.common.base.Preconditions;

import de.jungblut.classification.AbstractClassifier;
import de.jungblut.math.DoubleMatrix;
import de.jungblut.math.DoubleVector;
import de.jungblut.math.ViterbiUtils;
import de.jungblut.math.dense.DenseDoubleMatrix;
import de.jungblut.math.dense.DenseDoubleVector;
import de.jungblut.math.minimize.DenseMatrixFolder;
import de.jungblut.math.minimize.Minimizer;
import de.jungblut.math.sparse.SparseDoubleRowMatrix;

/**
 * Maximum entropy markov model for named entity recognition (classifying labels
 * in sequence learning).
 * 
 * @author thomas.jungblut
 * 
 */
public final class MaxEntMarkovModel extends AbstractClassifier {

  private final Minimizer minimizer;
  private final boolean verbose;
  private final int numIterations;
  private DenseDoubleMatrix theta;
  private int classes;

  public MaxEntMarkovModel(Minimizer minimizer, int numIterations,
      boolean verbose) {
    this.minimizer = minimizer;
    this.numIterations = numIterations;
    this.verbose = verbose;
  }

  @Override
  public void train(DoubleVector[] features, DenseDoubleVector[] outcome) {
    Preconditions
        .checkArgument(
            features.length == outcome.length && features.length > 0,
            "There wasn't at least a single featurevector, or the two array didn't match in size.");
    this.classes = outcome[0].getDimension() == 1 ? 2 : outcome[0]
        .getDimension();
    DoubleMatrix mat = null;
    if (features[0].isSparse()) {
      mat = new SparseDoubleRowMatrix(features);
    } else {
      mat = new DenseDoubleMatrix(features);
    }
    ConditionalLikelihoodCostFunction func = new ConditionalLikelihoodCostFunction(
        mat, new DenseDoubleMatrix(outcome));
    DoubleVector input = minimizer.minimize(func,
        new DenseDoubleVector(mat.getColumnCount() * classes), numIterations,
        verbose);
    theta = DenseMatrixFolder.unfoldMatrix(input, classes,
        (int) (input.getLength() / (double) classes));
  }

  @Override
  public DoubleVector predict(DoubleVector features) {
    return ViterbiUtils
        .decode(theta,
            new SparseDoubleRowMatrix(Collections.singletonList(features)),
            classes).getRowVector(0);
  }

  // matrix prediction
  public DoubleMatrix predict(DoubleMatrix features) {
    return ViterbiUtils.decode(theta, features, classes);
  }
}
