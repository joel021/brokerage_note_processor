package com.api.calculator.stockprice.brokerage.note;

import com.api.calculator.stockprice.PDFCheckerTestUtil;
import com.api.calculator.stockprice.model.ExtractionError;
import com.api.calculator.stockprice.model.Operation;
import org.junit.Before;
import org.junit.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@SpringBootTest
public class ExtractorTests {

    private PDFCheckerTestUtil pdfCheckerTestUtil;
    private boolean resetCheckers = false;

    @Before
    public void setup() throws IOException {
        pdfCheckerTestUtil = new PDFCheckerTestUtil();
        TextExtractor textExtractor = new TextExtractor();
        UUID fileId = UUID.randomUUID();
        Path pdfsToTest = Paths.get("static/pdf_tests/pdfs");
        Files.createDirectories(pdfsToTest);

        if (pdfsToTest.toFile().listFiles() == null){
            return;
        }

        for(File pdfFile: pdfsToTest.toFile().listFiles()) {

            String fileUri = pdfFile.getAbsoluteFile().toString();

            String checkerUri = fileUri.replace(".pdf", ".csv").replace("\\pdfs","\\checkers");
            List<String[]> fileCheckerCsv = pdfCheckerTestUtil.readCheckCsv(checkerUri, ",");

            if (fileCheckerCsv.isEmpty() || resetCheckers){
                List<String> pages = textExtractor.parseFromFileUri(fileUri);
                List<Operation> allOperations = new BovespaExtractor().getOperations(pages, fileId);
                allOperations.addAll(new FutureExtractor().getOperations(pages, fileId));

                String[] head = {"noteNumber", "netValue", "operationsQuantity"};
                List<String[]> csvBody = new ArrayList<>();
                csvBody.add(head);

                Map<String, Float[]> checkerValues = new HashMap<>();

                for (Operation operation: allOperations){
                    Float[] values = checkerValues.get(operation.getNoteNumber());
                    if (values == null){
                        values = new Float[]{operation.getValue(), 1f};
                    }else{
                        values[0] += operation.getValue();
                        values[1] += 1;
                    }
                    checkerValues.put(operation.getNoteNumber(), values);
                }

                for(String noteNumber: checkerValues.keySet()){
                    csvBody.add(new String[]{noteNumber, String.valueOf(checkerValues.get(noteNumber)[0]),
                            String.valueOf(checkerValues.get(noteNumber)[1])});
                }
                pdfCheckerTestUtil.createCheckerFile(checkerUri, csvBody);
            }
        }
    }

    @Test
    public void textExtractor() throws IOException {
        TextExtractor textExtractor = new TextExtractor();
        Path pdfsToTest = Paths.get("static/pdf_tests/pdfs");
        Files.createDirectories(pdfsToTest);
        if(pdfsToTest.toFile().listFiles() == null){
            return;
        }

        for(File pdfFile: pdfsToTest.toFile().listFiles()){
            List<String> pages = textExtractor.parseFromFileUri(pdfFile.toURI().toString());
            System.out.println("\n\n");
            System.out.println(pdfFile);
            for(String page: pages){
                System.out.println(page);
            }
            assert !pages.isEmpty();
        }
    }

    @Test
    public void testWithChecker() throws IOException {
        TextExtractor textExtractor = new TextExtractor();

        UUID fileId = UUID.randomUUID();
        BovespaExtractor bovespaExtractor = new BovespaExtractor();
        FutureExtractor futureExtractor = new FutureExtractor();

        Path root = Paths.get("static/pdf_tests/pdfs");
        Files.createDirectories(root);
        if(root.toFile().listFiles() != null){
            return;
        }
        for(File pdfFile: root.toFile().listFiles()) {

            String fileUri = pdfFile.getAbsoluteFile().toString();
            String checkerUri = fileUri.replace(".pdf", ".csv").replace("\\pdfs\\","\\checkers\\");
            List<String[]> checkOperations = pdfCheckerTestUtil.readCheckCsv(checkerUri, ",");
            List<String> pages = textExtractor.parseFromFileUri(fileUri);
            List<Operation> bovespaExtractorOperations = bovespaExtractor.getOperations(pages, fileId);
            List<Operation> allOperations = futureExtractor.getOperations(pages, fileId);
            allOperations.addAll(bovespaExtractorOperations);

            for(Operation operation: allOperations){

                for(String[] lineChecker: checkOperations){

                    //TODO dev an way to check values. Proposed solution is use the generated files.
                }
            }
        }
    }

    @Test
    public void getOperationsTest() throws IOException {
        TextExtractor textExtractor = new TextExtractor();

        BovespaExtractor bovespaExtractor = new BovespaExtractor();
        FutureExtractor futureExtractor = new FutureExtractor();

        Path root = Paths.get("static/pdf_tests/pdfs");
        Files.createDirectories(root);
        if(root.toFile().listFiles() == null){
            return;
        }
        for(File pdfFile: root.toFile().listFiles()){
            UUID fileId = UUID.randomUUID();
            List<String> pages = textExtractor.parseFromFileUri(pdfFile.getAbsoluteFile().toString());
            List<Operation> bovespaExtractorOperations = bovespaExtractor.getOperations(pages, fileId);
            List<Operation> futureExtractorOperations = futureExtractor.getOperations(pages, fileId);

            for(Operation operation: bovespaExtractorOperations){

                assert operation.getName() != null;
                assert operation.getDate() != null;
                assert operation.getValue() != 0;
                assert operation.getQtd() > 0;
                assert operation.getActiveType() != null;
                assert operation.getTypeMarket() != null;
                assert !operation.getNoteNumber().isEmpty();
                assert operation.getActiveType().equals(Constants.ACTIVE) || operation.getCloseMonth() != null;
                assert operation.getName().length() > 2;
            }

            for(Operation operation: futureExtractorOperations){
                assert operation.getName() != null;
                assert operation.getDate() != null;
                assert operation.getValue() != 0;
                assert operation.getQtd() > 0;
                assert operation.getActiveType() != null;
                assert operation.getTypeMarket() != null;
                assert !operation.getNoteNumber().isEmpty();
                assert operation.getName().length() > 2;
            }

            for(ExtractionError e: futureExtractor.getExtractionErrors()){
                System.out.println(e.getError());
            }
            assert futureExtractor.getExtractionErrors().isEmpty();

            for(ExtractionError e: bovespaExtractor.getExtractionErrors()){
                System.out.println(e.getError());
            }
            assert bovespaExtractor.getExtractionErrors().isEmpty();
        }
    }
}
