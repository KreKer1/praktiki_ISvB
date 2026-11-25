package ru.edu.pr07;

public class StandardCourierPolicy implements DeliveryPolicy {
    @Override
    public boolean supports(DeliveryRequest r) {
        return r.type() == DeliveryType.STANDARD && r.weightKg() <= 20;
    }

    @Override
    public double calculateCost(DeliveryRequest r) {
        return Math.max(500, r.distanceKm() * 50);
    }
}
