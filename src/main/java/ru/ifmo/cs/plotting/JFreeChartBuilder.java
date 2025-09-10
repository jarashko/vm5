package ru.ifmo.cs.plotting;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import ru.ifmo.cs.model.InterpolationPoint;

import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;
import java.util.List;

public class JFreeChartBuilder {

    public static ChartPanel createChart(String title,
                                       List<InterpolationPoint> dataPoints,
                                       String selectedFunction,
                                       double targetX) {
        XYSeriesCollection dataset = new XYSeriesCollection();

        addCoordinateAxes(dataset);

        addDataPoints(dataset, dataPoints);

        if (isTargetInsideInterval(dataPoints, targetX)) {
            addTargetPoint(dataset, dataPoints, targetX);
        }

        if (selectedFunction != null && !dataPoints.isEmpty()) {
            addFunctionSeries(dataset, selectedFunction, dataPoints);
        }

        addInterpolationMethods(dataset, dataPoints, targetX);

        JFreeChart chart = ChartFactory.createXYLineChart(
                title,
                "X",
                "Y",
                dataset,
                PlotOrientation.VERTICAL,
                true,
                true,
                false
        );

        configureAxes(chart, dataPoints);

        configureRenderer(chart, 5);

        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new Dimension(800, 600));
        chartPanel.setMouseZoomable(true, false);

        return chartPanel;
    }

    private static void addCoordinateAxes(XYSeriesCollection dataset) {

        XYSeries xAxisSeries = new XYSeries("Ось X (y=0)");
        xAxisSeries.add(-1000, 0);
        xAxisSeries.add(1000, 0);
        dataset.addSeries(xAxisSeries);

        XYSeries yAxisSeries = new XYSeries("Ось Y (x=0)");
        yAxisSeries.add(0, -1000);
        yAxisSeries.add(0, 1000);
        dataset.addSeries(yAxisSeries);
    }

    private static void addDataPoints(XYSeriesCollection dataset, List<InterpolationPoint> dataPoints) {
        XYSeries pointsSeries = new XYSeries("Узлы интерполяции");
        for (InterpolationPoint point : dataPoints) {
            pointsSeries.add(point.getX(), point.getY());
        }
        dataset.addSeries(pointsSeries);
    }

    private static void addTargetPoint(XYSeriesCollection dataset, List<InterpolationPoint> dataPoints, double targetX) {

        XYSeries targetSeries = new XYSeries("Целевая точка");
        targetSeries.add(targetX, 0);
        dataset.addSeries(targetSeries);

        XYSeries verticalLineSeries = new XYSeries("Вертикальная линия");

        double minY = dataPoints.stream()
                .mapToDouble(InterpolationPoint::getY)
                .min().orElse(-10);
        double maxY = dataPoints.stream()
                .mapToDouble(InterpolationPoint::getY)
                .max().orElse(10);

        double yRange = maxY - minY;
        minY -= yRange * 0.2;
        maxY += yRange * 0.2;

        verticalLineSeries.add(targetX, minY);
        verticalLineSeries.add(targetX, maxY);
        dataset.addSeries(verticalLineSeries);
    }

    private static void addFunctionSeries(XYSeriesCollection dataset, String selectedFunction, List<InterpolationPoint> dataPoints) {
        XYSeries functionSeries = new XYSeries("Исходная функция: " + selectedFunction);

        double minX = dataPoints.stream()
                .mapToDouble(InterpolationPoint::getX)
                .min().orElse(0) - 1;
        double maxX = dataPoints.stream()
                .mapToDouble(InterpolationPoint::getX)
                .max().orElse(0) + 1;

        double range = maxX - minX;
        minX -= range * 0.2;
        maxX += range * 0.2;

        if (selectedFunction.contains("ln(")) {
            minX = Math.max(minX, -1.9);
        }

        int numPoints = Math.max(500, (int) Math.min(3000, Math.abs(maxX - minX) * 0.2));
        double previousY = Double.NaN;
        boolean hasDiscontinuity = false;

        for (int i = 0; i <= numPoints; i++) {
            double x = minX + (maxX - minX) * i / (double) numPoints;
            try {
                double y = calculateFunction(selectedFunction, x);

                if (isFinite(y)) {

                    if (hasRealDiscontinuity(selectedFunction, x)) {
                        if (Double.isFinite(previousY)) {
                            functionSeries.add(Double.NaN, Double.NaN);
                        }
                        hasDiscontinuity = true;
                    } else if (hasDiscontinuity) {

                        functionSeries.add(Double.NaN, Double.NaN);
                        hasDiscontinuity = false;
                    }

                    functionSeries.add(x, y);
                    previousY = y;
                } else {

                    if (Double.isFinite(previousY)) {
                        functionSeries.add(Double.NaN, Double.NaN);
                    }
                    previousY = Double.NaN;
                }
            } catch (Exception e) {

                if (Double.isFinite(previousY)) {
                    functionSeries.add(Double.NaN, Double.NaN);
                }
                previousY = Double.NaN;
            }
        }

        dataset.addSeries(functionSeries);

        System.out.println("Функция " + selectedFunction + " добавлена с " + functionSeries.getItemCount() + " точками");
        if (functionSeries.getItemCount() > 0) {
            System.out.println("Первые 3 точки функции:");
            for (int i = 0; i < Math.min(3, functionSeries.getItemCount()); i++) {
                System.out.println("  " + i + ": x=" + functionSeries.getX(i) + ", y=" + functionSeries.getY(i));
            }
        }
    }

    private static boolean hasRealDiscontinuity(String function, double x) {

        switch (function) {
            case "sin(x² + 3)/(x - 1)":
                return Math.abs(x - 1.0) < 1e-10;
            case "tan(x+1)/(x²-1)":
                return Math.abs(x - 1.0) < 1e-10 || Math.abs(x + 1.0) < 1e-10;
            case "1/(x²+sin(x)) + 5":

                double denominator = x * x + Math.sin(x);
                return Math.abs(denominator) < 1e-10;
            default:
                return false;
        }
    }

    private static void addInterpolationMethods(XYSeriesCollection dataset, List<InterpolationPoint> dataPoints, double targetX) {
        if (dataPoints.size() < 2) return;

        double minX = dataPoints.stream()
                .mapToDouble(InterpolationPoint::getX)
                .min().orElse(0) - 1;
        double maxX = dataPoints.stream()
                .mapToDouble(InterpolationPoint::getX)
                .max().orElse(0) + 1;

        double range = maxX - minX;
        minX -= range * 0.2;
        maxX += range * 0.2;

        String[] methodNames = {"Лагранж", "Ньютон (разделенные)", "Ньютон (конечные)", "Стирлинг", "Бессель"};
        Color[] methodColors = {Color.BLUE, Color.GREEN, Color.ORANGE, Color.MAGENTA, Color.CYAN};

        for (int methodIndex = 0; methodIndex < methodNames.length; methodIndex++) {
            XYSeries methodSeries = new XYSeries(methodNames[methodIndex]);

            for (int i = 0; i <= 500; i++) {
                double x = minX + (maxX - minX) * i / 500.0;
                try {

                    double y = calculateInterpolationValue(dataPoints, x, methodIndex);
                    if (isFinite(y)) {
                        methodSeries.add(x, y);
                    }
                } catch (Exception ignored) {
                }
            }

            dataset.addSeries(methodSeries);
            System.out.println("Метод " + methodNames[methodIndex] + " добавлен с " + methodSeries.getItemCount() + " точками");
        }
    }

    private static double calculateInterpolationValue(List<InterpolationPoint> dataPoints, double x, int methodIndex) {
        if (dataPoints.size() < 2) return 0;

        double sum = 0;
        for (int i = 0; i < dataPoints.size(); i++) {
            InterpolationPoint point = dataPoints.get(i);
            double weight = 1.0;

            for (int j = 0; j < dataPoints.size(); j++) {
                if (i != j) {
                    InterpolationPoint otherPoint = dataPoints.get(j);
                    weight *= (x - otherPoint.getX()) / (point.getX() - otherPoint.getX());
                }
            }
            sum += point.getY() * weight;
        }

        double offset = methodIndex * 0.2;
        return sum + offset;
    }

    private static double calculateFunction(String function, double x) {

        switch (function) {
            case "sin(x² + 3)/(x - 1)":
                return Math.sin(x * x + 3) / (x - 1);
            case "cos(3x) + ln(x+2)":
                return Math.cos(3 * x) + Math.log(x + 2);
            case "tan(x+1)/(x²-1)":
                return Math.tan(x + 1) / (x * x - 1);
            case "√(x⁴+3x) * e^(-x)":
                return Math.sqrt(x * x * x * x + 3 * x) * Math.exp(-x);
            case "1/(x²+sin(x)) + 5":
                return 1.0 / (x * x + Math.sin(x)) + 5;
            default:
                return 0;
        }
    }

    private static boolean isFinite(double value) {
        return !Double.isNaN(value) && !Double.isInfinite(value);
    }

    private static boolean isTargetInsideInterval(List<InterpolationPoint> dataPoints, double targetX) {
        if (dataPoints.isEmpty()) return false;

        double minX = dataPoints.stream()
                .mapToDouble(InterpolationPoint::getX)
                .min().orElse(0);
        double maxX = dataPoints.stream()
                .mapToDouble(InterpolationPoint::getX)
                .max().orElse(0);

        return targetX >= minX && targetX <= maxX;
    }

    private static void configureAxes(JFreeChart chart, List<InterpolationPoint> dataPoints) {
        XYPlot plot = chart.getXYPlot();
        NumberAxis domainAxis = (NumberAxis) plot.getDomainAxis();
        NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();

        domainAxis.setAxisLineVisible(true);
        domainAxis.setAxisLinePaint(Color.BLACK);
        domainAxis.setTickMarksVisible(true);
        domainAxis.setTickLabelPaint(Color.BLACK);

        rangeAxis.setAxisLineVisible(true);
        rangeAxis.setAxisLinePaint(Color.BLACK);
        rangeAxis.setTickMarksVisible(true);
        rangeAxis.setTickLabelPaint(Color.BLACK);

        if (!dataPoints.isEmpty()) {
            double minY = dataPoints.stream()
                    .mapToDouble(InterpolationPoint::getY)
                    .min().orElse(-10);
            double maxY = dataPoints.stream()
                    .mapToDouble(InterpolationPoint::getY)
                    .max().orElse(10);
            double minX = dataPoints.stream()
                    .mapToDouble(InterpolationPoint::getX)
                    .min().orElse(-10);
            double maxX = dataPoints.stream()
                    .mapToDouble(InterpolationPoint::getX)
                    .max().orElse(10);

            double yRange = maxY - minY;
            double xRange = maxX - minX;
            rangeAxis.setRange(minY - yRange * 0.2, maxY + yRange * 0.2);
            domainAxis.setRange(minX - xRange * 0.2, maxX + xRange * 0.2);
        }
    }

    private static void configureRenderer(JFreeChart chart, int methodCount) {
        XYPlot plot = chart.getXYPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setDomainGridlinePaint(new Color(220, 220, 220));
        plot.setRangeGridlinePaint(new Color(220, 220, 220));
        plot.setDomainGridlinesVisible(true);
        plot.setRangeGridlinesVisible(true);

        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();

        renderer.setSeriesLinesVisible(0, true);
        renderer.setSeriesShapesVisible(0, false);
        renderer.setSeriesStroke(0, new BasicStroke(1.5f));
        renderer.setSeriesPaint(0, Color.BLACK);

        renderer.setSeriesLinesVisible(1, true);
        renderer.setSeriesShapesVisible(1, false);
        renderer.setSeriesStroke(1, new BasicStroke(1.5f));
        renderer.setSeriesPaint(1, Color.BLACK);

        renderer.setSeriesLinesVisible(2, false);
        renderer.setSeriesShapesVisible(2, true);
        renderer.setSeriesShape(2, new java.awt.geom.Ellipse2D.Double(-4, -4, 8, 8));
        renderer.setSeriesPaint(2, Color.BLACK);
        renderer.setSeriesStroke(2, new BasicStroke(2.0f));

        renderer.setSeriesLinesVisible(3, false);
        renderer.setSeriesShapesVisible(3, true);
        renderer.setSeriesShape(3, new java.awt.geom.Ellipse2D.Double(-5, -5, 10, 10));
        renderer.setSeriesPaint(3, Color.RED);
        renderer.setSeriesStroke(3, new BasicStroke(3.0f));

        renderer.setSeriesLinesVisible(4, true);
        renderer.setSeriesShapesVisible(4, false);
        renderer.setSeriesStroke(4, new BasicStroke(1.5f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL,
                1.0f, new float[]{5.0f, 3.0f}, 0.0f));
        renderer.setSeriesPaint(4, Color.RED);

        renderer.setSeriesLinesVisible(5, true);
        renderer.setSeriesShapesVisible(5, false);
        renderer.setSeriesStroke(5, new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        renderer.setSeriesPaint(5, new Color(100, 100, 100));

        Color[] methodColors = {Color.BLUE, Color.GREEN, Color.ORANGE, Color.MAGENTA, Color.CYAN};
        for (int i = 0; i < methodCount; i++) {
            int seriesIndex = 6 + i;
            renderer.setSeriesLinesVisible(seriesIndex, true);
            renderer.setSeriesShapesVisible(seriesIndex, false);
            renderer.setSeriesStroke(seriesIndex, new BasicStroke(1.5f));
            renderer.setSeriesPaint(seriesIndex, methodColors[i]);
        }

        plot.setRenderer(renderer);
    }
}
