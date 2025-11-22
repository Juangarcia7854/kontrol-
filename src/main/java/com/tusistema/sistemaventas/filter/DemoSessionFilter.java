package com.tusistema.sistemaventas.filter;

import com.tusistema.sistemaventas.model.Usuario; // Importa tu modelo Usuario
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;

@Component
@Order(1) // Ejecutar este filtro TEMPRANO, pero DESPUÉS de los de Security
public class DemoSessionFilter implements Filter {

    private static final Logger logger = LoggerFactory.getLogger(DemoSessionFilter.class);

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // Verificar si el usuario está autenticado y es una instancia de nuestro Usuario
        if (authentication != null && authentication.isAuthenticated() && authentication.getPrincipal() instanceof Usuario) {
            Usuario currentUser = (Usuario) authentication.getPrincipal();

            // Verificar si es un usuario demo y si su tiempo ha expirado
            if (currentUser.isDemoUser() && currentUser.getDemoExpiryTime() != null &&
                LocalDateTime.now().isAfter(currentUser.getDemoExpiryTime())) {

                logger.info("Sesión de demo expirada para usuario: {}. Desconectando...", currentUser.getUsername());

                // Cerrar la sesión de Spring Security
                new SecurityContextLogoutHandler().logout(request, response, authentication);

                // Redirigir a la página de login con un mensaje
                response.sendRedirect(request.getContextPath() + "/login?demoExpired=true");
                return; // Importante: detener la cadena de filtros aquí
            }
        }

        // Si no es un usuario demo expirado, continuar con la cadena normal de filtros
        chain.doFilter(request, response);
    }

    // Métodos init() y destroy() de la interfaz Filter (pueden dejarse vacíos)
    @Override
    public void init(jakarta.servlet.FilterConfig filterConfig) throws ServletException {}
    @Override
    public void destroy() {}
}