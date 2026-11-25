package ru.edu.pr06;

import java.time.LocalDate;
import java.util.Scanner;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class SelfCheck {
    public static void main(String[] args) {
        if (args.length > 0 && args[0].equals("--interactive")) {
            interactive();
            return;
        }
        runChecks();
        Path path = Path.of("data", "sample.csv");
        if (!Files.exists(path)) {
            path = Path.of("pr9", "data", "sample.csv");
        }
        checkCsv(path);
    }

    private static void runChecks() {
        StockItem item = new StockItem("NB-01", "Notebook", Category.COMPUTER, 12);

        if (item.getQuantity() == 12) {
            System.out.println("PASS: начальный остаток");
        } else {
            System.out.println("FAIL: начальный остаток");
        }

        item.increase(5);

        if (item.getQuantity() == 17) {
            System.out.println("PASS: приход");
        } else {
            System.out.println("FAIL: приход");
        }
        //проверка отклонения выдачи
        item.decrease(3);


        try {
            item.decrease(15);
            System.out.println("FAIL: выдали больше, чем есть");
        }catch (IllegalArgumentException e) {
            if (item.getQuantity() == 14) {
                System.out.println("PASS: выдача отклонена, остаток не изменился");
            } else {
                System.out.println("FAIL: ОСТАТОК ИЗМЕНИЛСЯ ПОСЛЕ ОШИБКИ");
            }
        }

        //добавление товара
        Warehouse warehouse = new Warehouse();
        warehouse.addItem(item);

        StockItem duplicate = new StockItem("NB-01", "Запасной ноутбук", Category.COMPUTER, 99);

        try {
            warehouse.addItem(duplicate);
            System.out.println("FAIL: добавили товар с повторным кодом");
        } catch (IllegalArgumentException e){
            System.out.println("PASS: повторный код отклонён");
        }

        if (warehouse.itemsCount() == 1) {
            System.out.println("PASS: на складе одна карточка");
        } else {
            System.out.println("FAIL: ожидали одну карточку");
        }

        try {
            warehouse.addItem(null);
            System.out.println("FAIL: склад принял пустую карточку");
        } catch (IllegalArgumentException e) {
            System.out.println("PASS: склад отклонил пустую карточку");
        }

        warehouse.receive("NB-01", 5);

        if (item.getQuantity() == 19 && warehouse.movementsCount() == 1) {
            System.out.println("PASS: приход записан в первую карточку");
        } else {
            System.out.println("FAIL: остаток первой карточки - " + item.getQuantity());
        }

        warehouse.issue("NB-01", 4, LocalDate.of(2026, 11, 23));

        if (item.getQuantity() == 15 && warehouse.movementsCount() == 2) {
            System.out.println("PASS: выдача со склада");
        }else  {
            System.out.println("FAIL: ожидал 15, получил " + item.getQuantity());
        }

        try {
            warehouse.receive("XX-99", 5);
            System.out.println("FAIL: приняли товар с неизвестным кодом");
        } catch (IllegalArgumentException e) {
            System.out.println("PASS: приход по неизвестному коду отклонён");
        }

        try {
            warehouse.issue("XX-99", 3, LocalDate.of(2026, 11, 23));
            System.out.println("FAIL: выдали товар с неизвестным кодом");
        } catch (IllegalArgumentException e) {
            System.out.println("PASS: выдача по неизвестному коду отклонена");
        }

        //--------------------ИНДИВИДУАЛКА___/_/_/_/))(_()_*)?(?
        try {
            warehouse.issue("NB-01", 1, LocalDate.of(2026, 11, 28));
            System.out.println("FAIL: выдача прошла");
        } catch (IllegalArgumentException e) {
            if (item.getQuantity() == 15 && warehouse.movementsCount() == 2) {
                System.out.println("PASS: в субботу выдача запрещена");
            } else {
                System.out.println("FAIL: изменилась карточка или история");
            }
        }


        try {
            warehouse.issue("NB-01", 1, LocalDate.of(2026, 11, 29));
            System.out.println("FAIL: выдача прошла");
        } catch (IllegalArgumentException e) {
            if (item.getQuantity() == 15 && warehouse.movementsCount() == 2) {
                System.out.println("PASS: в воскресенье выдача запрещена");
            } else {
                System.out.println("FAIL: изменилась карточка или история");
            }
        }

        int received = warehouse.addMovementsByType(warehouse, MovementType.RECEIVE).size();
        if (received == 1) {
            System.out.println("PASS: найден один приход");
        } else {
            System.out.println("FAIL: ожидали один приход, получили " + received);
        }

        int issued = warehouse.addMovementsByType(warehouse, MovementType.ISSUE).size();
        if (issued == 1) {
            System.out.println("PASS: найдена одна выдача");
        } else {
            System.out.println("FAIL: ожидали одну выдачу, получили " + issued);
        }

        warehouse.addItem(new StockItem("PC-01", "Системный блок", Category.COMPUTER, 5));
        warehouse.addItem(new StockItem("MN-01", "Монитор", Category.DISPLAY, 7));

        var computers = warehouse.findByCategory(Category.COMPUTER);

        if (computers.size() == 2) {
            System.out.println("PASS: найдены две карточки COMPUTER");
        } else {
            System.out.println("FAIL: ожидали две карточки COMPUTER");
        }

        if (warehouse.findByCategory(Category.NETWORK).isEmpty()) {
            System.out.println("PASS: для NETWORK список пуст");
        } else {
            System.out.println("FAIL: нашли товар категории NETWORK");
        }

        computers.clear();

        if (warehouse.findByCategory(Category.COMPUTER).size() == 2) {
            System.out.println("PASS: очистка списка не удалила карточки со склада");
        } else {
            System.out.println("FAIL: карточки исчезли со склада");
        }

        var totals = warehouse.totalsByCategory();

        if (totals.getOrDefault(Category.COMPUTER, 0) == 20
                && totals.getOrDefault(Category.DISPLAY, 0) == 7) {
            System.out.println("PASS: сводка по категориям верна");
        } else {
            System.out.println("FAIL: неверная сводка " + totals);
        }




        // 1. Карточка с отрицательным остатком
        try {
            new StockItem("BAD-01", "Ноутбук", Category.COMPUTER, -1);
            System.out.println("FAIL: создали карточку с остатком -1");
        } catch (IllegalArgumentException e) {
            System.out.println("PASS: отрицательный остаток отклонён");
        }

// 2. Приход нуля штук
        StockItem testItem = new StockItem("TEST-01", "Ноутбук", Category.COMPUTER, 12);

        try {
            testItem.increase(0);
            System.out.println("FAIL: приняли 0 штук");
        } catch (IllegalArgumentException e) {
            System.out.println("PASS: приход 0 штук отклонён");
        }

// 3. Обычное уменьшение остатка на карточке
        testItem.decrease(5);

        if (testItem.getQuantity() == 7) {
            System.out.println("PASS: после уменьшения с 12 осталось 7");
        } else {
            System.out.println("FAIL: ожидали остаток 7");
        }

// 4. Склад отклоняет слишком большую выдачу в будний день
        try {
            warehouse.issue("NB-01", 100, LocalDate.of(2026, 11, 23));
            System.out.println("FAIL: склад выдал больше, чем есть");
        } catch (IllegalArgumentException e) {
            if (item.getQuantity() == 15 && warehouse.movementsCount() == 2) {
                System.out.println("PASS: склад отклонил выдачу, остаток и история прежние");
            } else {
                System.out.println("FAIL: изменился остаток или история");
            }
        }
    }

    // Читаем CSV и сверяем каждую строку с ожидаемым результатом.
    private static void checkCsv(Path path) {
        Warehouse warehouse = new Warehouse();
        try {
            var lines = Files.readAllLines(path);
            for (int i = 1; i < lines.size(); i++) {
                String line = lines.get(i);
                if (line.isBlank() || line.startsWith("#")) {
                    continue;
                }
                String[] parts = line.split(";", -1);
                if (parts.length != 4 && parts.length != 5 && parts.length != 7) {
                    System.out.println("FAIL data/" + (i + 1) + " — неверное число полей");
                    continue;
                }
                String expected = "OK:qty=" + parts[3];
                if (parts.length >= 5 && !parts[4].isEmpty()) {
                    expected = parts[4];
                }
                String actual = checkRow(warehouse, parts);
                if (expected.equals(actual)) {
                    System.out.println("PASS data/" + (i + 1) + " — " + actual);
                } else {
                    System.out.println("FAIL data/" + (i + 1) + " — ожидали " + expected + ", получили " + actual);
                }
            }
        } catch (IOException e) {
            System.out.println("FAIL CSV — " + e.getMessage());
        }
    }

    private static String checkRow(Warehouse warehouse, String[] parts) {
        if (parts.length == 7) {
            return checkIssueRow(warehouse, parts);
        }
        for (int i = 0; i < 4; i++) {
            if (!parts[i].equals(parts[i].strip())) {
                return "SKIP:extra spaces";
            }
        }
        Category category;
        try {
            category = Category.valueOf(parts[2]);
        } catch (IllegalArgumentException e) {
            return "SKIP:category not found";
        }
        int quantity;
        try {
            quantity = Integer.parseInt(parts[3]);
        } catch (NumberFormatException e) {
            return "SKIP:quantity not a number";
        }
        if (quantity < 0) {
            return "SKIP:negative quantity";
        }
        try {
            StockItem item = new StockItem(parts[0], parts[1], category, quantity);
            warehouse.addItem(item);
            return "OK:qty=" + item.getQuantity();
        } catch (IllegalArgumentException e) {
            if ("Такой код уже занят".equals(e.getMessage())) {
                return "SKIP:duplicate code";
            }
            return "SKIP:invalid item";
        }
    }

    // Дополнительные строки варианта — выдача с заданной датой.
    private static String checkIssueRow(Warehouse warehouse, String[] parts) {
        if (!parts[5].equals("ISSUE")) {
            return "SKIP:unknown operation";
        }
        try {

            for (StockItem item : warehouse.findByCategory(Category.valueOf(parts[2]))) {
                if (item.getCode().equals(parts[0])) {
                    warehouse.issue(parts[0], Integer.parseInt(parts[3]), LocalDate.parse(parts[6]));
                    return "OK:qty=" + item.getQuantity();
                }
            }
            return "SKIP:code not found";
        } catch (IllegalArgumentException | java.time.DateTimeException e) {
            if ("В выходные склад не выдаёт товар".equals(e.getMessage())) {
                return "SKIP:weekend";
            }
            return "SKIP:invalid issue";
        }
    }

    // Ввод с клавиатуры для показа на защите.
    private static void interactive() {
        Warehouse warehouse = new Warehouse();
        Scanner scanner = new Scanner(System.in);
        System.out.println("Команды: add, receive, issue, find, totals, exit");

        while (scanner.hasNextLine()) {
            String line = scanner.nextLine().trim();
            if (line.equals("exit")) {
                break;
            }
            if (line.isEmpty()) {
                continue;
            }
            String[] parts = line.split("\\s+");
            try {
                switch (parts[0]) {
                    case "add" -> {
                        requireFields(parts, 5);
                        warehouse.addItem(new StockItem(parts[1], parts[2],
                                Category.valueOf(parts[3]), Integer.parseInt(parts[4])));
                        System.out.println("Карточка добавлена");
                    }
                    case "receive" -> {
                        requireFields(parts, 3);
                        warehouse.receive(parts[1], Integer.parseInt(parts[2]));
                        System.out.println("Товар принят");
                    }
                    case "issue" -> {
                        requireFields(parts, 3);
                        warehouse.issue(parts[1], Integer.parseInt(parts[2]));
                        System.out.println("Товар выдан");
                    }
                    case "find" -> {
                        requireFields(parts, 2);
                        for (StockItem item : warehouse.findByCategory(Category.valueOf(parts[1]))) {
                            System.out.println(item.getCode() + " — остаток " + item.getQuantity());
                        }
                    }
                    case "totals" -> {
                        requireFields(parts, 1);
                        System.out.println(warehouse.totalsByCategory());
                    }
                    default -> System.out.println("Неизвестная команда");
                }
            } catch (IllegalArgumentException e) {
                System.out.println("Ошибка: " + e.getMessage());
            }
        }
    }

    private static void requireFields(String[] fields, int count) {
        if (fields.length != count) {
            throw new IllegalArgumentException("Неверное число аргументов команды");
        }
    }
}
