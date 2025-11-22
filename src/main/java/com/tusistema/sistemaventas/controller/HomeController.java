package com.tusistema.sistemaventas.controller;

import com.tusistema.sistemaventas.model.Usuario; // Importa tu modelo Usuario
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model; // Importa Model
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Controlador para las páginas públicas y generales del sitio,
 * como la página de inicio (landing page).
 */
@Controller
public class HomeController {

    /**
     * Muestra la página principal (landing page).
     * Pasa un objeto Usuario vacío al modelo para el modal de registro.
     * @param model El modelo para pasar datos a la vista.
     * @return El nombre de la plantilla 'index.html'.
     */
    @GetMapping("/")
    public String showHomePage(Model model) {
        // Añadimos un objeto Usuario vacío para el formulario modal
        model.addAttribute("usuario", new Usuario());
        return "index"; // Carga 'templates/index.html'
    }
}