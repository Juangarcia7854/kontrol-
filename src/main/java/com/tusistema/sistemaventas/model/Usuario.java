package com.tusistema.sistemaventas.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime; // <-- Asegúrate de tener este import
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

@Document(collection = "usuarios")
public class Usuario implements UserDetails {

    @Id
    private String id;
    @Indexed(unique = true)
    private String username;
    private String password;
    private String nombreCompleto;
    @Indexed(unique = true)
    private String email;
    private boolean enabled = true;
    private Set<String> roles;

    // --- CAMPOS NUEVOS PARA DEMO ---
    private boolean demoUser = false;
    private LocalDateTime demoExpiryTime;
    // --- FIN CAMPOS NUEVOS ---

    public Usuario() {
    }

    public Usuario(String username, String password, String nombreCompleto, String email, Set<String> roles) {
        this.username = username;
        this.password = password;
        this.nombreCompleto = nombreCompleto;
        this.email = email;
        this.roles = roles;
        this.enabled = true;
        this.demoUser = false;
    }

    // --- (Métodos UserDetails SIN CAMBIOS) ---
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() { return roles.stream().map(SimpleGrantedAuthority::new).collect(Collectors.toList()); }
    @Override
    public String getPassword() { return password; }
    @Override
    public String getUsername() { return username; }
    @Override
    public boolean isAccountNonExpired() { return true; }
    @Override
    public boolean isAccountNonLocked() { return true; }
    @Override
    public boolean isCredentialsNonExpired() { return true; }
    @Override
    public boolean isEnabled() { return enabled; }

    // --- (Getters y Setters existentes) ---
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public void setUsername(String username) { this.username = username; }
    public void setPassword(String password) { this.password = password; }
    public String getNombreCompleto() { return nombreCompleto; }
    public void setNombreCompleto(String nombreCompleto) { this.nombreCompleto = nombreCompleto; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public Set<String> getRoles() { return roles; }
    public void setRoles(Set<String> roles) { this.roles = roles; }

    // ===============================================
    // == GETTERS/SETTERS PARA CAMPOS NUEVOS DE DEMO ==
    // ===============================================
    public boolean isDemoUser() { // El getter para boolean empieza con "is"
        return demoUser;
    }
    public void setDemoUser(boolean demoUser) {
        this.demoUser = demoUser;
    }
    public LocalDateTime getDemoExpiryTime() {
        return demoExpiryTime;
    }
    public void setDemoExpiryTime(LocalDateTime demoExpiryTime) {
        this.demoExpiryTime = demoExpiryTime;
    }
    // ===============================================
}