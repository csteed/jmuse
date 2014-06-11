import java.util.*;

public class StatisticsToolkit {
	public static double calculatePearsonCorrelationCoefficient(double x[],
			double y[], int n) {
		double sum_x = 0., sum_y = 0., sum_x2 = 0., sum_y2 = 0., sum_xy = 0.;

		for (int i = 0; i < /* x.length */n; i++) {
			sum_x += x[i];
			sum_x2 += (x[i] * x[i]);
			sum_y += y[i];
			sum_y2 += (y[i] * y[i]);
			sum_xy += (x[i] * y[i]);
		}

		return ((n * sum_xy) - (sum_x * sum_y))
				/ Math.sqrt(((n * sum_x2) - (sum_x * sum_x))
						* ((/* x.length */n * sum_y2) - (sum_y * sum_y)));
	}

	public static double[][] calculatePearsonCorrelationCoefficientMatrix(
			ArrayList data, int numVariables, int numValues) {
		double coef[][] = new double[numVariables][numVariables];

		for (int i = 0; i < numVariables; i++) {
			for (int j = 0; j < numVariables; j++) {
				if (i == j) {
					coef[i][j] = 1.0;
				} else if (i < j) {
					coef[i][j] = calculatePearsonCorrelationCoefficient(
							(double[]) data.get(i), (double[]) data.get(j),
							numValues);
				} else {
					coef[i][j] = coef[j][i];
				}
			}
		}

		return coef;
	}
}
