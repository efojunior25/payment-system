package com.xunim.paymentsystem.util;

import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

@Component
public class ValidationUtils {

    private static final Pattern CPF_PATTERN = Pattern.compile("\\d{11}");
    private static final Pattern CNPJ_PATTERN = Pattern.compile("\\d{14}");
    private static final Pattern PHONE_PATTERN = Pattern.compile("\\d{10,11}");

    public boolean isValidCPF(String cpf) {
        if (cpf == null || !CPF_PATTERN.matcher(cpf).matches()) {
            return false;
        }

        // Verifica se todos os dígitos são iguais
        if (cpf.chars().distinct().count() == 1) {
            return false;
        }

        // Algoritmo de validação do CPF
        int[] weights1 = {10, 9, 8, 7, 6, 5, 4, 3, 2};
        int[] weights2 = {11, 10, 9, 8, 7, 6, 5, 4, 3, 2};

        return isValidDocument(cpf, weights1, 9) && isValidDocument(cpf, weights2, 10);
    }

    public boolean isValidCNPJ(String cnpj) {
        if (cnpj == null || !CNPJ_PATTERN.matcher(cnpj).matches()) {
            return false;
        }

        // Verifica se todos os dígitos são iguais
        if (cnpj.chars().distinct().count() == 1) {
            return false;
        }

        // Algoritmo de validação do CNPJ
        int[] weights1 = {5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};
        int[] weights2 = {6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};

        return isValidDocument(cnpj, weights1, 12) && isValidDocument(cnpj, weights2, 13);
    }

    public boolean isValidPhone(String phone) {
        return phone != null && PHONE_PATTERN.matcher(phone).matches();
    }

    public DocumentType getDocumentType(String document) {
        if (document == null) {
            return DocumentType.INVALID;
        }

        if (document.length() == 11) {
            return isValidCPF(document) ? DocumentType.CPF : DocumentType.INVALID;
        } else if (document.length() == 14) {
            return isValidCNPJ(document) ? DocumentType.CNPJ : DocumentType.INVALID;
        }

        return DocumentType.INVALID;
    }

    private boolean isValidDocument(String document, int[] weights, int position) {
        int sum = 0;
        for (int i = 0; i < weights.length; i++) {
            sum += Character.getNumericValue(document.charAt(i)) * weights[i];
        }

        int remainder = sum % 11;
        int checkDigit = (remainder < 2) ? 0 : 11 - remainder;

        return checkDigit == Character.getNumericValue(document.charAt(position));
    }

    public enum DocumentType {
        CPF, CNPJ, INVALID
    }
}