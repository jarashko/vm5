package ru.ifmo.cs.controller;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.Node;
import ru.ifmo.cs.plotting.SwingChartNode;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.util.converter.DoubleStringConverter;
import ru.ifmo.cs.model.InterpolationPoint;
import ru.ifmo.cs.model.InterpolationResult;
import ru.ifmo.cs.service.InterpolationService;
import ru.ifmo.cs.service.FileService;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class InterpolationController {

    @FXML private TableView<InterpolationPoint> pointsTable;
    @FXML private TableColumn<InterpolationPoint, Double> xColumn;
    @FXML private TableColumn<InterpolationPoint, Double> yColumn;

    @FXML private TextField xInput;
    @FXML private TextField yInput;
    @FXML private TextField targetXInput;
    @FXML private Button addPointButton;

    @FXML private LineChart<Number, Number> chart;

    private Node swingChartNode;
    @FXML private NumberAxis xAxis;
    @FXML private NumberAxis yAxis;

    @FXML private TextArea resultsArea;
    @FXML private Label statusBar;

    @FXML private ComboBox<String> functionComboBox;
    @FXML private TextField intervalStartInput;
    @FXML private TextField intervalEndInput;
    @FXML private TextField pointsCountInput;
    @FXML private Button generateFunctionButton;

    private final ObservableList<InterpolationPoint> dataPoints = FXCollections.observableArrayList();
    private final InterpolationService interpolationService = new InterpolationService();
    private final FileService fileService = new FileService();

    @FXML
    public void initialize() {
        setupTable();
        setupChart();
        setupFunctionComboBox();
        clearAll();
    }

    private void setupTable() {
        xColumn.setCellValueFactory(new PropertyValueFactory<>("x"));
        yColumn.setCellValueFactory(new PropertyValueFactory<>("y"));

        xColumn.setCellFactory(TextFieldTableCell.forTableColumn(new DoubleStringConverter()));
        yColumn.setCellFactory(TextFieldTableCell.forTableColumn(new DoubleStringConverter()));

        pointsTable.setEditable(true);
        pointsTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        pointsTable.setItems(dataPoints);

        xColumn.setOnEditCommit(event -> {
            InterpolationPoint point = event.getRowValue();
            point.setX(event.getNewValue());
            updateStatus("Точка обновлена: " + point);
        });

        yColumn.setOnEditCommit(event -> {
            InterpolationPoint point = event.getRowValue();
            point.setY(event.getNewValue());
            updateStatus("Точка обновлена: " + point);
        });
    }

    private void setupChart() {
        chart.setAnimated(false);
        chart.setCreateSymbols(false);
        chart.setLegendVisible(true);

        chart.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());
        chart.getStylesheets().add(getClass().getResource("/css/chart-styles.css").toExternalForm());
    }

    private void plotSelectedFunction() {
        if (dataPoints.size() < 2) return;

        String selectedFunction = functionComboBox.getValue();
        System.out.println("Выбранная функция: " + selectedFunction);
        if (selectedFunction == null) {
            System.out.println("Функция не выбрана, пропускаем отрисовку");
            return;
        }

        chart.getData().removeIf(series -> series.getName() != null && series.getName().contains("Выбранная функция"));

        double minX = dataPoints.stream().mapToDouble(InterpolationPoint::getX).min().getAsDouble();
        double maxX = dataPoints.stream().mapToDouble(InterpolationPoint::getX).max().getAsDouble();
        double range = maxX - minX;
        double extendedMinX = minX - range * 0.2;
        double extendedMaxX = maxX + range * 0.2;

        int plotPoints = Math.max(100, (int) Math.min(2000, Math.abs(extendedMaxX - extendedMinX) * 0.1));

        if (selectedFunction.contains("ln(")) {
            extendedMinX = Math.max(extendedMinX, -1.9);
        }

        XYChart.Series<Number, Number> functionSeries = new XYChart.Series<>();
        functionSeries.setName("Выбранная функция: " + selectedFunction);

        double step = (extendedMaxX - extendedMinX) / plotPoints;
        int validPoints = 0;

        for (int i = 0; i <= plotPoints; i++) {
            double x = extendedMinX + i * step;
            try {
                double y = calculateFunction(selectedFunction, x);

                if (Double.isFinite(y)) {

                    functionSeries.getData().add(new XYChart.Data<>(x, y));
                    validPoints++;
                }
            } catch (Exception e) {

            }
        }

        System.out.println("Валидных точек для функции: " + validPoints + " из " + (plotPoints + 1));

        chart.getData().add(functionSeries);

        System.out.println("Функция добавлена: " + functionSeries.getName() + ", точек: " + functionSeries.getData().size());
        System.out.println("Всего серий на графике: " + chart.getData().size());
    }

    private void setupFunctionComboBox() {
        functionComboBox.getItems().addAll(
            "sin(x² + 3)/(x - 1)", "tan(x+1)/(x²-1)", "√(x⁴+3x) * e^(-x)",
            "cos(3x) + ln(x+2)", "1/(x²+sin(x)) + 5"
        );
        functionComboBox.setValue("sin(x² + 3)/(x - 1)");

        functionComboBox.setOnAction(e -> {
            if (!dataPoints.isEmpty()) {
                updateStatus("Функция изменена. График функции обновлен.");

                plotSelectedFunction();
                showOutdatedResults();
            }
        });

        intervalStartInput.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!dataPoints.isEmpty() && !newVal.trim().isEmpty()) {
                updateStatus("Начало интервала изменено. Нажмите 'Интерполировать' для обновления интерполяции.");
                showOutdatedResults();
            }
        });

        intervalEndInput.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!dataPoints.isEmpty() && !newVal.trim().isEmpty()) {
                updateStatus("Конец интервала изменен. Нажмите 'Интерполировать' для обновления интерполяции.");
                showOutdatedResults();
            }
        });

        pointsCountInput.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!dataPoints.isEmpty() && !newVal.trim().isEmpty()) {
                updateStatus("Количество точек изменено. Нажмите 'Интерполировать' для обновления интерполяции.");
                showOutdatedResults();
            }
        });

        targetXInput.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!dataPoints.isEmpty() && !newVal.trim().isEmpty()) {
                updateStatus("X для интерполяции изменен. Нажмите 'Интерполировать' для обновления интерполяции.");
                showOutdatedResults();
            }
        });
    }

    @FXML
    private void handleAddPoint() {
        try {
            double x = parseDouble(xInput.getText(), "X");
            double y = parseDouble(yInput.getText(), "Y");

            if (!Double.isFinite(x) || !Double.isFinite(y)) {
                updateStatus("Ошибка: X и Y должны быть конечными числами");
                return;
            }

            dataPoints.add(new InterpolationPoint(x, y));
            clearInputs();
            updateStatus("Точка добавлена: (" + x + ", " + y + "). Нажмите 'Интерполировать' для интерполяции.");
            showOutdatedResults();
        } catch (NumberFormatException e) {
            updateStatus("Ошибка: " + e.getMessage());
        }
    }

    @FXML
    private void handleRemovePoint() {
        ObservableList<InterpolationPoint> selectedPoints = pointsTable.getSelectionModel().getSelectedItems();

        if (selectedPoints.isEmpty()) {
            updateStatus("Ошибка: Не выбраны точки для удаления");
            return;
        }

        dataPoints.removeAll(selectedPoints);
        updateStatus("Удалено точек: " + selectedPoints.size() + ". Нажмите 'Интерполировать' для обновления интерполяции.");
        showOutdatedResults();
    }

    @FXML
    private void handleClearAll() {
        dataPoints.clear();
        chart.getData().clear();
        resultsArea.clear();
        updateStatus("Все точки удалены");
    }

    @FXML
    private void handleLoadFile() {
        File file = fileService.showOpenDialog(pointsTable.getScene().getWindow());
        if (file == null) return;

        try {
            List<InterpolationPoint> points = fileService.loadInterpolationPoints(file);

            List<InterpolationPoint> validPoints = new ArrayList<>();
            int invalidCount = 0;

            for (InterpolationPoint point : points) {
                if (Double.isFinite(point.getX()) && Double.isFinite(point.getY())) {
                    validPoints.add(point);
                } else {
                    invalidCount++;
                }
            }

            if (validPoints.size() < 2) {
                updateStatus("Ошибка: Недостаточно валидных точек для интерполяции (минимум 2)");
                return;
            }

            dataPoints.setAll(validPoints);
            updateStatus("Загружено точек: " + validPoints.size() +
                        (invalidCount > 0 ? " (пропущено некорректных: " + invalidCount + ")" : ""));
        } catch (Exception e) {
            updateStatus("Ошибка загрузки: " + e.getMessage());
        }
    }

    @FXML
    private void handleSaveFile() {
        if (dataPoints.isEmpty()) {
            updateStatus("Ошибка: Нет данных для сохранения");
            return;
        }

        File file = fileService.showSaveDialog(pointsTable.getScene().getWindow());
        if (file == null) return;

        try {
            fileService.saveInterpolationPoints(dataPoints, file);
            updateStatus("Данные сохранены в: " + file.getName());
        } catch (Exception e) {
            updateStatus("Ошибка сохранения: " + e.getMessage());
        }
    }

    @FXML
    private void handleGenerateFunction() {
        try {
            String function = functionComboBox.getValue();
            double start = parseDouble(intervalStartInput.getText(), "Начало интервала");
            double end = parseDouble(intervalEndInput.getText(), "Конец интервала");
            int count = Integer.parseInt(pointsCountInput.getText().trim());

            if (count < 2) {
                updateStatus("Ошибка: Минимум 2 точки требуется");
                return;
            }

            if (start >= end) {
                updateStatus("Ошибка: Начало интервала должно быть меньше конца");
                return;
            }

            if (count > 1000) {
                updateStatus("Ошибка: Слишком много точек (максимум 1000)");
                return;
            }

            dataPoints.clear();
            double step = (end - start) / (count - 1);
            int validPoints = 0;
            int skippedPoints = 0;
            StringBuilder skippedInfo = new StringBuilder();

            for (int i = 0; i < count; i++) {
                double x = start + i * step;
                try {
                    double y = calculateFunction(function, x);
                    if (Double.isFinite(y)) {
                        dataPoints.add(new InterpolationPoint(x, y));
                        validPoints++;
                    } else {
                        skippedPoints++;
                        if (skippedPoints <= 3) {
                            skippedInfo.append(String.format("x=%.3f ", x));
                        }
                    }
                } catch (Exception e) {
                    skippedPoints++;
                    if (skippedPoints <= 3) {
                        skippedInfo.append(String.format("x=%.3f ", x));
                    }
                }
            }

            if (skippedPoints > 0 && validPoints < count) {
                int neededPoints = count - validPoints;
                int attempts = 0;
                int maxAttempts = neededPoints * 10;

                while (validPoints < count && attempts < maxAttempts) {
                    double x = start + Math.random() * (end - start);
                    double y = calculateFunction(function, x);

                    if (Double.isFinite(y)) {
                        boolean isDuplicate = false;
                        for (InterpolationPoint existingPoint : dataPoints) {
                            if (Math.abs(existingPoint.getX() - x) < 1e-10) {
                                isDuplicate = true;
                                break;
                            }
                        }

                        if (!isDuplicate) {
                            dataPoints.add(new InterpolationPoint(x, y));
                            validPoints++;
                        }
                    }
                    attempts++;
                }

                dataPoints.sort((p1, p2) -> Double.compare(p1.getX(), p2.getX()));
            }

            if (validPoints < 2) {
                updateStatus("Ошибка: Не удалось сгенерировать достаточно валидных точек (минимум 2)");
                dataPoints.clear();
                return;
            }

            String statusMessage = "Сгенерировано " + validPoints + " валидных точек для функции " + function;
            if (skippedPoints > 0) {
                statusMessage += "\nПропущено " + skippedPoints + " точек с некорректными значениями";
                if (skippedPoints <= 3) {
                    statusMessage += " (x: " + skippedInfo.toString().trim() + ")";
                } else {
                    statusMessage += " (показаны первые 3)";
                }
            }
            statusMessage += "\nНажмите 'Интерполировать' для интерполяции.";

            updateStatus(statusMessage);
            showOutdatedResults();
        } catch (NumberFormatException e) {
            updateStatus("Ошибка: " + e.getMessage());
        }
    }

    @FXML
    private void handleCalculate() {
        if (dataPoints.size() < 2) {
            updateStatus("❌ Ошибка: Минимум 2 точки требуется для интерполяции");
            return;
        }

        if (dataPoints.size() > 20) {
            updateStatus("⚠️  Предупреждение: Большое количество точек может замедлить вычисления");
        }

        double minX = dataPoints.stream().mapToDouble(p -> p.getX()).min().orElse(0);
        double maxX = dataPoints.stream().mapToDouble(p -> p.getX()).max().orElse(0);
        double range = maxX - minX;

        for (int i = 0; i < dataPoints.size(); i++) {
            for (int j = i + 1; j < dataPoints.size(); j++) {
                if (Math.abs(dataPoints.get(i).getX() - dataPoints.get(j).getX()) < 1e-15) {
                    updateStatus("❌ Ошибка: Найдены дублирующиеся x-координаты в точках " + i + " и " + j);
                    return;
                }
            }
        }

        dataPoints.sort((p1, p2) -> Double.compare(p1.getX(), p2.getX()));

        try {
            double targetX = parseDouble(targetXInput.getText(), "X для интерполяции");

            if (targetX < minX || targetX > maxX) {

                if (targetX < minX - range * 0.5 || targetX > maxX + range * 0.5) {
                    updateStatus("❌ Ошибка: Целевая точка X=" + String.format("%.3f", targetX) +
                               " должна находиться внутри интервала интерполяции [" +
                               String.format("%.3f", minX) + ", " + String.format("%.3f", maxX) + "]. " +
                               "Интерполяция за пределами интервала данных может дать неточные результаты.");
                    return;
                } else {
                    updateStatus("⚠️  Предупреждение: X=" + String.format("%.3f", targetX) +
                               " находится за пределами интервала [" +
                               String.format("%.3f", minX) + ", " + String.format("%.3f", maxX) + "]. " +
                               "Результаты экстраполяции могут быть неточными.");
                }
            }

            InterpolationResult result = new InterpolationResult(dataPoints, targetX);

            double lagrangeValue = interpolationService.lagrangeInterpolation(dataPoints, targetX);
            double newtonDividedValue = interpolationService.newtonDividedDifferences(dataPoints, targetX);
            double newtonFiniteValue = interpolationService.newtonFiniteDifferences(dataPoints, targetX);
            double stirlingValue = interpolationService.stirlingInterpolation(dataPoints, targetX);
            double besselValue = interpolationService.besselInterpolation(dataPoints, targetX);

            int errorCount = 0;
            StringBuilder errorMessages = new StringBuilder();

            if (Double.isNaN(lagrangeValue) || Double.isInfinite(lagrangeValue)) {
                errorCount++;
                errorMessages.append("❌ Метод Лагранжа: ").append(formatResult(lagrangeValue)).append("\n");
            }
            if (Double.isNaN(newtonDividedValue) || Double.isInfinite(newtonDividedValue)) {
                errorCount++;
                errorMessages.append("❌ Метод Ньютона (разделенные): ").append(formatResult(newtonDividedValue)).append("\n");
            }
            if (Double.isNaN(newtonFiniteValue) || Double.isInfinite(newtonFiniteValue)) {
                errorCount++;
                errorMessages.append("❌ Метод Ньютона (конечные): ").append(formatResult(newtonFiniteValue)).append("\n");
            }
            if (Double.isNaN(stirlingValue) || Double.isInfinite(stirlingValue)) {
                errorCount++;
                errorMessages.append("❌ Метод Стирлинга: ").append(formatResult(stirlingValue)).append("\n");
            }
            if (Double.isNaN(besselValue) || Double.isInfinite(besselValue)) {
                errorCount++;
                errorMessages.append("❌ Метод Бесселя: ").append(formatResult(besselValue)).append("\n");
            }

            if (errorCount > 0) {
                updateStatus("⚠️  " + errorCount + " метод(ов) дал(и) некорректный результат:\n" + errorMessages.toString());
                if (errorCount == 5) {
                    updateStatus("❌ Все методы дали некорректные результаты. " +
                               "Возможные причины:\n" +
                               "• X находится слишком далеко от интервала данных (экстраполяция)\n" +
                               "• Слишком большое количество точек для экстраполяции\n" +
                               "• Проблемы с точностью вычислений при больших значениях\n" +
                               "Попробуйте уменьшить количество точек или выбрать X ближе к интервалу данных.");
                    return;
                }
            }

            result.setLagrangeValue(lagrangeValue);
            result.setNewtonDividedValue(newtonDividedValue);
            result.setNewtonFiniteValue(newtonFiniteValue);
            result.setStirlingValue(stirlingValue);
            result.setBesselValue(besselValue);
            result.setFiniteDifferencesTable(interpolationService.calculateFiniteDifferencesTable(dataPoints));

        resultsArea.clear();
        chart.getData().clear();

        displayResults(result);
        plotGraphs(result);

            updateStatus("Интерполяция завершена успешно");
        } catch (NumberFormatException e) {
            updateStatus("Ошибка: " + e.getMessage());
        } catch (Exception e) {
            updateStatus("Ошибка расчета: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private double calculateFunction(String function, double x) {
        switch (function) {
            case "sin(x² + 3)/(x - 1)":
                if (Math.abs(x - 1) < 1e-15)
                    return Double.NaN;
                double numerator = Math.sin(x * x + 3);
                if (!Double.isFinite(numerator))
                    return Double.NaN;
                return numerator / (x - 1);

            case "tan(x+1)/(x²-1)":
                if (Math.abs(x - 1) < 1e-15 || Math.abs(x + 1) < 1e-15)
                    return Double.NaN;
                double tanX1 = Math.tan(x + 1);
                if (!Double.isFinite(tanX1))
                    return Double.NaN;
                return tanX1 / (x * x - 1);

            case "√(x⁴+3x) * e^(-x)":
                double x4 = x * x * x * x;
                double sqrtArg = x4 + 3 * x;
                if (sqrtArg < 0)
                    return Double.NaN;
                double sqrtVal = Math.sqrt(sqrtArg);
                double expNegX = Math.exp(-x);
                if (!Double.isFinite(sqrtVal) || !Double.isFinite(expNegX))
                    return Double.NaN;
                return sqrtVal * expNegX;

            case "cos(3x) + ln(x+2)":
                if (x + 2 <= 0)
                    return Double.NaN;
                double cos3x = Math.cos(3 * x);
                double lnX2 = Math.log(x + 2);
                if (!Double.isFinite(cos3x) || !Double.isFinite(lnX2))
                    return Double.NaN;
                return cos3x + lnX2;

            case "1/(x²+sin(x)) + 5":
                double denominator = x * x + Math.sin(x);
                if (Math.abs(denominator) < 1e-15)
                    return Double.NaN;
                return 1.0 / denominator + 5;

            default:
                throw new IllegalArgumentException("Неизвестная функция");
        }
    }

    private void displayResults(InterpolationResult result) {
        StringBuilder sb = new StringBuilder();

        sb.append("╔══════════════════════════════════════════════════════════════════════════════╗\n");
        sb.append("║                              РЕЗУЛЬТАТЫ ИНТЕРПОЛЯЦИИ                        ║\n");
        sb.append("╚══════════════════════════════════════════════════════════════════════════════╝\n\n");

        String selectedFunction = functionComboBox.getValue();
        sb.append("📊 Целевая функция: ").append(selectedFunction).append("\n");

        if (!dataPoints.isEmpty()) {
            double minX = dataPoints.stream().mapToDouble(InterpolationPoint::getX).min().getAsDouble();
            double maxX = dataPoints.stream().mapToDouble(InterpolationPoint::getX).max().getAsDouble();
            sb.append("📏 Интервал данных: [").append(String.format("%.6f", minX))
              .append("; ").append(String.format("%.6f", maxX)).append("]\n");
            sb.append("📈 Количество узлов: ").append(dataPoints.size()).append("\n");
        }

        sb.append("🎯 Аргумент интерполяции: ").append(String.format("%.6f", result.getTargetX())).append("\n\n");

        sb.append("Метод: Многочлен Лагранжа\n");
        sb.append("  f(x) ≈ ").append(formatResult(result.getLagrangeValue())).append("\n");
        sb.append("  Узлы интерполяции:\n");
        for (InterpolationPoint p : result.getPoints()) {
            sb.append(String.format("    (%.4f; %.4f)\n", p.getX(), p.getY()));
        }
        sb.append("\n");

        sb.append("Метод: Ньютон (разделенные)\n");
        sb.append("  f(x) ≈ ").append(formatResult(result.getNewtonDividedValue())).append("\n");
        sb.append("  Узлы интерполяции:\n");
        for (InterpolationPoint p : result.getPoints()) {
            sb.append(String.format("    (%.4f; %.4f)\n", p.getX(), p.getY()));
        }
        sb.append("\n");

        sb.append("Метод: Ньютон (конечные)\n");
        sb.append("  f(x) ≈ ").append(formatResult(result.getNewtonFiniteValue())).append("\n");
        sb.append("  Узлы интерполяции:\n");
        for (InterpolationPoint p : result.getPoints()) {
            sb.append(String.format("    (%.4f; %.4f)\n", p.getX(), p.getY()));
        }
        sb.append("\n");

        sb.append("Метод: Стирлинга\n");
        sb.append("  f(x) ≈ ").append(formatResult(result.getStirlingValue())).append("\n");
        sb.append("  Узлы интерполяции:\n");
        for (InterpolationPoint p : result.getPoints()) {
            sb.append(String.format("    (%.4f; %.4f)\n", p.getX(), p.getY()));
        }
        sb.append("\n");

        sb.append("Метод: Бесселя\n");
        sb.append("  f(x) ≈ ").append(formatResult(result.getBesselValue())).append("\n");
        sb.append("  Узлы интерполяции:\n");
        for (InterpolationPoint p : result.getPoints()) {
            sb.append(String.format("    (%.4f; %.4f)\n", p.getX(), p.getY()));
        }
        sb.append("\n");

        double[] vals = new double[] {
            result.getLagrangeValue(),
            result.getNewtonDividedValue(),
            result.getNewtonFiniteValue(),
            result.getStirlingValue(),
            result.getBesselValue()
        };
        int validCount = 0;
        double sum = 0.0;
        for (double v : vals) {
            if (Double.isFinite(v)) { sum += v; validCount++; }
        }
        double mean = validCount > 0 ? sum / validCount : Double.NaN;

        sb.append("\n");
        sb.append("╔══════════════════════════════════════════════════════════════════════════════╗\n");
        sb.append("║                            ТАБЛИЦА КОНЕЧНЫХ РАЗНОСТЕЙ                       ║\n");
        sb.append("╚══════════════════════════════════════════════════════════════════════════════╝\n");
        displayFiniteDifferencesTable(sb, result.getFiniteDifferencesTable());

        sb.append("\n");
        sb.append("╔══════════════════════════════════════════════════════════════════════════════╗\n");
        sb.append("║                              СРАВНЕНИЕ МЕТОДОВ                             ║\n");
        sb.append("╚══════════════════════════════════════════════════════════════════════════════╝\n");
        sb.append("Таблица сравнения (метод | результат | разность от среднего)\n");
        sb.append("────────────────────────────────────────────────────────────\n");
        sb.append(String.format("Лагранж            | %s | %s\n", formatResult(vals[0]), formatResult(vals[0]-mean)));
        sb.append(String.format("Ньютон (раздел.)   | %s | %s\n", formatResult(vals[1]), formatResult(vals[1]-mean)));
        sb.append(String.format("Ньютон (конеч.)    | %s | %s\n", formatResult(vals[2]), formatResult(vals[2]-mean)));
        sb.append(String.format("Стирлинг           | %s | %s\n", formatResult(vals[3]), formatResult(vals[3]-mean)));
        sb.append(String.format("Бессель            | %s | %s\n", formatResult(vals[4]), formatResult(vals[4]-mean)));

        resultsArea.setText(sb.toString());
    }

    private String formatResult(double value) {
        if (Double.isNaN(value)) {
            return "NaN";
        } else if (Double.isInfinite(value)) {
            return value > 0 ? "+∞" : "-∞";
        } else {
            return String.format("%.8f", value);
        }
    }

    private void displayFiniteDifferencesTable(StringBuilder sb, double[][] table) {
        int n = table.length;

        sb.append("┌─────┬─────────────┬─────────────");
        for (int j = 1; j < n; j++) {
            sb.append("┬─────────────");
        }
        sb.append("┐\n");

        sb.append("│  i  │      x      │      y      ");
        for (int j = 1; j < n; j++) {
            sb.append(String.format("│    Δ%d y     ", j));
        }
        sb.append("│\n");

        sb.append("├─────┼─────────────┼─────────────");
        for (int j = 1; j < n; j++) {
            sb.append("┼─────────────");
        }
        sb.append("┤\n");

        for (int i = 0; i < n; i++) {
            sb.append(String.format("│ %3d │", i));
            sb.append(String.format(" %11.6f │", dataPoints.get(i).getX()));
            sb.append(String.format(" %11.6f │", dataPoints.get(i).getY()));

            for (int j = 1; j < n - i; j++) {
                double value = table[i][j];
                if (Double.isNaN(value)) {
                    sb.append("      NaN     │");
                } else if (Double.isInfinite(value)) {
                    sb.append(String.format(" %10s │", value > 0 ? "+∞" : "-∞"));
                } else {
                    sb.append(String.format(" %11.6f │", value));
                }
            }

            for (int j = n - i; j < n; j++) {
                sb.append("             │");
            }
            sb.append("\n");
        }

        sb.append("└─────┴─────────────┴─────────────");
        for (int j = 1; j < n; j++) {
            sb.append("┴─────────────");
        }
        sb.append("┘\n");
    }

    private void plotGraphs(InterpolationResult result) {
        System.out.println("=== НАЧАЛО plotGraphs ===");

        chart.getData().clear();

        String selectedFunction = functionComboBox.getValue();
        double targetX = result.getTargetX();

        try {

            if (swingChartNode != null && swingChartNode.getParent() != null) {
                javafx.scene.Parent parent = swingChartNode.getParent();
                if (parent instanceof javafx.scene.layout.Pane) {
                    javafx.scene.layout.Pane pane = (javafx.scene.layout.Pane) parent;
                    pane.getChildren().remove(swingChartNode);

                    if (!pane.getChildren().contains(chart)) {
                        pane.getChildren().add(chart);
                    }
                }
                swingChartNode = null;
            }

            swingChartNode = SwingChartNode.createChartNode(
                "Интерполяция функций",
                dataPoints,
                selectedFunction,
                targetX
            );

            if (chart.getParent() != null) {

                javafx.scene.Parent parent = chart.getParent();
                if (parent instanceof javafx.scene.layout.Pane) {
                    javafx.scene.layout.Pane pane = (javafx.scene.layout.Pane) parent;

                    pane.getChildren().remove(chart);

                    pane.getChildren().add(swingChartNode);

                    System.out.println("JFreeChart встроен в основное окно");
                }
            }
        } catch (Exception e) {
            System.err.println("Ошибка при создании SwingNode: " + e.getMessage());
            e.printStackTrace();

            createJFreeChartWindow(selectedFunction, targetX);
        }
    }

    private void createJFreeChartWindow(String selectedFunction, double targetX) {

        javax.swing.SwingUtilities.invokeLater(() -> {
            try {
                javax.swing.UIManager.setLookAndFeel(javax.swing.UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {
            }

            javax.swing.JPanel chartPanel = ru.ifmo.cs.plotting.JFreeChartBuilder.createChart(
                "Интерполяция функций",
                dataPoints,
                selectedFunction,
                targetX
            );

            javax.swing.JFrame chartFrame = new javax.swing.JFrame("График интерполяции");
            chartFrame.setDefaultCloseOperation(javax.swing.JFrame.DISPOSE_ON_CLOSE);
            chartFrame.add(chartPanel);
            chartFrame.pack();
            chartFrame.setLocationRelativeTo(null);
            chartFrame.setVisible(true);

            System.out.println("JFreeChart окно отображено");
        });

        chart.requestLayout();
    }

    private void plotSelectedFunction_OLD() {
        if (dataPoints.size() < 2) return;

        String selectedFunction = functionComboBox.getValue();
        System.out.println("Выбранная функция: " + selectedFunction);
        if (selectedFunction == null) {
            System.out.println("Функция не выбрана, пропускаем отрисовку");
            return;
        }

        chart.getData().removeIf(series -> series.getName() != null && series.getName().contains("Выбранная функция"));

        double minX = dataPoints.stream().mapToDouble(InterpolationPoint::getX).min().getAsDouble();
        double maxX = dataPoints.stream().mapToDouble(InterpolationPoint::getX).max().getAsDouble();
        double range = maxX - minX;
        double extendedMinX = minX - range * 0.2;
        double extendedMaxX = maxX + range * 0.2;

        int plotPoints = Math.max(100, (int) Math.min(2000, Math.abs(extendedMaxX - extendedMinX) * 0.1));

        if (selectedFunction.contains("ln(")) {
            extendedMinX = Math.max(extendedMinX, -1.9);
        }

        XYChart.Series<Number, Number> functionSeries = new XYChart.Series<>();
        functionSeries.setName("Выбранная функция: " + selectedFunction);

        double step = (extendedMaxX - extendedMinX) / plotPoints;
        double previousY = Double.NaN;
        double maxJump = 10.0;

        int validPoints = 0;
        for (int i = 0; i <= plotPoints; i++) {
            double x = extendedMinX + i * step;
            try {
                double y = calculateFunction(selectedFunction, x);

                if (Double.isFinite(y)) {

                    functionSeries.getData().add(new XYChart.Data<>(x, y));
                    validPoints++;
                }
            } catch (Exception e) {

            }
        }

        System.out.println("Валидных точек для функции: " + validPoints + " из " + (plotPoints + 1));

        chart.getData().add(functionSeries);

        System.out.println("Функция добавлена: " + functionSeries.getName() + ", точек: " + functionSeries.getData().size());
        System.out.println("Всего серий на графике: " + chart.getData().size());

        if (functionSeries.getData().size() > 0) {
            System.out.println("Первые 3 точки функции:");
            for (int i = 0; i < Math.min(3, functionSeries.getData().size()); i++) {
                XYChart.Data<Number, Number> point = functionSeries.getData().get(i);
                System.out.println("  " + i + ": x=" + point.getXValue() + ", y=" + point.getYValue());
            }
        }

    }

    private void plotInterpolationPolynomials(InterpolationResult result) {
        if (dataPoints.size() < 2) return;

        double minX = dataPoints.stream().mapToDouble(InterpolationPoint::getX).min().getAsDouble();
        double maxX = dataPoints.stream().mapToDouble(InterpolationPoint::getX).max().getAsDouble();
        double range = maxX - minX;
        double extendedMinX = minX - range * 0.2;
        double extendedMaxX = maxX + range * 0.2;

        int plotPoints = Math.max(500, (int) Math.min(3000, Math.abs(extendedMaxX - extendedMinX) * 0.2));

        XYChart.Series<Number, Number> lagrangeSeries = new XYChart.Series<>();
        lagrangeSeries.setName("Лагранж");

        double step = (extendedMaxX - extendedMinX) / plotPoints;
        double previousY = Double.NaN;
        double maxJump = 10.0;

        for (int i = 0; i <= plotPoints; i++) {
            double x = extendedMinX + i * step;
            try {
                double y = interpolationService.lagrangeInterpolation(dataPoints, x);

                if (Double.isFinite(y)) {
                    lagrangeSeries.getData().add(new XYChart.Data<>(x, y));
                }
            } catch (Exception e) {
                previousY = Double.NaN;
            }
        }

        chart.getData().add(lagrangeSeries);
        System.out.println("Лагранж добавлен: " + lagrangeSeries.getName() + ", точек: " + lagrangeSeries.getData().size());

        XYChart.Series<Number, Number> newtonDividedSeries = new XYChart.Series<>();
        newtonDividedSeries.setName("Ньютон (разделенные)");

        step = (extendedMaxX - extendedMinX) / plotPoints;
        previousY = Double.NaN;
        maxJump = 100.0;

        for (int i = 0; i <= plotPoints; i++) {
            double x = extendedMinX + i * step;
            try {
                double y = interpolationService.newtonDividedDifferences(dataPoints, x);

                if (Double.isFinite(y)) {
                    newtonDividedSeries.getData().add(new XYChart.Data<>(x, y));
                }
            } catch (Exception e) {
                previousY = Double.NaN;
            }
        }

        chart.getData().add(newtonDividedSeries);

        XYChart.Series<Number, Number> newtonFiniteSeries = new XYChart.Series<>();
        newtonFiniteSeries.setName("Ньютон (конечные)");

        step = (extendedMaxX - extendedMinX) / plotPoints;
        previousY = Double.NaN;
        maxJump = 100.0;

        for (int i = 0; i <= plotPoints; i++) {
            double x = extendedMinX + i * step;
            try {
                double y = interpolationService.newtonFiniteDifferences(dataPoints, x);

                if (Double.isFinite(y)) {
                    newtonFiniteSeries.getData().add(new XYChart.Data<>(x, y));
                }
            } catch (Exception e) {
                previousY = Double.NaN;
            }
        }

        chart.getData().add(newtonFiniteSeries);

        if (dataPoints.size() >= 3) {
            XYChart.Series<Number, Number> stirlingSeries = new XYChart.Series<>();
            stirlingSeries.setName("Стирлинг");

            step = (extendedMaxX - extendedMinX) / plotPoints;
            previousY = Double.NaN;
            maxJump = 10.0;

            for (int i = 0; i <= plotPoints; i++) {
                double x = extendedMinX + i * step;
                try {
                    double y = interpolationService.stirlingInterpolation(dataPoints, x);

                    if (Double.isFinite(y)) {
                        stirlingSeries.getData().add(new XYChart.Data<>(x, y));
                    }
                } catch (Exception e) {
                    previousY = Double.NaN;
                }
            }

            chart.getData().add(stirlingSeries);
        }

        if (dataPoints.size() >= 3) {
            XYChart.Series<Number, Number> besselSeries = new XYChart.Series<>();
            besselSeries.setName("Бессель");

            step = (extendedMaxX - extendedMinX) / plotPoints;
            previousY = Double.NaN;
            maxJump = 10.0;

            for (int i = 0; i <= plotPoints; i++) {
                double x = extendedMinX + i * step;
                try {
                    double y = interpolationService.besselInterpolation(dataPoints, x);

                    if (Double.isFinite(y)) {
                        besselSeries.getData().add(new XYChart.Data<>(x, y));
                    }
                } catch (Exception e) {
                    previousY = Double.NaN;
                }
            }

            chart.getData().add(besselSeries);
        }

        System.out.println("=== СОСТОЯНИЕ ГРАФИКА ===");
        System.out.println("Всего серий: " + chart.getData().size());
        for (int i = 0; i < chart.getData().size(); i++) {
            XYChart.Series<Number, Number> series = chart.getData().get(i);
            System.out.println("Серия " + i + ": " + series.getName() + ", точек: " + series.getData().size());
        }

        if (chart.getXAxis() != null) {
            System.out.println("X ось: " + chart.getXAxis().getClass().getSimpleName());
        }
        if (chart.getYAxis() != null) {
            System.out.println("Y ось: " + chart.getYAxis().getClass().getSimpleName());
        }

        Platform.runLater(() -> {
            chart.requestLayout();
            chart.layout();
        });

    }

    private void clearInputs() {
        xInput.clear();
        yInput.clear();
    }

    private void clearResults() {
        resultsArea.clear();
        chart.getData().clear();
    }

    private void showOutdatedResults() {
        StringBuilder sb = new StringBuilder();
        sb.append("╔══════════════════════════════════════════════════════════════════════════════╗\n");
        sb.append("║                          ⚠️  РЕЗУЛЬТАТЫ УСТАРЕЛИ ⚠️                        ║\n");
        sb.append("╚══════════════════════════════════════════════════════════════════════════════╝\n\n");
        sb.append("📝 Параметры были изменены. Нажмите кнопку 'Интерполировать' для обновления результатов.\n\n");
        sb.append("🔄 Доступные действия:\n");
        sb.append("• Изменить функцию и пересчитать\n");
        sb.append("• Изменить интервал и количество точек\n");
        sb.append("• Изменить X для интерполяции\n");
        sb.append("• Добавить или удалить точки данных\n\n");
        sb.append("💡 Результаты будут обновлены автоматически после нажатия 'Интерполировать'.\n");

        resultsArea.setText(sb.toString());
    }

    private void displayMethodComparison(StringBuilder sb, InterpolationResult result) {
        sb.append("📊 МЕТОД ЛАГРАНЖА:\n");
        sb.append("────────────────────────────────────────────────────────────────────────────────\n");
        sb.append("• Принцип: P(x) = Σ[yᵢ × ∏(x-xⱼ)/(xᵢ-xⱼ)] для j≠i\n");
        sb.append("• Преимущества: Простота понимания, универсальность\n");
        sb.append("• Недостатки: O(n²) операций, неэффективен для больших n\n");
        sb.append("• Точность: ").append(formatResult(result.getLagrangeValue())).append("\n");
        sb.append("• Применение: Небольшие наборы данных, равномерные сетки\n\n");

        sb.append("📊 МЕТОД НЬЮТОНА (РАЗДЕЛЕННЫЕ РАЗНОСТИ):\n");
        sb.append("────────────────────────────────────────────────────────────────────────────────\n");
        sb.append("• Принцип: P(x) = f[x₀] + f[x₀,x₁](x-x₀) + f[x₀,x₁,x₂](x-x₀)(x-x₁) + ...\n");
        sb.append("• Преимущества: O(n²) операций, легко добавлять новые точки\n");
        sb.append("• Недостатки: Может быть неустойчив при близких x-координатах\n");
        sb.append("• Точность: ").append(formatResult(result.getNewtonDividedValue())).append("\n");
        sb.append("• Применение: Неравномерные сетки, динамическое добавление точек\n\n");

        sb.append("📊 МЕТОД НЬЮТОНА (КОНЕЧНЫЕ РАЗНОСТИ):\n");
        sb.append("────────────────────────────────────────────────────────────────────────────────\n");
        sb.append("• Принцип: P(x) = f₀ + Δf₀t + Δ²f₀t(t-1)/2! + ... где t=(x-x₀)/h\n");
        sb.append("• Преимущества: O(n²) операций, высокая точность для равномерных сеток\n");
        sb.append("• Недостатки: Требует равномерную сетку, может быть неустойчив\n");
        sb.append("• Точность: ").append(formatResult(result.getNewtonFiniteValue())).append("\n");
        sb.append("• Применение: Равномерные сетки, интерполяция в середине интервала\n\n");

        sb.append("📊 МЕТОД СТИРЛИНГА:\n");
        sb.append("────────────────────────────────────────────────────────────────────────────────\n");
        sb.append("• Принцип: Центрированная интерполяция с использованием центральных разностей\n");
        sb.append("• Преимущества: Высокая точность в центре интервала, симметричная формула\n");
        sb.append("• Недостатки: Требует равномерную сетку, сложность вычислений\n");
        sb.append("• Точность: ").append(formatResult(result.getStirlingValue())).append("\n");
        sb.append("• Применение: Интерполяция в середине равномерных сеток\n\n");

        sb.append("📊 МЕТОД БЕССЕЛЯ:\n");
        sb.append("────────────────────────────────────────────────────────────────────────────────\n");
        sb.append("• Принцип: Усреднение значений в центре с использованием центральных разностей\n");
        sb.append("• Преимущества: Хорошая точность для интерполяции между узлами\n");
        sb.append("• Недостатки: Требует равномерную сетку, сложность реализации\n");
        sb.append("• Точность: ").append(formatResult(result.getBesselValue())).append("\n");
        sb.append("• Применение: Интерполяция между узлами равномерных сеток\n\n");

        sb.append("📈 АНАЛИЗ ТОЧНОСТИ:\n");
        sb.append("────────────────────────────────────────────────────────────────────────────────\n");

        if (Double.isFinite(result.getLagrangeValue()) &&
            Double.isFinite(result.getNewtonDividedValue()) &&
            Double.isFinite(result.getNewtonFiniteValue()) &&
            Double.isFinite(result.getStirlingValue()) &&
            Double.isFinite(result.getBesselValue())) {

            double lagrange = result.getLagrangeValue();
            double newtonDivided = result.getNewtonDividedValue();
            double newtonFinite = result.getNewtonFiniteValue();
            double stirling = result.getStirlingValue();
            double bessel = result.getBesselValue();

            double maxValue = Math.max(Math.max(Math.max(Math.abs(lagrange), Math.abs(newtonDivided)),
                                             Math.max(Math.abs(newtonFinite), Math.abs(stirling))), Math.abs(bessel));
            double minValue = Math.min(Math.min(Math.min(Math.abs(lagrange), Math.abs(newtonDivided)),
                                             Math.min(Math.abs(newtonFinite), Math.abs(stirling))), Math.abs(bessel));

            sb.append("• Максимальное значение: ").append(String.format("%.8f", maxValue)).append("\n");
            sb.append("• Минимальное значение: ").append(String.format("%.8f", minValue)).append("\n");
            sb.append("• Размах значений: ").append(String.format("%.8f", maxValue - minValue)).append("\n");

            double relativeError = maxValue > 0 ? (maxValue - minValue) / maxValue : 0;
            sb.append("• Относительная погрешность: ").append(String.format("%.2e", relativeError)).append("\n");

            if (relativeError < 1e-10) {
                sb.append("✅ Отличное совпадение - все методы дают практически одинаковый результат\n");
            } else if (relativeError < 1e-6) {
                sb.append("✅ Хорошее совпадение - методы дают близкие результаты\n");
            } else if (relativeError < 1e-3) {
                sb.append("⚠️  Удовлетворительное совпадение - есть небольшие различия\n");
            } else {
                sb.append("❌ Заметные различия - возможно, проблема с данными или алгоритмом\n");
            }
        } else {
            sb.append("❌ Невозможно проанализировать точность - есть некорректные результаты\n");
        }

        sb.append("\n💡 РЕКОМЕНДАЦИИ:\n");
        sb.append("────────────────────────────────────────────────────────────────────────────────\n");
        sb.append("• Для равномерных сеток: используйте метод конечных разностей\n");
        sb.append("• Для неравномерных сеток: используйте метод разделенных разностей\n");
        sb.append("• Для небольших наборов данных: метод Лагранжа прост и надежен\n");
        sb.append("• При больших различиях между методами: проверьте качество данных\n");
    }

    private void clearAll() {
        dataPoints.clear();
        chart.getData().clear();
        resultsArea.clear();
        statusBar.setText("Готово");

        if (swingChartNode != null && swingChartNode.getParent() != null) {
            javafx.scene.Parent parent = swingChartNode.getParent();
            if (parent instanceof javafx.scene.layout.Pane) {
                javafx.scene.layout.Pane pane = (javafx.scene.layout.Pane) parent;
                pane.getChildren().remove(swingChartNode);

                if (!pane.getChildren().contains(chart)) {
                    pane.getChildren().add(chart);
                }
            }
            swingChartNode = null;
        }
    }

    private double parseDouble(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new NumberFormatException("Поле '" + fieldName + "' пустое");
        }
        try {
            String normalizedValue = value.trim().replace(',', '.');
            double parsed = Double.parseDouble(normalizedValue);
            if (!Double.isFinite(parsed)) {
                throw new NumberFormatException("В поле '" + fieldName + "' недопустимое значение (NaN/Infinity)");
            }
            return parsed;
        } catch (NumberFormatException e) {
            throw new NumberFormatException("Неверный формат числа в поле '" + fieldName + "'");
        }
    }

    private void updateStatus(String message) {
        statusBar.setText(message);
    }
}

