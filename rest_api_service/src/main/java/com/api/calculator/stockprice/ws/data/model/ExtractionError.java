package com.api.calculator.stockprice.ws.data.model;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import java.util.UUID;

@JsonIdentityInfo(
        generator = ObjectIdGenerators.PropertyGenerator.class,
        property = "extractionErrorId")
@Entity(name = "extraction_error")
public class ExtractionError {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer extractionErrorId;

    private String error;

    private UUID fileId;

    public ExtractionError(){

    }

    public ExtractionError(Integer extractionErrorId, String error, UUID fileId){
        this.extractionErrorId = extractionErrorId;
        this.error = error;
        this.fileId = fileId;
    }

    public Integer getExtractionErrorId() {
        return extractionErrorId;
    }

    public void setExtractionErrorId(Integer extractionErrorId) {
        this.extractionErrorId = extractionErrorId;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public UUID getFileId() {
        return fileId;
    }

    public void setFileId(UUID fileId) {
        this.fileId = fileId;
    }
}