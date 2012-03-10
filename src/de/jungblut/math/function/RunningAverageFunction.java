package de.jungblut.math.function;

public final class RunningAverageFunction implements DoubleDoubleVectorFunction {

  private final double newk;

  public RunningAverageFunction(double newk) {
    super();
    this.newk = newk;
  }

  @Override
  public double calculate(int index, double left, double right) {
    return left + (right / newk) - (left / newk);
  }

}
