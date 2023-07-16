package com.api.calculator.stockprice.brokerage.note;


import com.api.calculator.stockprice.model.Operation;
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

    public static ByteArrayInputStream generateOperations(List<Operation> operationList) throws IOException {

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        CSVPrinter csvPrinter = new CSVPrinter(new PrintWriter(out), CSVFormat.DEFAULT.withQuoteMode(QuoteMode.MINIMAL));

        csvPrinter.printRecord(Arrays.asList("name","activeType","qtd","value","date","typeOp","typeMarket","wallet",
                "closeMonth","noteNumber","fileId"));

        for (Operation operation : operationList) {

            csvPrinter.printRecord(
                    Arrays.asList(operation.getName(),operation.getActiveType(),
                            operation.getQtd(), operation.getValue(),
                    operation.getDate().toString(), operation.getTypeOp(), operation.getTypeMarket(),
                    operation.getWallet(), operation.getCloseMonth(), operation.getNoteNumber(), operation.getFileId() ));
        }

        csvPrinter.flush();
        return new ByteArrayInputStream(out.toByteArray());
    }
}
