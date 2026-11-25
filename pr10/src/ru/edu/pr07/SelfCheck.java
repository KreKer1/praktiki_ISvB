package ru.edu.pr07;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Scanner;

public class SelfCheck {
    public static void main(String[] args) {
        DeliveryService service = new DeliveryService(new DeliveryPolicyFactory());
        if (args.length > 0 && args[0].equals("--interactive")) {
            interactive(service);
            return;
        }
        runChecks();
        Path path = Path.of("data", "sample.csv");
        if (!Files.exists(path)) {
            path = Path.of("pr10", "data", "sample.csv");
        }
        checkCsv(service, path);
    }

    private static void runChecks() {
        DeliveryPolicyFactory factory = new DeliveryPolicyFactory();
        DeliveryService service = new DeliveryService(factory);
        DeliveryPolicy pickup = new PickupPolicy();
        DeliveryPolicy standard = new StandardCourierPolicy();
        DeliveryPolicy express = new ExpressPolicy();
        DeliveryRequest standard12 = new DeliveryRequest(DeliveryType.STANDARD, 12, 4.5, 12000);
        DeliveryRequest express5 = new DeliveryRequest(DeliveryType.EXPRESS, 5, 5, 4000);
        DeliveryRequest pickup10 = new DeliveryRequest(DeliveryType.PICKUP, 0, 10, 8000);
        DeliveryRequest standardHeavy = new DeliveryRequest(DeliveryType.STANDARD, 12, 25, 12000);
        DeliveryRequest expressLimit = new DeliveryRequest(DeliveryType.EXPRESS, 5, 15, 4000);
        DeliveryRequest expressHeavy = new DeliveryRequest(DeliveryType.EXPRESS, 5, 15.1, 4000);

        check("самовывоз доступен", pickup.supports(pickup10));
        check("самовывоз бесплатный", sameCost(pickup.calculateCost(pickup10), 0));
        check("стандарт: вес 15 кг подходит",
                standard.supports(new DeliveryRequest(DeliveryType.STANDARD, 12, 15, 12000)));
        check("стандарт: другой тип отклонён", !standard.supports(express5));
        check("стандарт: вес 25 кг отклонён", !standard.supports(standardHeavy));
        check("стандарт: цена на 12 км", sameCost(standard.calculateCost(standard12), 600));
        check("стандарт: минимальная цена",
                sameCost(standard.calculateCost(new DeliveryRequest(DeliveryType.STANDARD, 5, 1, 500)), 500));
        check("экспресс: вес 5 кг подходит", express.supports(express5));
        check("экспресс: вес 15 кг подходит по варианту", express.supports(expressLimit));
        check("экспресс: минимальная цена с надбавкой", sameCost(express.calculateCost(express5), 1040));
        check("экспресс: цена на 20 км",
                sameCost(express.calculateCost(new DeliveryRequest(DeliveryType.EXPRESS, 20, 5, 10000)), 2100));
        check("фабрика: стандарт", factory.get(DeliveryType.STANDARD) instanceof StandardCourierPolicy);
        check("фабрика: экспресс", factory.get(DeliveryType.EXPRESS) instanceof ExpressPolicy);
        check("фабрика: самовывоз", factory.get(DeliveryType.PICKUP) instanceof PickupPolicy);
        check("сервис: цена стандарта", sameCost(service.calculate(standard12), 600));
        try {
            service.calculate(standardHeavy);
            check("сервис отклоняет тяжёлый стандарт", false);
        } catch (IllegalArgumentException e) {
            check("сервис отклоняет тяжёлый стандарт", true);
        }
        check("сервис: цена самовывоза", sameCost(service.calculate(pickup10), 0));
        check("сервис: цена экспресса", sameCost(service.calculate(express5), 1040));

        // Вариант 18
        check("А: расчёт экспресса при весе ровно 15 кг", sameCost(service.calculate(expressLimit), 1040));
        check("А: экспресс отклоняет вес больше 15 кг", !express.supports(expressHeavy));
        check("Б: addExpressAvailable для экспресса 15 кг", service.addExpressAvailable(expressLimit));
        check("Б: addExpressAvailable для стандартной заявки", !service.addExpressAvailable(standard12));
    }

    private static void check(String name, boolean ok) {
        System.out.println((ok ? "PASS: " : "FAIL: ") + name);
    }

    private static boolean sameCost(double actual, double expected) {
        return Math.abs(actual - expected) < 0.001;
    }

    // CSV: ожидаем цену или понятную причину пропуска.
    private static void checkCsv(DeliveryService service, Path path) {
        try {
            var lines = Files.readAllLines(path);
            for (int i = 1; i < lines.size(); i++) {
                String line = lines.get(i);
                if (line.isBlank() || line.startsWith("#")) {
                    continue;
                }
                String[] parts = line.split(";", -1);
                String name = "data/" + (i + 1);
                if (parts.length != 5) {
                    System.out.println("FAIL " + name + " — неверное число полей");
                    continue;
                }
                String expected = parts[4];
                DeliveryRequest request;
                try {
                    request = requestFrom(parts);
                } catch (IllegalArgumentException e) {
                    csvResult(name, expected.equals("SKIP:invalid request"), "SKIP:invalid request", expected);
                    continue;
                }

                double cost;
                try {
                    cost = service.calculate(request);
                } catch (IllegalArgumentException e) {
                    csvResult(name, expected.equals("SKIP:weight exceeds limit"), "SKIP:weight exceeds limit", expected);
                    continue;
                }
                boolean matches = false;
                if (expected.startsWith("OK:cost=")) {
                    try {
                        matches = sameCost(cost, Double.parseDouble(expected.substring(8)));
                    } catch (NumberFormatException e) {
                        matches = false;
                    }
                }
                csvResult(name, matches, "OK:cost=" + cost, expected);
            }
        } catch (IOException e) {
            System.out.println("FAIL CSV — " + e.getMessage());
        }
    }

    private static void csvResult(String name, boolean matches, String actual, String expected) {
        if (matches) {
            System.out.println("PASS " + name + " — " + actual);
        } else {
            System.out.println("FAIL " + name + " — ожидали " + expected + ", получили " + actual);
        }
    }

    private static DeliveryRequest requestFrom(String[] parts) {
        DeliveryType type = DeliveryType.valueOf(parts[0]);
        double distance = Double.parseDouble(parts[1]);
        double weight = Double.parseDouble(parts[2]);
        double order = Double.parseDouble(parts[3]);
        if (!Double.isFinite(distance) || !Double.isFinite(weight) || !Double.isFinite(order)
                || distance < 0 || weight < 0 || order < 0) {
            throw new IllegalArgumentException("Введите конечные неотрицательные числа");
        }
        return new DeliveryRequest(type, distance, weight, order);
    }

    private static void interactive(DeliveryService service) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Введите тип, расстояние, вес и сумму заказа через пробел. Для выхода — exit.");
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine().trim();
            if (line.isEmpty() || line.equals("exit")) {
                break;
            }
            String[] parts = line.split("\\s+");
            if (parts.length != 4) {
                System.out.println("Ошибка: нужно четыре поля");
                continue;
            }
            try {
                DeliveryRequest request = requestFrom(parts);
                System.out.println("Стоимость: " + service.calculate(request));
            } catch (IllegalArgumentException e) {
                System.out.println("Ошибка: " + e.getMessage());
            }
        }
    }
}
