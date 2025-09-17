package com.xunim.paymentsystem.dto.request;

import com.xunim.paymentsystem.enums.PaymentType;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreatePaymentRequestDTO {

    @NotNull(message = "ID da conta origem é obrigatório")
    private Long fromAccountId;

    @NotNull(message = "ID da conta destino é obrigatório")
    private Long toAccountId;

    @NotNull(message = "Valor é obrigatório")
    @DecimalMin(value = "0.01", message = "Valor deve ser maior que zero")
    @Digits(integer = 13, fraction = 2, message = "Valor deve ter no máximo 13 dígitos inteiros e 2 decimais")
    private BigDecimal amount;

    @NotNull(message = "Tipo de pagamento é obrigatório")
    private PaymentType paymentType;

    @Size(max = 500, message = "Descrição deve ter no máximo 500 caracteres")
    private String description;

    @Size(max = 255, message = "ID externo deve ter no máximo 255 caracteres")
    private String externalId;
}