package ru.edu.pr03;

public class ApprovalRouter {
    public static final long FINANCE_LIMIT = 500_000L;
    public ApprovalRoute route(long amount, boolean urgent, RiskLevel risk, boolean containsPersonalData) {
        checkArguments(amount, risk);
        if (requiresCombinedReview(containsPersonalData, urgent)) { // вариант 18: строже R1
            return ApprovalRoute.COMBINED_REVIEW;
        }
        if (requiresSecurityReview(containsPersonalData)) {   // R1
            return ApprovalRoute.SECURITY_REVIEW;
        }
        if (requiresDirectorReview(risk)) {                   // R2
            return ApprovalRoute.DIRECTOR_REVIEW;
        }
        if (requiresFinanceReview(amount)) {                  // R3
            return ApprovalRoute.FINANCE_REVIEW;
        }
        if (requiresFastTrack(amount, urgent)) {              // R4
            return ApprovalRoute.FAST_TRACK;
        }
        return ApprovalRoute.STANDARD;                        // R5
    }

    // вариант 18 часть Б:
    public int addCombinedScore(boolean containsPersonalData, boolean urgent) {
        int score = 0;
        if (containsPersonalData) {
            score += 100;
        }
        if (urgent) {
            score += 100;
        }
        return score;
    }

    private boolean requiresCombinedReview(boolean containsPersonalData, boolean urgent) {
        return containsPersonalData && urgent;
    }

    private boolean requiresSecurityReview(boolean containsPersonalData) {
        return containsPersonalData;
    }

    private boolean requiresDirectorReview(RiskLevel risk) {
        return risk == RiskLevel.HIGH;
    }

    private boolean requiresFinanceReview(long amount) {
        return amount >= FINANCE_LIMIT;
    }

    private boolean requiresFastTrack(long amount, boolean urgent) {
        return urgent && amount < FINANCE_LIMIT;
    }

    // Защитные условия | отсекают ошибки до проверки правил
    private void checkArguments(long amount, RiskLevel risk) {
        if (amount < 0) {
            throw new IllegalArgumentException("Ошибка: amount must be >= 0, got " + amount);
        }
        if (risk == null) {
            throw new IllegalArgumentException("Ошибка: risk is null");
        }
    }
}