package ru.edu.pr04;
public class OrderItem {
    private final Product product; private int quantity;
    public OrderItem(Product product,int quantity) {
        if (product == null) {
            throw new IllegalArgumentException("Ошибка: товар не указан");
        }
        this.product=product; setQuantity(quantity);
    }
    // проверка количества
    private void setQuantity(int quantity) {
        if (quantity < 1 || quantity > 100) {
            throw new IllegalArgumentException("Ошибка: количество должно быть от 1 до 100");
        }
        this.quantity=quantity; }
    // добавляем количество
    public void increase(int delta) {
        if (delta <= 0) {
            throw new IllegalArgumentException("Ошибка: добавляемое количество должно быть больше 0");
        }
        int newQuantity = quantity + delta;
        setQuantity(newQuantity);
    }
    public Product getProduct() { return product; } public int getQuantity() { return quantity; }
    // стоимость позиции
    public double getLineTotal() {
        double price = product.getPrice();
        return price * quantity;
    }
}
