package com.restaurante.config;

import com.restaurante.security.JwtAuthFilter;
import com.restaurante.security.UserDetailsServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;
    private final UserDetailsServiceImpl userDetailsService;

    @Bean
    public PasswordEncoder passwordEncoder() {
        // Fuerza 12 para mayor seguridad en el hash de Aiven
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        // En producción (Render), lo ideal es poner la URL de tu Frontend aquí
        config.setAllowedOriginPatterns(List.of("*"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setExposedHeaders(List.of("Authorization"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // Deshabilitado para APIs REST (Stateless)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // No usamos JSESSIONID
                .authorizeHttpRequests(auth -> auth
                        // 1. Peticiones OPTIONS (CORS) siempre permitidas
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // 2. Rutas de Autenticación (Login y Registro)
                        // Esto permite que /api/auth/login y /api/auth/register sean públicos
                        .requestMatchers("/api/auth/**").permitAll()

                        // 3. Recursos públicos (Menú del restaurante)
                        .requestMatchers(HttpMethod.GET, "/api/productos/menu").permitAll()

                        // 4. Reglas por Roles (Ajustadas a tu modelo de negocio)
                        .requestMatchers("/api/usuarios/**").hasAnyRole("ADMIN", "GERENTE")
                        .requestMatchers("/api/roles/**").hasAnyRole("ADMIN", "GERENTE")
                        .requestMatchers("/api/reportes/**").hasAnyRole("ADMIN", "GERENTE")
                        .requestMatchers("/api/inventario/**").hasAnyRole("ADMIN", "GERENTE")
                        .requestMatchers("/api/compras/**").hasAnyRole("ADMIN", "GERENTE")
                        .requestMatchers("/api/proveedores/**").hasAnyRole("ADMIN", "GERENTE")
                        .requestMatchers("/api/cocina/**").hasAnyRole("ADMIN", "GERENTE", "COCINERO")
                        .requestMatchers("/api/turnos/**").hasAnyRole("ADMIN", "GERENTE", "CAJERO")

                        // 5. Cualquier otra ruta requiere Token válido
                        .anyRequest().authenticated()
                )
                // Usamos nuestro proveedor con BCrypt y UserDetailsService
                .authenticationProvider(authenticationProvider())
                // Inyectamos el filtro JWT antes del filtro de usuario/password de Spring
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}