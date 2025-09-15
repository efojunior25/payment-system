package com.xunim.paymentsystem.enums;

public enum PaymentType {
    PIX("Pix"),
    TRANSFER("Transferência"),
    CARD("Cartão"),
    BOLETO("Boleto");

    private final String description;

    private PaymentType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
