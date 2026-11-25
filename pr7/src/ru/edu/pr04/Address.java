package ru.edu.pr04;

// адрес доставки
public record Address(String city, String street) {
    public Address {
        if (city == null || city.isBlank()) {
            throw new IllegalArgumentException("Ошибка: город не указан");
        }
        if (street == null || street.isBlank()) {
            throw new IllegalArgumentException("Ошибка: улица не указана");
        }
    }
}
