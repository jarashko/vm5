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
            term *= (t - i + 1) / i;
            result += finiteDifferences[0][i] * term;
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
        
        double result = finiteDifferences[center][0];
        double term = 1.0;
        
        for (int i = 1; i < n; i++) {
            if (i % 2 == 1) {
                term *= t;
                if (center - (i+1)/2 >= 0 && center + (i-1)/2 < n) {
                    result += finiteDifferences[center - (i+1)/2][i] * term / factorial(i);
                }
            } else {
                term *= (t * t - (i/2) * (i/2));
                if (center - i/2 >= 0 && center + i/2 < n) {
                    result += finiteDifferences[center - i/2][i] * term / factorial(i);
                }
            }
            if (!Double.isFinite(result)) {
                return Double.NaN;
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
        
        double result;
        if (center + 1 < n) {
            result = (finiteDifferences[center][0] + finiteDifferences[center + 1][0]) / 2.0;
        } else {
            result = finiteDifferences[center][0];
        }
        
        double term = t - 0.5;
        
        for (int i = 1; i < n; i++) {
            if (i % 2 == 1) {
                if (center - (i-1)/2 >= 0 && center + (i+1)/2 < n) {
                    result += finiteDifferences[center - (i-1)/2][i] * term / factorial(i);
                }
            } else {
                term *= (t - 0.5) * (t + 0.5);
                if (center - i/2 >= 0 && center + i/2 < n) {
                    result += finiteDifferences[center - i/2][i] * term / factorial(i);
                }
            }
            if (!Double.isFinite(result)) {
                return Double.NaN;
            }
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
