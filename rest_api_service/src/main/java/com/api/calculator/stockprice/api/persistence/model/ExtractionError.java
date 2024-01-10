package com.api.calculator.stockprice.api.persistence.model;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;

import java.util.UUID;

@Data
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
}