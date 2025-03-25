package com.pedestrianassistant.Util.Url;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ResetUrlBuilder {

    @Value("${frontend.reset-password-url}")
    private String baseResetUrl;

    /**
     * Builds a full reset password URL with the token as a query parameter.
     *
     * @param token the password reset token
     * @return full URL to send in email
     */
    public String buildUrl(String token) {
        return baseResetUrl + "?token=" + token;
    }
}
