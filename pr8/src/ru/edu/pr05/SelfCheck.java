package ru.edu.pr05;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.Scanner;

public class SelfCheck {

    static int passed = 0;
    static int failed = 0;

    static void check(String name, boolean ok) {
        if (ok) {
            passed++;
            System.out.println("PASS " + name);
        } else {
            failed++;
            System.out.println("FAIL " + name);
        }
    }

    static LocalDate parseDate(String text) {
        return LocalDate.parse(text);
    }

    // новая заявка
    static BusinessTrip newTrip() {
        return new BusinessTrip("T-01", parseDate("2026-10-10"), parseDate("2026-10-12"));
    }

    // одобренная заявка
    static BusinessTrip approvedTrip() {
        BusinessTrip trip = newTrip();
        trip.submit();
        trip.approve("Петров П.П.");
        return trip;
    }

    static void testConstructor() {
        BusinessTrip trip = newTrip();
        check("1. Заявка создана: " + trip.getId() + ", статус " + trip.getStatus(), trip.getStatus() == TripStatus.DRAFT);

        try {
            new BusinessTrip(null, parseDate("2026-10-10"), parseDate("2026-10-12"));
            check("2. id = null → ожидалась ошибка", false);
        } catch (IllegalArgumentException e) {
            check("2. id = null → " + e.getMessage(), true);
        }

        try {
            new BusinessTrip("", parseDate("2026-10-10"), parseDate("2026-10-12"));
            check("3. id = \"\" → ожидалась ошибка", false);
        } catch (IllegalArgumentException e) {
            check("3. id = \"\" → " + e.getMessage(), true);
        }

        try {
            new BusinessTrip("T-02", null, parseDate("2026-10-12"));
            check("4. start = null → ожидалась ошибка", false);
        } catch (IllegalArgumentException e) {
            check("4. start = null → " + e.getMessage(), true);
        }

        try {
            new BusinessTrip("T-03", parseDate("2026-10-12"), parseDate("2026-10-10"));
            check("5. start > end → ожидалась ошибка", false);
        } catch (IllegalArgumentException e) {
            check("5. start > end → " + e.getMessage(), true);
        }
    }

    static void testTransitions() {
        // отправка заявки
        BusinessTrip submittedTrip = newTrip();
        submittedTrip.submit();
        check("6. submit из DRAFT → " + submittedTrip.getStatus(), submittedTrip.getStatus() == TripStatus.SUBMITTED);

        // повторная отправка
        try {
            submittedTrip.submit();
            check("7. submit из SUBMITTED → ожидалась ошибка", false);
        } catch (IllegalStateException e) {
            check("7. submit из SUBMITTED → " + e.getMessage(), true);
        }

        // одобрение заявки
        submittedTrip.approve("Петров П.П.");
        check("8. approve из SUBMITTED → " + submittedTrip.getStatus(), submittedTrip.getStatus() == TripStatus.APPROVED);

        // неверный статус
        try {
            newTrip().approve("Петров П.П.");
            check("9. approve из DRAFT → ожидалась ошибка", false);
        } catch (IllegalStateException e) {
            check("9. approve из DRAFT → " + e.getMessage(), true);
        }

        // достаточная причина
        BusinessTrip rejectedTrip = newTrip();
        rejectedTrip.submit();
        rejectedTrip.reject("Нет бюджет");
        check("10. reject(10 символов) → " + rejectedTrip.getStatus(), rejectedTrip.getStatus() == TripStatus.REJECTED);

        // короткая причина
        BusinessTrip shortReasonTrip = newTrip();
        shortReasonTrip.submit();
        try {
            shortReasonTrip.reject("Нет");
            check("11. reject(3 символа) → ожидалась ошибка", false);
        } catch (IllegalArgumentException e) {
            check("11. reject(3 символа) → " + e.getMessage() + ", статус " + shortReasonTrip.getStatus(),
                    shortReasonTrip.getStatus() == TripStatus.SUBMITTED);
        }

        // неверный статус
        try {
            newTrip().reject("Причина отклонения");
            check("12. reject из DRAFT → ожидалась ошибка", false);
        } catch (IllegalStateException e) {
            check("12. reject из DRAFT → " + e.getMessage(), true);
        }

        // отмена черновика
        BusinessTrip cancelledTrip = newTrip();
        cancelledTrip.cancel();
        check("13. cancel из DRAFT → " + cancelledTrip.getStatus(), cancelledTrip.getStatus() == TripStatus.CANCELLED);

        // запрет отмены
        BusinessTrip completedTrip = approvedTrip();
        completedTrip.complete(parseDate("2026-10-12"));
        try {
            completedTrip.cancel();
            check("14. cancel из COMPLETED → ожидалась ошибка", false);
        } catch (IllegalStateException e) {
            check("14. cancel из COMPLETED → " + e.getMessage(), true);
        }

        // день окончания
        BusinessTrip lastDayTrip = approvedTrip();
        lastDayTrip.complete(parseDate("2026-10-12"));
        check("15. complete(сегодня = end) → " + lastDayTrip.getStatus(), lastDayTrip.getStatus() == TripStatus.COMPLETED);

        // неверный статус
        BusinessTrip notApprovedTrip = newTrip();
        notApprovedTrip.submit();
        try {
            notApprovedTrip.complete(parseDate("2026-10-12"));
            check("16. complete из SUBMITTED → ожидалась ошибка", false);
        } catch (IllegalStateException e) {
            check("16. complete из SUBMITTED → " + e.getMessage(), true);
        }

        // раннее завершение
        BusinessTrip unfinishedTrip = approvedTrip();
        try {
            unfinishedTrip.complete(parseDate("2026-10-11"));
            check("17. complete(сегодня < end) → ожидалась ошибка", false);
        } catch (IllegalStateException e) {
            check("17. complete(сегодня < end) → " + e.getMessage(), true);
        }

        // сохранённая причина
        check("18. getRejectionReason → " + rejectedTrip.getRejectionReason(), "Нет бюджет".equals(rejectedTrip.getRejectionReason()));
    }

    static void testVariant() {
        BusinessTripService service = new BusinessTripService();

        // пустое имя
        BusinessTrip noApproverTrip = newTrip();
        noApproverTrip.submit();
        try {
            noApproverTrip.approve("");
            check("19. approve(\"\") → ожидалась ошибка", false);
        } catch (IllegalArgumentException e) {
            check("19. approve(\"\") → " + e.getMessage() + ", статус " + noApproverTrip.getStatus(),
                    noApproverTrip.getStatus() == TripStatus.SUBMITTED);
        }

        // имя отсутствует
        try {
            noApproverTrip.approve(null);
            check("20. approve(null) → ожидалась ошибка", false);
        } catch (IllegalArgumentException e) {
            check("20. approve(null) → " + e.getMessage(), true);
        }

        // имя руководителя
        BusinessTrip approvedTrip = approvedTrip();
        check("21. addApprover после approve → " + service.addApprover(approvedTrip),
                "Петров П.П.".equals(service.addApprover(approvedTrip)));

        // без одобрения
        check("22. addApprover без approve → " + service.addApprover(newTrip()), service.addApprover(newTrip()) == null);

        // поиск заявок
        service.create("T-10", parseDate("2026-10-10"), parseDate("2026-10-12"));
        BusinessTrip secondTrip = service.create("T-11", parseDate("2026-10-15"), parseDate("2026-10-20"));
        secondTrip.submit();
        List<BusinessTrip> drafts = service.findByStatus(TripStatus.DRAFT);
        check("23. findByStatus(DRAFT) → " + drafts.size() + " заявка, findByStatus(SUBMITTED) → "
                        + service.findByStatus(TripStatus.SUBMITTED).size(),
                drafts.size() == 1 && service.findByStatus(TripStatus.SUBMITTED).size() == 1);
    }

    // чтение файла
    // цепочка действий
    static void runCsv() throws Exception {
        List<String> lines = Files.readAllLines(Path.of("data/sample.csv"));
        for (int i = 1; i < lines.size(); i++) {
            String line = lines.get(i);
            if (line.isBlank() || line.startsWith("#")) {
                continue;
            }
            String[] fields = line.split(";", -1);
            String expected = fields[4];

            BusinessTrip trip;
            try {
                trip = new BusinessTrip(fields[0], parseDate(fields[1]), parseDate(fields[2]));
            } catch (IllegalArgumentException e) {
                check("data/" + i + " " + fields[0] + " пропущена → " + e.getMessage(), expected.startsWith("SKIP"));
                continue;
            }

            try {
                for (String step : fields[3].split(">")) {
                    doAction(trip, step);
                }
                boolean ok = expected.startsWith("OK:status=" + trip.getStatus());
                if (expected.contains("approver=")) {
                    ok = ok && expected.endsWith("approver=" + trip.getApprovedBy());
                }
                check("data/" + i + " " + fields[0] + " " + fields[3] + " → " + trip.getStatus(), ok);
            } catch (IllegalStateException e) {
                check("data/" + i + " " + fields[0] + " " + fields[3] + " → " + e.getMessage(),
                        expected.equals("ERROR:IllegalStateException"));
            } catch (IllegalArgumentException e) {
                check("data/" + i + " " + fields[0] + " " + fields[3] + " → " + e.getMessage(),
                        expected.equals("ERROR:IllegalArgumentException"));
            }
        }
    }

    // одно действие
    static void doAction(BusinessTrip trip, String step) {
        String[] parts = step.split(":", 2);
        String action = parts[0];
        String argument = null;
        if (parts.length > 1) {
            argument = parts[1];
        }

        if (action.equals("SUBMIT")) {
            trip.submit();
        } else if (action.equals("APPROVE")) {
            if (argument == null) {
                argument = "Петров П.П.";
            }
            trip.approve(argument);
        } else if (action.equals("REJECT")) {
            if (argument == null) {
                argument = "Причина отклонения заявки";
            }
            trip.reject(argument);
        } else if (action.equals("CANCEL")) {
            trip.cancel();
        } else if (action.equals("COMPLETE")) {
            LocalDate today = trip.getEnd();
            if (argument != null) {
                today = parseDate(argument);
            }
            trip.complete(today);
        } else {
            throw new IllegalArgumentException("Ошибка: неизвестное действие " + action);
        }
    }

    static void interactive() {
        Scanner scanner = new Scanner(System.in);
        BusinessTripService service = new BusinessTripService();
        BusinessTrip trip = null;
        System.out.println("Команды: create id начало конец | submit | approve имя | reject причина | cancel | complete дата | status | find СТАТУС | exit");

        while (true) {
            System.out.print("> ");
            if (!scanner.hasNextLine()) {
                break;
            }
            String line = scanner.nextLine().trim();
            if (line.isEmpty() || line.equals("exit")) {
                break;
            }
            String[] parts = line.split(" ", 2);
            String argument = "";
            if (parts.length > 1) {
                argument = parts[1];
            }
            try {
                if (parts[0].equals("create")) {
                    String[] arguments = argument.split("\\s+");
                    trip = service.create(arguments[0], parseDate(arguments[1]), parseDate(arguments[2]));
                } else if (parts[0].equals("find")) {
                    TripStatus status;
                    try {
                        status = TripStatus.valueOf(argument);
                    } catch (IllegalArgumentException e) {
                        System.out.println("Ошибка: нет такого статуса, есть DRAFT, SUBMITTED, APPROVED, REJECTED, CANCELLED, COMPLETED");
                        continue;
                    }
                    for (BusinessTrip foundTrip : service.findByStatus(status)) {
                        System.out.println("  " + foundTrip.getId() + " " + foundTrip.getStart() + ".." + foundTrip.getEnd());
                    }
                    continue;
                } else if (trip == null) {
                    System.out.println("Ошибка: сначала создайте заявку командой create");
                    continue;
                } else {
                    String command = parts[0];
                    if (command.equals("submit")) {
                        trip.submit();
                    } else if (command.equals("approve")) {
                        trip.approve(argument);
                    } else if (command.equals("reject")) {
                        trip.reject(argument);
                    } else if (command.equals("cancel")) {
                        trip.cancel();
                    } else if (command.equals("complete")) {
                        trip.complete(parseDate(argument));
                    } else if (!command.equals("status")) {
                        System.out.println("Ошибка: неизвестная команда");
                    }
                }
                System.out.println("Заявка " + trip.getId() + ": статус " + trip.getStatus());
                if (trip.getApprovedBy() != null) {
                    System.out.println("Одобрил: " + trip.getApprovedBy());
                }
                if (trip.getRejectionReason() != null) {
                    System.out.println("Причина: " + trip.getRejectionReason());
                }
            } catch (java.time.format.DateTimeParseException e) {
                System.out.println("Ошибка: дата должна быть в формате ГГГГ-ММ-ДД");
            } catch (ArrayIndexOutOfBoundsException e) {
                System.out.println("Ошибка: не хватает данных в команде");
            } catch (IllegalArgumentException | IllegalStateException e) {
                System.out.println(e.getMessage());
            }
        }
    }

    static void demo() {

        BusinessTrip trip = new BusinessTrip("T-01", LocalDate.parse("2026-10-10"), LocalDate.parse("2026-10-12"));
        System.out.println("start = " + trip.getStart() + ", end = " + trip.getEnd());
        System.out.println("start.isAfter(end)  = " + trip.getStart().isAfter(trip.getEnd()));
        System.out.println("start.isBefore(end) = " + trip.getStart().isBefore(trip.getEnd()));
        try {
            new BusinessTrip("T-02", LocalDate.parse("2026-10-12"), LocalDate.parse("2026-10-10"));
            System.out.println("start > end → создана (ОШИБКА В ЛОГИКЕ)");
        } catch (IllegalArgumentException e) {
            System.out.println("start > end → " + e.getMessage());
        }
        BusinessTrip oneDay = new BusinessTrip("T-03", LocalDate.parse("2026-10-10"), LocalDate.parse("2026-10-10"));
        System.out.println("start = end → создана, статус " + oneDay.getStatus());

        BusinessTrip t9 = newTrip();
        t9.submit();
        String reason9 = "Нет бюдже";
        try {
            t9.reject(reason9);
            System.out.println("9 символов → " + t9.getStatus() + " (ОШИБКА В ЛОГИКЕ)");
        } catch (IllegalArgumentException e) {
            System.out.println("\"" + reason9 + "\" (" + reason9.length() + " симв.) → " + e.getMessage()
                    + ", статус " + t9.getStatus());
        }
        BusinessTrip rejectedTrip = newTrip();
        rejectedTrip.submit();
        String reason10 = "Нет бюджет";
        rejectedTrip.reject(reason10);
        System.out.println("\"" + reason10 + "\" (" + reason10.length() + " симв.) → статус " + rejectedTrip.getStatus()
                + ", причина: " + rejectedTrip.getRejectionReason());
    }

    public static void main(String[] args) throws Exception {
        if (args.length > 0 && args[0].equals("--interactive")) {
            interactive();
            return;
        }
        if (args.length > 0 && args[0].equals("--demo")) {
            demo();
            return;
        }
        testConstructor();
        testTransitions();
        testVariant();
        runCsv();
        System.out.println("Итого: PASS=" + passed + ", FAIL=" + failed);
        if (failed > 0) {
            System.exit(1);
        }
    }
}
