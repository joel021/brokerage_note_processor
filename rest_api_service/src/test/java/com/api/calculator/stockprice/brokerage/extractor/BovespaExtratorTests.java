package com.api.calculator.stockprice.brokerage.extractor;

import com.api.calculator.stockprice.PDFCheckerTestUtil;
import com.api.calculator.stockprice.brokerage.extractor.extractor.BovespaExtractor;
import com.api.calculator.stockprice.brokerage.extractor.operation.Constants;
import com.api.calculator.stockprice.api.persistence.model.Operation;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class BovespaExtratorTests {


    private String pdfContent;

    private final String PATH = "static/tests/CM_CAPITAL/";
    private UUID fileId;

    private BovespaExtractor bovespaExtractor;

    @Before
    public void setup() {
        fileId = UUID.randomUUID();
        PDFCheckerTestUtil pdfCheckerTestUtil = new PDFCheckerTestUtil();
        pdfContent = pdfCheckerTestUtil.readPdfContentText(PATH+"/40075_976079_20230512.pdf.txt");
        bovespaExtractor = new BovespaExtractor();
    }

    @Test
    public void getExpirationDateLineIsNullTest() {

        String line = null;
        String result = bovespaExtractor.getExpirationDate(line);
        assertNull(result);
    }

    @Test
    public void getExpirationDateLineIsEmptyTest() {

        String line = "";
        String result = bovespaExtractor.getExpirationDate(line);
        assertNull(result);
    }

    @Test
    public void getExpirationDateLineNotSlashTest() {

        String line = "2023-10";
        String result = bovespaExtractor.getExpirationDate(line);
        assertNull(result);
    }

    @Test
    public void getExpirationDateTest() {

        String line = "law/rule";
        String result = bovespaExtractor.getExpirationDate(line);
        assertNull(result);
    }

    @Test
    public void getExpirationDateNotExpectedTest() {

        String line = " 2023/10 ";
        String result = bovespaExtractor.getExpirationDate(line);
        assertNull(result);
    }

    @Test
    public void getExpirationDateExpectedTest() {

        String line = " 06/23 ";
        String expeted = "2023-06";
        String result = bovespaExtractor.getExpirationDate(line);
        assertEquals(expeted, result);
    }


    @Test
    public void getOperationsTest() {

        List<Operation> bovespaExtractorOperations = bovespaExtractor.getOperations(pdfContent, fileId);

        Operation operation = bovespaExtractorOperations.get(0);
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
}
