package com.api.calculator.stockprice.brokerage.note;

import com.api.calculator.stockprice.ws.data.model.ExtractionError;
import com.api.calculator.stockprice.ws.data.model.Operation;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class OperationExtractor {

    private List<ExtractionError> errors;

    private final BovespaExtractor bovespaExtractor;
    private final FutureExtractor futureExtractor;

    private final TextExtractor textExtractor;

    public OperationExtractor(){
        errors = new ArrayList<>();
        bovespaExtractor = new BovespaExtractor();
        futureExtractor = new FutureExtractor();
        textExtractor = new TextExtractor();
    }

    public List<Operation> getAll(InputStream pdfInputStream, String password, UUID fileId) throws IOException {
        return getAll(textExtractor.parseFromStream(pdfInputStream, password), fileId);
    }

    public List<Operation> getAll(String pdfFileUri, UUID fileId) throws IOException {
        return getAll(textExtractor.parseFromFileUri(pdfFileUri), fileId);
    }

    private List<Operation> getAll(List<String> pages, UUID fileId){
        List<Operation> futureOperations = futureExtractor.getOperations(pages, fileId);
        bovespaExtractor.setOperations(futureOperations);
        bovespaExtractor.setExtractionErrors(futureExtractor.getExtractionErrors());

        List<Operation> allOperations = bovespaExtractor.getOperations(pages, fileId);
        this.errors = bovespaExtractor.getExtractionErrors();

        return allOperations;
    }

    public List<ExtractionError> getErrors(){
        return errors;
    }
}
