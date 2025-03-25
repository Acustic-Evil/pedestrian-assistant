package com.pedestrianassistant.Security.Service;

import com.pedestrianassistant.Dto.Request.Auth.AuthRequest;
import com.pedestrianassistant.Dto.Request.Auth.ResetPasswordRequest;
import com.pedestrianassistant.Dto.Request.Auth.SignUpRequest;
import com.pedestrianassistant.Dto.Response.Auth.AuthResponse;
import com.pedestrianassistant.Model.User.Role;
import com.pedestrianassistant.Model.User.User;
import com.pedestrianassistant.Security.Jwt.JwtService;
import com.pedestrianassistant.Security.User.UserDetailsImpl;
import com.pedestrianassistant.Service.Email.EmailService;
import com.pedestrianassistant.Service.User.PasswordResetTokenService;
import com.pedestrianassistant.Service.User.RoleService;
import com.pedestrianassistant.Service.User.UserService;
import com.pedestrianassistant.Util.Url.ResetUrlBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserService userService;
    private final RoleService roleService;
    private final PasswordEncoder passwordEncoder;

    private final PasswordResetTokenService passwordResetTokenService;
    private final ResetUrlBuilder resetUrlBuilder;
    private final EmailService emailService;

    public AuthResponse authenticate(AuthRequest request) {
        try {
            String loginIdentifier = request.getUsername() != null ? request.getUsername() : request.getEmail();
            if (loginIdentifier == null || loginIdentifier.isBlank()) {
                throw new RuntimeException("Username or email must be provided");
            }

            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginIdentifier,
                            request.getPassword()
                    )
            );

            User user = Optional.ofNullable(request.getUsername())
                    .flatMap(userService::findByUsername)
                    .or(() -> userService.findByEmail(request.getEmail()))
                    .orElseThrow(() -> new RuntimeException("User not found"));

            UserDetails userDetails = UserDetailsImpl.build(user);

            String jwt = jwtService.generateToken(
                    userDetails.getUsername(),
                    user.getId(),
                    user.getRole().getName()
            );

            return new AuthResponse(jwt);

        } catch (AuthenticationException e) {
            throw new RuntimeException("Invalid credentials");
        }
    }

    public AuthResponse register(SignUpRequest request) {
        if (userService.findByUsername(request.getUsername()).isPresent()) {
            throw new RuntimeException("Username is already taken");
        }

        if (userService.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("Email is already registered");
        }

        Role defaultRole = roleService.findByName("ROLE_USER")
                .orElseThrow(() -> new RuntimeException("Default role not found"));

        User newUser = new User();
        newUser.setName(request.getName());
        newUser.setSurname(request.getSurname());
        newUser.setUsername(request.getUsername());
        newUser.setEmail(request.getEmail());
        newUser.setPassword(passwordEncoder.encode(request.getPassword()));
        newUser.setRole(defaultRole);

        User savedUser = userService.save(newUser);

        String jwt = jwtService.generateToken(
                savedUser.getUsername(),
                savedUser.getId(),
                savedUser.getRole().getName()
        );

        return new AuthResponse(jwt);
    }

    public void confirmResetPassword(ResetPasswordRequest request) {
        var tokenEntity = passwordResetTokenService.validateToken(request.getToken());
        User user = tokenEntity.getUser();

        // if the new password matches the old one, the exception occurs
        if (passwordEncoder.matches(request.getNewPassword(), user.getPassword())) {
            throw new RuntimeException("New password must be different from the current password.");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userService.save(user); // resetting the password

        passwordResetTokenService.removeToken(request.getToken()); // deleting token
    }

    /**
     * Sends a password reset link to the user's email.
     *
     * @param email the user's email
     */
    @Transactional
    public void sendPasswordResetLink(String email) {
        User user = userService.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Email not found"));

        var token = passwordResetTokenService.createToken(user);
        String resetUrl = resetUrlBuilder.buildUrl(token.getToken());

        String subject = "Восстановление пароля";
        String body = String.format("""
                Здравствуйте, %s!
                
                Мы получили запрос на восстановление пароля для вашей учётной записи.
                
                Перейдите по ссылке ниже, чтобы задать новый пароль:
                %s
                
                Если вы не запрашивали восстановление, просто проигнорируйте это письмо.
                """, user.getUsername(), resetUrl);

        emailService.send(user.getEmail(), subject, body);
    }
}
