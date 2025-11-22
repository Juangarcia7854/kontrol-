package com.tusistema.sistemaventas.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import com.tusistema.sistemaventas.model.CuentaPorCobrar;

@Repository
public interface CuentaPorCobrarRepository extends MongoRepository<CuentaPorCobrar, String> {
    
    // **[ACTUALIZADO]** Query para Paginación, Búsqueda (ventaId/clienteNombre) y Filtro de Estado
    @Query("{$and: [" +
           " { 'estado' : { $in: ?0 } }, " + 
           " { $or: [" +
           "     { 'ventaId' : { $regex: ?1, $options: 'i' } }, " + 
           "     { 'clienteNombre' : { $regex: ?1, $options: 'i' } }" +
           " ] }" +
           " ]}")
    Page<CuentaPorCobrar> findByStatusAndSearchTerm(String[] estados, String term, Pageable pageable);
}