package com.api.calculator.stockprice.brokerage.note.extractor;

import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.parser.PdfReaderContentParser;
import com.itextpdf.text.pdf.parser.SimpleTextExtractionStrategy;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class TextExtractor {

    public List<String> parseFromStream(InputStream pdfInputStream, String password) throws IOException {
        return parseFromReader(new PdfReader(pdfInputStream, password.getBytes()));
    }

    public List<String> parseFromBytes(byte[] pdfInputStream) throws IOException {
        return parseFromReader(new PdfReader(pdfInputStream));
    }

    public List<String> parseFromFileUri(String fileUri) throws IOException{
        return parseFromReader(new PdfReader(fileUri));
    }

    private List<String> parseFromReader(PdfReader reader) throws IOException {
        PdfReaderContentParser parser = new PdfReaderContentParser(reader);
        List<String> pages = new ArrayList<>();

        for (int i = 1; i <= reader.getNumberOfPages(); i++) {
            pages.add(parser.processContent(i, new SimpleTextExtractionStrategy()).getResultantText());
        }
        reader.close();
        return pages;
    }
}
