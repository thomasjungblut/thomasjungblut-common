package de.jungblut.math.par;

import java.util.Arrays;

/**
 * Implementation of the Strassen Matrix multiplication algorithm. <br/>
 * Cleaned and improved algorithm from {@link http
 * ://www.cs.huji.ac.il/~omrif01/Strassen}.
 * <p/>
 * <br/>
 * Contains "HAMA" tags for a first scratch of a BSP port.
 *
 * @author thomas.jungblut
 */
public class StrassenMatrixMultiplication {

    /**
     * Multiplies two quadratic (length must be a power of 2) matrices.
     *
     * @param a
     * @param b
     * @return
     */
    private static double[][] multiply(double[][] a, double[][] b) {
        checkInput(a, b);
        return strassen(a, b);
    }

    /**
     * Checks if our input matrices are quadratic and with a length of a power
     * of two.
     */
    public static void checkInput(double[][] a, double[][] b) {
        int n = a.length;
        if (!isPowerOfTwo(n)) {
            throw new IllegalArgumentException(
                    "Matrix has length of zero or is no power of two!");
        }
        for (int i = 0; i < n; i++) {
            int x = a[i].length;
            if (!isPowerOfTwo(x) || x != n) {
                throw new IllegalArgumentException(
                        "Matrix row has length of zero or is no power of two!");
            }
        }
    }

    /**
     * Reconstructs a matrix of 4 submatrices r,s,t and u.
     */
    public static double[][] reconstructMatrix(double[][] r,
                                               double[][] s, double[][] t, double[][] u) {
        final int doubledN = r.length * 2;
        final double[][] copy = new double[doubledN][doubledN];
        copyBack(copy, r, 0, 0);
        copyBack(copy, s, 0, r.length);
        copyBack(copy, t, r.length, 0);
        copyBack(copy, u, r.length, r.length);
        return copy;
    }

    /**
     * Copy method to reconstruct the matrix.
     */
    private static void copyBack(double[][] destination, double[][] r,
                                 int x, int y) {
        for (int i = 0; i < r.length; i++) {
            System.arraycopy(r[i], 0, destination[x + i], y, r.length);
        }
    }

    /**
     * Copy method.
     */
    public static double[][] copy(final int num, double[][] source,
                                  int x, int y) {
        double[][] destination = new double[num][num];
        for (int i = 0; i < num; i++) {
            System.arraycopy(source[x + i], y, destination[i], 0, num);
        }
        return destination;
    }

    /**
     * Main method which is called recursive until inputA contains only
     * one-element at [0][0]. It afterwards sums the matrices and reconstructs
     * the resulting matrix.
     */
    private static double[][] strassen(double[][] inputA,
                                       double[][] inputB) {
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

        // HAMA recursive tasks can be outsourced to grooms
        double[][] P1, P2, P3, P4, P5, P6, P7;
        P1 = strassen(a, add(f, h, -1)); // P1 = a(f-h) = af-ah
        P2 = strassen(add(a, b, 1), h); // P2 = (a+b)h = ah+bh
        P3 = strassen(add(c, d, 1), e); // P3 = (c+d)e = ce+de
        P4 = strassen(d, add(g, e, -1)); // P4 = d(g-e) = dg-de
        // P5 = (a+d)(e+h)=ae+de+ah+dh
        P5 = strassen(add(a, d, 1), add(e, h, 1));
        // P6 = (b-d)(g+h)=bg-dg+bh-dh
        P6 = strassen(add(b, d, -1), add(g, h, 1));
        // P7 = (a-c)(e+f)=ae-ce+af-cf
        P7 = strassen(add(a, c, -1), add(e, f, 1));

        // HAMA master delegates the addition to grooms
        double[][] r, s, t, u;
        r = add(add(P5, P4, 1), add(P2, P6, -1), -1); // r = P5+P4-P2+P6 = ae+bg
        s = add(P1, P2, 1); // s = P1+P2 = af+bh
        t = add(P3, P4, 1); // t = P3+P4 = ce+dg
        u = add(add(P5, P1, 1), add(P3, P7, 1), -1); // u = P5+P1-P3-P7 = cf+dh

        // HAMA master is reconstructing the answer

        return reconstructMatrix(r, s, t, u);
    }

    /**
     * Matrix addition method with a sign of b.
     */
    public static double[][] add(double[][] a, double[][] b, int signOfb) {
        int n = a.length;
        double[][] C = new double[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                C[i][j] = a[i][j] + signOfb * b[i][j];
            }
        }
        return C;
    }

    /**
     * Bit twiddeling to determine if length is a power of two. Note that zero
     * is no power of two.
     */
    private static boolean isPowerOfTwo(int length) {
        return ((length != 0) && ((length & (length - 1)) == 0));
    }

    /**
     * Pretty print for matrices.
     */
    public static void printMatrix(double[][] x) {
        for (double[] aX : x) System.out.println(Arrays.toString(aX));
    }

    public static void main(String[] args) {

        double[][] a = new double[][]{{8, 9}, {5, -1}};
        double[][] b = new double[][]{{-2, 3}, {4, 0}};

        /*
           * Answer: [20.0, 24.0] [-14.0, 15.0]
           */
        printMatrix(multiply(a, b));
    }

}
