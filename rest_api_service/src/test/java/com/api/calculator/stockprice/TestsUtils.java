package com.api.calculator.stockprice;

import java.nio.charset.StandardCharsets;

import org.springframework.http.MediaType;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class TestsUtils {
    
    public static final MediaType CONTENT_TYPE = new MediaType(MediaType.APPLICATION_JSON.getType(),
                MediaType.APPLICATION_JSON.getSubtype(),
            StandardCharsets.UTF_8);

    public static String objectToJson(Object object) throws JsonProcessingException{

        return new ObjectMapper().writer().withDefaultPrettyPrinter().writeValueAsString(object);
    }


}
