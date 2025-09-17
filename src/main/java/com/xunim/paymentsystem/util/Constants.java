package com.xunim.paymentsystem.util;

public final class Constants {

    private Constants() {
        // Utility class
    }

    public static final class Cache {
        public static final String USER_BY_ID = "users::id";
        public static final String USER_BY_EMAIL = "users::email";
        public static final String ACCOUNT_BY_ID = "accounts::id";
        public static final String ACCOUNT_BY_NUMBER = "accounts::number";

        private Cache() {}
    }

    public static final class Messages {
        public static final String USER_NOT_FOUND = "Usuário não encontrado";
        public static final String ACCOUNT_NOT_FOUND = "Conta não encontrada";
        public static final String PAYMENT_NOT_FOUND = "Pagamento não encontrado";
        public static final String EMAIL_ALREADY_EXISTS = "Email já cadastrado";
        public static final String DOCUMENT_ALREADY_EXISTS = "Documento já cadastrado";
        public static final String INSUFFICIENT_BALANCE = "Saldo insuficiente";
        public static final String INVALID_OPERATION = "Operação inválida";

        private Messages() {}
    }

    public static final class Patterns {
        public static final String TRANSACTION_ID = "TXN-%s-%d";
        public static final String ACCOUNT_NUMBER = "%s-%d-%d";

        private Patterns() {}
    }
}