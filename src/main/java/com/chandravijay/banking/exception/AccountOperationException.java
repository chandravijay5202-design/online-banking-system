package com.chandravijay.banking.exception;

public class AccountOperationException extends RuntimeException {
    public AccountOperationException(String message) {
        super(message);
    }
}
