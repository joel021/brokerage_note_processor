package com.api.calculator.stockprice.ws.data.service.google;

import com.api.calculator.stockprice.ws.data.model.GmailCredentials;

import javax.mail.MessagingException;
import java.io.IOException;

public interface GmailService {
    void setGmailCredentials(GmailCredentials gmailCredentials);

    boolean sendMessage(String recipientAddress, String subject, String body) throws MessagingException, IOException;
}
