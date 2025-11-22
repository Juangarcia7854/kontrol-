package com.tusistema.sistemaventas.controller;

import com.tusistema.sistemaventas.model.ConfiguracionSistema;
import com.tusistema.sistemaventas.service.ConfiguracionSistemaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AuthController {

    // ✅ AÑADIDO: Inyectamos el servicio de configuración
    private final ConfiguracionSistemaService configuracionService;

    @Autowired
    public AuthController(ConfiguracionSistemaService configuracionService) {
        this.configuracionService = configuracionService;
    }

    @GetMapping("/login")
    public String loginPage(@RequestParam(value = "error", required = false) String error,
                            @RequestParam(value = "logout", required = false) String logout,
                            Model model) {
        
        if (error != null) {
            model.addAttribute("loginError", "Nombre de usuario o contraseña incorrectos.");
        }
        
        if (logout != null) {
            model.addAttribute("logoutMessage", "Has cerrado sesión exitosamente.");
        }

        // ✅ AÑADIDO: Pasamos los datos de la empresa (necesarios para el selector de moneda) a la vista
        ConfiguracionSistema datosEmpresa = configuracionService.obtenerConfiguracion();
        model.addAttribute("datosEmpresa", datosEmpresa != null ? datosEmpresa : new ConfiguracionSistema());
        
        return "login";
    }
}