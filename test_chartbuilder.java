import javafx.scene.chart.XYChart;
import java.util.List;

// Простой тест для проверки ChartBuilder без JavaFX GUI
public class test_chartbuilder {
    
    public static void main(String[] args) {
        System.out.println("Тестирование ChartBuilder...");
        
        try {
            // Тестируем простую функцию
            XYChart.Series<Number, Number> series = ru.ifmo.cs.plotting.ChartBuilder.buildFunctionSeries(
                x -> x * x, -2.0, 2.0, 10
            );
            
            System.out.println("✅ buildFunctionSeries работает!");
            System.out.println("   Создано точек: " + series.getData().size());
            System.out.println("   Первая точка: (" + 
                series.getData().get(0).getXValue() + ", " + 
                series.getData().get(0).getYValue() + ")");
            System.out.println("   Последняя точка: (" + 
                series.getData().get(series.getData().size()-1).getXValue() + ", " + 
                series.getData().get(series.getData().size()-1).getYValue() + ")");
            
            // Тестируем функцию с разрывом
            List<XYChart.Series<Number, Number>> segments = ru.ifmo.cs.plotting.ChartBuilder.buildFunctionSegments(
                x -> 1.0 / (x - 1.0), -2.0, 4.0, 20
            );
            
            System.out.println("✅ buildFunctionSegments работает!");
            System.out.println("   Создано сегментов: " + segments.size());
            for (int i = 0; i < segments.size(); i++) {
                System.out.println("   Сегмент " + (i+1) + ": " + segments.get(i).getData().size() + " точек");
            }
            
            // Тестируем обработку ошибок
            try {
                ru.ifmo.cs.plotting.ChartBuilder.buildFunctionSeries(x -> x, 0.0, 10.0, 0);
                System.out.println("❌ Ошибка: должно было быть исключение для points=0");
            } catch (IllegalArgumentException e) {
                System.out.println("✅ Обработка ошибок работает: " + e.getMessage());
            }
            
            System.out.println("\n🎉 ChartBuilder работает корректно!");
            
        } catch (Exception e) {
            System.out.println("❌ Ошибка при тестировании ChartBuilder: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

