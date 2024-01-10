package com.api.calculator.stockprice.brokerage.extractor.operation;

import com.api.calculator.stockprice.exceptions.ResourceNotFoundException;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Options {


    private final Pattern pattern = Pattern.compile("[A-Z]{1}[A-z]*[0-9]?[A-Z][0-9]{2}");

    private String getBaseName(String optionName) throws ResourceNotFoundException {

        Matcher matcher = pattern.matcher(optionName);

        if (matcher.find()) {
            String result = matcher.group();
            return result.substring(0, result.length()-3);
        }
        throw new ResourceNotFoundException("O código da opção não foi encontrado em: "+optionName);
    }

    public String getActiveName(String optionName) throws ResourceNotFoundException {

        if (optionName != null) {
            Constants constants = new Constants();

            String baseName = getBaseName(optionName);

            for (String activeName : constants.ACTIVIES.keySet()) {

                if (activeName.contains(baseName)) {
                    return activeName;
                }
            }
        }
        throw new ResourceNotFoundException("Não foi possível encontrar um ativo correspondente para a opção "+optionName);
    }

}
