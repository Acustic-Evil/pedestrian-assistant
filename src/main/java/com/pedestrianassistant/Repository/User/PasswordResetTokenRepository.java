package com.pedestrianassistant.Repository.User;

import com.pedestrianassistant.Model.User.PasswordResetToken;
import com.pedestrianassistant.Model.User.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, UUID> {
    Optional<PasswordResetToken> findByToken(String token);
    void deleteByUser(User user);
}
