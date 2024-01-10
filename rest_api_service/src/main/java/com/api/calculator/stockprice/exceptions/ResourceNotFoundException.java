package com.api.calculator.stockprice.exceptions;

import org.springframework.http.HttpStatus;

public class ResourceNotFoundException extends ApiException {

  public ResourceNotFoundException(String message) {
    super(message, HttpStatus.NOT_FOUND);
  }
}