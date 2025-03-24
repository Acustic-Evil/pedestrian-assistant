package com.pedestrianassistant.Dto.Request.Auth;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AuthRequest {
    private String username; // optional
    private String email;    // optional
    private String password;
}
