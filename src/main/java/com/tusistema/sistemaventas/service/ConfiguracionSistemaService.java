package com.tusistema.sistemaventas.service;

import com.tusistema.sistemaventas.model.ConfiguracionSistema;
import com.tusistema.sistemaventas.repository.ConfiguracionSistemaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;

@Service
public class ConfiguracionSistemaService {

    private static final Logger logger = LoggerFactory.getLogger(ConfiguracionSistemaService.class);
    private final ConfiguracionSistemaRepository configuracionRepository;

    public static final String CONFIGURACION_ID_UNICA = "unicaConfiguracionGlobal";

    @Autowired
    public ConfiguracionSistemaService(ConfiguracionSistemaRepository configuracionRepository) {
        this.configuracionRepository = configuracionRepository;
    }

    @Transactional
    public ConfiguracionSistema obtenerConfiguracion() {
        Optional<ConfiguracionSistema> configOpt = configuracionRepository.findById(CONFIGURACION_ID_UNICA);
        if (configOpt.isPresent()) {
            return configOpt.get();
        } else {
            logger.info("No se encontró configuración del sistema, creando una nueva con valores por defecto.");
            ConfiguracionSistema nuevaConfig = new ConfiguracionSistema();
            nuevaConfig.setId(CONFIGURACION_ID_UNICA);
            nuevaConfig.setNombreEmpresa("Nombre de tu Empresa (Default)");
            nuevaConfig.setNitRucEmpresa("000000000-0");
            nuevaConfig.setDireccion("Dirección por Defecto");
            nuevaConfig.setTelefono("+00 000 0000");
            nuevaConfig.setEmailContacto("contacto@tuempresa.com");
            nuevaConfig.setIvaPorDefecto(new BigDecimal("19.00"));
            nuevaConfig.setMonedaPorDefecto("COP");
            nuevaConfig.setIdiomaPorDefecto("es");
            nuevaConfig.setUrlLogo("");

            return configuracionRepository.save(nuevaConfig);
        }
    }

    @Transactional
    public ConfiguracionSistema guardarConfiguracion(ConfiguracionSistema configuracion) {
        configuracion.setId(CONFIGURACION_ID_UNICA);
        return configuracionRepository.save(configuracion);
    }
}