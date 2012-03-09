package de.jungblut.math.forkjoin;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveTask;

import static de.jungblut.math.forkjoin.StrassenMatrixMultiplication.*;

/**
 * Uses static imports of the {@link StrassenMatrixMultiplication} class and
 * extends it with the fork/join framework introduced with java7.
 * 
 * @author thomas.jungblut
 */
public class ForkJoinStrassenMatrixMultiplication {

  /**
   * Multiplies two quadratic (length must be a power of 2) matrices.
   * 
   * @param a
   * @param b
   * @return
   */
  private static double[][] multiply(double[][] a, double[][] b)
      throws InterruptedException, ExecutionException {
    checkInput(a, b);
    // setup a pool
    ForkJoinPool pool = new ForkJoinPool();
    // start the initial computing instance
    StrassenComputer computer = new StrassenComputer(a, b);
    // submit it to the pool
    pool.submit(computer);
    // wait for the result
    return computer.get();
  }

  public static final class StrassenComputer extends RecursiveTask<double[][]> {

    private static final long serialVersionUID = -3691620760085314284L;
    private final double[][] inputA;
    private final double[][] inputB;

    public StrassenComputer(double[][] inputA, double[][] inputB) {
      super();
      this.inputA = inputA;
      this.inputB = inputB;
    }

    @Override
    protected double[][] compute() {
      int n = inputA.length;
      if (n == 1) {
        double[][] toReturn = new double[1][1];
        toReturn[0][0] = inputA[0][0] * inputB[0][0];
        return toReturn;
      }

      int nHalf = n / 2;
      double[][] a = copy(nHalf, inputA, 0, 0);
      double[][] b = copy(nHalf, inputA, 0, nHalf);
      double[][] c = copy(nHalf, inputA, nHalf, 0);
      double[][] d = copy(nHalf, inputA, nHalf, nHalf);
      double[][] e = copy(nHalf, inputB, 0, 0);
      double[][] f = copy(nHalf, inputB, 0, nHalf);
      double[][] g = copy(nHalf, inputB, nHalf, 0);
      double[][] h = copy(nHalf, inputB, nHalf, nHalf);

      // create fork instances
      ForkJoinTask<double[][]> fork1 = new StrassenComputer(a, add(f, h, -1))
          .fork(); // P1 = a(f-h) = af-ah
      ForkJoinTask<double[][]> fork2 = new StrassenComputer(add(a, b, 1), h)
          .fork(); // P2 = (a+b)h = ah+bh
      ForkJoinTask<double[][]> fork3 = new StrassenComputer(add(c, d, 1), e)
          .fork();// P3 = (c+d)e = ce+de
      ForkJoinTask<double[][]> fork4 = new StrassenComputer(d, add(g, e, -1))
          .fork();// P4 = d(g-e) = dg-de
      // P5 = (a+d)(e+h)=ae+de+ah+dh
      ForkJoinTask<double[][]> fork5 = new StrassenComputer(add(a, d, 1), add(
          e, h, 1)).fork();
      // P6 = (b-d)(g+h)=bg-dg+bh-dh
      ForkJoinTask<double[][]> fork6 = new StrassenComputer(add(b, d, -1), add(
          g, h, 1)).fork();
      // P7 = (a-c)(e+f)=ae-ce+af-cf
      ForkJoinTask<double[][]> fork7 = new StrassenComputer(add(a, c, -1), add(
          e, f, 1)).fork();

      // r = P5+P4-P2+P6 = ae+bg
      double[][] r = add(add(fork5.join(), fork4.join(), 1),
          add(fork2.join(), fork6.join(), -1), -1);
      // s = P1+P2 = af+bh
      double[][] s = add(fork1.join(), fork2.join(), 1);
      // t = P3+P4 = ce+dg
      double[][] t = add(fork3.join(), fork4.join(), 1);
      // u = P5+P1-P3-P7 = cf+dh
      double[][] u = add(add(fork5.join(), fork1.join(), 1),
          add(fork3.join(), fork7.join(), 1), -1);

      return reconstructMatrix(r, s, t, u);

    }
  }

  public static void main(String[] args) throws InterruptedException,
      ExecutionException {

    double[][] a = new double[][] { { 8, 9 }, { 5, -1 } };
    double[][] b = new double[][] { { -2, 3 }, { 4, 0 } };

    /*
     * Answer: [20.0, 24.0] [-14.0, 15.0]
     */
    printMatrix(multiply(a, b));
  }

}
