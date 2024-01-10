package com.api.calculator.stockprice.brokerage.extractor.operation;


import com.api.calculator.stockprice.api.persistence.model.Operation;
import lombok.Data;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.QuoteMode;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;

public class CsvGenerator {

    public static ByteArrayInputStream generateOperations(List<OperationCSV> operationList) throws IOException {

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        CSVPrinter csvPrinter = new CSVPrinter(new PrintWriter(out), CSVFormat.DEFAULT.withQuoteMode(QuoteMode.MINIMAL));

        csvPrinter.printRecord(Arrays.asList("name","activeType","qtd","value","date","typeOp","typeMarket","wallet",
                "closeMonth","noteNumber","fileName"));

        for (OperationCSV operation : operationList) {

            csvPrinter.printRecord(
                    Arrays.asList(operation.operation.getName(),operation.operation.getActiveType(),
                            operation.operation.getQtd(), operation.operation.getValue(),
                    operation.operation.getDate().toString(), operation.operation.getTypeOp(), operation.operation.getTypeMarket(),
                    operation.operation.getWallet(), operation.operation.getCloseMonth(), operation.operation.getNoteNumber(), operation.getFileName() ));
        }

        csvPrinter.flush();
        return new ByteArrayInputStream(out.toByteArray());
    }

    @Data
    public static class OperationCSV{
        private String fileName;
        Operation operation;

        public OperationCSV(Operation operation, String fileName){
            this.operation = operation;
            this.fileName = fileName;
        }

    }
}
