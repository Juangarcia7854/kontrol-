package com.tusistema.sistemaventas.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.SimpMessageType;
import org.springframework.security.config.annotation.web.messaging.MessageSecurityMetadataSourceRegistry;
import org.springframework.security.config.annotation.web.socket.AbstractSecurityWebSocketMessageBrokerConfigurer;

// @Configuration: Marca la clase como configuración de Spring.
// extends AbstractSecurityWebSocketMessageBrokerConfigurer:
// Esta clase base permite integrar la seguridad de Spring (Spring Security) con los WebSockets.
// Sin esto, los WebSockets vivirían en un mundo aparte sin saber quién es el usuario logueado.
@Configuration
public class WebSocketSecurityConfig extends AbstractSecurityWebSocketMessageBrokerConfigurer {

    // Este método configura las reglas de autorización para los mensajes que llegan al servidor.
    @Override
    protected void configureInbound(MessageSecurityMetadataSourceRegistry messages) {
        messages
            // .anyMessage().permitAll():
            // Aquí estás diciendo: "Deja pasar TODO".
            // CUALQUIER usuario (incluso anónimo) puede conectarse, suscribirse y enviar mensajes.
            // 
            // NOTA: En un entorno de producción real con datos sensibles, esto suele cambiarse
            // para requerir autenticación, pero para desarrollo facilita mucho probar sin errores de "403 Forbidden".
            .anyMessage().permitAll();
            
            // El bloque comentado abajo es un ejemplo de cómo sería una configuración "Estricta":
            // 1. CONNECT/DISCONNECT: Se permiten siempre para establecer el túnel.
            // 2. SUBSCRIBE: Solo gente logueada puede escuchar ("/topic/**").
            // 3. SEND: Solo gente logueada puede enviar.
            /*
            .simpTypeMatchers(SimpMessageType.CONNECT, SimpMessageType.HEARTBEAT, SimpMessageType.UNSUBSCRIBE, SimpMessageType.DISCONNECT).permitAll()
            .simpDestSubscribeMatchers("/topic/**").authenticated() 
            .anyMessage().authenticated(); 
            */
    }

    /**
     * Desactiva la protección CSRF y la política de "Mismo Origen" (Same Origin Policy) para WebSockets.
     * * ¿Por qué return true?
     * Si tu frontend estuviera en un puerto (ej: 3000) y tu backend en otro (ej: 8080), 
     * el navegador bloquearía la conexión por seguridad.
     * Al poner esto en 'true', permites que el socket se conecte desde orígenes distintos.
     * Es vital para que SockJS funcione correctamente en muchos entornos.
     */
    @Override
    protected boolean sameOriginDisabled() {
        return true;
    }
}