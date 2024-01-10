package com.api.calculator.stockprice.exceptions;

import org.springframework.http.HttpStatus;

public class NotAcceptedException extends ApiException {

    public NotAcceptedException(String message) {
        super(message, HttpStatus.NOT_ACCEPTABLE);
    }
}
