package com.restaurante.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

public class AuthDTO {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LoginRequest {
        @NotBlank(message = "El email es obligatorio")
        @Email(message = "Formato de email inválido")
        private String email;

        @NotBlank(message = "La contraseña es obligatoria")
        private String password;
    }

    @Data
    @NoArgsConstructor
    public static class LoginResponse {
        private String token;
        private String tipo = "Bearer";
        private Integer id;
        private String nombre;
        private String email;
        private String rol;

        public LoginResponse(String token, Integer id, String nombre, String email, String rol) {
            this.token = token;
            this.id = id;
            this.nombre = nombre;
            this.email = email;
            this.rol = rol;
        }
    }

    // ✅ NUEVO: Registro de usuario
    @Data
    public static class RegisterRequest {
        private String nombre;
        private String email;
        private String password;
        private String telefono;
    }

    // ✅ NUEVO: Solicitud para recuperar contraseña
    @Data
    public static class ForgotPasswordRequest {
        private String email;
    }

    // ✅ NUEVO: Resetear contraseña
    @Data
    public static class ResetPasswordRequest {
        private String token;
        private String nuevaPassword;
    }

    // ✅ NUEVO: Google Login
    @Data
    public static class GoogleRequest {
        private String email;
        private String nombre;
        private String googleId;
        private Boolean soloExistentes;
    }


}
