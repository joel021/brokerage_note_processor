package com.api.calculator.stockprice;

public class StringUtils {

    public static float stringToFloat(String value){
        return Float.parseFloat(value.replace(" ", "")
                .replace(".", "")
                .replace(",", "."));
    }
}
