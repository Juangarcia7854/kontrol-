package com.tusistema.sistemaventas.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class PreferenciasController {

    @PostMapping("/cambiar-moneda")
    public String cambiarMoneda(@RequestParam("moneda") String moneda, HttpSession session, RedirectAttributes redirectAttributes) {
        if (moneda != null && !moneda.isEmpty()) {
            session.setAttribute("moneda", moneda);
            redirectAttributes.addFlashAttribute("successMessage", "Moneda cambiada a " + moneda);
        }
        return "redirect:/dashboard";
    }
}