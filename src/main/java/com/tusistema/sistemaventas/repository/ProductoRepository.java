package com.tusistema.sistemaventas.repository;

import com.tusistema.sistemaventas.model.Producto;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ProductoRepository extends MongoRepository<Producto, String> {

    Optional<Producto> findByNombreIgnoreCase(String nombre);
    Optional<Producto> findByCodigoBarras(String codigoBarras);
    boolean existsByNombre(String nombre);
    
    // Este método es el que usaremos para el buscador del POS
    List<Producto> findByNombreContainingIgnoreCase(String nombre);
    
    @Query("{$or: [{'nombre': {$regex: ?0, $options: 'i'}}, {'codigoBarras': ?0}]}")
    List<Producto> buscarPorNombreOCodigo(String termino);
}