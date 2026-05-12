package com.restaurante.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import jakarta.mail.internet.MimeMessage;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Async
    public void enviarRecuperacionPassword(String destinatario, String token) {
        try {
            MimeMessage mensaje = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mensaje, true, "UTF-8");

            helper.setFrom("gamargabriel@gmail.com", "Fast & Healthy");
            helper.setTo(destinatario);
            helper.setSubject("🔐 Recuperar contraseña — Fast & Healthy");

            String url = "http://localhost:5173/reset-password?token=" + token;
            String html = """
                <div style="font-family: Arial, sans-serif; max-width: 500px; margin: 0 auto; background: #0a0a0a; color: #f0f0f0; border-radius: 16px; overflow: hidden;">
                    <div style="background: linear-gradient(135deg, #c00000, #e00000); padding: 32px; text-align: center;">
                        <div style="font-size: 48px;">🐔</div>
                        <h1 style="color: white; margin: 8px 0; font-size: 24px;">Fast & Healthy</h1>
                    </div>
                    <div style="padding: 32px;">
                        <h2 style="color: #f0f0f0; margin-bottom: 12px;">Recuperar contraseña</h2>
                        <p style="color: #aaa; line-height: 1.6;">Recibimos una solicitud para restablecer tu contraseña. Haz clic en el botón para crear una nueva:</p>
                        <div style="text-align: center; margin: 28px 0;">
                            <a href="%s" style="background: linear-gradient(135deg, #c00000, #e00000); color: white; padding: 14px 32px; border-radius: 10px; text-decoration: none; font-weight: bold; font-size: 15px;">
                                🔐 Restablecer contraseña
                            </a>
                        </div>
                        <p style="color: #666; font-size: 12px; text-align: center;">Este enlace expira en 1 hora. Si no solicitaste esto, ignora este correo.</p>
                    </div>
                </div>
            """.formatted(url);

            helper.setText(html, true);
            mailSender.send(mensaje);
            log.info("✅ Email de recuperación enviado a: {}", destinatario);
        } catch (Exception e) {
            log.error("❌ Error enviando email a {}: {}", destinatario, e.getMessage());
        }
    }

    @Async
    public void enviarBienvenida(String destinatario, String nombre) {
        try {
            MimeMessage mensaje = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mensaje, true, "UTF-8");

            helper.setFrom("gamargabriel@gmail.com", "Fast & Healthy");
            helper.setTo(destinatario);
            helper.setSubject("🎉 Bienvenido a Fast & Healthy");

            String html = """
                <div style="font-family: Arial, sans-serif; max-width: 500px; margin: 0 auto; background: #0a0a0a; color: #f0f0f0; border-radius: 16px; overflow: hidden;">
                    <div style="background: linear-gradient(135deg, #c00000, #e00000); padding: 32px; text-align: center;">
                        <div style="font-size: 48px;">🐔</div>
                        <h1 style="color: white; margin: 8px 0; font-size: 24px;">Fast & Healthy</h1>
                    </div>
                    <div style="padding: 32px;">
                        <h2 style="color: #f0f0f0;">¡Hola, %s! 👋</h2>
                        <p style="color: #aaa; line-height: 1.6;">Tu cuenta ha sido creada exitosamente. Ya puedes ver nuestro menú y disfrutar de la mejor comida.</p>
                        <div style="text-align: center; margin: 28px 0;">
                            <a href="http://localhost:5173/login" style="background: linear-gradient(135deg, #c00000, #e00000); color: white; padding: 14px 32px; border-radius: 10px; text-decoration: none; font-weight: bold; font-size: 15px;">
                                🍽️ Ver el menú
                            </a>
                        </div>
                        <p style="color: #666; font-size: 12px; text-align: center;">Fast & Healthy — Sistema de Gestión</p>
                    </div>
                </div>
            """.formatted(nombre);

            helper.setText(html, true);
            mailSender.send(mensaje);
            log.info("✅ Email de bienvenida enviado a: {}", destinatario);
        } catch (Exception e) {
            log.error("❌ Error enviando bienvenida a {}: {}", destinatario, e.getMessage());
        }
    }
}