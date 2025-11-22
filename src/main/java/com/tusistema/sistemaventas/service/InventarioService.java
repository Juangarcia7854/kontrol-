package com.tusistema.sistemaventas.service;

// --- DTO ELIMINADO ---
import com.tusistema.sistemaventas.model.Inventario;
import com.tusistema.sistemaventas.model.MovimientoInventario;
import com.tusistema.sistemaventas.model.Producto;
import com.tusistema.sistemaventas.repository.InventarioRepository;
import com.tusistema.sistemaventas.repository.MovimientoInventarioRepository;
import com.tusistema.sistemaventas.repository.ProductoRepository;
import org.springframework.stereotype.Service;
// --- ⬇️ IMPORT AÑADIDO ⬇️ ---
import org.springframework.transaction.annotation.Transactional;
// --- ⬆️ FIN DE IMPORT ⬆️ ---

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class InventarioService {

    private final InventarioRepository inventarioRepository;
    private final MovimientoInventarioRepository movimientoRepository;
    private final ProductoRepository productoRepository; 

    public InventarioService(InventarioRepository inventarioRepository,
                             MovimientoInventarioRepository movimientoRepository,
                             ProductoRepository productoRepository) { 
        this.inventarioRepository = inventarioRepository;
        this.movimientoRepository = movimientoRepository;
        this.productoRepository = productoRepository; 
    }

    // --- MÉTODO ELIMINADO ---
    /*
    public List<InventarioVistaDTO> obtenerInventarioVista() {
        ...
    }
    */

    /**
     * Método principal para cualquier actualización de stock.
     */
    // --- ⬇️ ¡ANOTACIÓN AÑADIDA! ⬇️ ---
    @Transactional
    public void registrarMovimiento(String productoId, int cantidad, String tipoMovimiento, String motivo, String username) {
        
        Inventario inventario = inventarioRepository.findByProductoId(productoId)
                .orElse(new Inventario(productoId, 0)); 

        int stockAnterior = inventario.getCantidad();
        int stockNuevo = stockAnterior + cantidad;

        if (stockNuevo < 0) {
            // Esto lanzará una excepción que el controlador SÍ atrapará (¡Bien!)
            throw new RuntimeException("Stock insuficiente para el producto ID: " + productoId);
        }

        // 1. Guardamos el inventario
        inventario.setCantidad(stockNuevo);
        inventario.setFechaUltimaActualizacion(LocalDateTime.now());
        inventarioRepository.save(inventario); 

        // --- ⬇️ ¡CORRECCIÓN! Bloque try...catch eliminado ⬇️ ---
        // Ahora, si esto falla, lanzará una excepción que el
        // ProductoController SÍ PUEDE VER, y mostrará un mensaje de ERROR.
        
        // 2. ACTUALIZAMOS EL DOCUMENTO 'PRODUCTO'
        Producto producto = productoRepository.findById(productoId)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado para actualizar stock: " + productoId));
        
        producto.setStock(stockNuevo); // Actualizamos el campo 'stock' en el producto
        productoRepository.save(producto); // <-- Guardado en la colección 'productos'
        
        // --- ⬆️ FIN DE LA CORRECCIÓN ⬆️ ---


        // 3. Guardamos el historial (Movimiento)
        // (Dejamos este try...catch porque fallar al guardar el historial
        // no es tan grave como fallar al guardar el stock)
        try {
            MovimientoInventario movimiento = new MovimientoInventario();
            movimiento.setProductoId(productoId);
            movimiento.setCantidad(cantidad);
            movimiento.setTipoMovimiento(tipoMovimiento);
            movimiento.setMotivo(motivo);
            movimiento.setUsuarioId(username); 
            movimiento.setStockAnterior(stockAnterior);
            movimiento.setStockNuevo(stockNuevo);
            
            movimientoRepository.save(movimiento);
        } catch (Exception e) {
            System.err.println("--- ¡ERROR DEPURANDO! ---");
            System.err.println("El stock SÍ se actualizó, pero falló al guardar el MovimientoInventario.");
            System.err.println("Error: " + e.getMessage());
        }
    }
    
    public int obtenerStockActual(String productoId) {
        return productoRepository.findById(productoId)
                .map(Producto::getStock)
                .orElse(0);
    }
}