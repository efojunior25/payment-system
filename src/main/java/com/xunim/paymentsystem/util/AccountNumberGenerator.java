package com.xunim.paymentsystem.util;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;

@Component
public class AccountNumberGenerator {

    private static final String BANK_CODE = "001";
    private static final SecureRandom random = new SecureRandom();

    public String generateAccountNumber() {
        // Gera 6 dígitos aleatórios para o número da conta
        int accountNumber = 100000 + random.nextInt(900000);

        // Calcula o dígito verificador
        int checkDigit = calculateCheckDigit(accountNumber);

        return String.format("%s-%d-%d", BANK_CODE, accountNumber, checkDigit);
    }

    private int calculateCheckDigit(int accountNumber) {
        // Algoritmo simples para dígito verificador
        String numberStr = String.valueOf(accountNumber);
        int sum = 0;

        for (int i = 0; i < numberStr.length(); i++) {
            int digit = Character.getNumericValue(numberStr.charAt(i));
            sum += digit * (i + 1);
        }

        return sum % 10;
    }
}