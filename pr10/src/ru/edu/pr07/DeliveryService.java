package ru.edu.pr07;

public class DeliveryService {
    private final DeliveryPolicyFactory factory;

    public DeliveryService(DeliveryPolicyFactory factory) {
        this.factory = factory;
    }

    public double calculate(DeliveryRequest r) {
        DeliveryPolicy policy = factory.get(r.type());
        if (!policy.supports(r)) {
            throw new IllegalArgumentException("Unsupported delivery");
        }
        return policy.calculateCost(r);
    }

    // Вариант 18 — узнаём, можно ли отправить заявку экспрессом.
    public boolean addExpressAvailable(DeliveryRequest r) {
        return factory.get(DeliveryType.EXPRESS).supports(r);
    }
}
