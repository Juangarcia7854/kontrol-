package com.tusistema.sistemaventas.service;

import com.tusistema.sistemaventas.model.Inventario;
import com.tusistema.sistemaventas.model.MovimientoInventario;
import com.tusistema.sistemaventas.model.Producto;
import com.tusistema.sistemaventas.repository.InventarioRepository;
import com.tusistema.sistemaventas.repository.MovimientoInventarioRepository;
import com.tusistema.sistemaventas.repository.ProductoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ProductoService {

    private static final Logger logger = LoggerFactory.getLogger(ProductoService.class);
    private final ProductoRepository productoRepository;
    private final FileStorageService fileStorageService;
    private final InventarioService inventarioService;
    private final InventarioRepository inventarioRepository;
    private final MovimientoInventarioRepository movimientoInventarioRepository;

    @Autowired
    public ProductoService(ProductoRepository productoRepository,
                         FileStorageService fileStorageService,
                         InventarioService inventarioService,
                         InventarioRepository inventarioRepository,
                         MovimientoInventarioRepository movimientoInventarioRepository) {
        this.productoRepository = productoRepository;
        this.fileStorageService = fileStorageService;
        this.inventarioService = inventarioService;
        this.inventarioRepository = inventarioRepository;
        this.movimientoInventarioRepository = movimientoInventarioRepository;
    }

    @Transactional(readOnly = true)
    public List<Producto> obtenerTodosLosProductos() {
        logger.debug("SERVICE: Obteniendo todos los productos.");
        return productoRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Producto> obtenerProductoPorId(String id) {
        logger.debug("SERVICE: Buscando producto por ID: {}", id);
        return productoRepository.findById(id);
    }

    @Transactional
    public Producto guardarProductoConImagen(Producto producto, MultipartFile imagenFile, String usuarioId) throws IllegalArgumentException {
        boolean esNuevo = !StringUtils.hasText(producto.getId());
        String imagenUrlAnterior = null;
        if (!esNuevo) {
            imagenUrlAnterior = productoRepository.findById(producto.getId()).map(Producto::getImagenUrl).orElse(null);
        }
        if (imagenFile != null && !imagenFile.isEmpty()) {
            if (StringUtils.hasText(imagenUrlAnterior)) {
                fileStorageService.deleteFileByUrl(imagenUrlAnterior);
            }
            String nombreArchivo = fileStorageService.storeFile(imagenFile);
            producto.setImagenUrl(fileStorageService.getFileUrl(nombreArchivo));
        } else if (!esNuevo) {
            producto.setImagenUrl(imagenUrlAnterior);
        } else {
             producto.setImagenUrl(null); // Asegura que no tenga URL si es nuevo y no hay imagen
        }
        
        Producto productoGuardado = productoRepository.save(producto);
        
        // Inicializa inventario si es nuevo y tiene stock inicial > 0
        if (esNuevo && producto.getStockInicial() > 0) {
             // Primero crea el registro de inventario si no existe
            inventarioRepository.findByProductoId(productoGuardado.getId()).orElseGet(() -> {
                Inventario nuevoInventario = new Inventario(productoGuardado.getId(), 0); // Empieza en 0 antes del movimiento
                return inventarioRepository.save(nuevoInventario);
            });
            // Luego registra el movimiento inicial
            inventarioService.registrarMovimiento(
                productoGuardado.getId(),
                producto.getStockInicial(),
                MovimientoInventario.TIPO_INICIAL,
                "Stock inicial al crear el producto",
                usuarioId // Asegúrate de que este ID sea válido
            );
        } else if (esNuevo) {
             // Si es nuevo pero sin stock inicial, solo crea el registro en 0
             inventarioRepository.findByProductoId(productoGuardado.getId()).orElseGet(() -> {
                Inventario nuevoInventario = new Inventario(productoGuardado.getId(), 0); 
                return inventarioRepository.save(nuevoInventario);
            });
        }
        
        return productoGuardado;
    }

    @Transactional
    public void eliminarProducto(String id) {
        logger.info("Iniciando eliminación completa del producto ID: {}", id);
        
        Optional<Producto> productoOpt = obtenerProductoPorId(id);
        if (productoOpt.isEmpty()) {
             logger.warn("No se puede eliminar, producto no encontrado con ID: {}", id);
            throw new RuntimeException("No se puede eliminar, producto no encontrado con ID: " + id);
        }
        Producto producto = productoOpt.get();

        // 1. Eliminamos el registro de inventario
        inventarioRepository.findByProductoId(id).ifPresent(inventario -> {
            logger.info("Eliminando registro de inventario para producto ID: {}", id);
            inventarioRepository.delete(inventario);
        });

        // 2. Eliminamos el historial de movimientos de inventario
        List<MovimientoInventario> movimientos = movimientoInventarioRepository.findByProductoIdOrderByFechaDesc(id);
        if (!movimientos.isEmpty()) {
            logger.info("Eliminando {} movimientos de inventario para producto ID: {}", movimientos.size(), id);
            movimientoInventarioRepository.deleteAll(movimientos);
        }

        // 3. Eliminamos la imagen
        if (StringUtils.hasText(producto.getImagenUrl())) {
            fileStorageService.deleteFileByUrl(producto.getImagenUrl());
        }
        
        // 4. Finalmente, eliminamos el producto
        productoRepository.deleteById(id);
        logger.info("Producto ID: {} eliminado exitosamente.", id);
    }

    @Transactional(readOnly = true)
    public List<Producto> buscarProductosParaPos(String termino) {
        if (termino == null || termino.trim().length() < 2) {
            return List.of();
        }
        // Limita la búsqueda para no sobrecargar
        return productoRepository.findByNombreContainingIgnoreCase(termino).stream()
                .limit(10)
                .collect(Collectors.toList());
    }
    
    // --- ================================== ---
    // --- == NUEVO MÉTODO PARA PREDICCIÓN == ---
    // --- ================================== ---
    /**
     * Obtiene una lista de productos a partir de una lista de IDs.
     * Útil para obtener los nombres de los productos para la vista de predicción.
     * @param ids Lista de IDs de productos a buscar.
     * @return Lista de productos encontrados.
     */
    @Transactional(readOnly = true)
    public List<Producto> obtenerProductosPorIds(List<String> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of(); // Devuelve lista vacía si no hay IDs
        }
        logger.debug("SERVICE: Buscando productos por IDs: {}", ids);
        // Usa el método que viene por defecto en MongoRepository
        return productoRepository.findAllById(ids); 
    }
}