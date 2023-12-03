package com.api.calculator.stockprice.brokerage.note.extractor;

import com.api.calculator.stockprice.brokerage.note.operation.Constants;
import com.api.calculator.stockprice.ws.data.model.ExtractionError;
import com.api.calculator.stockprice.ws.data.model.Operation;

import java.sql.Date;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.Math.abs;

public class FutureExtractor extends Extractor {

    private final Pattern NAME_PATTERN;
    private final Pattern QTD_PATTERN;
    private final Pattern VALUE_PATTERN;
    private final Pattern LIQUID_VALUE_LINE_PATTERN;
    private final Pattern LIQUID_VALUE_PATTERN;
    private final Pattern BOVESPA_PATTERN;

    public FutureExtractor(){
        NAME_PATTERN = Pattern.compile("(?<=[C|V] ?)[A-Z]{3,} *[A-Z]*[0-9]*(?= ?.?[0-9]{2}/[0-9]{2}/[0-9]{4} [0-9]* [0-9]+.[0-9]+,)");
        QTD_PATTERN = Pattern.compile("( [1-9][0-9]* )");
        VALUE_PATTERN = Pattern.compile("( [0-9]+,[0-9]{2} *[CD])");
        LIQUID_VALUE_LINE_PATTERN = Pattern.compile("([0-9]*,[0-9]* *[0-9]*,[0-9]* *[0-9]*,[0-9]* *[|] *[0-9]*,[0-9]* *[|] *[CD] *[0-9]*,[0-9]* *[|] *[CD] [0-9]*,[0-9]* *[|] *[CD])");
        LIQUID_VALUE_PATTERN = Pattern.compile("( [0-9]+[,][0-9]{2} *[|]* [CD])");
        BOVESPA_PATTERN = Pattern.compile("(B ?O ?V ?E ?S ?P ?A)");
    }

    @Override
    public List<Operation> getOperations(List<String> pages, UUID fileId) {
        this.fileId = fileId;

        int futureOpQtd = 0;
        for (String page : pages) {

            Matcher bovespaMatch = BOVESPA_PATTERN.matcher(page);
            if (bovespaMatch.find()) {
                futureOpQtd = 0;
            } else {
                futureOpQtd = setOperations(page, futureOpQtd);
            }
        }
        return operations;
    }

    private int setOperations(String page, int initialQtd){

        int sumValue = 0, totalAbsValue = 0, opQtd = initialQtd, i = 0;
        Date date = null;
        String noteNumber = getNoteNumber(page);

        String[] lines = page.split("\n");
        boolean isIncompletePage = page.contains("CONTINUA...");

        for(; i < lines.length; i ++){

            Matcher nameMatcher = NAME_PATTERN.matcher(lines[i]);
            if (nameMatcher.find()){
                Operation operation = getOperation(lines[i]);
                operation.setFileId(fileId);
                operation.setNoteNumber(noteNumber);
                operation.setName(nameMatcher.group().replace(" ",""));
                operations.add(operation);

                sumValue += operation.getValue();
                totalAbsValue += abs(operation.getValue());
                opQtd += 1;
            }else if (lines[i].contains("Data pregão")){

                if(isIncompletePage){
                    return opQtd;
                }
                date = operationDate(lines[i+1]);
                break;
            }
        }
        calcEquivalentTax(getLiquidValue(lines, i), sumValue, totalAbsValue, opQtd, date);
        return 0;
    }

    private float getLiquidValue(String[] lines, int i){

        String liquidValueLineString = "";
        for (; i < lines.length; i ++){
            Matcher liquidValueLineMatcher = LIQUID_VALUE_LINE_PATTERN.matcher(lines[i]);
            if (liquidValueLineMatcher.find()){
                liquidValueLineString = liquidValueLineMatcher.group();
                break;
            }
        }

        Matcher liquidValueMatcher = LIQUID_VALUE_PATTERN.matcher(liquidValueLineString);
        String liquidValueString = "";

        while(liquidValueMatcher.find()){
            liquidValueString = liquidValueMatcher.group();
        }
        if (liquidValueString.isEmpty()){
            extractionErrors.add(new ExtractionError(null, "Não foi possível obter valor líquido", fileId));
            return 0;
        }
        return stringToFloat(liquidValueString.replace("C", "").replace("D","").replace("|",""))
                * debitOrCredit(liquidValueString.substring(liquidValueString.length()-1));
    }

    private Operation getOperation(String line){
        Operation operation = new Operation();

        Matcher qtdMatcher = QTD_PATTERN.matcher(line);
        qtdMatcher.find();
        operation.setQtd(Integer.parseInt(qtdMatcher.group().replace(" ", "")));

        Matcher valueMatcher = VALUE_PATTERN.matcher(line);
        valueMatcher.find();
        String valueString = valueMatcher.group();
        operation.setValue(stringToFloat(valueString.replace("C", "").replace("D",""))
                * debitOrCredit(valueString.substring(valueString.length()-1)));

        operation.setTypeMarket(Constants.FUTURE_MARKET);
        operation.setTypeOp(line.replace(" ","").contains(Constants.SWINGTRADE) ? Constants.SWINGTRADE : Constants.DAYTRADE);
        operation.setActiveType(Constants.ACTIVE);
        return operation;
    }
}
