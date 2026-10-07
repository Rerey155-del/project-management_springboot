package com.nodewave.portal.feature.auth;

import com.nodewave.portal.core.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "1. Feature Slice: Auth", description = "Autentikasi Pengguna & Profil Akun")
@RestController
@RequestMapping("/api")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @Operation(summary = "Login Pengguna", description = "Login menggunakan email dan password untuk mendapatkan token JWT Bearer.")
    @PostMapping("/auth/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Profil Pengguna Login", description = "Mengambil data profil pengguna yang sedang login berdasarkan token JWT.")
    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> getProfile(@AuthenticationPrincipal UserPrincipal principal) {
        UserDto userDto = authService.getCurrentUser(principal);
        return ResponseEntity.ok(Map.of(
                "message", "Ini adalah data profil anda",
                "data", userDto
        ));
    }
}
