package ru.edu.pr04;
import java.nio.file.Files;
import java.nio.file.Path;
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

    static void testProduct() {
        Product product = new Product("M01", "Monitor", 32990);
        check("1. Товар создан: " + product.getName() + ", " + product.getPrice(), product.getPrice() == 32990);

        try {
            new Product(null, "Monitor", 100);
            check("2. id = null → ожидалась ошибка", false);
        } catch (IllegalArgumentException e) {
            check("2. id = null → " + e.getMessage(), true);
        }

        try {
            new Product("", "Monitor", 100);
            check("3. id = \"\" → ожидалась ошибка", false);
        } catch (IllegalArgumentException e) {
            check("3. id = \"\" → " + e.getMessage(), true);
        }

        try {
            new Product("M01", null, 100);
            check("4. name = null → ", false);
        } catch (IllegalArgumentException e) {
            check("4. name = null → " + e.getMessage(), true);
        }

        try {
            new Product("M01", "Monitor", 0);
            check("5. price = 0 → ", false);
        } catch (IllegalArgumentException e) {
            check("5. price = 0 → " + e.getMessage(), true);
        }

        try {
            new Product("M01", "Monitor", -100);
            check("6. price = -100 → ", false);
        } catch (IllegalArgumentException e) {
            check("6. price = -100 → " + e.getMessage(), true);
        }
    }

    static void testOrderItem() {
        Product monitor = new Product("M01", "Monitor", 32990);
        OrderItem item = new OrderItem(monitor, 2);
        check("7. Позиция создана: количество " + item.getQuantity(), item.getQuantity() == 2);
        try {
            new OrderItem(monitor, 0);
            check("8. quantity = 0 → ожидалась ошибка", false);
        } catch (IllegalArgumentException e) {
            check("8. quantity = 0 → " + e.getMessage(), true);
        }
        try {
            new OrderItem(monitor, 101);
            check("9. quantity = 101 → ожидалась ошибка", false);
        } catch (IllegalArgumentException e) {
            check("9. quantity = 101 → " + e.getMessage(), true);
        }
        OrderItem minimumItem = new OrderItem(monitor, 1);
        check("10. Нижняя граница: количество " + minimumItem.getQuantity(), minimumItem.getQuantity() == 1);
        OrderItem maximumItem = new OrderItem(monitor, 100);
        check("11. Верхняя граница: количество " + maximumItem.getQuantity(), maximumItem.getQuantity() == 100);
        OrderItem increasedItem = new OrderItem(monitor, 2);
        increasedItem.increase(3);
        check("12. increase(3) с 2 → " + increasedItem.getQuantity(), increasedItem.getQuantity() == 5);
    }

    static Order newOrder() {
        Address address = new Address("Москва", "ул. Ленина, 1");
        Employee employee = new Employee("E1", "Иванов И.И.", address);
        OrderService service = new OrderService();
        return service.createOrder(employee);
    }

    static void testOrder() {
        Product monitor = new Product("M01", "Monitor", 32990);
        Product keyboard = new Product("K01", "Keyboard", 4490);

        Order twoProductOrder = newOrder();
        twoProductOrder.addProduct(monitor, 2);
        twoProductOrder.addProduct(keyboard, 3);
        check("13. Два разных товара → позиций " + twoProductOrder.getItems().size(), twoProductOrder.getItems().size() == 2);

        Order mergedOrder = newOrder();
        mergedOrder.addProduct(monitor, 2);
        mergedOrder.addProduct(monitor, 1);
        int quantity = mergedOrder.getItems().get(0).getQuantity();
        check("14. Повтор M01 → позиций " + mergedOrder.getItems().size() + ", количество " + quantity,
                mergedOrder.getItems().size() == 1 && quantity == 3);

        double expected = 32990 * 2 + 4490 * 3;
        check("15. total = " + twoProductOrder.total(), twoProductOrder.total() == expected);

        twoProductOrder.confirm();
        check("16. confirm → статус " + twoProductOrder.getStatus(), twoProductOrder.getStatus() == OrderStatus.CONFIRMED);

        try {
            newOrder().confirm();
            check("17. confirm пустого → ожидалась ошибка", false);
        } catch (IllegalStateException e) {
            check("17. confirm пустого → " + e.getMessage(), true);
        }

        try {
            twoProductOrder.addProduct(keyboard, 1);
            check("18. addProduct → ожидалась ошибка", false);
        } catch (IllegalStateException e) {
            check("18. addProduct после confirm → " + e.getMessage(), true);
        }
    }

    // проверка варианта

    static void testVariant() {
        try {
            new Employee("E2", "Петров П.П.", null);
            check("19. Сотрудник без адреса → ожидалась ошибка", false);
        } catch (IllegalArgumentException e) {
            check("19. Сотрудник без адреса → " + e.getMessage(), true);
        }

        try {
            new Address("Москва", "");
            check("20. Адрес без улицы → ожидалась ошибка", false);
        } catch (IllegalArgumentException e) {
            check("20. Адрес без улицы → " + e.getMessage(), true);
        }

        String info = new OrderService().addDeliveryInfo(newOrder());
        check("21. " + info, info.equals("Заказ для Иванов И.И. по адресу Москва, ул. Ленина, 1"));

        Order order = newOrder();
        order.addProduct(new Product("M01", "Monitor", 32990), 1);
        order.confirm();
        String info2 = new OrderService().addDeliveryInfo(order);
        check("22. После confirm: " + info2, info2.contains("Москва"));
    }

    // чтение файла

    static void runCsv() throws Exception {
        List<String> lines = Files.readAllLines(Path.of("data/sample.csv"));
        Order order = newOrder();
        for (int i = 1; i < lines.size(); i++) {
            String line = lines.get(i);
            if (line.isBlank() || line.startsWith("#")) {
                continue;
            }
            String[] fields = line.split(";", -1);
            if (fields[0].equals("TOTAL")) {
                int positionCount = Integer.parseInt(fields[1].replace("positions=", ""));
                double sum = Double.parseDouble(fields[2].replace("sum=", ""));
                check("data/" + i + " итог: " + order.getItems().size() + " позиций, " + order.total(),
                        order.getItems().size() == positionCount && order.total() == sum);
                continue;
            }
            String expected = fields[4];
            if (fields[0].equals("ADDRESS")) {
                try {
                    new Address(fields[1], fields[2]);
                    check("data/" + i + " адрес создан", expected.equals("OK"));
                } catch (IllegalArgumentException e) {
                    check("data/" + i + " " + e.getMessage(), expected.startsWith("SKIP"));
                }
                continue;
            }
            try {
                double price = Double.parseDouble(fields[2]);
                int addedQuantity = Integer.parseInt(fields[3]);
                Product product = new Product(fields[0], fields[1], price);
                order.addProduct(product, addedQuantity);
                int quantity = 0;
                for (OrderItem item : order.getItems()) {
                    if (item.getProduct().getId().equals(fields[0])) {
                        quantity = item.getQuantity();
                    }
                }
                check("data/" + i + " " + fields[0] + " qty=" + quantity, expected.endsWith("qty=" + quantity));
            } catch (IllegalArgumentException e) {
                check("data/" + i + " " + fields[0] + " пропущен → " + e.getMessage(), expected.startsWith("SKIP"));
            }
        }
    }

    // ввод команд

    static void interactive() {
        Scanner scanner = new Scanner(System.in);
        Order order = newOrder();
        System.out.println("Команды: add id название цена кол-во | remove id | total | confirm | info | exit");

        while (true) {
            System.out.print("> ");
            if (!scanner.hasNextLine()) {
                break;
            }
            String line = scanner.nextLine().trim();
            if (line.isEmpty() || line.equals("exit")) {
                break;
            }
            String[] parts = line.split("\\s+");
            try {
                String command = parts[0];
                if (command.equals("add")) {
                    double price = Double.parseDouble(parts[3]);
                    int quantity = Integer.parseInt(parts[4]);
                    Product product = new Product(parts[1], parts[2], price);
                    order.addProduct(product, quantity);
                } else if (command.equals("remove")) {
                    order.removeProduct(parts[1]);
                } else if (command.equals("confirm")) {
                    order.confirm();
                } else if (command.equals("info")) {
                    OrderService service = new OrderService();
                    System.out.println(service.addDeliveryInfo(order));
                } else if (!command.equals("total")) {
                    System.out.println("Ошибка: неизвестная команда");
                }
                System.out.println("Позиций: " + order.getItems().size() + ", сумма: " + order.total() + ", статус: " + order.getStatus());
            } catch (NumberFormatException e) {
                System.out.println("Ошибка: цена и количество должны быть числами");
            } catch (ArrayIndexOutOfBoundsException e) {
                System.out.println("Ошибка: не хватает данных в команде");
            } catch (IllegalArgumentException | IllegalStateException e) {
                System.out.println(e.getMessage());
            }
        }
    }

    static void demo() {
        Product product = new Product("M01", "Monitor", 32990);

        try {
            new OrderItem(product, 0);
            System.out.println("quantity = 0 → создан (ОШИБКА В ЛОГИКЕ)");
        } catch (IllegalArgumentException e) {
            System.out.println("quantity = 0 → " + e.getMessage());
        }

        try {
            new OrderItem(product, 101);
            System.out.println("quantity = 101 → создан (ОШИБКА В ЛОГИКЕ)");
        } catch (IllegalArgumentException e) {
            System.out.println("quantity = 101 → " + e.getMessage());
        }
        System.out.println("quantity = 1 → создан, количество " + new OrderItem(product, 1).getQuantity());
        System.out.println("quantity = 100 → создан, количество " + new OrderItem(product, 100).getQuantity());

        Order order = newOrder();
        order.addProduct(product, 2);
        System.out.println("add M01 x2 → позиций " + order.getItems().size()
                + ", количество " + order.getItems().get(0).getQuantity());
        order.addProduct(new Product("M01", "Monitor", 32990), 1);
        System.out.println("add M01 x1 → позиций " + order.getItems().size()
                + ", количество " + order.getItems().get(0).getQuantity());
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
        testProduct();
        testOrderItem();
        testOrder();
        testVariant();
        runCsv();
        System.out.println("Итого: PASS=" + passed + ", FAIL=" + failed);
        if (failed > 0) {
            System.exit(1);
        }
    }
}
