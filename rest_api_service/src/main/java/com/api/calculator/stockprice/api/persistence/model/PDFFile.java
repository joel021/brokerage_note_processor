package com.api.calculator.stockprice.api.persistence.model;

import java.util.Date;
import java.util.Objects;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@JsonIdentityInfo(
        generator = ObjectIdGenerators.PropertyGenerator.class,
        property = "fileId")
@Entity(name = "file")
public class PDFFile {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable=false, unique=true, nullable=false)
    private UUID fileId;

    @NotNull(message = "A PDF file must be associated with an user.")
    private UUID userId;

    @NotBlank(message = "A note brokerage must have a name.")
    private String name;

    private String password;

    private Date extractedAt;

    private String stockBroker;

    private Date updatedAt;

    private Date deletedAt;

    public PDFFile(UUID fileId, UUID userId, String name, String password, String stockBroker, Date updatedAt){
        this.fileId = fileId;
        this.userId = userId;
        this.name = name;
        this.password = password;
        this.stockBroker = stockBroker;
        this.updatedAt = updatedAt;
    }

    public PDFFile(){}

    @Override
    public boolean equals(Object arg0) {

        if (!(arg0 instanceof PDFFile)){
            return false;
        }

        PDFFile pdfFile = (PDFFile) arg0;

        return pdfFile.getFileId() == this.fileId || Objects.equals(pdfFile.getName(), this.name);
    }

    @Override
    public int hashCode(){
        return 1;
    }

    public UUID getFileId() {
        return fileId;
    }

    public void setFileId(UUID fileId) {
        this.fileId = fileId;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Date getExtractedAt() {
        return extractedAt;
    }

    public void setExtractedAt(Date extractedAt) {
        this.extractedAt = extractedAt;
    }

    public String getStockBroker() {
        return stockBroker;
    }

    public void setStockBroker(String stockBroker) {
        this.stockBroker = stockBroker;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Date getDeletedAt() {
        return deletedAt;
    }

    public void setDeletedAt(Date deletedAt) {
        this.deletedAt = deletedAt;
    }

}
