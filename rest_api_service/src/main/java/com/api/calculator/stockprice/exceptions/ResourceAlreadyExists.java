package com.api.calculator.stockprice.exceptions;

import lombok.Data;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.ArrayList;
import java.util.List;

@Data
@ResponseStatus(value = HttpStatus.CONFLICT)
public class ResourceAlreadyExists extends Exception {

    private static final long serialVersionUID = 1L;
    private List<String> errors = new ArrayList<>();
    public ResourceAlreadyExists(String message) {
        super(message);
        errors.add(message);
    }

    
}