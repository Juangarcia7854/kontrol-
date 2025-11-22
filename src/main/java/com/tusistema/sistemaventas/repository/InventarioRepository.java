package com.tusistema.sistemaventas.repository;

import com.tusistema.sistemaventas.model.Inventario;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.Optional;

public interface InventarioRepository extends MongoRepository<Inventario, String> {

    // Método para encontrar fácilmente el inventario de un producto específico
    Optional<Inventario> findByProductoId(String productoId);
}