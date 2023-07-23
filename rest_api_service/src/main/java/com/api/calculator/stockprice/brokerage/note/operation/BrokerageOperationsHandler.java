package com.api.calculator.stockprice.brokerage.note.operation;

import com.api.calculator.stockprice.brokerage.note.extractor.BovespaExtractor;
import com.api.calculator.stockprice.brokerage.note.extractor.FutureExtractor;
import com.api.calculator.stockprice.brokerage.note.extractor.OperationExtractor;
import com.api.calculator.stockprice.brokerage.note.extractor.TextExtractor;
import com.api.calculator.stockprice.ws.data.model.ExtractionError;
import com.api.calculator.stockprice.ws.data.model.Operation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;

public class BrokerageOperationsHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(BrokerageOperationsHandler.class);
    private List<ExtractionError> errors;

    public BrokerageOperationsHandler(){
        this.errors = new ArrayList<>();
    }

    public void processPdfFile(Callback context, UUID userId, UUID fileId, String fileUri, List<Operation> userOperations){
        OperationExtractor operationExtractor = new OperationExtractor();
        try {
            userOperations.addAll(operationExtractor.getAll(fileUri, fileId));
            OperationBalancer operationBalancer = new OperationBalancer(userOperations);

            context.onProcessBrokerage(userId, operationBalancer.getClosedOperations(),
                    operationBalancer.getOpenedOperations(), operationExtractor.getErrors());
        } catch (IOException e) {
            LOGGER.warn("Can not open the PDF.");
        }
    }

    public List<Operation> extractAllOperations(String fileUri, UUID fileId) throws IOException {
        TextExtractor textExtractor = new TextExtractor();
        BovespaExtractor bovespaExtractor = new BovespaExtractor();
        FutureExtractor futureExtractor = new FutureExtractor();

        List<String> pages = textExtractor.parseFromFileUri(fileUri);
        List<Operation> bovespaExtractorOperations = bovespaExtractor.getOperations(pages, fileId);
        List<Operation> allOperations = futureExtractor.getOperations(pages, fileId);
        allOperations.addAll(bovespaExtractorOperations);

        this.errors = bovespaExtractor.getExtractionErrors();
        return allOperations;
    }

    public interface Callback {
        void onProcessBrokerage(UUID userId, List<Operation> closedOperations, List<Operation> openedOperations, List<ExtractionError> errors);
    }
}
