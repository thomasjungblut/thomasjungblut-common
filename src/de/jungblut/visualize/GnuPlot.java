package de.jungblut.visualize;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;

import de.jungblut.math.DenseDoubleMatrix;
import de.jungblut.math.DenseDoubleVector;

// for windows only and only if gnuplot 4.4 can be found in the path
public class GnuPlot {

  public static void plot(DenseDoubleMatrix x, DenseDoubleVector y,
      DenseDoubleVector model) {

    try (BufferedWriter bw = new BufferedWriter(new FileWriter(new File(
        "/gnuplot.in")))) {
      for (int i = 0; i < y.getLength(); i++) {
        bw.write(x.get(i, 0) + " " + y.get(i) + "\r\n");
      }
    } catch (IOException e) {
      e.printStackTrace();
    }

    String exec = "plot " + modelToGNUPlot(model)
        + " with lines, '/gnuplot.in' with points;";
    try {
      Files.write(FileSystems.getDefault().getPath("/exec.gp"),
          exec.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.WRITE,
          StandardOpenOption.TRUNCATE_EXISTING);
      Process exec2 = Runtime.getRuntime().exec(
          new String[] { "gnuplot", "-p", "/exec.gp" });
      // Scanner scan = new Scanner(System.in);
      // scan.nextLine();
      // Thread.sleep(3000L);
      exec2.waitFor();
    } catch (IOException e) {
      e.printStackTrace();
    } catch (InterruptedException e) {
      e.printStackTrace();
    }

  }

  static String modelToGNUPlot(DenseDoubleVector p) {
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
