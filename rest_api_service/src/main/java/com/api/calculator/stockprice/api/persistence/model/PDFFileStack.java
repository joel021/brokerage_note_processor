package com.api.calculator.stockprice.api.persistence.model;

public class PDFFileStack {

    private String uri;
    private String id;
    private String userId;

    public PDFFileStack(String userId, String id, String uri){

    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
