package com.api.calculator.stockprice.exceptions;

import org.springframework.http.HttpStatus;

public class InternalException extends ApiException {

    public InternalException(String message) {
        super(message, HttpStatus.INTERNAL_SERVER_ERROR);
    }

}
