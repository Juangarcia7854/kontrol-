package com.tusistema.sistemaventas.repository;

import com.tusistema.sistemaventas.model.Usuario;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.Optional;

public interface UsuarioRepository extends MongoRepository<Usuario, String> {
    
    // Busca un usuario por su nombre de usuario (username)
    Optional<Usuario> findByUsername(String username);
    
    // Verifica si existe un usuario con un nombre de usuario específico
    Boolean existsByUsername(String username);
    
    // Busca un usuario por su dirección de email
    Optional<Usuario> findByEmail(String email); 
    
    // Verifica si existe un usuario con una dirección de email específica
    Boolean existsByEmail(String email); // <-- MÉTODO AÑADIDO
    
}