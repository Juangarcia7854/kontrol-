package com.tusistema.sistemaventas.repository; // Asegúrate que el paquete sea el correcto

import com.tusistema.sistemaventas.model.ConfiguracionSistema;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.Optional;

public interface ConfiguracionSistemaRepository extends MongoRepository<ConfiguracionSistema, String> {

    /**
     * Intenta encontrar la primera configuración disponible.
     * Como solo debería haber una, este método es útil.
     * @return Optional<ConfiguracionSistema>
     */
    Optional<ConfiguracionSistema> findFirstByOrderByIdAsc(); // O cualquier otro criterio para obtener la única
}
