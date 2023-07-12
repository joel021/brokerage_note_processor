package com.api.calculator.stockprice.exceptions;

import lombok.Data;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.ArrayList;
import java.util.List;

@Data
@ResponseStatus(value = HttpStatus.FORBIDDEN)
public class NotAllowedException extends Exception {
    private static final long serialVersionUID = 1L;
    public final HttpStatus status = HttpStatus.FORBIDDEN;
    private List<String> errors = new ArrayList<>();
    public NotAllowedException(String message) {
        super(message);
        errors.add(message);
    }
}
