package com.restaurante.service;

import com.restaurante.dto.AuthDTO;
import com.restaurante.entity.PasswordResetToken;
import com.restaurante.entity.Rol;
import com.restaurante.entity.Usuario;
import com.restaurante.exception.BusinessException;
import com.restaurante.exception.ResourceNotFoundException;
import com.restaurante.repository.PasswordResetTokenRepository;
import com.restaurante.repository.RolRepository;
import com.restaurante.repository.UsuarioRepository;
import com.restaurante.security.JwtUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final JwtUtils jwtUtils;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    @Transactional
    public AuthDTO.LoginResponse login(AuthDTO.LoginRequest request) {
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        Usuario usuario = usuarioRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        usuario.setUltimoAcceso(LocalDateTime.now());
        usuarioRepository.save(usuario);

        String token = jwtUtils.generateToken(usuario.getEmail(), usuario.getRol().getNombre());

        return new AuthDTO.LoginResponse(
                token,
                usuario.getId(),
                usuario.getNombre(),
                usuario.getEmail(),
                usuario.getRol().getNombre()
        );
    }

    @Transactional
    public void registrarCliente(AuthDTO.RegisterRequest request) {
        if (usuarioRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException("El correo ya está registrado");
        }
        if (request.getNombre() == null || request.getNombre().trim().isEmpty()) {
            throw new BusinessException("El nombre es obligatorio");
        }
        if (request.getPassword() == null || request.getPassword().length() < 6) {
            throw new BusinessException("La contraseña debe tener al menos 6 caracteres");
        }

        // Buscar rol CLIENTE (id 7)
        Rol rolCliente = rolRepository.findById(7)
                .orElseThrow(() -> new ResourceNotFoundException("Rol CLIENTE no encontrado", 7));

        Usuario usuario = Usuario.builder()
                .nombre(request.getNombre().trim())
                .email(request.getEmail().trim().toLowerCase())
                .password(passwordEncoder.encode(request.getPassword()))
                .telefono(request.getTelefono())
                .rol(rolCliente)
                .activo(true)
                .fechaCreacion(LocalDateTime.now())
                .build();

        usuarioRepository.save(usuario);
        log.info("Cliente registrado: {}", request.getEmail());
        emailService.enviarBienvenida(request.getEmail(), request.getNombre().trim());
    }

    @Transactional
    public void solicitarRecuperacion(String email) {
        // Siempre responde igual para no revelar si el email existe
        usuarioRepository.findByEmail(email).ifPresent(usuario -> {
            // Eliminar tokens anteriores del mismo email
            tokenRepository.deleteByEmail(email);

            String token = UUID.randomUUID().toString();
            PasswordResetToken resetToken = PasswordResetToken.builder()
                    .token(token)
                    .email(email)
                    .fechaExpiracion(LocalDateTime.now().plusHours(1))
                    .usado(false)
                    .build();
            tokenRepository.save(resetToken);

            // Por ahora lo logueamos — cuando tengas SMTP configurado
            // aquí va el envío del email
            emailService.enviarRecuperacionPassword(email, token);
        });
    }

    @Transactional
    public void resetearPassword(String token, String nuevaPassword) {
        PasswordResetToken resetToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new BusinessException("Token inválido o expirado"));

        if (resetToken.isExpirado()) {
            throw new BusinessException("El token ha expirado. Solicita uno nuevo");
        }
        if (resetToken.getUsado()) {
            throw new BusinessException("Este token ya fue utilizado");
        }
        if (nuevaPassword == null || nuevaPassword.length() < 6) {
            throw new BusinessException("La contraseña debe tener al menos 6 caracteres");
        }

        Usuario usuario = usuarioRepository.findByEmail(resetToken.getEmail())
                .orElseThrow(() -> new BusinessException("Usuario no encontrado"));

        usuario.setPassword(passwordEncoder.encode(nuevaPassword));
        usuarioRepository.save(usuario);

        resetToken.setUsado(true);
        tokenRepository.save(resetToken);

        log.info("Contraseña actualizada para: {}", resetToken.getEmail());
    }

    public boolean validarToken(String token) {
        return tokenRepository.findByToken(token)
                .map(t -> !t.isExpirado() && !t.getUsado())
                .orElse(false);
    }

    // ✅ NUEVO: Google Login
    @Transactional
    public AuthDTO.LoginResponse loginConGoogle(AuthDTO.GoogleRequest request) {
        Optional<Usuario> existente = usuarioRepository.findByEmail(request.getEmail());

        Usuario usuario;
        if (existente.isPresent()) {
            usuario = existente.get();
        } else {
            if (Boolean.TRUE.equals(request.getSoloExistentes())) {
                throw new BusinessException("El correo " + request.getEmail() + " no está registrado. Crea una cuenta primero.");
            }
            // Si no existe, crear cuenta con rol CLIENTE
            Rol rolCliente = rolRepository.findById(7)
                    .orElseThrow(() -> new ResourceNotFoundException("Rol CLIENTE", 7));
            usuario = Usuario.builder()
                    .nombre(request.getNombre())
                    .email(request.getEmail())
                    .password(passwordEncoder.encode(request.getGoogleId()))
                    .rol(rolCliente)
                    .activo(true)
                    .fechaCreacion(LocalDateTime.now())
                    .build();
            usuarioRepository.save(usuario);
            emailService.enviarBienvenida(request.getEmail(), request.getNombre());
        }

        usuario.setUltimoAcceso(LocalDateTime.now());
        usuarioRepository.save(usuario);

        String token = jwtUtils.generateToken(usuario.getEmail(), usuario.getRol().getNombre());
        return new AuthDTO.LoginResponse(
                token, usuario.getId(), usuario.getNombre(),
                usuario.getEmail(), usuario.getRol().getNombre()
        );
    }
}


