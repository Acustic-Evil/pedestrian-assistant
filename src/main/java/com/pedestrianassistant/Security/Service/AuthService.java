package com.pedestrianassistant.Security.Service;

import com.pedestrianassistant.Dto.Request.Auth.AuthRequest;
import com.pedestrianassistant.Dto.Request.Auth.SignUpRequest;
import com.pedestrianassistant.Dto.Response.Auth.AuthResponse;
import com.pedestrianassistant.Model.User.Role;
import com.pedestrianassistant.Security.Jwt.JwtService;
import com.pedestrianassistant.Security.User.UserDetailsImpl;
import com.pedestrianassistant.Model.User.User;
import com.pedestrianassistant.Service.User.RoleService;
import com.pedestrianassistant.Service.User.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
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
        newUser.setPassword(passwordEncoder.encode(request.getPassword())); // Securely encoded
        newUser.setRole(defaultRole);

        User savedUser = userService.save(newUser);

        String jwt = jwtService.generateToken(
                savedUser.getUsername(),
                savedUser.getId(),
                savedUser.getRole().getName()
        );

        return new AuthResponse(jwt);
    }


}
