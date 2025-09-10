package ru.ifmo.cs;

import ru.ifmo.cs.model.InterpolationPoint;
import ru.ifmo.cs.plotting.JFreeChartBuilder;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

public class ChartTest {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {
            }

            List<InterpolationPoint> dataPoints = new ArrayList<>();
            dataPoints.add(new InterpolationPoint(-2, 1));
            dataPoints.add(new InterpolationPoint(-1, 2));
            dataPoints.add(new InterpolationPoint(0, 3));
            dataPoints.add(new InterpolationPoint(1, 4));
            dataPoints.add(new InterpolationPoint(2, 5));

            JPanel chartPanel = JFreeChartBuilder.createChart(
                "Тест JFreeChart",
                dataPoints,
                "sin(x² + 3)/(x - 1)",
                0.5
            );

            JFrame frame = new JFrame("Тест JFreeChart");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.add(chartPanel);
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);

            System.out.println("JFreeChart тест запущен!");
        });
    }
}

