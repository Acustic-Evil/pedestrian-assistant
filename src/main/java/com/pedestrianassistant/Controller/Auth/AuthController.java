package com.pedestrianassistant.Controller.Auth;

import com.pedestrianassistant.Dto.Request.Auth.AuthRequest;
import com.pedestrianassistant.Dto.Request.Auth.ResetPasswordRequest;
import com.pedestrianassistant.Dto.Request.Auth.SignUpRequest;
import com.pedestrianassistant.Dto.Response.Auth.AuthResponse;
import com.pedestrianassistant.Security.Service.AuthService;
import com.pedestrianassistant.Service.User.PasswordResetTokenService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin("http://localhost:3000/")
public class AuthController {

    private final AuthService authService;
    private final PasswordResetTokenService passwordResetTokenService;

    @PostMapping("/")
    public ResponseEntity<AuthResponse> login(@RequestBody AuthRequest request) {
        AuthResponse response = authService.authenticate(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody SignUpRequest request) {
        AuthResponse response = authService.register(request);
        return ResponseEntity.ok(response);

    }

    @GetMapping("/reset/validate")
    public ResponseEntity<?> validateResetToken(@RequestParam String token) {
        try {
            passwordResetTokenService.validateToken(token);
            return ResponseEntity.ok("Token is valid");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("Invalid or expired token");
        }
    }

    @PostMapping("/reset/request")
    public ResponseEntity<?> requestReset(@RequestParam String email) {
        authService.sendPasswordResetLink(email);
        return ResponseEntity.ok("Password reset link sent");
    }

    @PostMapping("/reset/confirm")
    public ResponseEntity<?> confirmReset(@Valid @RequestBody ResetPasswordRequest request) {
        authService.confirmResetPassword(request);
        return ResponseEntity.ok("Password successfully reset");
    }
}
