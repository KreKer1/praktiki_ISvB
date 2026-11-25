package ru.edu.pr03;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Scanner;

import static ru.edu.pr03.ApprovalRoute.*;
import static ru.edu.pr03.RiskLevel.*;

public class SelfCheck {
    private static final ApprovalRouter ROUTER = new ApprovalRouter();
    private static int passed = 0;
    private static int total = 0;

    public static void main(String[] args) {
        // Пересечения правил (route)
        checkRoute("personal + urgent -> COMBINED (вариант 18 вместо SECURITY)", 10_000, true, LOW, true, COMBINED_REVIEW);
        checkRoute("HIGH важнее urgent", 10_000, true, HIGH, false, DIRECTOR_REVIEW);
        checkRoute("personal важнее HIGH", 10_000, false, HIGH, true, SECURITY_REVIEW);
        checkRoute("сумма >= 500000 важнее urgent", 600_000, true, LOW, false, FINANCE_REVIEW);
        // Базовые маршруты (route)
        checkRoute("срочная небольшая -> FAST_TRACK", 10_000, true, LOW, false, FAST_TRACK);
        checkRoute("ровно 500000 -> FINANCE_REVIEW", 500_000, false, LOW, false, FINANCE_REVIEW);
        checkRoute("ничего особенного -> STANDARD", 10_000, false, LOW, false, STANDARD);
        checkRoute("MEDIUM не включает директора", 10_000, false, MEDIUM, false, STANDARD);
        checkRoute("только personal -> SECURITY_REVIEW", 10_000, false, LOW, true, SECURITY_REVIEW);
        // Границы суммы (route)
        checkRoute("499999 срочная -> FAST_TRACK", 499_999, true, LOW, false, FAST_TRACK);
        checkRoute("500001 -> FINANCE_REVIEW", 500_001, false, LOW, false, FINANCE_REVIEW);
        // Защитные условия (route)
        checkError("amount < 0 -> ошибка", -1, LOW, "amount must be >= 0");
        checkError("risk == null -> ошибка", 10_000, null, "risk is null");
        // Вариант 18, часть А: COMBINED_REVIEW (route)
        checkRoute("A: personal + urgent + HIGH + 600000 -> COMBINED", 600_000, true, HIGH, true, COMBINED_REVIEW);
        checkRoute("A: personal без срочности -> по-прежнему SECURITY", 600_000, false, LOW, true, SECURITY_REVIEW);
        checkRoute("A: срочная без personal -> по-прежнему FAST_TRACK", 10_000, true, MEDIUM, false, FAST_TRACK);
        // Вариант 18, часть Б: addCombinedScore
        checkScore("Б: ни personal, ни urgent -> 0", false, false, 0);
        checkScore("Б: только personal -> 100", true, false, 100);
        checkScore("Б: только urgent -> 100", false, true, 100);
        checkScore("Б: personal и urgent -> 200", true, true, 200);

        System.out.println("Unit: " + passed + "/" + total + " passed");

        runDataDriven(Path.of("data", "sample.csv"));
        runInteractive();
    }

    // Режим 2: строки файла "amount;urgent;risk;personal;expected", expected = маршрут или ERROR
    private static void runDataDriven(Path file) {
        List<String> lines;
        try {
            lines = Files.readAllLines(file, StandardCharsets.UTF_8);
        } catch (IOException e) {
            System.out.println("FAIL data: не удалось прочитать " + file + " (" + e.getMessage() + ")");
            return;
        }
        int ok = 0;
        int count = 0;
        boolean header = true;
        for (String line : lines) {
            if (line.isEmpty() || line.startsWith("#")) {
                continue;
            }
            if (header) { // первая значимая строка — заголовок
                header = false;
                continue;
            }
            count++;
            String result = dataResult(line);
            if (result == null) {
                ok++;
                System.out.println("PASS data/" + count);
            } else {
                System.out.println("FAIL data/" + count + ": " + result);
            }
        }
        System.out.println("Data-driven: " + ok + "/" + count + " passed");
    }

    // null — тест-кейс прошёл, иначе причина провала
    private static String dataResult(String line) {
        int cut = line.lastIndexOf(';');
        String expected = line.substring(cut + 1);
        String actual;
        try {
            Request r = parseRequest(line.substring(0, cut));
            actual = ROUTER.route(r.amount(), r.urgent(), r.risk(), r.personal()).name();
        } catch (IllegalArgumentException e) {
            actual = "ERROR";
        }
        return actual.equals(expected) ? null : "[" + line + "] ожидалось " + expected + ", получено " + actual;
    }

    // Режим 3: заявки с консоли до пустой строки или exit
    private static void runInteractive() {
        Scanner in = new Scanner(System.in, StandardCharsets.UTF_8);
        System.out.println("Введите заявку amount;urgent;risk;personal (пустая строка или exit — выход):");
        while (in.hasNextLine()) {
            String line = in.nextLine();
            if (line.isEmpty() || line.equals("exit")) {
                break;
            }
            try {
                Request r = parseRequest(line);
                System.out.println("Маршрут: " + ROUTER.route(r.amount(), r.urgent(), r.risk(), r.personal()));
            } catch (IllegalArgumentException e) {
                System.out.println(e.getMessage());
            }
        }
    }

    private record Request(long amount, boolean urgent, RiskLevel risk, boolean personal) {
    }

    // Строгий разбор строки заявки: ровно 4 поля через ';', без пробелов, регистр важен
    private static Request parseRequest(String line) {
        String[] parts = line.split(";", -1);
        if (parts.length != 4) {
            throw new IllegalArgumentException("Ошибка: ожидалось 4 значения, получено " + parts.length);
        }
        return new Request(parseAmount(parts[0]), parseBool(parts[1], "urgent"),
                parseRisk(parts[2]), parseBool(parts[3], "personal"));
    }

    private static long parseAmount(String text) {
        if (!text.matches("-?[0-9]+")) {
            throw new IllegalArgumentException("Ошибка: amount \"" + text + "\" не является целым числом");
        }
        try {
            return Long.parseLong(text);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Ошибка: amount \"" + text + "\" слишком большое");
        }
    }

    private static boolean parseBool(String text, String field) {
        if (text.equals("true")) {
            return true;
        }
        if (text.equals("false")) {
            return false;
        }
        throw new IllegalArgumentException("Ошибка: " + field + " должно быть true или false, получено \"" + text + "\"");
    }

    // "null" оставлен для проверки защитного условия route
    private static RiskLevel parseRisk(String text) {
        if (text.equals("null")) {
            return null;
        }
        try {
            return RiskLevel.valueOf(text);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Ошибка: risk должен быть LOW, MEDIUM или HIGH, получено \"" + text + "\"");
        }
    }

    private static void checkRoute(String name, long amount, boolean urgent, RiskLevel risk,
                                   boolean personal, ApprovalRoute expected) {
        try {
            ApprovalRoute actual = ROUTER.route(amount, urgent, risk, personal);
            report(name, actual == expected, "ожидалось " + expected + ", получено " + actual);
        } catch (IllegalArgumentException e) {
            report(name, false, "неожиданная ошибка: " + e.getMessage());
        }
    }

    private static void checkError(String name, long amount, RiskLevel risk, String fragment) {
        try {
            ApprovalRoute actual = ROUTER.route(amount, false, risk, false);
            report(name, false, "ожидалась ошибка, получено " + actual);
        } catch (IllegalArgumentException e) {
            report(name, e.getMessage().contains(fragment), "не тот текст ошибки: " + e.getMessage());
        }
    }

    private static void checkScore(String name, boolean personal, boolean urgent, int expected) {
        int actual = ROUTER.addCombinedScore(personal, urgent);
        report(name, actual == expected, "ожидалось " + expected + ", получено " + actual);
    }

    private static void report(String name, boolean ok, String reason) {
        total++;
        if (ok) {
            passed++;
            System.out.println("PASS " + name);
        } else {
            System.out.println("FAIL " + name + ": " + reason);
        }
    }
}