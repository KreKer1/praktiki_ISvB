package ru.edu.pr04;
import java.util.*;
public class Order {
    private final Employee employee; private final List<OrderItem> items=new ArrayList<>(); private OrderStatus status=OrderStatus.DRAFT;
    public Order(Employee employee) { this.employee=Objects.requireNonNull(employee); }
    public void addProduct(Product product,int quantity) {
        if (status == OrderStatus.CONFIRMED) {
            throw new IllegalStateException("Ошибка: заказ уже подтверждён");
        }
        if (product == null) {
            throw new IllegalArgumentException("Ошибка: товар не указан");
        }
        // ищем повтор
        for (OrderItem item : items) {
            String existingId = item.getProduct().getId();
            if (existingId.equals(product.getId())) {
                item.increase(quantity);
                return;
            }
        }
        OrderItem newItem = new OrderItem(product, quantity);
        items.add(newItem);
    }
    // удаление товара
    public void removeProduct(String productId) {
        if (status == OrderStatus.CONFIRMED) {
            throw new IllegalStateException("Ошибка: заказ уже подтверждён");
        }
        for (int i = 0; i < items.size(); i++) {
            OrderItem item = items.get(i);
            String existingId = item.getProduct().getId();
            if (existingId.equals(productId)) {
                items.remove(i);
                return;
            }
        }
    }
    // считаем сумму
    public double total() {
        double sum = 0;
        for (OrderItem item : items) {
            sum = sum + item.getLineTotal();
        }
        return sum;
    }
    // подтверждение заказа
    public void confirm() {
        if (status == OrderStatus.CONFIRMED) {
            throw new IllegalStateException("Ошибка: заказ уже подтверждён");
        }
        if (items.isEmpty()) {
            throw new IllegalStateException("Ошибка: нельзя подтвердить пустой заказ");
        }
        status = OrderStatus.CONFIRMED;
    }
    public List<OrderItem> getItems() { return List.copyOf(items); }
    public OrderStatus getStatus() { return status; }

    // данные сотрудника
    public Employee getEmployee() {
        return employee;
    }
}
