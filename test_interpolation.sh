#!/bin/bash

echo "=== Тестирование программы интерполяции ==="
echo

# Компиляция и запуск тестов
echo "1. Запуск unit-тестов..."
./gradlew test
if [ $? -eq 0 ]; then
    echo "✅ Unit-тесты прошли успешно"
else
    echo "❌ Unit-тесты не прошли"
    exit 1
fi
echo

# Сборка приложения
echo "2. Сборка приложения..."
./gradlew build
if [ $? -eq 0 ]; then
    echo "✅ Сборка прошла успешно"
else
    echo "❌ Ошибка сборки"
    exit 1
fi
echo

# Создание тестовых данных
echo "3. Создание тестовых данных..."

# Тест 1: Нормальные данные
echo "Тест 1: Нормальные данные (sin(x))"
echo "0.0, 0.0" > test_normal.txt
echo "1.0, 0.8415" >> test_normal.txt
echo "2.0, 0.9093" >> test_normal.txt
echo "3.0, 0.1411" >> test_normal.txt
echo "4.0, -0.7568" >> test_normal.txt

# Тест 2: Проблемные данные
echo "Тест 2: Дублирующиеся x-координаты"
echo "1.0, 1.0" > test_duplicate.txt
echo "1.0, 2.0" >> test_duplicate.txt
echo "2.0, 3.0" >> test_duplicate.txt

# Тест 3: Экстремальные значения
echo "Тест 3: Экстремальные значения"
echo "1e-10, 1e10" > test_extreme.txt
echo "1e-5, 1e5" >> test_extreme.txt
echo "1e-3, 1e3" >> test_extreme.txt
echo "1e-1, 1e1" >> test_extreme.txt
echo "1.0, 1.0" >> test_extreme.txt

# Тест 4: NaN и Infinity
echo "Тест 4: NaN и Infinity значения"
echo "0.0, NaN" > test_nan.txt
echo "1.0, 1.0" >> test_nan.txt
echo "2.0, Infinity" >> test_nan.txt
echo "3.0, -Infinity" >> test_nan.txt
echo "4.0, 2.0" >> test_nan.txt

echo "✅ Тестовые данные созданы"
echo

echo "=== Рекомендации по тестированию ==="
echo
echo "1. Запустите программу: ./gradlew run"
echo "2. Протестируйте следующие сценарии:"
echo "   - Загрузите test_normal.txt и выполните интерполяцию"
echo "   - Загрузите test_duplicate.txt (должна появиться ошибка)"
echo "   - Загрузите test_extreme.txt и проверьте корректность"
echo "   - Загрузите test_nan.txt (некорректные точки должны быть пропущены)"
echo "   - Попробуйте сгенерировать функцию ln(x) на интервале [-1, 1]"
echo "   - Попробуйте сгенерировать функцию 1/x на интервале [-1, 1]"
echo "   - Попробуйте интерполировать значение за пределами диапазона данных"
echo "   - Попробуйте ввести некорректные значения (NaN, Infinity) вручную"
echo "   - Попробуйте сгенерировать более 1000 точек"
echo
echo "3. Проверьте отображение графиков:"
echo "   - Графики должны отображаться корректно"
echo "   - Некорректные значения не должны отображаться на графике"
echo "   - Легенда должна быть читаемой"
echo
echo "4. Проверьте таблицу конечных разностей:"
echo "   - NaN и Infinity должны отображаться как текст"
echo "   - Числа должны быть отформатированы корректно"
echo
echo "=== Тестирование завершено ==="

