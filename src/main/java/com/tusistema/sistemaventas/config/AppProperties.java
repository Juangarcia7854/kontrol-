package com.tusistema.sistemaventas.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

// @Component: Permite que Spring detecte esta clase y la pueda inyectar en otras partes (como Servicios o Controladores).
@Component
// @ConfigurationProperties: Esta es la parte mágica. Conecta esta clase con tu archivo 'application.properties'.
// Spring buscará claves que empiecen con "app" (ej: app.uploadDir o app.currencySymbol) y asignará sus valores aquí automáticamente.
@ConfigurationProperties(prefix = "app") // Busca propiedades que empiecen con "app."
public class AppProperties {

    // Define la carpeta donde se guardarán los archivos subidos.
    // Si no configuras nada en application.properties, usará "./uploads" por defecto.
    private String uploadDir = "./uploads"; // Valor por defecto si no se configura

    // Define el símbolo de moneda para mostrar en la web.
    // Útil si algún día quieres cambiar de "$" a "€" o "S/." sin tocar el código HTML, solo cambiando la configuración.
    private String currencySymbol = "$"; // Valor por defecto para el símbolo de moneda

    // --- Getters y Setters ---
    // Son OBLIGATORIOS para que @ConfigurationProperties funcione, ya que Spring usa los 'setters'
    // para inyectar los valores desde el archivo de configuración.

    public String getUploadDir() {
        return uploadDir;
    }

    public void setUploadDir(String uploadDir) {
        this.uploadDir = uploadDir;
    }

    public String getCurrencySymbol() {
        return currencySymbol;
    }

    public void setCurrencySymbol(String currencySymbol) {
        this.currencySymbol = currencySymbol;
    }

}