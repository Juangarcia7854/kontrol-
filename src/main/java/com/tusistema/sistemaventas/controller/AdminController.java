package com.tusistema.sistemaventas.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin") // Ruta base para este controlador
public class AdminController {

    private static final Logger logger = LoggerFactory.getLogger(AdminController.class);

    // El método @GetMapping("/configuracion/editar") HA SIDO REMOVIDO. Lo maneja DashboardController.
    // El método @PostMapping("/configuracion/editar") HA SIDO REMOVIDO. Lo maneja DashboardController.

    // El método @GetMapping("/usuarios") (que resultaba en /admin/usuarios) HA SIDO REMOVIDO.
    // Lo maneja UsuarioController (@RequestMapping("/admin/usuarios") y método @GetMapping).

    @GetMapping("/panel-general")
    public String mostrarPanelGeneralAdmin(Model model) {
        logger.info("AdminController: Accediendo a /admin/panel-general");
        model.addAttribute("pageTitle", "Panel General de Administración");
        return "admin/panel-general-admin"; // Asegúrate que esta plantilla exista
    }

    // Puedes añadir aquí otros métodos para rutas bajo /admin/* que sean únicas
    // y no estén manejadas por DashboardController o UsuarioController.
}