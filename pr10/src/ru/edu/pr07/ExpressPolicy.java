package ru.edu.pr07;

public class ExpressPolicy implements DeliveryPolicy {
    @Override
    public boolean supports(DeliveryRequest r) { // Вариант 18 — экспресс до 15 кг включительно.
        return r.type() == DeliveryType.EXPRESS && r.weightKg() <= 15;
    }

    @Override
    public double calculateCost(DeliveryRequest r) {
        return Math.max(1000, r.distanceKm() * 100) + r.orderAmount() * 0.01;
    }
}
