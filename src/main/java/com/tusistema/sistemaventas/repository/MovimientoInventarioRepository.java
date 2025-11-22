package com.tusistema.sistemaventas.repository;

import com.tusistema.sistemaventas.model.MovimientoInventario;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface MovimientoInventarioRepository extends MongoRepository<MovimientoInventario, String> {
    
    // Método útil para ver el historial de un producto
    List<MovimientoInventario> findByProductoIdOrderByFechaDesc(String productoId);
}