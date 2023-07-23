package com.api.calculator.stockprice.service;

import com.api.calculator.stockprice.brokerage.note.BrokerageOperationsHandler;
import com.api.calculator.stockprice.ws.data.model.ExtractionError;
import com.api.calculator.stockprice.ws.data.model.Operation;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class BrokerageOperationsHandlerTests implements BrokerageOperationsHandler.Callback {

    private BrokerageOperationsHandler brokerageOperationsHandler;
    private final Path pdfsToTest = Paths.get("static/pdf_tests");

    private UUID userId;

    @Before
    public void setup(){
        brokerageOperationsHandler = new BrokerageOperationsHandler();
        userId = UUID.randomUUID();
    }

    @Test
    public void processFiles(){

        for(File file: Objects.requireNonNull(pdfsToTest.toFile().listFiles())){
            System.out.println(file.toString());
            brokerageOperationsHandler.processPdfFile(this, userId, UUID.randomUUID(), file.toString(), new ArrayList<>());
        }
    }

    @Override
    public void onProcessBrokerage(UUID userId, List<Operation> closedOperations, List<Operation> openedOperations, List<ExtractionError> errors) {
        System.out.println(closedOperations.size()+" closed operations\n" +
                openedOperations.size()+" opened operations\n"+
                errors.size()+" errors");
        assert errors.isEmpty();
    }
}
