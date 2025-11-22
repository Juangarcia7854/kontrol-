package com.tusistema.sistemaventas.service;

import com.tusistema.sistemaventas.model.Usuario;
import com.tusistema.sistemaventas.repository.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
public class UsuarioService {

    private static final Logger logger = LoggerFactory.getLogger(UsuarioService.class);
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    // Roles disponibles
    private static final List<String> ROLES_DISPONIBLES = Arrays.asList(
            "ROLE_ADMIN",
            "ROLE_VENDEDOR",
            "ROLE_DEMO"
    );
    private static final String DEMO_ROLE = "ROLE_DEMO";

    @Autowired
    public UsuarioService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public List<Usuario> obtenerTodosLosUsuarios() {
        return usuarioRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Usuario> obtenerUsuarioPorId(String id) {
        return usuarioRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public Optional<Usuario> obtenerUsuarioPorUsername(String username) {
        return usuarioRepository.findByUsername(username);
    }

    // --- MÉTODO CORREGIDO: Lógica de guardado con corrección de ID ---
    @Transactional
    public Usuario guardarUsuario(Usuario usuario, String rawPassword) {
        // 1. Verificar si es edición (tiene ID y NO está vacío)
        if (usuario.getId() != null && !usuario.getId().trim().isEmpty()) {
            Usuario usuarioExistente = usuarioRepository.findById(usuario.getId())
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado para editar"));

            // Actualizar datos básicos
            usuarioExistente.setNombreCompleto(usuario.getNombreCompleto());
            usuarioExistente.setUsername(usuario.getUsername());
            usuarioExistente.setEmail(usuario.getEmail());
            usuarioExistente.setRoles(usuario.getRoles());
            usuarioExistente.setEnabled(usuario.isEnabled());

            // Solo actualizar contraseña si viene una nueva
            if (rawPassword != null && !rawPassword.isBlank()) {
                usuarioExistente.setPassword(passwordEncoder.encode(rawPassword));
            }
            
            return usuarioRepository.save(usuarioExistente);

        } else {
            // 2. Es creación de usuario nuevo
            
            // !!! ESTA LÍNEA ES LA SOLUCIÓN !!!
            // Forzamos a null si viene como cadena vacía "" para que MongoDB genere el ID
            usuario.setId(null); 

            if (rawPassword != null && !rawPassword.isBlank()) {
                usuario.setPassword(passwordEncoder.encode(rawPassword));
            }
            
            // Asegurar un rol por defecto si viene vacío
            if (usuario.getRoles() == null || usuario.getRoles().isEmpty()) {
                 usuario.setRoles(Set.of("ROLE_VENDEDOR"));
            }
            
            return usuarioRepository.save(usuario);
        }
    }

    @Transactional
    public void eliminarUsuario(String id) {
        usuarioRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public boolean existePorUsername(String username) {
        return usuarioRepository.existsByUsername(username);
    }

    @Transactional(readOnly = true)
    public boolean existePorEmail(String email) {
        return usuarioRepository.existsByEmail(email);
    }

    @Transactional(readOnly = true)
    public List<String> obtenerTodosLosRolesDisponibles() {
        return new ArrayList<>(ROLES_DISPONIBLES);
    }

    @Transactional
    public Usuario registrarNuevoUsuario(Usuario usuario) throws Exception {
        return usuarioRepository.save(usuario);
    }

    @Transactional
    public Usuario createAndSaveDemoUser() {
        String uniqueSuffix = UUID.randomUUID().toString().substring(0, 8);
        String demoUsername = "demo_" + uniqueSuffix;
        String demoPassword = UUID.randomUUID().toString();

        Usuario demoUser = new Usuario();
        demoUser.setUsername(demoUsername);
        demoUser.setPassword(passwordEncoder.encode(demoPassword));
        demoUser.setNombreCompleto("Usuario Demo");
        demoUser.setEmail(demoUsername + "@kontrolplus-demo.com");
        demoUser.setRoles(Set.of(DEMO_ROLE));
        demoUser.setEnabled(true);

        demoUser.setDemoUser(true);
        demoUser.setDemoExpiryTime(LocalDateTime.now().plusHours(2));

        Usuario savedDemoUser = usuarioRepository.save(demoUser);
        logger.info("Usuario Demo creado: {}, Expira: {}", savedDemoUser.getUsername(), savedDemoUser.getDemoExpiryTime());

        savedDemoUser.setPassword(demoPassword);
        return savedDemoUser;
    }
}