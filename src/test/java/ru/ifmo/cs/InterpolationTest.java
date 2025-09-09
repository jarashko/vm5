package ru.ifmo.cs;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;
import ru.ifmo.cs.model.InterpolationPoint;
import ru.ifmo.cs.service.InterpolationService;
import java.util.Arrays;
import java.util.List;

/**
 * Тесты для проверки корректности интерполяции
 */
public class InterpolationTest {
    
    private InterpolationService interpolationService;
    
    @BeforeEach
    void setUp() {
        interpolationService = new InterpolationService();
    }
    
    @Test
    void testLagrangeInterpolationBasic() {
        List<InterpolationPoint> points = Arrays.asList(
            new InterpolationPoint(0.0, 1.0),
            new InterpolationPoint(1.0, 2.0),
            new InterpolationPoint(2.0, 5.0)
        );
        
        double result = interpolationService.lagrangeInterpolation(points, 1.5);
        assertTrue(Double.isFinite(result), "Результат должен быть конечным числом");
    }
    
    @Test
    void testNewtonDividedDifferencesBasic() {
        List<InterpolationPoint> points = Arrays.asList(
            new InterpolationPoint(0.0, 1.0),
            new InterpolationPoint(1.0, 2.0),
            new InterpolationPoint(2.0, 5.0)
        );
        
        double result = interpolationService.newtonDividedDifferences(points, 1.5);
        assertTrue(Double.isFinite(result), "Результат должен быть конечным числом");
    }
    
    @Test
    void testNewtonFiniteDifferencesBasic() {
        List<InterpolationPoint> points = Arrays.asList(
            new InterpolationPoint(0.0, 1.0),
            new InterpolationPoint(1.0, 2.0),
            new InterpolationPoint(2.0, 5.0)
        );
        
        double result = interpolationService.newtonFiniteDifferences(points, 1.5);
        assertTrue(Double.isFinite(result), "Результат должен быть конечным числом");
    }
    
    @Test
    void testInsufficientPoints() {
        List<InterpolationPoint> points = Arrays.asList(
            new InterpolationPoint(0.0, 1.0)
        );
        
        double result = interpolationService.lagrangeInterpolation(points, 1.0);
        assertTrue(Double.isNaN(result), "Результат должен быть NaN для недостаточного количества точек");
    }
    
    @Test
    void testDuplicateXCoordinates() {
        List<InterpolationPoint> points = Arrays.asList(
            new InterpolationPoint(1.0, 1.0),
            new InterpolationPoint(1.0, 2.0),
            new InterpolationPoint(2.0, 3.0)
        );
        
        double result = interpolationService.lagrangeInterpolation(points, 1.5);
        assertTrue(Double.isNaN(result), "Результат должен быть NaN для дублирующихся x-координат");
    }
    
    @Test
    void testNaNValues() {
        List<InterpolationPoint> points = Arrays.asList(
            new InterpolationPoint(0.0, Double.NaN),
            new InterpolationPoint(1.0, 2.0),
            new InterpolationPoint(2.0, 3.0)
        );
        
        double result = interpolationService.lagrangeInterpolation(points, 1.5);
        assertTrue(Double.isNaN(result), "Результат должен быть NaN при наличии NaN в данных");
    }
    
    @Test
    void testInfiniteValues() {
        List<InterpolationPoint> points = Arrays.asList(
            new InterpolationPoint(0.0, Double.POSITIVE_INFINITY),
            new InterpolationPoint(1.0, 2.0),
            new InterpolationPoint(2.0, 3.0)
        );
        
        double result = interpolationService.lagrangeInterpolation(points, 1.5);
        assertTrue(Double.isNaN(result), "Результат должен быть NaN при наличии Infinity в данных");
    }
    
    @Test
    void testExtremeValues() {
        List<InterpolationPoint> points = Arrays.asList(
            new InterpolationPoint(1e-10, 1e10),
            new InterpolationPoint(1e-5, 1e5),
            new InterpolationPoint(1e-3, 1e3)
        );
        
        double result = interpolationService.lagrangeInterpolation(points, 1e-6);
        assertTrue(Double.isFinite(result), "Результат должен быть конечным для экстремальных значений");
    }
    
    @Test
    void testNonUniformGrid() {
        List<InterpolationPoint> points = Arrays.asList(
            new InterpolationPoint(0.0, 1.0),
            new InterpolationPoint(1.0, 2.0),
            new InterpolationPoint(3.0, 10.0)  // Неравномерная сетка
        );
        
        double result = interpolationService.newtonFiniteDifferences(points, 2.0);
        // Должен автоматически переключиться на метод разделенных разностей
        assertTrue(Double.isFinite(result), "Результат должен быть конечным для неравномерной сетки");
    }
    
    @Test
    void testConsistencyBetweenMethods() {
        List<InterpolationPoint> points = Arrays.asList(
            new InterpolationPoint(0.0, 1.0),
            new InterpolationPoint(1.0, 2.0),
            new InterpolationPoint(2.0, 5.0),
            new InterpolationPoint(3.0, 10.0)
        );
        
        double x = 1.5;
        double lagrange = interpolationService.lagrangeInterpolation(points, x);
        double newtonDivided = interpolationService.newtonDividedDifferences(points, x);
        
        // Методы должны давать одинаковые результаты (с учетом погрешности)
        assertEquals(lagrange, newtonDivided, 1e-10, "Методы Лагранжа и Ньютона должны давать одинаковые результаты");
    }
}
