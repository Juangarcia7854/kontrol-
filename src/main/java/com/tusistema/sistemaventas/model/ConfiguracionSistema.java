package com.tusistema.sistemaventas.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

@Document(collection = "configuracion_sistema")
public class ConfiguracionSistema {

    @Id
    private String id;
    @NotBlank(message = "El nombre de la empresa es obligatorio")
    private String nombreEmpresa;
    @NotBlank(message = "El NIT o RUC es obligatorio")
    private String nitRucEmpresa;
    private String direccion;
    private String telefono;
    @Email(message = "Debe ser un email válido")
    private String emailContacto;
    @NotNull(message = "El porcentaje de IVA es obligatorio")
    @DecimalMin(value = "0.0", message = "El IVA no puede ser negativo")
    private BigDecimal ivaPorDefecto;
    @NotBlank(message = "La moneda por defecto es obligatoria")
    private String monedaPorDefecto;
    @NotBlank(message = "El idioma por defecto es obligatorio")
    private String idiomaPorDefecto;
    private String urlLogo;

    // Getters y Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getNombreEmpresa() { return nombreEmpresa; }
    public void setNombreEmpresa(String nombreEmpresa) { this.nombreEmpresa = nombreEmpresa; }
    public String getNitRucEmpresa() { return nitRucEmpresa; }
    public void setNitRucEmpresa(String nitRucEmpresa) { this.nitRucEmpresa = nitRucEmpresa; }
    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }
    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
    public String getEmailContacto() { return emailContacto; }
    public void setEmailContacto(String emailContacto) { this.emailContacto = emailContacto; }
    public BigDecimal getIvaPorDefecto() { return ivaPorDefecto; }
    public void setIvaPorDefecto(BigDecimal ivaPorDefecto) { this.ivaPorDefecto = ivaPorDefecto; }
    public String getMonedaPorDefecto() { return monedaPorDefecto; }
    public void setMonedaPorDefecto(String monedaPorDefecto) { this.monedaPorDefecto = monedaPorDefecto; }
    public String getIdiomaPorDefecto() { return idiomaPorDefecto; }
    public void setIdiomaPorDefecto(String idiomaPorDefecto) { this.idiomaPorDefecto = idiomaPorDefecto; }
    public String getUrlLogo() { return urlLogo; }
    public void setUrlLogo(String urlLogo) { this.urlLogo = urlLogo; }
}