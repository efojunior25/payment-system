package com.xunim.paymentsystem.enums;

public enum PaymentStatus {
    PENDING("Pendente"),
    PROCESSING("Processando"),
    COMPLETED("Concluido"),
    FAILED("Falhou"),
    CANCELLED("Cancelado");

    private final String description;

    private PaymentStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
