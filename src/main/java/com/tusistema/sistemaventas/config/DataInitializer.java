package com.tusistema.sistemaventas.config; // O el paquete que prefieras

import com.tusistema.sistemaventas.model.Usuario;
import com.tusistema.sistemaventas.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Set;

// @Component: Indica que Spring gestionará esta clase.
// implements CommandLineRunner: Esta interfaz es especial. Hace que el método 'run'
// se ejecute AUTOMÁTICAMENTE justo después de que la aplicación haya iniciado completamente.
@Component
public class DataInitializer implements CommandLineRunner {

    // Inyectamos el repositorio para poder guardar usuarios en la base de datos.
    @Autowired
    private UsuarioRepository usuarioRepository;

    // Inyectamos el codificador de contraseñas. NUNCA se deben guardar contraseñas en texto plano.
    // Spring Security usará esto para encriptar "admin123" antes de guardarlo.
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // Comprobamos si ya existe un usuario llamado "admin".
        // Esto es vital: si no hiciéramos esta comprobación, cada vez que reinicies el servidor
        // intentaría crear el usuario de nuevo y daría error por duplicado.
        if (!usuarioRepository.existsByUsername("admin")) {
            
            // Si no existe, creamos el objeto Usuario nuevo.
            Usuario admin = new Usuario();
            admin.setUsername("admin");
            
            // Aquí encriptamos la contraseña. En la BD no se guardará "admin123", 
            // sino un hash largo tipo "$2a$10$..."
            admin.setPassword(passwordEncoder.encode("admin123")); // ¡Usa una contraseña segura!
            
            admin.setNombreCompleto("Administrador del Sistema");
            
            // Asignamos los roles. Es importante que tenga ROLE_ADMIN para acceder a zonas restringidas.
            admin.setRoles(Set.of("ROLE_ADMIN", "ROLE_USER"));
            
            admin.setEnabled(true); // Activamos el usuario para que pueda hacer login.
            
            // Guardamos en la base de datos.
            usuarioRepository.save(admin);
            
            // Mensaje en la consola para avisar que se creó con éxito.
            System.out.println("Usuario 'admin' creado.");
        }
                
        }
    }