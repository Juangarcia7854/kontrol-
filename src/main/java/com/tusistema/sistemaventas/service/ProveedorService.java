package com.tusistema.sistemaventas.service;

import com.tusistema.sistemaventas.model.Proveedor;
import com.tusistema.sistemaventas.repository.ProveedorRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

@Service
public class ProveedorService {

    private static final Logger logger = LoggerFactory.getLogger(ProveedorService.class);
    private final ProveedorRepository proveedorRepository;

    @Autowired
    public ProveedorService(ProveedorRepository proveedorRepository) {
        this.proveedorRepository = proveedorRepository;
    }

    // **[NUEVO]** Método para Paginación, Búsqueda y Activos
    public Page<Proveedor> obtenerProveedoresActivosPaginated(int page, int size, String term) {
        logger.info("ProveedorService: Buscando proveedores activos. Página: {}, Tamaño: {}, Término: '{}'", page, size, term);
        Pageable pageable = PageRequest.of(page, size);
        String search = (term == null || term.trim().isEmpty()) ? "" : term.trim();
        
        return proveedorRepository.findActiveBySearchTerm(search, pageable);
    }
    
    // **[ELIMINADO]** Se elimina el método List<Proveedor> obtenerTodosLosProveedores()

    public Optional<Proveedor> obtenerProveedorPorId(String id) {
        return proveedorRepository.findById(id);
    }

    public Proveedor guardarProveedor(Proveedor proveedor) throws DuplicateKeyException, IllegalArgumentException {
        logger.info("ProveedorService.guardarProveedor - Proveedor recibido: ID='{}', Nombre='{}', RUC/NIT='{}'", 
                proveedor.getId(), proveedor.getNombre(), proveedor.getRucNit());

        // **[ACTUALIZADO]** Lógica de validación: chequear solo activos
        Optional<Proveedor> existentePorNombreActivo = proveedorRepository.findByNombreIgnoreCaseAndActivoTrue(proveedor.getNombre());
        if (existentePorNombreActivo.isPresent()) {
            if (proveedor.getId() == null || proveedor.getId().isEmpty() || !existentePorNombreActivo.get().getId().equals(proveedor.getId())) {
                throw new IllegalArgumentException("Ya existe un proveedor ACTIVO con el nombre: " + proveedor.getNombre());
            }
        }
        
        if (proveedor.getRucNit() != null && !proveedor.getRucNit().isEmpty()) {
            Optional<Proveedor> existentePorRucActivo = proveedorRepository.findByRucNitAndActivoTrue(proveedor.getRucNit());
            if (existentePorRucActivo.isPresent()) {
                 if (proveedor.getId() == null || proveedor.getId().isEmpty() || !existentePorRucActivo.get().getId().equals(proveedor.getId())) {
                    throw new IllegalArgumentException("Ya existe un proveedor ACTIVO con el RUC/NIT: " + proveedor.getRucNit());
                }
            }
        }
        
        // Asegurar que un nuevo proveedor siempre es activo
        if (proveedor.getId() == null || proveedor.getId().isEmpty()) {
            proveedor.setActivo(true);
        }

        try {
            Proveedor proveedorGuardado = proveedorRepository.save(proveedor);
            logger.info("Proveedor guardado: ID='{}', Nombre='{}'", 
                        proveedorGuardado.getId(), proveedorGuardado.getNombre());
            return proveedorGuardado;
        } catch (org.springframework.dao.DuplicateKeyException e) {
             logger.error("Error de DuplicateKeyException al guardar proveedor '{}': {}", proveedor.getNombre(), e.getMessage(), e);
             throw new IllegalArgumentException("Error de duplicado: Ya existe un proveedor con un identificador único similar (nombre o RUC/NIT).");
        } catch (Exception e) {
            logger.error("Error inesperado al guardar proveedor '{}': {}", proveedor.getNombre(), e.getMessage(), e);
            throw new RuntimeException("Error inesperado al guardar el proveedor.", e);
        }
    }

    // **[NUEVO]** Método de Soft Delete (Desactivar)
    public void desactivarProveedor(String id) {
        logger.info("ProveedorService: Desactivando proveedor con ID: {}", id);
        Proveedor proveedor = proveedorRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Proveedor no encontrado para desactivar."));

        if (!proveedor.isActivo()) {
             logger.warn("Proveedor con ID '{}' ya estaba inactivo.", id);
             return; 
        }
        
        proveedor.setActivo(false);
        proveedorRepository.save(proveedor);
        logger.info("Proveedor con ID '{}' marcado como inactivo (Soft Delete) exitosamente.", id);
    }

    // **[ELIMINADO]** Se elimina public void eliminarProveedor(String id)

    // **[ACTUALIZADO]** Métodos de existencia para chequear solo activos
    public boolean existePorNombre(String nombre) {
        return proveedorRepository.findByNombreIgnoreCaseAndActivoTrue(nombre).isPresent(); 
    }

    public boolean existePorRucNit(String rucNit) {
        return proveedorRepository.findByRucNitAndActivoTrue(rucNit).isPresent(); 
    }
}