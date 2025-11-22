package com.tusistema.sistemaventas.controller;

import com.tusistema.sistemaventas.model.ConfiguracionSistema;
import com.tusistema.sistemaventas.service.ConfiguracionSistemaService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

@Controller
@RequestMapping("/configuraciones")
@PreAuthorize("hasRole('ROLE_ADMIN')")
public class ConfiguracionController {

    @Autowired
    private ConfiguracionSistemaService configuracionService;
    
    // Inyectamos el LocaleResolver para poder cambiar el idioma de la sesión
    @Autowired
    private LocaleResolver localeResolver;

    @GetMapping
    public String mostrarPaginaConfiguraciones(Model model) {
        model.addAttribute("configuracion", configuracionService.obtenerConfiguracion());
        
        Map<String, String> idiomas = new LinkedHashMap<>();
        idiomas.put("es", "Español");
        idiomas.put("en", "Inglés");
        // Puedes añadir más si los tienes configurados
        // idiomas.put("pt", "Portugués"); 
        model.addAttribute("idiomasDisponibles", idiomas);

        return "configuraciones";
    }

    @PostMapping("/guardar")
    public String guardarConfiguracion(@Valid @ModelAttribute("configuracion") ConfiguracionSistema configuracion,
                                     BindingResult bindingResult,
                                     RedirectAttributes redirectAttributes,
                                     HttpServletRequest request,    // Inyectamos el request
                                     HttpServletResponse response) { // Inyectamos el response
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error de validación. Por favor, revisa los campos.");
            return "redirect:/configuraciones";
        }
        try {
            configuracionService.guardarConfiguracion(configuracion);
            
            // ✅ LÓGICA AÑADIDA PARA ACTUALIZAR EL IDIOMA DE LA SESIÓN
            // Después de guardar, actualizamos el idioma del usuario actual.
            String nuevoIdioma = configuracion.getIdiomaPorDefecto();
            if (nuevoIdioma != null && !nuevoIdioma.isEmpty()) {
                // Creamos un Locale a partir del string guardado (ej. "es" o "en")
                Locale nuevoLocale = new Locale(nuevoIdioma);
                // Usamos el LocaleResolver para cambiar el idioma en la sesión actual
                localeResolver.setLocale(request, response, nuevoLocale);
            }
            
            redirectAttributes.addFlashAttribute("successMessage", "¡Configuración guardada exitosamente!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error al guardar la configuración: " + e.getMessage());
        }
        return "redirect:/configuraciones";
    }
}