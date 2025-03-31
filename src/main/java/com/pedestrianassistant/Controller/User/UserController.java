package com.pedestrianassistant.Controller.User;

import com.pedestrianassistant.Dto.Response.Core.UserResponseDto;
import com.pedestrianassistant.Model.User.User;
import com.pedestrianassistant.Service.User.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/user/users")
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Retrieve all users.
     *
     * @return A list of all users.
     */
    @GetMapping()
    public ResponseEntity<List<UserResponseDto>> getAllUsers() {
        List<User> users = userService.findAll();

        List<UserResponseDto> userDtos = users.stream()
                .map(u -> new UserResponseDto(u.getId(), u.getUsername()))
                .toList();

        return ResponseEntity.ok(userDtos);
    }
}

