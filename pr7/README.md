# Практическая работа №7. ООП в Java: классы, объекты, инкапсуляция и заказ оборудования

Соколов Г.А., группа ЭФБО-12-25, вариант 18.

Консольное приложение формирует заказ оборудования сотрудника: проверяет товары и количество,
объединяет повторы, считает итог, запрещает менять подтверждённый заказ.
Вариант 18: у сотрудника есть адрес доставки (`record Address`), `OrderService.addDeliveryInfo(Order)`
возвращает строку «Заказ для … по адресу …».

## Структура

```
src/ru/edu/pr04/
  Product.java       товар, проверки id/name/price (TODO 1)
  OrderItem.java     позиция заказа, количество 1–100 (TODO 2–4)
  Order.java         заказ: addProduct, removeProduct, total, confirm (TODO 5–8)
  Employee.java      record сотрудника + адрес (вариант 18, часть А)
  Address.java       record адреса (вариант 18, часть А)
  OrderStatus.java   enum DRAFT / CONFIRMED
  OrderService.java  создание заказа + addDeliveryInfo (вариант 18, часть Б)
  SelfCheck.java     22 unit-проверки, data-driven режим, интерактивный режим
data/sample.csv      данные для data-driven режима
diagrams/            activity и class (.dot и .png)
DECISIONS.md         вариант, решения, ответы на вопросы, анализ фрагмента
```

## Сборка и запуск (из папки pr7)

```bash
javac -encoding UTF-8 -d out $(find src -name "*.java")
java -cp out ru.edu.pr04.SelfCheck
```

Windows: `run_selfcheck.bat`. Ожидаемый результат: `Итого: PASS=38, FAIL=0`
(22 unit-проверки + 16 строк из `data/sample.csv`).

Интерактивный режим:

```bash
java -cp out ru.edu.pr04.SelfCheck --interactive
```

Команды: `add <id> <название> <цена> <кол-во>`, `remove <id>`, `total`, `confirm`, `info`, `exit` (или пустая строка).
