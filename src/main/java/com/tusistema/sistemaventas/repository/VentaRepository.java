package com.tusistema.sistemaventas.repository;

import com.tusistema.sistemaventas.model.Venta;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface VentaRepository extends MongoRepository<Venta, String> {

    Optional<Venta> findByNumeroFactura(String numeroFactura);
    
    List<Venta> findByClienteId(String clienteId);
    
    List<Venta> findByUsuarioId(String usuarioId);
    
    List<Venta> findByFechaVentaBetween(LocalDateTime fechaInicio, LocalDateTime fechaFin);

    List<Venta> findByFechaVentaBetweenAndEstadoIn(LocalDateTime fechaInicio, LocalDateTime fechaFin, List<String> estados);

    List<Venta> findByEstadoIn(List<String> estados);

    // ========================================================================
    // ✅ INICIO: MÉTODOS AÑADIDOS PARA MODELOS WEKA
    // ========================================================================

    /**
     * Para el modelo Churn: Obtiene la última venta de un cliente específico.
     */
    Optional<Venta> findTopByClienteIdOrderByFechaVentaDesc(String clienteId);
    
    /**
     * Para el modelo Churn y Fraude: Cuenta el total de ventas de un cliente.
     */
    long countByClienteId(String clienteId);

    // ========================================================================
    // ✅ FIN: MÉTODOS AÑADIDOS
    // ========================================================================
}