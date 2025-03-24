package com.pedestrianassistant.Controller.Auth;

import com.pedestrianassistant.Dto.Request.Auth.AuthRequest;
import com.pedestrianassistant.Dto.Request.Auth.SignUpRequest;
import com.pedestrianassistant.Dto.Response.Auth.AuthResponse;
import com.pedestrianassistant.Security.Service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

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
}
