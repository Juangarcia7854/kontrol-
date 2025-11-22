package com.tusistema.sistemaventas.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

// @Configuration: Indica que es una clase de configuración de Spring.
// @EnableWebSocketMessageBroker: Habilita el manejo de mensajes por WebSocket
// utilizando un "Broker" (intermediario) de mensajes. Esto permite comunicación bidireccional instantánea.
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    // Este método configura el "enrutamiento" de los mensajes.
    // Define quién escucha qué y por dónde viajan los datos.
    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        
        // 1. Configura el broker de salida (Servidor -> Cliente).
        // Los clientes (el navegador) se suscribirán a rutas que empiecen con "/topic".
        // Ejemplo: Si el cliente se suscribe a "/topic/notificaciones", recibirá todo lo que envíes ahí.
        registry.enableSimpleBroker("/topic");
        
        // 2. Configura el prefijo de entrada (Cliente -> Servidor).
        // Si el navegador quiere enviar un mensaje al servidor, la URL debe empezar con "/app".
        // Ejemplo: "/app/nueva-venta".
        registry.setApplicationDestinationPrefixes("/app");
    }

    // Este método registra el "Punto de Entrada" (Endpoint) para la conexión inicial.
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Define la URL "/ws" como el punto donde se inicia la conexión WebSocket.
        // En tu JavaScript del frontend, te conectarás a: http://tuservidor.com/ws
        registry.addEndpoint("/ws")
                .withSockJS(); // .withSockJS(): Esto es vital. Habilita opciones de respaldo (fallback).
                               // Si el navegador del usuario es muy viejo o la red bloquea WebSockets puros,
                               // SockJS simulará la conexión usando otras técnicas (como HTTP Long Polling) para que funcione igual.
    }
}