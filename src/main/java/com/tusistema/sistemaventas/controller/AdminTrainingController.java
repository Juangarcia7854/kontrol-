package com.tusistema.sistemaventas.controller;

import com.tusistema.sistemaventas.service.ModelTrainingService;
import com.tusistema.sistemaventas.service.PrediccionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.concurrent.Future;

// Controlador dedicado exclusivamente a la gestión de los modelos de IA.
@Controller
@RequestMapping("/admin/modelos")
// Seguridad: Solo un usuario con rol 'ADMIN' puede acceder a estas funciones.
@PreAuthorize("hasRole('ADMIN')") 
public class AdminTrainingController {

    // Inyectamos el servicio que sabe ENTRENAR (crear los archivos .model).
    @Autowired private ModelTrainingService modelTrainingService;
    
    // Inyectamos el servicio que sabe PREDECIR (usar los archivos .model).
    // Lo necesitamos aquí para decirle: "¡Oye, los modelos cambiaron, borra tu memoria vieja!".
    @Autowired private PrediccionService prediccionService;

    // Muestra la vista HTML simple donde está el botón "Entrenar Ahora".
    @GetMapping
    public String mostrarPaginaEntrenamiento() {
        return "admin/entrenamiento-modelos"; 
    }

    // Este método recibe el clic del botón "Entrenar Todos" del formulario HTML.
    @PostMapping("/entrenar-todos")
    public String entrenarTodosLosModelos(RedirectAttributes attributes) {
        try {
            // 1. INICIO DE ENTRENAMIENTO (ASÍNCRONO)
            // Llamamos a los métodos de entrenamiento. Como devuelven 'Future', 
            // la llamada es inmediata y nos dan un objeto "promesa" (Future) de que terminarán.
            Future<String> resVenta = modelTrainingService.entrenarModeloVentaRapida();
            Future<String> resDev = modelTrainingService.entrenarModeloDevolucion();
            Future<String> resPromo = modelTrainingService.entrenarModeloPromocion();
            
            // 2. SINCRONIZACIÓN (ESPERA)
            // El método .get() BLOQUEA la ejecución hasta que la tarea termina.
            // Esperamos a que los 3 terminen para poder mostrar el mensaje de éxito al usuario.
            // Si no hiciéramos .get(), la página recargaría antes de que terminen de crearse los archivos.
            String msgVenta = resVenta.get();
            String msgDev = resDev.get();
            String msgPromo = resPromo.get();
            
            // 3. ACTUALIZACIÓN EN CALIENTE
            // Limpiamos la caché del servicio de predicción.
            // Esto obliga a que, la próxima vez que se pida una predicción, 
            // el sistema lea los NUEVOS archivos .model del disco en lugar de usar los viejos de la RAM.
            prediccionService.clearModelCache(); 

            // 4. FEEDBACK AL USUARIO
            // Usamos FlashAttributes porque vamos a hacer un redirect.
            // Estos mensajes sobreviven una recarga de página (ideales para alertas de éxito).
            attributes.addFlashAttribute("successMessage", "Entrenamiento completado.");
            // Concatenamos los resultados de cada modelo (ej: "Modelo Venta: 50 productos...")
            attributes.addFlashAttribute("results", msgVenta + "<br>" + msgDev + "<br>" + msgPromo);
        
        } catch (Exception e) {
            // Si algo falla (ej. error de disco, base de datos caída), mostramos el error.
            attributes.addFlashAttribute("errorMessage", "Error: " + e.getMessage());
        }
        
        // Redirigimos a la misma página para limpiar el formulario y mostrar la alerta.
        return "redirect:/admin/modelos";
    }
}