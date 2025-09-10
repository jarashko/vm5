package ru.ifmo.cs.plotting;

import javafx.scene.chart.XYChart;
import java.util.ArrayList;
import java.util.List;

public class ChartBuilder {

    public static XYChart.Series<Number, Number> buildFunctionSeries(
            java.util.function.Function<Double, Double> function,
            double a, double b, int points) {
        if (points <= 0) throw new IllegalArgumentException("points должно быть > 0");

        XYChart.Series<Number, Number> series = new XYChart.Series<>();
        double step = (b - a) / points;

        for (int i = 0; i <= points; i++) {
            double x = a + i * step;
            try {
                double y = function.apply(x);
                if (Double.isFinite(y)) {
                    series.getData().add(new XYChart.Data<>(x, y));
                }
            } catch (Exception e) {

                System.err.println("Разрыв в точке x=" + x + ": " + e.getMessage());
            }
        }

        return series;
    }

    public static List<XYChart.Series<Number, Number>> buildFunctionSegments(
            java.util.function.Function<Double, Double> function,
            double a, double b, int points) {
        if (points <= 0) throw new IllegalArgumentException("points должно быть > 0");

        List<XYChart.Series<Number, Number>> segments = new ArrayList<>();
        XYChart.Series<Number, Number> currentSegment = null;
        double step = (b - a) / points;
        double previousY = Double.NaN;
        double maxJump = 100.0;

        for (int i = 0; i <= points; i++) {
            double x = a + i * step;
            try {
                double y = function.apply(x);
                if (Double.isFinite(y)) {

                    if (Double.isFinite(previousY) && Math.abs(y - previousY) > maxJump) {

                        currentSegment = null;
                    }

                    if (currentSegment == null) {
                        currentSegment = new XYChart.Series<>();
                        segments.add(currentSegment);
                    }
                    currentSegment.getData().add(new XYChart.Data<>(x, y));
                    previousY = y;
                } else {
                    currentSegment = null;
                    previousY = Double.NaN;
                }
            } catch (Exception e) {
                currentSegment = null;
                previousY = Double.NaN;
            }
        }

        return segments;
    }

    public static List<XYChart.Series<Number, Number>> buildFunctionSegments(
            java.util.function.Function<Double, Double> function,
            double a, double b, int points, double maxJump) {
        if (points <= 0) throw new IllegalArgumentException("points должно быть > 0");

        List<XYChart.Series<Number, Number>> segments = new ArrayList<>();
        XYChart.Series<Number, Number> currentSegment = null;
        double step = (b - a) / points;
        double previousY = Double.NaN;

        for (int i = 0; i <= points; i++) {
            double x = a + i * step;
            try {
                double y = function.apply(x);
                if (Double.isFinite(y)) {

                    if (Double.isFinite(previousY) && Math.abs(y - previousY) > maxJump) {

                        currentSegment = null;
                    }

                    if (currentSegment == null) {
                        currentSegment = new XYChart.Series<>();
                        segments.add(currentSegment);
                    }
                    currentSegment.getData().add(new XYChart.Data<>(x, y));
                    previousY = y;
                } else {
                    currentSegment = null;
                    previousY = Double.NaN;
                }
            } catch (Exception e) {
                currentSegment = null;
                previousY = Double.NaN;
            }
        }

        return segments;
    }

    public static XYChart.Series<Number, Number> buildUnifiedFunctionSeries(
            java.util.function.Function<Double, Double> function,
            double a, double b, int points) {
        if (points <= 0) throw new IllegalArgumentException("points должно быть > 0");

        XYChart.Series<Number, Number> series = new XYChart.Series<>();
        double step = (b - a) / points;
        double previousY = Double.NaN;
        double maxJump = 100.0;

        for (int i = 0; i <= points; i++) {
            double x = a + i * step;
            try {
                double y = function.apply(x);
                if (Double.isFinite(y)) {

                    if (Double.isFinite(previousY) && Math.abs(y - previousY) > maxJump) {

                        series.getData().add(new XYChart.Data<>(x, Double.NaN));
                    }

                    series.getData().add(new XYChart.Data<>(x, y));
                    previousY = y;
                } else {

                    previousY = Double.NaN;
                }
            } catch (Exception e) {

                previousY = Double.NaN;
            }
        }

        return series;
    }
}

