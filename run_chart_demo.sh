#!/bin/bash

echo "Запуск демонстрации ChartBuilder..."
echo "=================================="

# Проверяем, что JavaFX доступен
if [ ! -d "lib" ]; then
    echo "Ошибка: Папка lib не найдена. Убедитесь, что JavaFX установлен."
    exit 1
fi

# Проверяем, что классы скомпилированы
if [ ! -f "build/classes/java/main/ru/ifmo/cs/ChartBuilderDemo.class" ]; then
    echo "Компилируем проект..."
    ./gradlew compileJava
fi

# Запускаем демонстрацию
echo "Запуск GUI демонстрации ChartBuilder..."
java --module-path lib --add-modules javafx.controls,javafx.fxml -cp build/classes/java/main ru.ifmo.cs.ChartBuilderDemo

echo "Демонстрация завершена."
