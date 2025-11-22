package com.tusistema.sistemaventas.controller;

import com.tusistema.sistemaventas.model.Cliente;
import com.tusistema.sistemaventas.model.ConfiguracionSistema; 
import com.tusistema.sistemaventas.service.ClienteService;
import com.tusistema.sistemaventas.service.EmpresaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Controller
@RequestMapping("/pos")
// --- ⬇️ ¡AQUÍ ESTÁ LA CORRECCIÓN! ⬇️ ---
// Se añade 'ROLE_DEMO' para permitir el acceso a la demo
@PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_VENDEDOR', 'ROLE_DEMO')")
public class PosController {

    private static final Logger logger = LoggerFactory.getLogger(PosController.class);

    private final ClienteService clienteService;
    private final MessageSource messageSource;
    private final EmpresaService empresaService;

    @Autowired
    public PosController(ClienteService clienteService, MessageSource messageSource, EmpresaService empresaService) {
        this.clienteService = clienteService;
        this.messageSource = messageSource;
        this.empresaService = empresaService;
    }
          
    @GetMapping
    public String mostrarTerminalPos(Model model, Locale locale) {
        try {
            List<Cliente> clientes = clienteService.obtenerTodosLosClientes();
            model.addAttribute("clientes", clientes);

            ConfiguracionSistema datos = Optional.ofNullable(empresaService.obtenerDatosEmpresa())
                                                 .orElse(new ConfiguracionSistema());
            model.addAttribute("datosEmpresa", datos);

        } catch (Exception e) {
            logger.error("Error crítico al cargar los datos para el POS: {}", e.getMessage(), e);
            model.addAttribute("clientes", Collections.emptyList());
            model.addAttribute("datosEmpresa", new ConfiguracionSistema());
        }

        model.addAttribute("pageTitle", messageSource.getMessage("pos.titulo", null, locale));
        
        // ✅ CORRECCIÓN: Se restaura la ruta correcta a la plantilla.
        // Spring ahora buscará el archivo en: /templates/pos/terminal-pos.html
        return "pos/terminal-pos";
    }
}