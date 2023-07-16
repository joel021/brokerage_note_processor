package com.api.calculator.stockprice.model;

public class UserExtended extends User {
    private String passwordConfirmation;
    private String token;

    public UserExtended(User user){
        super(user.getId(), user.getName(), user.getEmail(), user.getPassword(), user.getRole(), user.getVerificationCode());
    }

    public UserExtended(){
        super();
    }
    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getPasswordConfirmation() {
        return passwordConfirmation;
    }

    public void setPasswordConfirmation(String passwordConfirmation) {
        this.passwordConfirmation = passwordConfirmation;
    }
}