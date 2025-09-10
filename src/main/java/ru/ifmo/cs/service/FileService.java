package ru.ifmo.cs.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import javafx.stage.FileChooser;
import javafx.stage.Window;
import ru.ifmo.cs.model.InterpolationPoint;

public class FileService {
    private static final FileChooser.ExtensionFilter TXT_FILTER =
            new FileChooser.ExtensionFilter("Text Files (*.txt)", "*.txt");
    private static final FileChooser.ExtensionFilter CSV_FILTER =
            new FileChooser.ExtensionFilter("CSV Files (*.csv)", "*.csv");
    private static final FileChooser.ExtensionFilter ALL_FILTER =
            new FileChooser.ExtensionFilter("All Files", "*.*");

    public File showOpenDialog(Window owner) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Открыть файл с данными");
        fileChooser.getExtensionFilters().addAll(TXT_FILTER, CSV_FILTER, ALL_FILTER);
        return fileChooser.showOpenDialog(owner);
    }

    public File showSaveDialog(Window owner) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Сохранить данные");
        fileChooser.getExtensionFilters().addAll(TXT_FILTER, CSV_FILTER, ALL_FILTER);
        return fileChooser.showSaveDialog(owner);
    }

    public List<InterpolationPoint> loadInterpolationPoints(File file) throws IOException {
        List<InterpolationPoint> points = new ArrayList<>();
        int lineNumber = 0;
        int validPoints = 0;
        int invalidPoints = 0;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                line = line.trim();

                if (line.isEmpty() || line.startsWith("#") || line.startsWith("
                    continue;
                }

                String[] parts = line.split("[,\\s]+");
                if (parts.length >= 2) {
                    try {
                        String xStr = parts[0].trim().replace(',', '.');
                        String yStr = parts[1].trim().replace(',', '.');

                        double x, y;
                        if (xStr.equalsIgnoreCase("NaN") || xStr.equalsIgnoreCase("nan")) {
                            x = Double.NaN;
                        } else if (xStr.equalsIgnoreCase("Infinity") || xStr.equalsIgnoreCase("Inf")) {
                            x = Double.POSITIVE_INFINITY;
                        } else if (xStr.equalsIgnoreCase("-Infinity") || xStr.equalsIgnoreCase("-Inf")) {
                            x = Double.NEGATIVE_INFINITY;
                        } else {
                            x = Double.parseDouble(xStr);
                        }

                        if (yStr.equalsIgnoreCase("NaN") || yStr.equalsIgnoreCase("nan")) {
                            y = Double.NaN;
                        } else if (yStr.equalsIgnoreCase("Infinity") || yStr.equalsIgnoreCase("Inf")) {
                            y = Double.POSITIVE_INFINITY;
                        } else if (yStr.equalsIgnoreCase("-Infinity") || yStr.equalsIgnoreCase("-Inf")) {
                            y = Double.NEGATIVE_INFINITY;
                        } else {
                            y = Double.parseDouble(yStr);
                        }

                        points.add(new InterpolationPoint(x, y));
                        if (Double.isFinite(x) && Double.isFinite(y)) {
                            validPoints++;
                        } else {
                            invalidPoints++;
                        }
                    } catch (NumberFormatException e) {
                        System.err.println("Ошибка формата данных в строке " + lineNumber + ": " + line);
                        invalidPoints++;
                    }
                } else {
                    System.err.println("Недостаточно данных в строке " + lineNumber + ": " + line);
                    invalidPoints++;
                }
            }
        }

        System.out.println("Загружено точек: " + points.size() + " (валидных: " + validPoints + ", некорректных: " + invalidPoints + ")");
        return points;
    }

    public void saveInterpolationPoints(List<InterpolationPoint> points, File file) throws IOException {
        try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
            for (InterpolationPoint point : points) {
                writer.printf("%.6f, %.6f%n", point.getX(), point.getY());
            }
        }
    }
}