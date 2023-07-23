package com.api.calculator.stockprice.ws.data.service;

import com.api.calculator.stockprice.ws.data.repository.ExtractionErrorRepository;
import com.api.calculator.stockprice.ws.data.model.ExtractionError;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class ExtractionErrorService {

    @Autowired
    private ExtractionErrorRepository extractionErrorRepository;

    public List<ExtractionError> findByFileId(UUID fileId){
        return this.extractionErrorRepository.findByFileId(fileId);
    }

    public int countByFileId(UUID fileId){
        return this.extractionErrorRepository.countByFileId(fileId);
    }

    public void save(ExtractionError extractionError){
        extractionErrorRepository.save(extractionError);
    }
}
