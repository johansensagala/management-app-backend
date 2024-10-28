package com.johansen.management_app_backend.controller;

import com.johansen.management_app_backend.model.User;
import com.johansen.management_app_backend.service.UserService;
import com.johansen.management_app_backend.util.JwtUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    private final JwtUtil jwtUtil;

    public AuthController(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/register")
    public ResponseEntity<String> registerUser(@RequestBody User user) {
        try {
            User createdUser = userService.registerUser(user);
            return new ResponseEntity<>("User registered successfully", HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody User user, HttpServletResponse response) {
        try {
            String token = userService.authenticateUser(user);
            Cookie cookie = new Cookie("token", token);
            cookie.setHttpOnly(true);
            cookie.setPath("/");
            cookie.setMaxAge(10 * 60 * 60);
            response.addCookie(cookie);

            Map<String, String> responseBody = new HashMap<>();
            responseBody.put("token", token);
            return new ResponseEntity<>(responseBody, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.UNAUTHORIZED);
        }
    }

    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = userService.getAllUsers();
        return new ResponseEntity<>(users, HttpStatus.OK);
    }

    @GetMapping("/users/not-in-members")
    public ResponseEntity<List<User>> getUsersNotInMembers() {
        List<User> users = userService.getAllUsersNotInMembers();
        return new ResponseEntity<>(users, HttpStatus.OK);
    }

    @PostMapping("/register-many")
    public ResponseEntity<String> registerManyUsers(@RequestBody List<User> users) {
        StringBuilder responseMessage = new StringBuilder();
        int successCount = 0;
        int failureCount = 0;

        for (User user : users) {
            try {
                userService.registerUser(user);
                successCount++;
            } catch (IllegalArgumentException e) {
                responseMessage.append("Failed to register user: ").append(user.getName()).append(" - ").append(e.getMessage()).append("\n");
                failureCount++;
            }
        }

        if (failureCount > 0) {
            return new ResponseEntity<>(responseMessage.toString(), HttpStatus.BAD_REQUEST);
        } else {
            return new ResponseEntity<>("Successfully registered " + successCount + " users.", HttpStatus.CREATED);
        }
    }
}
