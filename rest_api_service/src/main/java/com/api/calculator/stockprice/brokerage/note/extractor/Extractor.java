package com.api.calculator.stockprice.brokerage.note.extractor;

import com.api.calculator.stockprice.ws.data.model.ExtractionError;
import com.api.calculator.stockprice.ws.data.model.Operation;

import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.Math.abs;

abstract class Extractor {

    protected List<ExtractionError> extractionErrors;
    protected List<Operation> operations;

    protected UUID fileId;

    private final Pattern DEBIT_OR_CREDIT_PATTERN;
    private final Pattern NOTE_NUMBER_PATTERN1;
    private final Pattern NOTE_NUMBER_PATTERN2;
    private final Pattern DATE_PATTERN = Pattern.compile("([0-9]{2}[/][0-9]{2}[/][0-9]{4})");
    public Extractor(){
        DEBIT_OR_CREDIT_PATTERN = Pattern.compile(" *D *");
        NOTE_NUMBER_PATTERN1 = Pattern.compile("((?<=N ?r ?[.] ?n ?o ?t ?a)(\\d[.]?)+)", Pattern.CASE_INSENSITIVE);
        NOTE_NUMBER_PATTERN2 = Pattern.compile("\\d+[.]\\d+(?= +(.*) +\\d{2}/\\d{2})");
        this.extractionErrors = new ArrayList<>();
        this.operations = new ArrayList<>();
    }

    public void setOperations(List<Operation> operations){
        this.operations = operations;
    }
    public void setExtractionErrors(List<ExtractionError> extractionErrors){
        this.extractionErrors = extractionErrors;
    }

    public abstract List<Operation> getOperations(List<String> pages, UUID fileId);

    public List<ExtractionError> getExtractionErrors(){
        return extractionErrors;
    }

    public float stringToFloat(String value){
        return Float.parseFloat(value.replace(" ", "")
                                        .replace(".", "")
                                        .replace(",", "."));
    }

    public int debitOrCredit(String text){
        return DEBIT_OR_CREDIT_PATTERN.matcher(text).find() ? -1 : 1;
    }

    public void calcEquivalentTax(float liquidValue, float sumValue,  float totalAbsValue, int opQtd, Date date){

        float tax = (liquidValue - sumValue);

        int i = operations.size() - opQtd;
        for(i = i > -1 ? i : 0; i < operations.size(); i+=1) {
            Operation operation = operations.get(i);
            operation.setValue(operation.getValue() + tax * abs(operations.get(i).getValue()) / totalAbsValue);
            operation.setDate( date != null ? date : operation.getDate());
            operations.set(i, operation);
        }
    }

    public Date operationDate(String string) {

        Matcher dateMatcher = DATE_PATTERN.matcher(string);
        if (dateMatcher.find()){
            try {
                return new Date(new SimpleDateFormat("dd/MM/yyyy").parse(dateMatcher.group()).getTime());
            } catch (ParseException ignored) {}
        }
        this.extractionErrors.add(new ExtractionError(null, "Não foi possível a data da operação", fileId));
        return null;
    }

    public String getNoteNumber(String text) {
        text = text.replace("\n","");
        Matcher noteNumberMatcher = NOTE_NUMBER_PATTERN1.matcher(text);
        if (noteNumberMatcher.find()){
            return noteNumberMatcher.group();
        }
        noteNumberMatcher = NOTE_NUMBER_PATTERN2.matcher(text);
        if (noteNumberMatcher.find()){
            return noteNumberMatcher.group();
        } else{
            this.extractionErrors.add(new ExtractionError(null, "Obter o número da nota.", fileId));
            return "";
        }
    }
}
