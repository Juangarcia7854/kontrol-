package com.tusistema.sistemaventas.service;

import com.tusistema.sistemaventas.dto.DashboardUpdateDTO;
import com.tusistema.sistemaventas.dto.NotificacionDTO;
import com.tusistema.sistemaventas.model.*;
import com.tusistema.sistemaventas.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Service
public class VentaService {

    private static final Logger logger = LoggerFactory.getLogger(VentaService.class);

    private final VentaRepository ventaRepository;
    private final ProductoRepository productoRepository;
    private final ClienteRepository clienteRepository;
    private final UsuarioRepository usuarioRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final ConfiguracionSistemaRepository configuracionRepository;
    private final MongoTemplate mongoTemplate;
    private final DashboardService dashboardService;
    private final InventarioService inventarioService;

    @Autowired
    public VentaService(VentaRepository ventaRepository,
                        ProductoRepository productoRepository,
                        ClienteRepository clienteRepository,
                        UsuarioRepository usuarioRepository,
                        SimpMessagingTemplate messagingTemplate,
                        ConfiguracionSistemaRepository configuracionRepository,
                        MongoTemplate mongoTemplate,
                        DashboardService dashboardService,
                        InventarioService inventarioService) {
        this.ventaRepository = ventaRepository;
        this.productoRepository = productoRepository;
        this.clienteRepository = clienteRepository;
        this.usuarioRepository = usuarioRepository;
        this.messagingTemplate = messagingTemplate;
        this.configuracionRepository = configuracionRepository;
        this.mongoTemplate = mongoTemplate;
        this.dashboardService = dashboardService;
        this.inventarioService = inventarioService;
    }

    public List<Venta> obtenerTodasLasVentas() {
        return ventaRepository.findAll(Sort.by(Sort.Direction.DESC, "fechaVenta"));
    }
    public Optional<Venta> obtenerVentaPorId(String id) { return ventaRepository.findById(id); }
    public List<Venta> obtenerVentasPorEstados(List<String> estados) {
        if (estados == null || estados.isEmpty()) { return new ArrayList<>(); }
        return ventaRepository.findByEstadoIn(estados);
    }

    public List<Venta> buscarVentas(String termino, LocalDate fechaDesde, LocalDate fechaHasta, String estado) {
        if (!StringUtils.hasText(termino) && fechaDesde == null && fechaHasta == null && !StringUtils.hasText(estado)) {
            return obtenerTodasLasVentas();
        }

        final Query query = new Query();
        final List<Criteria> criteria = new ArrayList<>();

        if (StringUtils.hasText(termino)) {
            criteria.add(new Criteria().orOperator(
                Criteria.where("numeroFactura").regex(termino.trim(), "i"),
                Criteria.where("nombreCliente").regex(termino.trim(), "i"),
                Criteria.where("usernameUsuario").regex(termino.trim(), "i")
            ));
        }
        if (fechaDesde != null) {
            criteria.add(Criteria.where("fechaVenta").gte(fechaDesde.atStartOfDay()));
        }
        if (fechaHasta != null) {
            criteria.add(Criteria.where("fechaVenta").lte(fechaHasta.atTime(23, 59, 59)));
        }
        if (StringUtils.hasText(estado)) {
            criteria.add(Criteria.where("estado").is(estado));
        }

        if (!criteria.isEmpty()) {
            query.addCriteria(new Criteria().andOperator(criteria.toArray(new Criteria[0])));
        }
        query.with(Sort.by(Sort.Direction.DESC, "fechaVenta"));

        return mongoTemplate.find(query, Venta.class);
    }

    @Transactional
    public Venta crearVenta(Venta venta, String clienteId, String usuarioId) throws Exception {
        logger.info("Iniciando proceso de creación de venta...");
        Cliente cliente = clienteRepository.findById(clienteId).orElseThrow(() -> new Exception("Cliente no encontrado con ID: " + clienteId));
        Usuario usuario = usuarioRepository.findById(usuarioId).orElseThrow(() -> new Exception("Usuario no encontrado con ID: " + usuarioId));
        ConfiguracionSistema config = configuracionRepository.findFirstByOrderByIdAsc().orElseThrow(() -> new Exception("No se encontró la configuración del sistema para el IVA."));
        
        // Obtenemos el IVA (ej: 19.0)
        BigDecimal ivaConfigurado = config.getIvaPorDefecto(); 
        // Calculamos el factor (ej: 0.19)
        BigDecimal porcentajeIva = ivaConfigurado.divide(new BigDecimal("100"));
        
        venta.setClienteId(cliente.getId());
        venta.setNombreCliente(cliente.getNombre() + " " + cliente.getApellido());
        venta.setUsuarioId(usuario.getId());
        venta.setUsernameUsuario(usuario.getUsername());
        venta.setFechaVenta(LocalDateTime.now());
        BigDecimal subtotalVenta = BigDecimal.ZERO;
        for (DetalleVenta detalle : venta.getDetalles()) {
            Producto producto = productoRepository.findById(detalle.getProductoId()).orElseThrow(() -> new Exception("Producto no encontrado con ID: " + detalle.getProductoId()));
            detalle.setPrecioUnitario(producto.getPrecio());
            detalle.setNombreProducto(producto.getNombre());
            BigDecimal subtotalDetalle = producto.getPrecio().multiply(new BigDecimal(detalle.getCantidad()));
            detalle.setSubtotal(subtotalDetalle);
            subtotalVenta = subtotalVenta.add(subtotalDetalle);
        }
        venta.setSubtotal(subtotalVenta);
        BigDecimal montoIva = subtotalVenta.multiply(porcentajeIva);
        venta.setMontoIva(montoIva.setScale(2, RoundingMode.HALF_UP));
        
        // --- ⬇️ ¡AQUÍ ESTÁ LA CORRECCIÓN! ⬇️ ---
        // Guardamos el porcentaje (ej: 19.0) en la venta
        venta.setPorcentajeIva(ivaConfigurado.doubleValue());
        // --- ⬆️ FIN DE LA CORRECCIÓN ⬆️ ---
        
        venta.setTotalVenta(subtotalVenta.add(venta.getMontoIva()));
        venta.setEstado(Venta.ESTADO_COMPLETADA);
        long totalVentas = ventaRepository.count();
        venta.setNumeroFactura(String.format("F-%06d", totalVentas + 1));
        Venta ventaGuardada = ventaRepository.save(venta);
        logger.info("Venta #{} guardada correctamente.", ventaGuardada.getNumeroFactura());
        logger.info("Iniciando descuento de stock para la venta #{}", ventaGuardada.getNumeroFactura());
        for (DetalleVenta detalle : ventaGuardada.getDetalles()) {
            try {
                inventarioService.registrarMovimiento(detalle.getProductoId(), -detalle.getCantidad(), MovimientoInventario.TIPO_VENTA, "Venta #" + ventaGuardada.getNumeroFactura(), usuarioId);
            } catch (RuntimeException e) {
                logger.error("Error al descontar stock para el producto {}: {}", detalle.getProductoId(), e.getMessage());
                throw new Exception("No se pudo completar la venta. " + e.getMessage());
            }
        }
        logger.info("Descuento de stock completado.");
        Locale locale = new Locale("es", "CO");
        NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(locale);
        String totalFormateado = currencyFormatter.format(ventaGuardada.getTotalVenta());
        String mensajeVenta = String.format("Venta #%s por %s. Total: %s", ventaGuardada.getNumeroFactura(), ventaGuardada.getUsernameUsuario(), totalFormateado);
        messagingTemplate.convertAndSend("/topic/notificaciones", new NotificacionDTO("NUEVA_VENTA", mensajeVenta));
        DashboardUpdateDTO metricas = dashboardService.obtenerMetricasActuales();
        messagingTemplate.convertAndSend("/topic/dashboard", metricas);
        return ventaGuardada;
    }
}