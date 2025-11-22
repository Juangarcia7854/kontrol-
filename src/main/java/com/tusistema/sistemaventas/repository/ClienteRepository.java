package com.tusistema.sistemaventas.repository; // Reemplaza con tu paquete

import com.tusistema.sistemaventas.model.Cliente; // Reemplaza con tu paquete
import org.springframework.data.mongodb.repository.MongoRepository; // O JpaRepository si usas JPA SQL
// import org.springframework.data.jpa.repository.JpaRepository; // Si usas base de datos relacional

import java.util.List;
import java.util.Optional;

// Cambia MongoRepository a JpaRepository si usas una base de datos SQL
// y ajusta el tipo de ID si no es String (ej. Long)
public interface ClienteRepository extends MongoRepository<Cliente, String> {

    // Método para buscar un cliente por su número de documento.
    // Spring Data generará la implementación basado en el nombre del método.
    Optional<Cliente> findByNumeroDocumento(String numeroDocumento);

    // Método para buscar un cliente por su email.
    Optional<Cliente> findByEmail(String email);

    // Método para buscar clientes cuyo nombre o apellido contenga el término de búsqueda (ignorando mayúsculas/minúsculas).
    List<Cliente> findByNombreContainingIgnoreCaseOrApellidoContainingIgnoreCase(String nombre, String apellido);

    // Método para verificar si existe un cliente con un número de documento específico.
    boolean existsByNumeroDocumento(String numeroDocumento);

    // Método para verificar si existe un cliente con un email específico.
    boolean existsByEmail(String email);

    // Los métodos como findAll(), findById(), save(), deleteById(), count()
    // ya vienen heredados de MongoRepository (o JpaRepository).
}