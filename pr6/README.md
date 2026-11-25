# Практическая работа 6

Небольшая логическая задача с ветвлениями и конечным набором решений. Вариант 18.

Программа выбирает маршрут согласования служебной заявки по сумме, срочности, уровню риска и наличию персональных данных.

## Правила (от строгого к общему)
0. Вариант 18: персональные данные **и** срочная заявка → `COMBINED_REVIEW`
1. R1: персональные данные → `SECURITY_REVIEW`
2. R2: риск `HIGH` → `DIRECTOR_REVIEW`
3. R3: сумма ≥ `FINANCE_LIMIT` (500 000) → `FINANCE_REVIEW`
4. R4: срочная и сумма < 500 000 → `FAST_TRACK`
5. R5: остальное → `STANDARD`

Сумма < 0 или `risk == null` → `IllegalArgumentException`.

## Файлы
- `src/ru/edu/pr03/ApprovalRouter.java` — метод `route` и `addCombinedScore` (часть Б варианта).
- `src/ru/edu/pr03/ApprovalRoute.java`, `RiskLevel.java` — enum маршрутов и уровней риска.
- `src/ru/edu/pr03/SelfCheck.java` — точка входа: unit-проверки, прогон `data/sample.csv`, ввод заявок с консоли.
- `data/sample.csv` — контрольные значения `amount;urgent;risk;personal;expected`.
- `diagrams/` — Activity-диаграмма и диаграмма классов (`.dot` и `.png`).
- `DECISIONS.md` — принципиальные решения и ответы на контрольные вопросы.

## Сборка и запуск
Запускать из папки `pr6` (там лежит `data/sample.csv`).

```bash
mkdir -p out
javac -encoding UTF-8 -d out $(find src -name "*.java")
java -cp out ru.edu.pr03.SelfCheck
```

На Windows — `run_selfcheck.bat`. В IntelliJ IDEA: правый клик по `SelfCheck` → Run.

После проверок программа ждёт заявки с клавиатуры, например `120000;true;LOW;false` → `Маршрут: FAST_TRACK`. Пустая строка или `exit` — выход.
