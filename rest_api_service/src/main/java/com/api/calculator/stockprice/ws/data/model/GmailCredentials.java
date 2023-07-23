package com.api.calculator.stockprice.ws.data.model;

public class GmailCredentials {

    private final String userEmail;
    private final String clientId;
    private final String clientSecret;

    private final String accessToken;
    private final String refreshToken;

    public GmailCredentials() {
        refreshToken = "1//0hsL-XDuLGnIGCgYIARAAGBESNwF-L9IrPeIzmaN1KqS0jAZx46p99Dr7k-a4PTyjY8xbEDk1THwKH4Jsfl0I3HBdqHoOp3qlB1Y";
        accessToken = "ya29.a0AVvZVsoD0t7h10dr-jjnuGk16_uOp3ogkhAIcbAuMdQGCuW5rTReZ9q0hw5CyX20_4EmEV3M97e-jfWR39C3AxVk6jO75ZFojBvJzif11kOkPD1KZ8uZ3cE4fSOEgKORz3JJxcNgmzWLfW5iMoBm8ViIcWXxaCgYKAcASARESFQGbdwaI-SFkhwsPgSfTchcm2XvddQ0163";
        clientSecret = "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";
        clientId = "960189672126-72jgbbn51f9dsp0g3q424mic9e30k3rf.apps.googleusercontent.com";
        userEmail = "pires.joel.ceo@gmail.com";
    }

    public String getUserEmail() {
        return userEmail;
    }

    public String getClientId() {
        return clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }
}