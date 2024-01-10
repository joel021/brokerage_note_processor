package com.api.calculator.stockprice.exceptions;

import lombok.Data;
import org.springframework.http.HttpStatus;

@Data
public class ApiException extends Exception {

    private HttpStatus httpStatus;

    public ApiException (String msg, HttpStatus httpStatus) {
        super(msg);
        this.httpStatus = httpStatus;
    }
}
