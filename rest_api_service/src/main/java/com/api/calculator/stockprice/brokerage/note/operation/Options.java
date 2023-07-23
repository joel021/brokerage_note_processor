package com.api.calculator.stockprice.brokerage.note.operation;

import com.api.calculator.stockprice.exceptions.ResourceNotFoundException;

public class Options {

    public String getActiveName(String optionName) throws ResourceNotFoundException {

        Constants constants = new Constants();

        for(String activeName: constants.ACTIVIES.keySet()){
            if(optionName.contains(activeName.substring(0, activeName.length()-2))
                    || optionName.contains(activeName.substring(0, activeName.length()-3))) {
                return activeName;
            }
        }
        throw new ResourceNotFoundException("Não foi possível encontrar um ativo correspondente para a opção "+optionName);
    }

}
