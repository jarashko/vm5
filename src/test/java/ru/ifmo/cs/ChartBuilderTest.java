package ru.ifmo.cs;

import javafx.scene.chart.XYChart;
import org.junit.jupiter.api.Test;
import ru.ifmo.cs.plotting.ChartBuilder;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ChartBuilderTest {

    @Test
    public void testBuildFunctionSeries() {

        XYChart.Series<Number, Number> series = ChartBuilder.buildFunctionSeries(
            x -> x, 0.0, 10.0, 10
        );

        assertNotNull(series);
        assertEquals(11, series.getData().size());

        XYChart.Data<Number, Number> firstPoint = series.getData().get(0);
        assertEquals(0.0, firstPoint.getXValue().doubleValue(), 1e-10);
        assertEquals(0.0, firstPoint.getYValue().doubleValue(), 1e-10);

        XYChart.Data<Number, Number> lastPoint = series.getData().get(10);
        assertEquals(10.0, lastPoint.getXValue().doubleValue(), 1e-10);
        assertEquals(10.0, lastPoint.getYValue().doubleValue(), 1e-10);
    }

    @Test
    public void testBuildFunctionSeriesWithDiscontinuity() {

        XYChart.Series<Number, Number> series = ChartBuilder.buildFunctionSeries(
            x -> 1.0 / (x - 5.0), 0.0, 10.0, 20
        );

        assertNotNull(series);

        assertTrue(series.getData().size() < 21);
        assertTrue(series.getData().size() > 0);
    }

    @Test
    public void testBuildFunctionSegments() {

        List<XYChart.Series<Number, Number>> segments = ChartBuilder.buildFunctionSegments(
            x -> 1.0 / (x - 5.0), 0.0, 10.0, 20
        );

        assertNotNull(segments);

        assertEquals(2, segments.size());

        XYChart.Series<Number, Number> firstSegment = segments.get(0);
        assertTrue(firstSegment.getData().size() > 0);
        assertTrue(firstSegment.getData().get(firstSegment.getData().size() - 1).getXValue().doubleValue() < 5.0);

        XYChart.Series<Number, Number> secondSegment = segments.get(1);
        assertTrue(secondSegment.getData().size() > 0);
        assertTrue(secondSegment.getData().get(0).getXValue().doubleValue() > 5.0);
    }

    @Test
    public void testBuildFunctionSegmentsWithCustomThreshold() {

        List<XYChart.Series<Number, Number>> segments = ChartBuilder.buildFunctionSegments(
            x -> x * x, -5.0, 5.0, 20, 10.0
        );

        assertNotNull(segments);

        assertEquals(1, segments.size());
        assertEquals(21, segments.get(0).getData().size());
    }

    @Test
    public void testBuildFunctionSegmentsWithSharpJumps() {

        List<XYChart.Series<Number, Number>> segments = ChartBuilder.buildFunctionSegments(
            x -> x > 0 ? 100.0 : -100.0, -2.0, 2.0, 20, 50.0
        );

        assertNotNull(segments);

        assertEquals(2, segments.size());
    }

    @Test
    public void testInvalidParameters() {

        assertThrows(IllegalArgumentException.class, () -> {
            ChartBuilder.buildFunctionSeries(x -> x, 0.0, 10.0, 0);
        });

        assertThrows(IllegalArgumentException.class, () -> {
            ChartBuilder.buildFunctionSeries(x -> x, 0.0, 10.0, -1);
        });

        assertThrows(IllegalArgumentException.class, () -> {
            ChartBuilder.buildFunctionSegments(x -> x, 0.0, 10.0, 0);
        });
    }

    @Test
    public void testQuadraticFunction() {

        XYChart.Series<Number, Number> series = ChartBuilder.buildFunctionSeries(
            x -> x * x, -2.0, 2.0, 20
        );

        assertNotNull(series);
        assertEquals(21, series.getData().size());

        XYChart.Data<Number, Number> point1 = series.getData().get(0);
        XYChart.Data<Number, Number> point2 = series.getData().get(20);
        assertEquals(point1.getYValue().doubleValue(), point2.getYValue().doubleValue(), 1e-10);
        assertEquals(4.0, point1.getYValue().doubleValue(), 1e-10);
    }
}
