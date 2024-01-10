package com.api.calculator.stockprice;

import com.api.calculator.stockprice.api.persistence.model.Operation;
import com.api.calculator.stockprice.brokerage.extractor.extractor.BovespaExtractor;
import com.api.calculator.stockprice.brokerage.extractor.extractor.FutureExtractor;
import com.api.calculator.stockprice.brokerage.extractor.extractor.TextExtractor;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class TestsFilesSetupTests {


    private final String PATH_FILES = "./static/tests/CM_CAPITAL";

    @Before
    public void setup() throws Exception {

        Path pdfsToTest = Paths.get(PATH_FILES);
        Files.createDirectories(pdfsToTest);
        assert Objects.requireNonNull(pdfsToTest.toFile().listFiles()).length > 0;

        PDFCheckerTestUtil pdfCheckerTestUtil = new PDFCheckerTestUtil();
        TextExtractor textExtractor = new TextExtractor();
        UUID fileId = UUID.randomUUID();

        for(File pdfFile: pdfsToTest.toFile().listFiles()) {

            String pdfFileUri = pdfFile.getAbsoluteFile().toString();

            if (!pdfFileUri.endsWith(".pdf")){
                continue;
            }
            String csvFileUri = pdfFileUri.replace(".pdf", ".csv");
            List<String> pages = textExtractor.parseFromFileUri(pdfFileUri);

            StringBuilder pagesContent = new StringBuilder();
            for(String page: pages) {
                pagesContent.append(page);
            }

            String pagesString = pagesContent.toString().replace("\n", " ")
                    .replace("\r", " ").replaceAll("[0-9]{3}[.][0-9]{3}[.][0-9]{3}[-][0-9]{2}", "");
            pdfCheckerTestUtil.saveTextFile(pdfFileUri+".txt", pagesString);

            List<Operation> allOperations = new BovespaExtractor().getOperations(pages, fileId);
            allOperations.addAll(new FutureExtractor().getOperations(pages, fileId));

            String[] head = {"noteNumber", "netValue", "operationsQuantity"};
            List<String[]> csvBody = new ArrayList<>();
            csvBody.add(head);

            Map<String, Float[]> checkerValues = new HashMap<>();

            for (Operation operation: allOperations){
                Float[] values = checkerValues.get(operation.getNoteNumber());
                if (values == null) {
                    values = new Float[]{operation.getValue(), 1f};
                } else {
                    values[0] += operation.getValue();
                    values[1] += 1;
                }
                checkerValues.put(operation.getNoteNumber(), values);
            }

            for(String noteNumber: checkerValues.keySet()){
                csvBody.add(new String[]{noteNumber, String.valueOf(checkerValues.get(noteNumber)[0]),
                        String.valueOf(checkerValues.get(noteNumber)[1])});
            }
            pdfCheckerTestUtil.createCheckerFile(csvFileUri, csvBody);
        }
    }

    @Test
    public void runTest() {

    }
}
