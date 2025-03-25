package com.pedestrianassistant.Service.User;

import com.pedestrianassistant.Model.User.PasswordResetToken;
import com.pedestrianassistant.Model.User.User;

public interface PasswordResetTokenService {

    /**
     * Creates and stores a new password reset token for the given user.
     * If the user already has an active token, it will be replaced.
     *
     * @param user the user requesting the password reset
     * @return the generated PasswordResetToken entity
     */
    PasswordResetToken createToken(User user);

    /**
     * Validates a password reset token.
     * Checks both existence and expiration.
     *
     * @param token the raw token string to validate
     * @return the associated PasswordResetToken if valid
     * @throws RuntimeException if the token is invalid or expired
     */
    PasswordResetToken validateToken(String token);

    /**
     * Deletes a password reset token after use or invalidation.
     *
     * @param token the token string to remove
     */
    void removeToken(String token);
}
