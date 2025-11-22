package com.tusistema.sistemaventas.service;

import com.tusistema.sistemaventas.model.Devolucion;
import com.tusistema.sistemaventas.model.DetalleDevolucion;
import com.tusistema.sistemaventas.model.DetalleVenta;
import com.tusistema.sistemaventas.model.MovimientoInventario;
import com.tusistema.sistemaventas.model.Producto;
import com.tusistema.sistemaventas.model.Venta;
import com.tusistema.sistemaventas.model.Usuario;
import com.tusistema.sistemaventas.repository.DevolucionRepository;
import com.tusistema.sistemaventas.repository.ProductoRepository;
import com.tusistema.sistemaventas.repository.VentaRepository;
import com.tusistema.sistemaventas.repository.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class DevolucionService {

    private static final Logger logger = LoggerFactory.getLogger(DevolucionService.class);

    private final DevolucionRepository devolucionRepository;
    private final VentaRepository ventaRepository;
    private final ProductoRepository productoRepository;
    private final UsuarioRepository usuarioRepository;
    private final InventarioService inventarioService;

    @Autowired
    public DevolucionService(DevolucionRepository devolucionRepository,
                             VentaRepository ventaRepository,
                             ProductoRepository productoRepository,
                             UsuarioRepository usuarioRepository,
                             InventarioService inventarioService) {
        this.devolucionRepository = devolucionRepository;
        this.ventaRepository = ventaRepository;
        this.productoRepository = productoRepository;
        this.usuarioRepository = usuarioRepository;
        this.inventarioService = inventarioService;
    }

    // ✅ MÉTODO ORIGINAL RESTAURADO
    @Transactional(readOnly = true)
    public List<Devolucion> obtenerTodasLasDevoluciones() {
        return devolucionRepository.findAll();
    }

    // ✅ MÉTODO ORIGINAL RESTAURADO
    @Transactional(readOnly = true)
    public Optional<Devolucion> obtenerDevolucionPorId(String id) {
        return devolucionRepository.findById(id);
    }

    @Transactional
    public Devolucion procesarDevolucion(Devolucion devolucion, String ventaOriginalId, String usuarioIdProcesa) throws Exception {
        logger.info("Servicio: Iniciando procesamiento de devolución para venta ID: {}", ventaOriginalId);

        Venta ventaOriginal = ventaRepository.findById(ventaOriginalId)
                .orElseThrow(() -> new IllegalArgumentException("Venta original no encontrada con ID: " + ventaOriginalId));

        Usuario usuario = usuarioRepository.findById(usuarioIdProcesa)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado con ID: " + usuarioIdProcesa));

        devolucion.setVentaOriginalId(ventaOriginal.getId());
        devolucion.setNumeroFacturaVentaOriginal(ventaOriginal.getNumeroFactura());
        devolucion.setClienteId(ventaOriginal.getClienteId());
        devolucion.setNombreCliente(ventaOriginal.getNombreCliente());
        devolucion.setUsuarioId(usuarioIdProcesa);
        devolucion.setUsernameUsuario(usuario.getUsername());
        devolucion.setFechaDevolucion(LocalDateTime.now());
        devolucion.setEstadoDevolucion("PROCESADA");

        if (devolucion.getDetalles() == null || devolucion.getDetalles().isEmpty()) {
            throw new IllegalArgumentException("La devolución debe tener al menos un producto.");
        }

        for (DetalleDevolucion detalleDev : devolucion.getDetalles()) {
            if (detalleDev.getProductoId() == null || detalleDev.getCantidadDevuelta() == null || detalleDev.getCantidadDevuelta() <= 0) {
                continue; 
            }
            
            Producto producto = productoRepository.findById(detalleDev.getProductoId())
                    .orElseThrow(() -> new IllegalArgumentException("Producto a devolver no encontrado con ID: " + detalleDev.getProductoId()));
            
            inventarioService.registrarMovimiento(
                producto.getId(),
                detalleDev.getCantidadDevuelta(), // La cantidad es POSITIVA porque es una ENTRADA
                MovimientoInventario.TIPO_DEVOLUCION,
                "Devolución de Venta #" + ventaOriginal.getNumeroFactura(),
                usuarioIdProcesa
            );

            logger.info("Stock de inventario actualizado para producto ID {}", producto.getId());
            
            if (detalleDev.getNombreProducto() == null || detalleDev.getNombreProducto().isEmpty()){
                 detalleDev.setNombreProducto(producto.getNombre());
            }
        }

        devolucion.calcularTotalDevolucion();
        
        Devolucion devolucionGuardada = devolucionRepository.save(devolucion);
        logger.info("Devolución guardada con ID: {}", devolucionGuardada.getId());

        // Lógica para actualizar el estado de la venta original
        boolean todosLosItemsDevueltos = true;
        for (DetalleVenta dv : ventaOriginal.getDetalles()) {
            int totalDevueltoParaItem = devolucionRepository.findByVentaOriginalId(ventaOriginalId).stream()
                .flatMap(d -> d.getDetalles().stream())
                .filter(dd -> dd.getProductoId().equals(dv.getProductoId()))
                .mapToInt(DetalleDevolucion::getCantidadDevuelta)
                .sum();
            
            if (totalDevueltoParaItem < dv.getCantidad()) {
                todosLosItemsDevueltos = false;
                break;
            }
        }

        if(todosLosItemsDevueltos) {
            ventaOriginal.setEstado(Venta.ESTADO_DEVUELTA_TOTAL);
        } else {
            ventaOriginal.setEstado(Venta.ESTADO_DEVUELTA_PARCIAL);
        }
        
        ventaRepository.save(ventaOriginal);
        logger.info("Estado de la venta original ID {} actualizado a: {}", ventaOriginal.getId(), ventaOriginal.getEstado());

        return devolucionGuardada;
    }
}