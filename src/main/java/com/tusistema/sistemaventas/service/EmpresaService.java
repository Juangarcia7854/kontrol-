package com.tusistema.sistemaventas.service;

import com.tusistema.sistemaventas.model.ConfiguracionSistema;
import org.springframework.stereotype.Service;

@Service
public class EmpresaService {

    // CAMBIO IMPORTANTE: El método debe devolver el tipo de objeto correcto.
    public ConfiguracionSistema obtenerDatosEmpresa() {
        // Aquí va tu lógica para obtener la configuración desde la base de datos.
        // Por ahora, puedes devolver un objeto nuevo para probar.
        ConfiguracionSistema config = new ConfiguracionSistema();
        config.setIvaPorDefecto(new java.math.BigDecimal("19.0"));
        config.setMonedaPorDefecto("COP");
        return config;
    }
}