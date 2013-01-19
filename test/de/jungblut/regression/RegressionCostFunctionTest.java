package de.jungblut.regression;

import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import junit.framework.TestCase;

import org.junit.Test;

import de.jungblut.math.DoubleMatrix;
import de.jungblut.math.DoubleVector;
import de.jungblut.math.MathUtils;
import de.jungblut.math.dense.DenseDoubleMatrix;
import de.jungblut.math.dense.DenseDoubleVector;
import de.jungblut.math.minimize.Fmincg;
import de.jungblut.math.minimize.GradientDescent;
import de.jungblut.math.tuple.Tuple3;

public class RegressionCostFunctionTest extends TestCase {

  @Test
  public void testCostFunctionFminCG() throws Exception {
    List<String> lines = Files.readAllLines(
        Paths.get("files/logreg/ex1data2.txt"), Charset.defaultCharset());

    DoubleMatrix features = new DenseDoubleMatrix(lines.size(), 2);
    DenseDoubleVector outcome = new DenseDoubleVector(lines.size());
    for (int i = 0; i < lines.size(); i++) {
      String[] line = lines.get(i).split(",");
      features.set(i, 0, Integer.parseInt(line[0]));
      features.set(i, 1, Integer.parseInt(line[1]));
      outcome.set(i, Integer.parseInt(line[2]));
    }

    Tuple3<DoubleMatrix, DoubleVector, DoubleVector> featureNormalize = MathUtils
        .featureNormalize(features);
    features = featureNormalize.getFirst();

    RegressionCostFunction costFunction = new RegressionCostFunction(features,
        outcome, 0d);

    DoubleVector theta = GradientDescent.minimizeFunction(costFunction,
        new DenseDoubleVector(3), 0.01, 1.0e-9, 200, true);
    System.out.println(theta);

    theta = Fmincg.minimizeFunction(costFunction, new DenseDoubleVector(3),
        200, true);
    System.out.println(theta);
  }

}
