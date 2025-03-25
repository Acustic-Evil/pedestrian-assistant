package com.pedestrianassistant.Service.User;

import com.pedestrianassistant.Model.User.PasswordResetToken;
import com.pedestrianassistant.Model.User.User;
import com.pedestrianassistant.Repository.User.PasswordResetTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PasswordResetTokenServiceImpl implements PasswordResetTokenService {

    private final PasswordResetTokenRepository tokenRepository;

    @Override
    public PasswordResetToken createToken(User user) {
        tokenRepository.deleteByUser(user); // one token for one user

        String rawToken = UUID.randomUUID().toString();
        Instant expiryDate = Instant.now().plus(15, ChronoUnit.MINUTES);

        PasswordResetToken token = new PasswordResetToken();
        token.setToken(rawToken);
        token.setUser(user);
        token.setExpiryDate(expiryDate);

        return tokenRepository.save(token);
    }

    @Override
    public PasswordResetToken validateToken(String token) {
        return tokenRepository.findByToken(token)
                .filter(t -> t.getExpiryDate().isAfter(Instant.now()))
                .orElseThrow(() -> new RuntimeException("Token is invalid or expired"));
    }

    @Override
    public void removeToken(String token) {
        tokenRepository.findByToken(token).ifPresent(tokenRepository::delete);
    }
}
