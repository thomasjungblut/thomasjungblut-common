package de.jungblut.visualize;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.Scanner;

import de.jungblut.math.DoubleVector;
import de.jungblut.math.dense.DenseDoubleMatrix;
import de.jungblut.math.dense.DenseDoubleVector;
import de.jungblut.regression.PolynomialRegression;

// for windows only and only if gnuplot 4.4 can be found in the path
public class GnuPlot {

  public static void plot(DenseDoubleMatrix x, DoubleVector y,
      DoubleVector theta, int polyCount, DoubleVector mean, DoubleVector sigma) {
    /*
     * set xrange [" + (x.min(0) - 15) + ":" + (x.max(0) + 15) +
     * "] ; set yrange [" + (y.min() - 15) + ":" + (y.max() + 15) + "] ;
     */

    // calculate a few points
    DenseDoubleVector fromUpTo = DenseDoubleVector.fromUpTo(x.min(0) - 15,
        x.max(0) + 15, 0.05);

    DenseDoubleMatrix createPolynomials = PolynomialRegression
        .createPolynomials(new DenseDoubleMatrix(fromUpTo), polyCount);

    DenseDoubleMatrix xPolyNormalized = new DenseDoubleMatrix(
        DenseDoubleVector.ones(fromUpTo.getLength()),
        (DenseDoubleMatrix) createPolynomials.subtract(mean).divide(sigma));

    DoubleVector multiplyVector = xPolyNormalized.multiplyVector(theta);
    try (BufferedWriter bw = new BufferedWriter(new FileWriter(new File(
        "/gnuplot_function.in")))) {
      for (int i = 0; i < multiplyVector.getLength(); i++) {
        bw.write(fromUpTo.get(i) + " " + multiplyVector.get(i) + "\n");
      }
    } catch (IOException e) {
      e.printStackTrace();
    }

    try (BufferedWriter bw = new BufferedWriter(new FileWriter(new File(
        "/gnuplot.in")))) {
      for (int i = 0; i < y.getLength(); i++) {
        bw.write(x.get(i, 0) + " " + y.get(i) + "\n");
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
    // "plot "data" every 1000 using 1:2 with lines" for more data Page 30
    String exec = "set xzeroaxis; set yzeroaxis ; plot '/gnuplot.in' every 1000 using 1:2 with points, '/gnuplot_function.in' with lines;";
    try {
      Files.write(FileSystems.getDefault().getPath("/exec.gp"),
          exec.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.WRITE,
          StandardOpenOption.TRUNCATE_EXISTING);
      Process exec2 = Runtime.getRuntime().exec(
          new String[] { "gnuplot", "-p", "/exec.gp" });
      Scanner scan = new Scanner(System.in);
      scan.nextLine();
      exec2.destroy();
    } catch (IOException e) {
      e.printStackTrace();
    }

  }

  public static String modelToGNUPlot(DoubleVector p) {
    String s = "";
    for (int i = 0; i < p.getLength(); i++) {
      if (i == 0) {
        s += "" + p.get(i);
      } else if (i == 1) {
        s = p.get(i) + "*x + " + s;
      } else {
        s = p.get(i) + "*x**" + i + "+" + s;
      }
    }
    return s;
  }
}
