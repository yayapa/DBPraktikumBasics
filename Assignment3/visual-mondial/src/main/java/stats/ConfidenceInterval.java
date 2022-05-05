package stats;

import static java.lang.Math.sqrt;

import java.util.Arrays;
import org.apache.commons.math3.distribution.TDistribution;

public final class ConfidenceInterval {

  /**
   * Lower value of the interval, inclusive.
   */
  private final double lower;

  /**
   * Upper value of the interval, inclusive.
   */
  private final double upper;

  /**
   * The confidence level this interval was computed with.
   */
  private final double level;

  private ConfidenceInterval(double lower, double upper, double level) {
    checkParameters(lower, upper, level);
    this.lower = lower;
    this.upper = upper;
    this.level = level;
  }

  /**
   * Computes a new confidence interval for the population mean of the given array of values.
   * The values must have been collected i.i.d..
   *
   * @param values an array of real values
   * @param level  the confidence level, strictly between 0 and 1
   * @return a confidence interval for the population mean
   */
  public static ConfidenceInterval forMean(double[] values, double level) {
    int n = values.length;
    double mean = getMean(values);
    double variance = getVariance(values, mean);
    TDistribution tDist = new TDistribution(n - 1);
    double delta = tDist.inverseCumulativeProbability((1.0 + level) / 2);
    double meanVarianceSqrt = sqrt(variance / n);
    double lower = mean - delta * meanVarianceSqrt;
    double upper = mean + delta * meanVarianceSqrt;
    return new ConfidenceInterval(lower, upper, level);
  }

  /**
   * Computes a new confidence interval for the difference
   * between the population means of the given two arrays.
   * The values in each array must have been collected i.i.d..
   *
   * @param values1 an array of real values from one population
   * @param values2 an array of real values from another population
   * @param level   the confidence level, strictly between 0 and 1
   * @return a confidence interval for the difference between both population means
   */
  public static ConfidenceInterval forMeanDifference(double[] values1, double[] values2,
                                                     double level) {
    int n = values1.length;
    int m = values2.length;
    double meanX = getMean(values1);
    double meanY = getMean(values2);
    double d = meanX - meanY;
    double v = getVariance(values1, meanX);
    double z = getVariance(values2, meanY);
    double varD = v / n + z / m;
    double r = (varD * varD / ((v / n) * (v / n) / (n - 1) + (z / m) * (z / m) / (m - 1)));

    TDistribution tDist = new TDistribution(r);
    double delta = tDist.inverseCumulativeProbability((1.0 + level) / 2);

    double lower = d - delta * sqrt(varD);
    double upper = d + delta * sqrt(varD);

    return new ConfidenceInterval(lower, upper, level);
  }

  public double getLower() {
    return lower;
  }

  public double getUpper() {
    return upper;
  }

  public double getLevel() {
    return level;
  }

  private static double getMean(double[] values) {
    return Arrays.stream(values).average().getAsDouble();
  }

  private static double getVariance(double[] values, double mean) {
    return (Arrays.stream(values).map(x -> (x - mean) * (x - mean)).sum()) / (values.length - 1);
  }

  private void checkParameters(double lower, double upper, double level) {
    if (lower >= upper) {
      throw new IllegalArgumentException(
          "Interval is invalid: lower value is greeter than the upper value");
    }
    if (level <= 0 || level >= 1) {
      throw new IllegalArgumentException("Confidence level is out of (0,1) interval");
    }
  }
}
