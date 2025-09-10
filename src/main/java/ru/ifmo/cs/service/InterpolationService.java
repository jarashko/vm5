package ru.ifmo.cs.service;

import ru.ifmo.cs.model.InterpolationPoint;
import java.util.List;

public class InterpolationService {

    public double lagrangeInterpolation(List<InterpolationPoint> points, double x) {
        int n = points.size();
        if (n < 2) return Double.NaN;

        double result = 0.0;

        for (int i = 0; i < n; i++) {
            double term = points.get(i).getY();
            if (!Double.isFinite(term)) {
                return Double.NaN;
            }

            for (int j = 0; j < n; j++) {
                if (i != j) {
                    double denominator = points.get(i).getX() - points.get(j).getX();
                    if (Math.abs(denominator) < 1e-15) {
                        return Double.NaN;
                    }
                    term *= (x - points.get(j).getX()) / denominator;
                    if (!Double.isFinite(term)) {
                        return Double.NaN;
                    }
                }
            }
            result += term;
            if (!Double.isFinite(result)) {
                return Double.NaN;
            }
        }

        return result;
    }

    public double newtonDividedDifferences(List<InterpolationPoint> points, double x) {
        int n = points.size();
        if (n < 2) return Double.NaN;

        double[] xValues = new double[n];
        double[] yValues = new double[n];

        for (int i = 0; i < n; i++) {
            xValues[i] = points.get(i).getX();
            yValues[i] = points.get(i).getY();
            if (!Double.isFinite(xValues[i]) || !Double.isFinite(yValues[i])) {
                return Double.NaN;
            }
        }

        double[][] dividedDifferences = new double[n][n];
        for (int i = 0; i < n; i++) {
            dividedDifferences[i][0] = yValues[i];
        }

        for (int j = 1; j < n; j++) {
            for (int i = 0; i < n - j; i++) {
                double denominator = xValues[i + j] - xValues[i];
                if (Math.abs(denominator) < 1e-15) {
                    return Double.NaN;
                }
                dividedDifferences[i][j] = (dividedDifferences[i + 1][j - 1] - dividedDifferences[i][j - 1])
                                         / denominator;
                if (!Double.isFinite(dividedDifferences[i][j])) {
                    return Double.NaN;
                }
            }
        }

        double result = dividedDifferences[0][0];
        double term = 1.0;

        for (int i = 1; i < n; i++) {
            term *= (x - xValues[i - 1]);
            if (!Double.isFinite(term)) {
                return Double.NaN;
            }
            result += dividedDifferences[0][i] * term;
            if (!Double.isFinite(result)) {
                return Double.NaN;
            }
        }

        return result;
    }

    public double newtonFiniteDifferences(List<InterpolationPoint> points, double x) {
        int n = points.size();
        if (n < 2) return Double.NaN;

        double[] xValues = new double[n];
        double[] yValues = new double[n];

        for (int i = 0; i < n; i++) {
            xValues[i] = points.get(i).getX();
            yValues[i] = points.get(i).getY();
        }

        double h = xValues[1] - xValues[0];
        double tolerance = 1e-10;
        for (int i = 2; i < n; i++) {
            if (Math.abs((xValues[i] - xValues[i-1]) - h) > tolerance) {
                return newtonDividedDifferences(points, x);
            }
        }

        double minX = xValues[0];
        double maxX = xValues[n-1];
        double range = maxX - minX;
        if (x < minX - range * 10 || x > maxX + range * 10) {
            return newtonDividedDifferences(points, x);
        }

        double[][] finiteDifferences = new double[n][n];
        for (int i = 0; i < n; i++) {
            finiteDifferences[i][0] = yValues[i];
        }

        for (int j = 1; j < n; j++) {
            for (int i = 0; i < n - j; i++) {
                finiteDifferences[i][j] = finiteDifferences[i + 1][j - 1] - finiteDifferences[i][j - 1];
            }
        }

        double t = (x - xValues[0]) / h;

        double result = finiteDifferences[0][0];
        double term = 1.0;

        for (int i = 1; i < n; i++) {
            term *= (t - i + 1);
            result += finiteDifferences[0][i] * term / factorial(i);
        }

        return result;
    }

    public double[][] calculateFiniteDifferencesTable(List<InterpolationPoint> points) {
        int n = points.size();
        double[][] table = new double[n][n];

        for (int i = 0; i < n; i++) {
            table[i][0] = points.get(i).getY();
        }

        for (int j = 1; j < n; j++) {
            for (int i = 0; i < n - j; i++) {
                table[i][j] = table[i + 1][j - 1] - table[i][j - 1];
            }
        }

        return table;
    }

    public double stirlingInterpolation(List<InterpolationPoint> points, double x) {
        int n = points.size();
        if (n < 2) return Double.NaN;

        if (n < 3) {
            return newtonDividedDifferences(points, x);
        }

        double[] xValues = new double[n];
        double[] yValues = new double[n];

        for (int i = 0; i < n; i++) {
            xValues[i] = points.get(i).getX();
            yValues[i] = points.get(i).getY();
            if (!Double.isFinite(xValues[i]) || !Double.isFinite(yValues[i])) {
                return Double.NaN;
            }
        }

        double h = xValues[1] - xValues[0];
        double tolerance = 1e-10;
        for (int i = 2; i < n; i++) {
            if (Math.abs((xValues[i] - xValues[i-1]) - h) > tolerance) {
                return newtonDividedDifferences(points, x);
            }
        }

        double minX = xValues[0];
        double maxX = xValues[n-1];
        double range = maxX - minX;
        if (x < minX - range * 10 || x > maxX + range * 10) {
            return newtonDividedDifferences(points, x);
        }

        double[][] finiteDifferences = new double[n][n];
        for (int i = 0; i < n; i++) {
            finiteDifferences[i][0] = yValues[i];
        }

        for (int j = 1; j < n; j++) {
            for (int i = 0; i < n - j; i++) {
                finiteDifferences[i][j] = finiteDifferences[i + 1][j - 1] - finiteDifferences[i][j - 1];
            }
        }

        int center = n / 2;
        double t = (x - xValues[center]) / h;

        // Формула Стирлинга: P(x) = y₀ + t(Δy₋₁ + Δy₀)/2! + t²/2! Δ²y₋₁ + ...
        double result = finiteDifferences[center][0];
        
        // Первый член: t(Δy₋₁ + Δy₀)/2!
        if (center > 0 && center < n - 1) {
            double delta1 = finiteDifferences[center - 1][1] + finiteDifferences[center][1];
            result += t * delta1 / 2.0;
        }
        
        // Второй член: t²/2! Δ²y₋₁
        if (center > 0 && center < n - 2) {
            result += (t * t / 2.0) * finiteDifferences[center - 1][2];
        }
        
        // Остальные члены для нечетных порядков
        double term = t;
        for (int i = 3; i < n; i += 2) {
            if (center - (i+1)/2 >= 0 && center + (i-1)/2 < n) {
                double delta = finiteDifferences[center - (i+1)/2][i] + finiteDifferences[center - (i-1)/2][i];
                term *= (t * t - ((i-1)/2) * ((i-1)/2));
                result += term * delta / (2.0 * factorial(i));
            }
        }
        
        // Члены для четных порядков
        term = t * t;
        for (int i = 4; i < n; i += 2) {
            if (center - i/2 >= 0 && center + i/2 < n) {
                term *= (t * t - (i/2) * (i/2));
                result += term * finiteDifferences[center - i/2][i] / factorial(i);
            }
        }

        return result;
    }

    public double besselInterpolation(List<InterpolationPoint> points, double x) {
        int n = points.size();
        if (n < 2) return Double.NaN;

        if (n < 3) {
            return newtonDividedDifferences(points, x);
        }

        double[] xValues = new double[n];
        double[] yValues = new double[n];

        for (int i = 0; i < n; i++) {
            xValues[i] = points.get(i).getX();
            yValues[i] = points.get(i).getY();
            if (!Double.isFinite(xValues[i]) || !Double.isFinite(yValues[i])) {
                return Double.NaN;
            }
        }

        double h = xValues[1] - xValues[0];
        double tolerance = 1e-10;
        for (int i = 2; i < n; i++) {
            if (Math.abs((xValues[i] - xValues[i-1]) - h) > tolerance) {
                return newtonDividedDifferences(points, x);
            }
        }

        double minX = xValues[0];
        double maxX = xValues[n-1];
        double range = maxX - minX;
        if (x < minX - range * 10 || x > maxX + range * 10) {
            return newtonDividedDifferences(points, x);
        }

        double[][] finiteDifferences = new double[n][n];
        for (int i = 0; i < n; i++) {
            finiteDifferences[i][0] = yValues[i];
        }

        for (int j = 1; j < n; j++) {
            for (int i = 0; i < n - j; i++) {
                finiteDifferences[i][j] = finiteDifferences[i + 1][j - 1] - finiteDifferences[i][j - 1];
            }
        }

        int center = n / 2;
        double t = (x - xValues[center]) / h;

        // Формула Бесселя: P(x) = (y₀ + y₁)/2 + (t-1/2)Δy₀ + t(t-1)/2! (Δ²y₋₁ + Δ²y₀)/2 + ...
        double result;
        if (center + 1 < n) {
            result = (finiteDifferences[center][0] + finiteDifferences[center + 1][0]) / 2.0;
        } else {
            result = finiteDifferences[center][0];
        }

        // Первый член: (t-1/2)Δy₀
        if (center < n - 1) {
            result += (t - 0.5) * finiteDifferences[center][1];
        }
        
        // Второй член: t(t-1)/2! (Δ²y₋₁ + Δ²y₀)/2
        if (center > 0 && center < n - 1) {
            double delta2 = finiteDifferences[center - 1][2] + finiteDifferences[center][2];
            result += (t * (t - 1) / 2.0) * (delta2 / 2.0);
        }
        
        // Третий член: (t-1/2)t(t-1)/3! Δ³y₋₁
        if (center > 0 && center < n - 2) {
            result += (t - 0.5) * t * (t - 1) * finiteDifferences[center - 1][3] / 6.0;
        }
        
        // Четвертый член: t(t-1)(t+1)(t-2)/4! (Δ⁴y₋₂ + Δ⁴y₋₁)/2
        if (center > 1 && center < n - 1) {
            double delta4 = finiteDifferences[center - 2][4] + finiteDifferences[center - 1][4];
            result += (t * (t - 1) * (t + 1) * (t - 2) / 24.0) * (delta4 / 2.0);
        }
        
        // Пятый член: (t-1/2)t(t-1)(t+1)(t-2)/5! Δ⁵y₋₂
        if (center > 1 && center < n - 2) {
            result += (t - 0.5) * t * (t - 1) * (t + 1) * (t - 2) * finiteDifferences[center - 2][5] / 120.0;
        }
        
        // Шестой член: t(t-1)(t+1)(t-2)(t+2)(t-3)/6! (Δ⁶y₋₃ + Δ⁶y₋₂)/2
        if (center > 2 && center < n - 1) {
            double delta6 = finiteDifferences[center - 3][6] + finiteDifferences[center - 2][6];
            result += (t * (t - 1) * (t + 1) * (t - 2) * (t + 2) * (t - 3) / 720.0) * (delta6 / 2.0);
        }

        return result;
    }

    private long factorial(int n) {
        if (n < 0) return 0;
        if (n <= 1) return 1;
        long result = 1;
        for (int i = 2; i <= n; i++) {
            result *= i;
        }
        return result;
    }
}
