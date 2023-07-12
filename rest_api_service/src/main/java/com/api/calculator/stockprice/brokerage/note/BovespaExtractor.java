package com.api.calculator.stockprice.brokerage.note;

import com.api.calculator.stockprice.exceptions.ResourceNotFoundException;
import com.api.calculator.stockprice.model.ExtractionError;
import com.api.calculator.stockprice.model.Operation;

import java.sql.Date;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.regex.*;

import static java.lang.Math.abs;

public class BovespaExtractor extends Extractor {

    private final Pattern EXEC_OPT_NAME_PATTERN;
    private final Pattern OPTION_NAME_PATTERN;
    private final Pattern END_CASH_NAME;
    private final Pattern LIQUID_LINE_PATTERN;
    private final Pattern LIQUID_VALUE_PATTERN;
    private final Pattern EXPIRATION_DATE_PATTERN;
    private final Pattern START_CASH_NAME;
    private final Pattern OPTION_LINE_PATTERN;
    private final Pattern QTD_VALUE_PATTERN;
    public BovespaExtractor() {
        EXEC_OPT_NAME_PATTERN = Pattern.compile("((?<=[0-9]{2}/[0-9]{2})( *[A-Z] ?)+[0-9]+)");
        OPTION_NAME_PATTERN = Pattern.compile("((?<=[0-9]{2}/[0-9]{2}) .*[A-Z][0-9]+)");
        START_CASH_NAME = Pattern.compile("(V ?I ?S ?T ?A)");
        END_CASH_NAME = Pattern.compile("( *[0-9]+)");
        LIQUID_LINE_PATTERN = Pattern.compile("((.*)L ?í?i?quido para [0-9]{2}/[0-9]{2}/[0-9]{4} *[C-D])",Pattern.CASE_INSENSITIVE);
        LIQUID_VALUE_PATTERN = Pattern.compile("(([0-9]*[.])*[0-9]+,[0-9]{2})");
        EXPIRATION_DATE_PATTERN = Pattern.compile("( [0-9]{2}/[0-9]{2} )");
        OPTION_LINE_PATTERN = Pattern.compile("OPC?Ç?A?Ã?ODE");
        QTD_VALUE_PATTERN = Pattern.compile("((?<=[ +])[0-9]+ +[0-9]?.?[0-9]+,[0-9]{2})");
    }

    @Override
    public List<Operation> getOperations(List<String> pages, UUID fileId){
        this.fileId = fileId;

        for(String page: pages){
            this.setOperations(page.split("(BOV *ES *PA)"), getNoteNumber(page));
        }
        return this.operations;
    }

    private void setOperations(String[] bovespaOperationsArray, String noteNumber){

        if (bovespaOperationsArray.length < 2){
            return;
        }

        Date operationDate = operationDate(bovespaOperationsArray[bovespaOperationsArray.length-1]);
        float sumValue = 0;
        float totalAbsValue = 0;

        for(int i = 1; i < bovespaOperationsArray.length; i++){

            String operationLine = bovespaOperationsArray[i].split("\n")[0];
            Operation operation = getOperation(operationLine);
            operation.setDate(operationDate);
            operation.setFileId(this.fileId);
            operation.setNoteNumber(noteNumber);
            setQtdAndValue(operation,operationLine);

            sumValue += operation.getValue();
            totalAbsValue += abs(operation.getValue());
            this.operations.add(operation);
        }

        calcEquivalentTax(getLiquidValue(sumValue, bovespaOperationsArray[bovespaOperationsArray.length-1]),
                sumValue, totalAbsValue, bovespaOperationsArray.length-1, null);
    }

    private float getLiquidValue(float sumValue, String lastBovespaSplitStr){
        int multiplier = sumValue > 0 ? 1 : -1;

        Matcher liquidValueMatcher = LIQUID_LINE_PATTERN.matcher(lastBovespaSplitStr);
        if (liquidValueMatcher.find()){
            liquidValueMatcher = LIQUID_VALUE_PATTERN.matcher(liquidValueMatcher.group());
            liquidValueMatcher.find();
            return stringToFloat(liquidValueMatcher.group()) * multiplier;
        } else {

            int digits = String.valueOf((int)sumValue).length()-1;
            liquidValueMatcher = Pattern.compile("(([0-9]*[.]*[0-9]+){" + digits + "},[0-9]{2} *[" + (sumValue > 0 ? "C" : "D") + "])").matcher(lastBovespaSplitStr);
            List<Float> liquidValues = new ArrayList<>();

            while (liquidValueMatcher.find()) {
                try {
                    String valueString = liquidValueMatcher.group();
                    liquidValues.add(stringToFloat(valueString.substring(0, valueString.length() - 1))
                            * multiplier);

                } catch (Exception ignored) {}
            }

            if (liquidValues.isEmpty()) {
                extractionErrors.add(new ExtractionError(null, "Valor líquido não definido.", fileId));
                return sumValue;
            }

            return Collections.min(liquidValues);
        }
    }

    private void setQtdAndValue(Operation operation, String operationLine) {
        Matcher qtdValueMatcher = QTD_VALUE_PATTERN.matcher(operationLine);
        qtdValueMatcher.find();
        String[] qtdValueSplit = qtdValueMatcher.group().split(" ");
        operation.setQtd(Integer.parseInt(qtdValueSplit[0]));
        operation.setValue(operation.getQtd()*stringToFloat(qtdValueSplit[1])*
                debitOrCredit(operationLine.substring(operationLine.length()-3)));
    }

    private Operation getOperation(String operationLine) {

        String operationLineCleanned = operationLine.replace(" ", "");
        if (OPTION_LINE_PATTERN.matcher(operationLineCleanned).find()){
            return getOptionOperation(operationLine);
        }else if (operationLineCleanned.contains("VISTA")){
            return getCashOperation(operationLine);
        }else if (operationLineCleanned.contains("EXOPCDE") || operationLineCleanned.contains("EXERCOPC")){
            return getExecOptionOperation(operationLine);
        }else{
            this.extractionErrors.add(new ExtractionError(null, "Não foi possível obter o ativo de " +
                    "exercício da opção.", fileId));
            return new Operation();
        }
    }

    private Operation getOptionOperation(String operationLine){
        Operation operation = new Operation();
        Matcher nameMatcher = OPTION_NAME_PATTERN.matcher(operationLine);
        if (nameMatcher.find()){
            operation.setName(nameMatcher.group().replace(" ", ""));
        }
        operation.setActiveType(operationLine.replace(" ","").contains("COMPRA") ? "CALL" : "PUT");
        operation.setTypeMarket(Constants.OPTION_MARKET);
        operation.setCloseMonth(getExpirationDate(operationLine));
        return operation;
    }

    private Operation getExecOptionOperation(String operationLine) {
        Operation operation = new Operation();
        Matcher execOptionMatcher = EXEC_OPT_NAME_PATTERN.matcher(operationLine);
        execOptionMatcher.find();
        String optionName = execOptionMatcher.group().replace(" ", "");
        try {
            operation.setName(new Options().getActiveName(optionName));
        } catch (ResourceNotFoundException e) {
            operation.setName(optionName);
            extractionErrors.add(new ExtractionError(null, e.getMessage(), fileId));
        }
        operation.setActiveType(Constants.ACTIVE);
        operation.setTypeMarket(Constants.OPTION_MARKET);
        operation.setCloseMonth(getExpirationDate(operationLine));
        return operation;
    }

    private String getExpirationDate(String line){
        Matcher expirationDateMather = EXPIRATION_DATE_PATTERN.matcher(line);

        if(expirationDateMather.find()){
            String[] dateSplit = expirationDateMather.group().replace(" ", "").split("/");
            return dateSplit[1]+"-"+dateSplit[0];
        }
        return null;
    }

    private Operation getCashOperation(String operationLineString){

        Matcher startNameMatcher = START_CASH_NAME.matcher(operationLineString);
        startNameMatcher.find();
        String nameFound = operationLineString.substring(startNameMatcher.end());
        Matcher endNameMatcher = END_CASH_NAME.matcher(nameFound);
        endNameMatcher.find();
        nameFound = nameFound.substring(0,endNameMatcher.start()).replaceAll("\\P{L}+", "");

        Operation operation = new Operation();
        operation.setName(nameFound);
        Constants constants = new Constants();

        for(String key: constants.ACTIVIES.keySet()){
            if(nameFound.contains(constants.ACTIVIES.get(key)) || constants.ACTIVIES.get(key).contains(nameFound)){
                operation.setName(key);
                break;
            }
        }
        operation.setActiveType(Constants.ACTIVE);
        operation.setTypeMarket(Constants.CASH_MARKET);

        return operation;
    }


}
