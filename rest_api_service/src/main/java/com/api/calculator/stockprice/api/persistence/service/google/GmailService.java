package com.api.calculator.stockprice.api.persistence.service.google;

import com.api.calculator.stockprice.api.persistence.model.GmailCredentials;

import javax.mail.MessagingException;
import java.io.IOException;

public interface GmailService {
    void setGmailCredentials(GmailCredentials gmailCredentials);

    boolean sendMessage(String recipientAddress, String subject, String body) throws MessagingException, IOException;
}
