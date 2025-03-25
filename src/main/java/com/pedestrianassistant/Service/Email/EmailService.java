package com.pedestrianassistant.Service.Email;


public interface EmailService {

    /**
     * Sends a plain text email.
     *
     * @param to      recipient email address
     * @param subject email subject
     * @param text    email content
     */
    void send(String to, String subject, String text);
}
