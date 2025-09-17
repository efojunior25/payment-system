package com.xunim.paymentsystem.exception;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ErrorDetails {
    private String field;
    private Object rejectedValue;
    private String message;
    private String code;
}