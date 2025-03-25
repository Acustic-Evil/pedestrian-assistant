package com.pedestrianassistant.Config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;

@Configuration
public class EmailConfig {

    @Value("${spring.mail.host}")
    private String host;

    @Value("${spring.mail.port}")
    private int port;

    @Value("${spring.mail.username}")
    private String username;

    @Value("${spring.mail.password}")
    private String password;

    @Value("${spring.mail.properties.mail.smtp.auth}")
    private boolean smtpAuth;

    @Value("${spring.mail.properties.mail.smtp.starttls.enable}")
    private boolean tlsEnabled;

    @Value("${spring.mail.properties.mail.smtp.starttls.required}")
    private boolean tlsRequired;

    @Value("${spring.mail.protocol}")
    private String protocol;

    @Bean
    public JavaMailSenderImpl javaMailSender() {
        JavaMailSenderImpl sender = new JavaMailSenderImpl();
        sender.setHost(host);
        sender.setPort(port);
        sender.setUsername(username);
        sender.setPassword(password);

        Properties props = new Properties();
        props.put("mail.smtp.auth", String.valueOf(smtpAuth));
        props.put("mail.smtp.starttls.enable", tlsEnabled);
        props.put("mail.smtp.starttls.required", tlsRequired);
        props.put("mail.smtp.ssl.trust", host);
        props.put("mail.transport.protocol", protocol);
        props.put("mail.debug", "true");

        sender.setJavaMailProperties(props);
        return sender;
    }
}
