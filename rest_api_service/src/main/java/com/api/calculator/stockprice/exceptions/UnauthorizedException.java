package com.api.calculator.stockprice.exceptions;

import lombok.Data;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.ArrayList;
import java.util.List;

@Data
@ResponseStatus(value = HttpStatus.UNAUTHORIZED)
public class UnauthorizedException extends Exception {

    private static final long serialVersionUID = 43L;
    public List<String> errors = new ArrayList<>();
    public UnauthorizedException(String message) {
        super(message);
        errors.add(message);
    }
}
