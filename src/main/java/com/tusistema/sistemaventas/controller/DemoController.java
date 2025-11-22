package com.tusistema.sistemaventas.controller;

import com.tusistema.sistemaventas.model.Usuario;
import com.tusistema.sistemaventas.service.UsuarioService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
// --- IMPORT CAMBIADO ---
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DemoController {

    private static final Logger logger = LoggerFactory.getLogger(DemoController.class);

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    // --- TIPO DE DATO CAMBIADO ---
    private HttpSessionSecurityContextRepository securityContextRepository;

    @GetMapping("/demo/start")
    public String startDemoSession(HttpServletRequest request, HttpServletResponse response) {
        try {
            Usuario demoUser = usuarioService.createAndSaveDemoUser();

            UsernamePasswordAuthenticationToken token = UsernamePasswordAuthenticationToken.unauthenticated(
                    demoUser.getUsername(),
                    demoUser.getPassword()
            );

            Authentication authentication = authenticationManager.authenticate(token);

            SecurityContext context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(authentication);
            SecurityContextHolder.setContext(context);

            // Ahora usamos HttpSessionSecurityContextRepository directamente
            securityContextRepository.saveContext(context, request, response);

            logger.info("Sesión de demo iniciada para: {}", demoUser.getUsername());
            return "redirect:/dashboard";

        } catch (Exception e) {
            logger.error("Error al iniciar la sesión de demo: {}", e.getMessage(), e);
            return "redirect:/login?demoError=true";
        }
    }
}