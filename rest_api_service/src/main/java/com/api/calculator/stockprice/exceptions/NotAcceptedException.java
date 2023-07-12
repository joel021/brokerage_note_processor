package com.api.calculator.stockprice.exceptions;

import lombok.Data;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.ArrayList;
import java.util.List;
@Data
@ResponseStatus(value = HttpStatus.NOT_ACCEPTABLE)
public class NotAcceptedException extends Exception {

    private static final long serialVersionUID = 5L;
    private List<String> errors = new ArrayList<>();

    public NotAcceptedException(String message) {
        super(message);
        errors.add(message);
    }
}
