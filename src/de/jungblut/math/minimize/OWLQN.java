package de.jungblut.math.minimize;

import gnu.trove.list.array.TDoubleArrayList;

import java.util.ArrayList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.math3.util.FastMath;

import de.jungblut.math.DoubleVector;
import de.jungblut.math.dense.DenseDoubleVector;

/**
 * Java translation of C++ code of
 * "Orthant-Wise Limited-memory Quasi-Newton Optimizer for L1-regularized Objectives"
 * (@see <a href=
 * "http://research.microsoft.com/en-us/downloads/b1eb1016-1738-4bd5-83a9-370
 * c9d498a03/">http://research.microsoft.com/</a>). <br/>
 * <br/>
 * 
 * The Orthant-Wise Limited-memory Quasi-Newton algorithm (OWL-QN) is a
 * numerical optimization procedure for finding the optimum of an objective of
 * the form {smooth function} plus {L1-norm of the parameters}. It has been used
 * for training log-linear models (such as logistic regression) with
 * L1-regularization. The algorithm is described in
 * "Scalable training of L1-regularized log-linear models" by Galen Andrew and
 * Jianfeng Gao. <br/>
 * <br/>
 * 
 * Orthant-Wise Limited-memory Quasi-Newton algorithm minimizes functions of the
 * form<br/>
 * <br/>
 * 
 * f(w) = loss(w) + C |w|_1<br/>
 * <br/>
 * 
 * where loss is an arbitrary differentiable convex loss function, and |w|_1 is
 * the L1 norm of the weight (parameter) vector. It is based on the L-BFGS
 * Quasi-Newton algorithm, with modifications to deal with the fact that the L1
 * norm is not differentiable. The algorithm is very fast, and capable of
 * scaling efficiently to problems with millions of parameters.<br/>
 * <br/>
 * 
 * 
 * This is a straight forward translation, with the use of my math library.
 * 
 * @author thomas.jungblut
 *
 */
public class OWLQN extends AbstractMinimizer {

  private static final Log LOG = LogFactory.getLog(OWLQN.class);

  private DoubleVector x, grad, newX, newGrad, dir;
  private DoubleVector steepestDescDir;
  private double[] alphas;
  private TDoubleArrayList roList;
  private TDoubleArrayList costs;
  private ArrayList<DoubleVector> sList, yList;
  private double value;
  private int m = 10;
  private double l1weight = 0;

  private double tol = 1e-4;
  private boolean gradCheck = false;

  @Override
  public DoubleVector minimize(CostFunction f, DoubleVector theta,
      int maxIterations, boolean verbose) {

    DoubleVector zeros = new DenseDoubleVector(theta.getDimension());
    this.x = theta;
    this.grad = zeros;
    this.newX = theta.deepCopy();
    this.newGrad = zeros;
    this.dir = zeros;
    this.steepestDescDir = newGrad;

    this.alphas = new double[m];
    this.roList = new TDoubleArrayList(m);
    this.costs = new TDoubleArrayList(m);
    this.sList = new ArrayList<>();
    this.yList = new ArrayList<>();

    this.value = evaluateL1(f);
    this.grad = newGrad;

    for (int i = 0; i < maxIterations; i++) {
      updateDir(f, verbose);
      boolean continueIterations = backTrackingLineSearch(i, f);
      shift();
      costs.add(value);

      // also break on too small average improvement over 5 iterations
      if (costs.size() > 5) {
        double first = costs.get(0);
        while (costs.size() > 5) {
          costs.removeAt(0);
        }
        double avgImprovement = (first - value) / costs.size();
        double perc = avgImprovement / Math.abs(value);
        if (perc < tol) {
          break;
        }
      }

      // break if we can't get any improvement
      if (!continueIterations) {
        break;
      }

      if (verbose) {
        LOG.info("Iteration " + i + " | Cost: " + value);
      }

    }

    // cleanup all the stuff in this class
    x = null;
    grad = null;
    newGrad = null;
    dir = null;
    steepestDescDir = null;
    alphas = null;
    roList = null;
    costs = null;
    sList = null;
    yList = null;

    return newX;
  }

  private void updateDir(CostFunction f, boolean verbose) {
    makeSteepestDescDir();
    mapDirectionByInverseHessian();
    fixDirectionSigns();

    if (gradCheck) {
      testDirectionDerivation(f);
    }
  }

  private void testDirectionDerivation(CostFunction f) {
    double dirNorm = FastMath.sqrt(dir.dot(dir));
    // if dirNorm is 0, we probably hit the minimum. So we have no gradient to
    // descent to.
    if (dirNorm != 0d) {
      double eps = 1.05e-8 / dirNorm;
      getNextPoint(eps);
      double val2 = evaluateL1(f);
      double numDeriv = (val2 - value) / eps;
      double deriv = directionDerivation();
      LOG.info("GradCheck: expected= " + numDeriv + " vs. " + deriv
          + "! AbsDiff= " + Math.abs(numDeriv - deriv));
    }
  }

  private void fixDirectionSigns() {
    if (l1weight > 0) {
      for (int i = 0; i < dir.getDimension(); i++) {
        if (dir.get(i) * steepestDescDir.get(i) <= 0) {
          dir.set(i, 0);
        }
      }
    }
  }

  private void mapDirectionByInverseHessian() {
    int count = sList.size();

    if (count != 0) {
      for (int i = count - 1; i >= 0; i--) {
        alphas[i] = -sList.get(i).dot(dir) / roList.get(i);
        addMult(dir, yList.get(i), alphas[i]);
      }

      DoubleVector lastY = yList.get(count - 1);
      double yDotY = lastY.dot(lastY);
      double scalar = roList.get(count - 1) / yDotY;
      scale(dir, scalar);

      for (int i = 0; i < count; i++) {
        double beta = yList.get(i).dot(dir) / roList.get(i);
        addMult(dir, sList.get(i), -alphas[i] - beta);
      }
    }
  }

  private void makeSteepestDescDir() {
    if (l1weight == 0) {
      scaleInto(dir, grad, -1);
    } else {

      for (int i = 0; i < dir.getDimension(); i++) {
        if (x.get(i) < 0) {
          dir.set(i, -grad.get(i) + l1weight);
        } else if (x.get(i) > 0) {
          dir.set(i, -grad.get(i) - l1weight);
        } else {
          if (grad.get(i) < -l1weight) {
            dir.set(i, -grad.get(i) - l1weight);
          } else if (grad.get(i) > l1weight) {
            dir.set(i, -grad.get(i) + l1weight);
          } else {
            dir.set(i, 0);
          }
        }
      }
    }

    steepestDescDir = dir;
  }

  private boolean backTrackingLineSearch(int iter, CostFunction f) {
    double origDirDeriv = directionDerivation();
    // if a non-descent direction is chosen, the line search will break anyway,
    // so throw here
    // The most likely reason for this is a bug in your function's gradient
    // computation
    if (origDirDeriv > 0d) {
      throw new RuntimeException(
          "L-BFGS chose a non-descent direction: check your gradient!");
    } else if (origDirDeriv == 0d) {
      LOG.info("L-BFGS apparently found the minimum. No direction to descent anymore.");
      return false;
    }

    double alpha = 1.0;
    double backoff = 0.5;
    if (iter == 0) {
      double normDir = FastMath.sqrt(dir.dot(dir));
      alpha = (1 / normDir);
      backoff = 0.1;
    }

    double c1 = 1e-4;
    double oldValue = value;

    while (true) {
      getNextPoint(alpha);
      value = evaluateL1(f);

      if (Double.isNaN(value) || value <= oldValue + c1 * origDirDeriv * alpha) {
        break;
      }

      alpha *= backoff;
    }

    return true;
  }

  private void getNextPoint(double alpha) {
    addMultInto(newX, x, dir, alpha);

    if (l1weight > 0) {
      for (int i = 0; i < x.getDimension(); i++) {
        if (x.get(i) * newX.get(i) < 0.0) {
          newX.set(i, 0d);
        }
      }
    }
  }

  private void addMultInto(DoubleVector a, DoubleVector b, DoubleVector c,
      double d) {
    for (int i = 0; i < a.getDimension(); i++) {
      a.set(i, b.get(i) + c.get(i) * d);
    }
  }

  private void addMult(DoubleVector a, DoubleVector b, double c) {
    for (int i = 0; i < a.getDimension(); i++) {
      a.set(i, a.get(i) + b.get(i) * c);
    }
  }

  private void scale(DoubleVector a, double b) {
    for (int i = 0; i < a.getDimension(); i++) {
      a.set(i, a.get(i) * b);
    }
  }

  void scaleInto(DoubleVector a, DoubleVector b, double c) {
    for (int i = 0; i < a.getDimension(); i++) {
      a.set(i, b.get(i) * c);
    }
  }

  private double directionDerivation() {
    if (l1weight == 0.0) {
      return dir.dot(grad);
    } else {
      double val = 0.0;
      for (int i = 0; i < dir.getDimension(); i++) {
        if (dir.get(i) != 0) {
          if (x.get(i) < 0) {
            val += dir.get(i) * (grad.get(i) - l1weight);
          } else if (x.get(i) > 0) {
            val += dir.get(i) * (grad.get(i) + l1weight);
          } else if (dir.get(i) < 0) {
            val += dir.get(i) * (grad.get(i) - l1weight);
          } else if (dir.get(i) > 0) {
            val += dir.get(i) * (grad.get(i) + l1weight);
          }
        }
      }

      return val;
    }
  }

  private double evaluateL1(CostFunction f) {
    CostGradientTuple evaluateCost = f.evaluateCost(newX);
    newGrad = evaluateCost.getGradient();
    double val = evaluateCost.getCost();
    if (l1weight > 0) {
      for (int i = 0; i < newGrad.getDimension(); i++) {
        val += Math.abs(newX.get(i)) * l1weight;
      }
    }

    return val;
  }

  private void shift() {
    DoubleVector nextS = null;
    DoubleVector nextY = null;

    int listSize = sList.size();

    if (listSize < m) {
      nextS = new DenseDoubleVector(x.getDimension());
      nextY = new DenseDoubleVector(x.getDimension());
    }

    if (nextS == null) {
      nextS = sList.get(0);
      sList.remove(0);
      nextY = yList.get(0);
      yList.remove(0);
      roList.removeAt(0);
    }

    addMultInto(nextS, newX, x, -1);
    addMultInto(nextY, newGrad, grad, -1);
    double ro = nextS.dot(nextY);

    sList.add(nextS);
    yList.add(nextY);
    roList.add(ro);

    DoubleVector tmpNewX = newX.deepCopy();
    newX = x.deepCopy();
    x = tmpNewX;

    DoubleVector tmpNewGrad = newGrad.deepCopy();
    newGrad = grad.deepCopy();
    grad = tmpNewGrad;
  }

  /**
   * Set to true this will check the gradients every iteration and print out if
   * it aligns with the numerical gradient.
   */
  public OWLQN doGradChecks() {
    this.gradCheck = true;
    return this;
  }

  /**
   * The amount of directions and gradients to keep, this is the "limited" part
   * of L-BFGS. It defaults to 10.
   */
  public OWLQN setM(int m) {
    this.m = m;
    return this;
  }

  /**
   * This implementation also supports l1 weight adjustment (without the
   * costfunction knowing about it). This is turned off by default.
   */
  public OWLQN setL1Weight(double l1weight) {
    this.l1weight = l1weight;
    return this;
  }

  /**
   * The breaking tolerance over a window of five iterations. This defaults to
   * 1e-4.
   */
  public OWLQN setTolerance(double tol) {
    this.tol = tol;
    return this;
  }

  /**
   * Minimizes the given cost function with L-BFGS.
   * 
   * @param f the costfunction to minimize.
   * @param theta the initial weights.
   * @param maxIterations the maximum amount of iterations.
   * @param verbose true if progress output shall be printed.
   * @return the optimized set of parameters for the cost function.
   */
  public static DoubleVector minimizeFunction(CostFunction f,
      DoubleVector theta, int maxIterations, boolean verbose) {
    return new OWLQN().minimize(f, theta, maxIterations, verbose);
  }
}
