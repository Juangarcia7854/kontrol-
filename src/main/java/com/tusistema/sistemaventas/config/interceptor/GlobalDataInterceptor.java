package com.tusistema.sistemaventas.config.interceptor;

import com.tusistema.sistemaventas.model.ConfiguracionSistema;
import com.tusistema.sistemaventas.service.ConfiguracionSistemaService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

// @Component: Marca esta clase para que Spring la reconozca y la cargue automáticamente al arrancar.
@Component
public class GlobalDataInterceptor implements HandlerInterceptor {

    // @Autowired: Inyecta el servicio de configuración para poder consultar la base de datos.
    @Autowired
    private ConfiguracionSistemaService configuracionService;

    // Este método se ejecuta automáticamente después de que el controlador procesa la petición, 
    // pero antes de que se muestre la página HTML al usuario.
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        // Verificamos dos cosas:
        // 1. Que el objeto 'modelAndView' exista (no sea null).
        // 2. Que NO sea una redirección (usando el método auxiliar de abajo), para no cargar datos innecesarios.
        if (modelAndView != null && !isRedirectView(modelAndView)) {
            // Buscamos la configuración del sistema (ej: Nombre de la tienda, Logo, Email) usando el servicio.
            ConfiguracionSistema config = configuracionService.obtenerConfiguracion();
            
            // Agregamos el objeto 'config' a la vista con el nombre "datosEmpresa".
            // Esto hace que ${datosEmpresa} esté disponible en TODAS las plantillas HTML sin repetir código.
            modelAndView.addObject("datosEmpresa", config);
        }
    }

    // Método privado auxiliar para comprobar si la vista es una redirección (ej: return "redirect:/inicio").
    private boolean isRedirectView(ModelAndView modelAndView) {
        // Obtenemos el nombre de la vista que devolvió el controlador.
        String viewName = modelAndView.getViewName();
        
        // Si el nombre empieza con "redirect:", devolvemos true.
        return viewName != null && viewName.startsWith("redirect:");
    }
}