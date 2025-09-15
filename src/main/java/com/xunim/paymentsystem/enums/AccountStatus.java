package com.xunim.paymentsystem.enums;

public enum AccountStatus {
    ACTIVE("Ativa"),
    BLOCKED("Bloqueada"),
    CLOSED("Encerrada");

    private final String description;

    AccountStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
