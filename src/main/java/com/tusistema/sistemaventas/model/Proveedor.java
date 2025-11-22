package com.tusistema.sistemaventas.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Document(collection = "proveedores")
public class Proveedor {

    @Id
    private String id;

    @NotBlank(message = "El nombre del proveedor es obligatorio.")
    @Size(min = 2, max = 150, message = "El nombre debe tener entre 2 y 150 caracteres.")
    @Indexed(unique = true)
    private String nombre;

    @Size(max = 100, message = "El nombre del contacto no puede exceder los 100 caracteres.")
    private String personaContacto;

    @Email(message = "Debe ingresar un correo electrónico válido.")
    @Size(max = 100, message = "El correo electrónico no puede exceder los 100 caracteres.")
    private String email;

    @Size(max = 20, message = "El teléfono no puede exceder los 20 caracteres.")
    private String telefono;

    @Size(max = 255, message = "La dirección no puede exceder los 255 caracteres.")
    private String direccion;

    @NotBlank(message = "El RUC/NIT es obligatorio.")
    @Size(max = 50, message = "El RUC/NIT no puede exceder los 50 caracteres.")
    @Indexed(unique = true)
    private String rucNit;

    @Size(max = 500, message = "Las notas no pueden exceder los 500 caracteres.")
    private String notas;

    // **[NUEVO]** Campo para Soft Delete (Eliminación Lógica)
    private boolean activo = true; 

    // Constructores
    public Proveedor() {
    }

    public Proveedor(String nombre, String personaContacto, String email, String telefono, String direccion, String rucNit, String notas) {
        this.nombre = nombre;
        this.personaContacto = personaContacto;
        this.email = email;
        this.telefono = telefono;
        this.direccion = direccion;
        this.rucNit = rucNit;
        this.notas = notas;
        this.activo = true; // Aseguramos que al crear, siempre es activo
    }

    // Getters y Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getPersonaContacto() { return personaContacto; }
    public void setPersonaContacto(String personaContacto) { this.personaContacto = personaContacto; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }
    public String getRucNit() { return rucNit; }
    public void setRucNit(String rucNit) { this.rucNit = rucNit; }
    public String getNotas() { return notas; }
    public void setNotas(String notas) { this.notas = notas; }
    
    // **[NUEVO]** Getters y Setters para 'activo'
    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }
}