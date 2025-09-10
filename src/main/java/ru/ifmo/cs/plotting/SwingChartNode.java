package ru.ifmo.cs.plotting;

import javafx.embed.swing.SwingNode;
import javafx.scene.Node;
import ru.ifmo.cs.model.InterpolationPoint;

import javax.swing.*;
import java.util.List;

public class SwingChartNode {

    public static Node createChartNode(String title,
                                     List<InterpolationPoint> dataPoints,
                                     String selectedFunction,
                                     double targetX) {
        SwingNode swingNode = new SwingNode();

        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {
            }

            JPanel chartPanel = JFreeChartBuilder.createChart(
                title,
                dataPoints,
                selectedFunction,
                targetX
            );

            swingNode.setContent(chartPanel);
        });

        return swingNode;
    }
}

