package ru.edu.pr04;
public class OrderService { public Order createOrder(Employee e) { return new Order(e); } 

    // адрес доставки
    public String addDeliveryInfo(Order order) {
        Employee employee = order.getEmployee();
        Address address = employee.address();
        String fullName = employee.fullName();
        String city = address.city();
        String street = address.street();
        return "Заказ для " + fullName + " по адресу " + city + ", " + street;
    }
}
