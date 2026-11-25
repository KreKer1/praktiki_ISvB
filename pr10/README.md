# Практическая работа №10

Интерфейсы, полиморфизм и расчёт доставки. Вариант 18.

## Запуск

Нужен JDK 17 или новее. Открой папку pr10 в терминале.

| Режим | Windows | Linux или macOS |
|---|---|---|
| Проверки кода и CSV | ` .\run_selfcheck.bat ` | `bash run_selfcheck.sh` |
| Ввод заявки | ` .\run_selfcheck.bat --interactive ` | `bash run_selfcheck.sh --interactive` |

В IntelliJ запускай `ru.edu.pr07.SelfCheck`. Рабочая папка — `pr10` или корень `repo_push`. Для ввода добавь `--interactive` в Program arguments.

## Где что лежит

- `DeliveryRequest.java` — тип, расстояние, вес и сумма заказа.
- `DeliveryPolicy.java` — общие методы для способов доставки.
- `StandardCourierPolicy.java`, `ExpressPolicy.java`, `PickupPolicy.java` — условия и цены.
- `DeliveryPolicyFactory.java` — выбирает класс по типу.
- `DeliveryService.java` — вызывает выбранный класс. Здесь же `addExpressAvailable`.
- `SelfCheck.java` — 18 базовых проверок, 4 проверки варианта, CSV и консоль.
- [Диаграммы](diagrams), [решения](DECISIONS.md), [отчёт](Отчёт_ПР10.docx).

## Что показать на защите

```text
EXPRESS 5 15 4000
EXPRESS 5 15.1 4000
STANDARD 12 1 0
exit
```

Первый запрос стоит 1040, второй отклоняется, третий стоит 600. Для нового способа доставки понадобятся значение в DeliveryType, новый класс с DeliveryPolicy и ещё одна ветка в фабрике. Этот пример можно показать на временной копии проекта.
