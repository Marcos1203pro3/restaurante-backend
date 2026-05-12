package com.restaurante.controller;

import com.restaurante.dto.ApiResponse;
import com.restaurante.dto.AuthDTO;
import com.restaurante.entity.Usuario;
import com.restaurante.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthDTO.LoginResponse>> login(
            @Valid @RequestBody AuthDTO.LoginRequest request) {
        AuthDTO.LoginResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.ok("Login exitoso", response));
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<String>> register(@RequestBody AuthDTO.RegisterRequest request) {
        authService.registrarCliente(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Cliente registrado exitosamente", null));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<String>> forgotPassword(@RequestBody AuthDTO.ForgotPasswordRequest request) {
        authService.solicitarRecuperacion(request.getEmail());
        return ResponseEntity.ok(ApiResponse.ok("Si el correo existe, recibirás instrucciones", null));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<String>> resetPassword(@RequestBody AuthDTO.ResetPasswordRequest request) {
        authService.resetearPassword(request.getToken(), request.getNuevaPassword());
        return ResponseEntity.ok(ApiResponse.ok("Contraseña actualizada exitosamente", null));
    }

    @GetMapping("/validate-token")
    public ResponseEntity<ApiResponse<Boolean>> validateToken(@RequestParam String token) {
        boolean valido = authService.validarToken(token);
        return ResponseEntity.ok(ApiResponse.ok("Token validado", valido));
    }

    // ✅ NUEVO: Google Login
    @PostMapping("/google")
    public ResponseEntity<ApiResponse<AuthDTO.LoginResponse>> loginGoogle(
            @RequestBody AuthDTO.GoogleRequest request) {
        AuthDTO.LoginResponse response = authService.loginConGoogle(request);
        return ResponseEntity.ok(ApiResponse.ok("Login exitoso", response));
    }
}
