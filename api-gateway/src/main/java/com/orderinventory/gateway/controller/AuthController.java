package com.orderinventory.gateway.controller;

import com.orderinventory.gateway.dto.AuthRequest;
import com.orderinventory.gateway.dto.AuthResponse;
import com.orderinventory.gateway.util.JwtUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final JwtUtil jwtUtil;

    // In-memory credential store for authentication demo
    private final Map<String, UserRecord> users = new ConcurrentHashMap<>();

    public AuthController(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;

        // Pre-configured default users
        users.put("admin", new UserRecord(1L, "admin", "admin123", "ADMIN"));
        users.put("john", new UserRecord(101L, "john", "password123", "CUSTOMER"));
        users.put("jane", new UserRecord(102L, "jane", "password123", "CUSTOMER"));
    }

    @PostMapping("/login")
    public Mono<ResponseEntity<?>> login(@RequestBody AuthRequest authRequest) {
        if (authRequest.getUsername() == null || authRequest.getPassword() == null) {
            return Mono.just(ResponseEntity.badRequest().body(Map.of("error", "Username and password are required")));
        }

        UserRecord user = users.get(authRequest.getUsername().trim());
        if (user != null && user.password().equals(authRequest.getPassword())) {
            String token = jwtUtil.generateToken(user.username(), user.id(), user.role());
            AuthResponse response = AuthResponse.builder()
                    .token(token)
                    .type("Bearer")
                    .userId(user.id())
                    .username(user.username())
                    .role(user.role())
                    .expiresInMs(jwtUtil.getExpirationMs())
                    .build();
            return Mono.just(ResponseEntity.ok(response));
        }

        return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("error", "Invalid username or password", "status", 401)));
    }

    @PostMapping("/register")
    public Mono<ResponseEntity<?>> register(@RequestBody AuthRequest authRequest) {
        if (authRequest.getUsername() == null || authRequest.getPassword() == null) {
            return Mono.just(ResponseEntity.badRequest().body(Map.of("error", "Username and password are required")));
        }

        String username = authRequest.getUsername().trim();
        if (users.containsKey(username)) {
            return Mono.just(ResponseEntity.badRequest().body(Map.of("error", "Username already exists")));
        }

        long newId = 200L + users.size();
        UserRecord newUser = new UserRecord(newId, username, authRequest.getPassword(), "CUSTOMER");
        users.put(username, newUser);

        String token = jwtUtil.generateToken(newUser.username(), newUser.id(), newUser.role());
        AuthResponse response = AuthResponse.builder()
                .token(token)
                .type("Bearer")
                .userId(newUser.id())
                .username(newUser.username())
                .role(newUser.role())
                .expiresInMs(jwtUtil.getExpirationMs())
                .build();
        return Mono.just(ResponseEntity.status(HttpStatus.CREATED).body(response));
    }

    private record UserRecord(Long id, String username, String password, String role) {}
}
