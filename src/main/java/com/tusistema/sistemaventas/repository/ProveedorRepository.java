package com.tusistema.sistemaventas.repository;

import com.tusistema.sistemaventas.model.Proveedor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import java.util.List;
import java.util.Optional;

public interface ProveedorRepository extends MongoRepository<Proveedor, String> {

    // **[NUEVO]** Método para Paginación, Búsqueda y filtro por activos.
    // Busca registros ACTVOS donde el término (?0) coincida (insensible a mayúsculas) en nombre, rucNit, email o personaContacto.
    @Query("{$and: [" +
           " { 'activo' : true }, " +
           " { $or: [" +
           "     { 'nombre' : { $regex: ?0, $options: 'i' } }, " + 
           "     { 'rucNit' : { $regex: ?0, $options: 'i' } }, " +
           "     { 'email' : { $regex: ?0, $options: 'i' } }, " +
           "     { 'personaContacto' : { $regex: ?0, $options: 'i' } }" +
           " ] }" +
           " ]}")
    Page<Proveedor> findActiveBySearchTerm(String term, Pageable pageable);

    // **[ACTUALIZADO]** Métodos de búsqueda/existencia restringidos a proveedores activos

    // Antes: List<Proveedor> findByNombreContainingIgnoreCase(String nombre);
    List<Proveedor> findByNombreContainingIgnoreCaseAndActivoTrue(String nombre);

    // Antes: Optional<Proveedor> findByNombreIgnoreCase(String nombre);
    Optional<Proveedor> findByNombreIgnoreCaseAndActivoTrue(String nombre); 

    // Antes: Optional<Proveedor> findByRucNit(String rucNit);
    Optional<Proveedor> findByRucNitAndActivoTrue(String rucNit); 

    // Antes: boolean existsByNombre(String nombre);
    // Este método ya no es tan útil, el findByNombreIgnoreCaseAndActivoTrue lo reemplaza en la práctica.
    boolean existsByNombreIgnoreCaseAndActivoTrue(String nombre);

    // Antes: boolean existsByRucNit(String rucNit);
    boolean existsByRucNitAndActivoTrue(String rucNit);
}