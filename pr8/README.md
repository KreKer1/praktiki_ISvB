# Практическая работа №8. Инкапсуляция, enum и управление состоянием объекта в Java

Соколов Г.А., группа ЭФБО-12-25, вариант 18.

Заявка на командировку (`BusinessTrip`) проходит статусы DRAFT → SUBMITTED → APPROVED → COMPLETED
(или REJECTED / CANCELLED). Статус меняется только доменными методами `submit`, `approve`, `reject`,
`cancel`, `complete`, каждый проверяет допустимость перехода; setter статуса нет.
Вариант 18: `approve(String approver)` сохраняет руководителя в поле `approvedBy`,
`BusinessTripService.addApprover(BusinessTrip)` возвращает его имя или `null`.

## Структура

```
src/ru/edu/pr05/
  TripStatus.java           enum статусов
  BusinessTrip.java         заявка: конструктор (TODO 1) и 5 доменных методов (TODO 2–6), вариант 18
  BusinessTripService.java  create, findByStatus, addApprover
  SelfCheck.java            23 unit-проверки, data-driven режим, интерактивный режим
data/sample.csv             данные для data-driven режима
diagrams/                   activity и class (.dot и .png)
DECISIONS.md                вариант, решения, ответы на вопросы, анализ фрагмента
```

## Сборка и запуск (из папки pr8)

```bash
javac -encoding UTF-8 -d out $(find src -name "*.java")
java -cp out ru.edu.pr05.SelfCheck
```

Windows: `run_selfcheck.bat`. Ожидаемый результат: `Итого: PASS=43, FAIL=0`
(23 unit-проверки + 20 строк из `data/sample.csv`).

Формат `sample.csv`: `id;start;end;action;expected`. В `action` можно указать цепочку шагов через `>`
с аргументом после `:`, например `SUBMIT>APPROVE:Петров П.П.>COMPLETE:2026-10-12`.
`expected`: `OK:status=X`, `ERROR:IllegalStateException`, `ERROR:IllegalArgumentException`, `SKIP:<причина>`.

Интерактивный режим:

```bash
java -cp out ru.edu.pr05.SelfCheck --interactive
```

Команды: `create T-01 2026-10-10 2026-10-12`, `submit`, `approve Петров П.П.`, `reject <причина>`,
`cancel`, `complete 2026-10-12`, `status`, `find APPROVED`, `exit` (или пустая строка).
