package com.api.calculator.stockprice.exceptions;

import lombok.Data;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.ArrayList;
import java.util.List;
@Data
@ResponseStatus(value = HttpStatus.NOT_FOUND)
public class ResourceNotFoundException extends Exception {

  private static final long serialVersionUID = 2L;
  public final HttpStatus status = HttpStatus.NOT_FOUND;
  private List<String> errors = new ArrayList<>();
  public ResourceNotFoundException(String message) {
    super(message);
    errors.add(message);
  }
}