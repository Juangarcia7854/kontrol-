package com.tusistema.sistemaventas.config; // Asegúrate de que este sea tu paquete (o uno apropiado)

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

// @ControllerAdvice: Esta anotación convierte a la clase en un "asistente global".
// Significa que el código que escribas aquí se aplicará a TODOS los Controladores de tu proyecto.
// Es ideal para manejar excepciones globales o, como en este caso, datos globales.
@ControllerAdvice
public class GlobalControllerAdvice {

    /**
     * Este método añade automáticamente el objeto HttpServletRequest actual
     * al modelo para todas las vistas renderizadas por los controladores.
     * Estará disponible en Thymeleaf bajo el nombre "request".
     *
     * @param request El HttpServletRequest actual inyectado por Spring.
     * @return El HttpServletRequest para ser añadido al modelo.
     */
    // @ModelAttribute("request"): Esta anotación hace la magia.
    // Antes de que se muestre CUALQUIER página, Spring ejecuta este método.
    // Toma el objeto devuelto y lo mete en el modelo con la etiqueta "request".
    @ModelAttribute("request")
    public HttpServletRequest addRequestToModel(HttpServletRequest request) {
        // Devolvemos la petición completa (que contiene la URL actual, parámetros, IP del cliente, etc.)
        // para que puedas usarla dentro de tus archivos HTML.
        return request;
    }
}