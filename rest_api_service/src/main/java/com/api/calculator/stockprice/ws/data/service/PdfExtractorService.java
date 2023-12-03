package com.api.calculator.stockprice.ws.data.service;

import com.api.calculator.stockprice.exceptions.InternalException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class PdfExtractorService {

    @Value("${pdf.extractor.service.url}")
    private String pdfExtractorServiceUrl;

    public void requestToExtractAllOfUser(UUID userId, String baseUri) throws InternalException {

        Map<String, String> userInfo = new HashMap<>();
        userInfo.put("userId", userId.toString());
        userInfo.put("baseUri", baseUri);

        try {
            if (HttpRequest.post(pdfExtractorServiceUrl + "/brokerage_note",
                    new ObjectMapper().writeValueAsString(userInfo)) != 204){
                throw new IOException();
            }
        } catch (IOException e) {
            throw new InternalException("Não foi possível extrair as informações dos seus PDFs.");
        }
    }
}
