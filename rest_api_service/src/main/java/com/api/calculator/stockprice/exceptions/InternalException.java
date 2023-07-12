package com.api.calculator.stockprice.exceptions;

import lombok.Data;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.ArrayList;
import java.util.List;

@Data
@ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
public class InternalException extends Exception {

    private static final long serialVersionUID = 3L;

    private List<String> errors;
    public final HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;

    public InternalException(String message) {
        super(message);
        errors = new ArrayList<>();
        errors.add(message);
    }

}
