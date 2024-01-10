package com.api.calculator.stockprice.brokerage.extractor;

import com.api.calculator.stockprice.brokerage.extractor.extractor.TextExtractor;
import org.junit.Before;
import org.junit.Test;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class ExtractorTests {

    private TextExtractor textExtractor;

    private List<File> pdfFiles;

    @Before
    public void setup() throws IOException {

        textExtractor = new TextExtractor();
        Path pdfsToTest = Paths.get("static/pdf_tests/pdfs");
        Files.createDirectories(pdfsToTest);
        assert Objects.requireNonNull(pdfsToTest.toFile().listFiles()).length > 0;

        pdfFiles = Arrays.stream(pdfsToTest.toFile().listFiles()).toList();
    }

    @Test(expected = IOException.class)
    public void parseFromFileUriNullTest() throws IOException {

        textExtractor.parseFromFileUri(null);
    }

    @Test
    public void parseFromFileUriTest() throws IOException {

        List<String> pages = textExtractor.parseFromFileUri(pdfFiles.get(0).toString());
        assert !pages.isEmpty();
    }

}
