package com.tusistema.sistemaventas.model; // Reemplaza con tu paquete exacto

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Pattern;

// Si usas Lombok, puedes descomentar estas líneas y quitar los getters/setters manuales
// import lombok.Data;
// import lombok.NoArgsConstructor;
// import lombok.AllArgsConstructor;

// @Data
// @NoArgsConstructor
// @AllArgsConstructor
@Document(collection = "clientes") // Define la colección en MongoDB
public class Cliente {

    @Id
    private String id; // MongoDB generará automáticamente un ObjectId y lo convertirá a String

    @NotBlank(message = "El nombre es obligatorio.")
    @Size(min = 2, max = 100, message = "El nombre debe tener entre 2 y 100 caracteres.")
    private String nombre;

    @Size(max = 100, message = "El apellido no puede exceder los 100 caracteres.")
    private String apellido;

    @Indexed(unique = true) // Asegura que el número de documento sea único en la base de datos
    @NotBlank(message = "El número de documento es obligatorio.")
    @Size(min = 5, max = 20, message = "El número de documento debe tener entre 5 y 20 caracteres.")
    private String numeroDocumento;

    @Size(max = 50, message = "El tipo de documento no puede exceder los 50 caracteres.")
    private String tipoDocumento; // Ej: CC, CE, NIT, Pasaporte

    @Email(message = "Debe ingresar un correo electrónico válido.")
    @Size(max = 100, message = "El correo electrónico no puede exceder los 100 caracteres.")
    @Indexed(unique = true, sparse = true) // Permite emails únicos, pero también valores nulos/vacíos si no es obligatorio
    private String email;

    @Pattern(regexp = "^(\\+\\d{1,3}[- ]?)?\\d{7,15}$", message = "El formato del teléfono no es válido. Solo números y opcionalmente un prefijo de país.")
    @Size(max = 20, message = "El teléfono no puede exceder los 20 caracteres.")
    private String telefono;

    @Size(max = 255, message = "La dirección no puede exceder los 255 caracteres.")
    private String direccion;

    @Size(max = 50, message = "La ciudad no puede exceder los 50 caracteres.")
    private String ciudad;

    @Size(max = 50, message = "El país no puede exceder los 50 caracteres.")
    private String pais;

    @Size(max = 500, message = "Las notas no pueden exceder los 500 caracteres.")
    private String notas;

    // Constructores
    public Cliente() {
    }

    public Cliente(String nombre, String apellido, String numeroDocumento, String tipoDocumento, String email, String telefono, String direccion, String ciudad, String pais, String notas) {
        this.nombre = nombre;
        this.apellido = apellido;
        this.numeroDocumento = numeroDocumento;
        this.tipoDocumento = tipoDocumento;
        this.email = email;
        this.telefono = telefono;
        this.direccion = direccion;
        this.ciudad = ciudad;
        this.pais = pais;
        this.notas = notas;
    }

    // Getters y Setters (necesarios si no usas Lombok)
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getApellido() {
        return apellido;
    }

    public void setApellido(String apellido) {
        this.apellido = apellido;
    }

    public String getNumeroDocumento() {
        return numeroDocumento;
    }

    public void setNumeroDocumento(String numeroDocumento) {
        this.numeroDocumento = numeroDocumento;
    }

    public String getTipoDocumento() {
        return tipoDocumento;
    }

    public void setTipoDocumento(String tipoDocumento) {
        this.tipoDocumento = tipoDocumento;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public String getCiudad() {
        return ciudad;
    }

    public void setCiudad(String ciudad) {
        this.ciudad = ciudad;
    }

    public String getPais() {
        return pais;
    }

    public void setPais(String pais) {
        this.pais = pais;
    }

    public String getNotas() {
        return notas;
    }

    public void setNotas(String notas) {
        this.notas = notas;
    }

    @Override
    public String toString() {
        return "Cliente{" +
               "id='" + id + '\'' +
               ", nombre='" + nombre + '\'' +
               ", apellido='" + apellido + '\'' +
               ", numeroDocumento='" + numeroDocumento + '\'' +
               '}';
    }
}