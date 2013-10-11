package helperClasses;

import java.util.Arrays;

/**
 * Class for simple statistics on double arrays.
 */
public class Statistics {
	double[] data;
	double size;

	public Statistics(double[] data) {
		this.data = data;
		size = data.length;
	}

	public double getMean() {
		double sum = 0.0;

		for (double a : data) {
			sum += a;
		}

		return sum / size;
	}

	public double getVariance() {
		double mean = getMean();
		double temp = 0;

		for (double a : data) {
			temp += (mean - a) * (mean - a);
		}

		return temp / size;
	}

	public double getStdDev() {
		return Math.sqrt(getVariance());
	}

	public double median() {
		double[] b = new double[data.length];
		System.arraycopy(data, 0, b, 0, b.length);
		Arrays.sort(b);

		if (data.length % 2 == 0) {
			return (b[(b.length / 2) - 1] + b[b.length / 2]) / 2.0;
		} else {
			return b[b.length / 2];
		}
	}
}
