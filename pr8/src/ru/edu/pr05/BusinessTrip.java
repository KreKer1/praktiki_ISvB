package ru.edu.pr05;
import java.time.*;
public class BusinessTrip {
    private final String id; private final LocalDate start; private final LocalDate end; private TripStatus status=TripStatus.DRAFT; private String rejectionReason;
    private String approvedBy;

    public BusinessTrip(String id, LocalDate start, LocalDate end) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Ошибка: id заявки пустой");
        }
        if (start == null || end == null) {
            throw new IllegalArgumentException("Ошибка: даты не указаны");
        }
        if (start.isAfter(end)) {
            throw new IllegalArgumentException("Ошибка: дата начала позже даты окончания");
        }
        this.id=id; this.start=start; this.end=end;
    }
    // отправка заявки
    public void submit() {
        if (status != TripStatus.DRAFT) {
            throw new IllegalStateException("Ошибка перехода: " + status + " → SUBMITTED недопустим");
        }
        status = TripStatus.SUBMITTED;
    }
    // одобрение заявки
    public void approve(String approver) {
        if (status != TripStatus.SUBMITTED) {
            throw new IllegalStateException("Ошибка перехода: " + status + " → APPROVED недопустим");
        }
        if (approver == null || approver.isBlank()) {
            throw new IllegalArgumentException("Ошибка: руководитель не указан");
        }
        approvedBy = approver;
        status = TripStatus.APPROVED;
    }
    // отклонение заявки
    public void reject(String reason) {
        if (status != TripStatus.SUBMITTED) {
            throw new IllegalStateException("Ошибка перехода: " + status + " → REJECTED недопустим");
        }
        if (reason == null || reason.length() < 10) {
            throw new IllegalArgumentException("Ошибка: причина должна содержать минимум 10 символов");
        }
        rejectionReason = reason;
        status = TripStatus.REJECTED;
    }
    // отмена заявки
    public void cancel() {
        if (status != TripStatus.DRAFT && status != TripStatus.SUBMITTED
                && status != TripStatus.APPROVED) {
            throw new IllegalStateException("Ошибка перехода: " + status + " → CANCELLED недопустим");
        }
        status = TripStatus.CANCELLED;
    }
    // завершение поездки
    public void complete(LocalDate today) {
        if (status != TripStatus.APPROVED) {
            throw new IllegalStateException("Ошибка перехода: " + status + " → COMPLETED недопустим");
        }
        if (today == null) {
            throw new IllegalArgumentException("Ошибка: текущая дата не указана");
        }
        if (today.isBefore(end)) {
            throw new IllegalStateException("Ошибка: дата окончания командировки ещё не наступила");
        }
        status = TripStatus.COMPLETED;
    }
    public TripStatus getStatus() { return status; } public String getRejectionReason() { return rejectionReason; }

    public String getId() {
        return id;
    }

    public LocalDate getStart() {
        return start;
    }

    public LocalDate getEnd() {
        return end;
    }

    public String getApprovedBy() {
        return approvedBy;
    }
}
