package ru.ifmo.cs;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.stage.Stage;
import ru.ifmo.cs.plotting.ChartBuilder;

import java.util.List;

public class ChartBuilderDemo extends Application {

    @Override
    public void start(Stage primaryStage) {

        NumberAxis xAxis = new NumberAxis();
        NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel("X");
        yAxis.setLabel("Y");

        LineChart<Number, Number> chart = new LineChart<>(xAxis, yAxis);
        chart.setTitle("Демонстрация ChartBuilder");
        chart.setAnimated(false);
        chart.setCreateSymbols(false);
        chart.setLegendVisible(true);

        chart.getStylesheets().add(ChartBuilderDemo.class.getResource("/css/styles.css").toExternalForm());

        XYChart.Series<Number, Number> linearSeries = ChartBuilder.buildFunctionSeries(
            x -> x, -5.0, 5.0, 50
        );
        linearSeries.setName("Выбранная функция: y = x");
        chart.getData().add(linearSeries);

        XYChart.Series<Number, Number> quadraticSeries = ChartBuilder.buildFunctionSeries(
            x -> x * x, -3.0, 3.0, 50
        );
        quadraticSeries.setName("Выбранная функция: y = x²");
        chart.getData().add(quadraticSeries);

        XYChart.Series<Number, Number> discontinuitySeries = ChartBuilder.buildFunctionSeries(
            x -> 1.0 / (x - 1.0), -2.0, 4.0, 100
        );

        discontinuitySeries.setName("Выбранная функция: y = 1/(x-1)");
        chart.getData().add(discontinuitySeries);

        XYChart.Series<Number, Number> sinSeries = ChartBuilder.buildFunctionSeries(
            x -> Math.sin(x), -Math.PI, Math.PI, 100
        );
        sinSeries.setName("Выбранная функция: y = sin(x)");
        chart.getData().add(sinSeries);

        Scene scene = new Scene(chart, 800, 600);
        primaryStage.setTitle("ChartBuilder Demo");
        primaryStage.setScene(scene);
        primaryStage.show();

        System.out.println("ChartBuilder Demo запущен!");
        System.out.println("Создано серий данных: " + chart.getData().size());
        System.out.println("Линейная функция: " + linearSeries.getData().size() + " точек");
        System.out.println("Квадратичная функция: " + quadraticSeries.getData().size() + " точек");
        System.out.println("Функция с разрывом: " + discontinuitySeries.getData().size() + " точек");
        System.out.println("Синусоида: " + sinSeries.getData().size() + " точек");
    }

    public static void main(String[] args) {
        launch(args);
    }
}
