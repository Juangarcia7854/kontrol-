package com.tusistema.sistemaventas.controller;

import com.tusistema.sistemaventas.model.Cliente;
import com.tusistema.sistemaventas.model.CuentaPorCobrar;
import com.tusistema.sistemaventas.model.Venta;
import com.tusistema.sistemaventas.repository.ClienteRepository;
import com.tusistema.sistemaventas.repository.VentaRepository;
import com.tusistema.sistemaventas.service.CuentaPorCobrarService;
import com.tusistema.sistemaventas.dto.FiltroCuentasCobrarDTO; // <-- DTO para Filtro
import com.tusistema.sistemaventas.dto.AbonoCuentaCobrarDTO;   // <-- DTO para Abono

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid; // Importante para la validación del DTO de Abono
import org.springframework.validation.BindingResult; // Importante para capturar errores de validación

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/cuentas-por-cobrar")
public class CuentaPorCobrarController {

    @Autowired
    private CuentaPorCobrarService cuentaPorCobrarService;
    @Autowired
    private VentaRepository ventaRepository; // Asumiendo que esta dependencia existe
    @Autowired
    private ClienteRepository clienteRepository;

    // 1. Listado de Cuentas con Paginación y Filtros (GET /)
    @GetMapping
    public String listarCuentas(@ModelAttribute("filtro") FiltroCuentasCobrarDTO filtro, Model model) {
        
        List<Cliente> clientes = clienteRepository.findAll();
        // Crear mapa para buscar el nombre del cliente por ID
        Map<String, Cliente> clientesMap = clientes.stream().collect(Collectors.toMap(Cliente::getId, cliente -> cliente));
        
        Page<CuentaPorCobrar> cuentasPage = cuentaPorCobrarService.listarCuentasFiltradas(filtro);
        
        model.addAttribute("cuentas", cuentasPage.getContent());
        model.addAttribute("clientesMap", clientesMap); 
        
        // Atributos de paginación
        model.addAttribute("page", cuentasPage); 
        model.addAttribute("currentPage", cuentasPage.getNumber());
        model.addAttribute("totalPages", cuentasPage.getTotalPages());
        model.addAttribute("totalItems", cuentasPage.getTotalElements());
        
        model.addAttribute("pageTitle", "Gestión de Cuentas por Cobrar");
        
        return "cuentas-por-cobrar/lista";
    }
    
    // 2. CORRECCIÓN: Ver Detalle de Cuenta (GET /detalle/{id})
    // Se ha cambiado de @GetMapping("/{id}") a @GetMapping("/detalle/{id}") para evitar el 404
    @GetMapping("/detalle/{id}")
    public String verDetalle(@PathVariable String id, Model model, RedirectAttributes redirectAttributes) {
        
        CuentaPorCobrar cuenta = cuentaPorCobrarService.obtenerPorId(id)
                .orElseThrow(() -> new IllegalArgumentException("ID de Cuenta no válido:" + id));
        Cliente cliente = clienteRepository.findById(cuenta.getClienteId()).orElse(null);

        // Se inicializa el DTO de abono para usarlo en el fragmento del formulario (aunque sea solo para la validación)
        if (!model.containsAttribute("abono")) {
            AbonoCuentaCobrarDTO abonoDTO = new AbonoCuentaCobrarDTO();
            abonoDTO.setCuentaId(cuenta.getId());
            model.addAttribute("abono", abonoDTO);
        }

        model.addAttribute("cuenta", cuenta);
        model.addAttribute("cliente", cliente);
        model.addAttribute("pageTitle", "Detalle de Cuenta");
        return "cuentas-por-cobrar/detalle";
    }

    // --- 3. MÉTODOS DE ABONO: Mostrar Formulario (GET /registrar-abono/{id}) ---
    
    @GetMapping("/registrar-abono/{id}") 
    public String mostrarFormularioAbono(@PathVariable String id, Model model, RedirectAttributes redirectAttributes) {
        
        Optional<CuentaPorCobrar> cuentaOptional = cuentaPorCobrarService.obtenerPorId(id);

        if (cuentaOptional.isPresent()) {
            CuentaPorCobrar cuenta = cuentaOptional.get();
            
            // Validación de estado: no permitir abono si ya está pagada o anulada
            if (cuenta.getEstado() == CuentaPorCobrar.EstadoCuenta.PAGADA || 
                cuenta.getEstado() == CuentaPorCobrar.EstadoCuenta.ANULADA) {
                redirectAttributes.addFlashAttribute("errorMessage", "No se puede registrar abonos para una cuenta en estado " + cuenta.getEstado().name() + ".");
                return "redirect:/cuentas-por-cobrar/detalle/" + id;
            }

            model.addAttribute("cuenta", cuenta);
            
            // Inicializar DTO si no viene de un error de validación anterior
            if (!model.containsAttribute("abono")) {
                AbonoCuentaCobrarDTO abonoDTO = new AbonoCuentaCobrarDTO();
                abonoDTO.setCuentaId(cuenta.getId());
                model.addAttribute("abono", abonoDTO);
            }

            model.addAttribute("pageTitle", "Registrar Abono a Cuenta: " + cuenta.getVentaId());
            return "cuentas-por-cobrar/form-abono"; 
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Cuenta por Cobrar no encontrada.");
            return "redirect:/cuentas-por-cobrar";
        }
    }

    // --- 4. MÉTODOS DE ABONO: Procesar Abono (POST /procesar-abono) ---
    
    @PostMapping("/procesar-abono")
    public String procesarAbono(@Valid @ModelAttribute("abono") AbonoCuentaCobrarDTO abonoDto, 
                               BindingResult bindingResult, 
                               RedirectAttributes redirectAttributes) {

        // 1. Manejo de errores de validación del DTO (jakarta.validation)
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error de validación en el abono. Verifique el monto y el método.");
            redirectAttributes.addFlashAttribute("abono", abonoDto); // Preservar datos ingresados
            return "redirect:/cuentas-por-cobrar/registrar-abono/" + abonoDto.getCuentaId();
        }

        // 2. Llamada al Servicio y manejo de excepciones de negocio
        try {
            cuentaPorCobrarService.registrarAbono(abonoDto);
            redirectAttributes.addFlashAttribute("successMessage", "Abono registrado exitosamente. La cuenta ha sido actualizada.");
            return "redirect:/cuentas-por-cobrar/detalle/" + abonoDto.getCuentaId();
        } catch (IllegalArgumentException e) {
            // Error de negocio: Cuenta no encontrada o monto excede el saldo
            redirectAttributes.addFlashAttribute("errorMessage", "Error al procesar el abono: " + e.getMessage());
            return "redirect:/cuentas-por-cobrar/detalle/" + abonoDto.getCuentaId();
        } catch (Exception e) {
            // Error inesperado
            redirectAttributes.addFlashAttribute("errorMessage", "Error inesperado al registrar el abono.");
            return "redirect:/cuentas-por-cobrar"; 
        }
    }
    
    // --- 5. Anular Cuenta (POST /anular/{id}) ---
    
    @PostMapping("/anular/{id}")
    public String anularCuenta(@PathVariable String id, RedirectAttributes redirectAttributes) {
        try {
            CuentaPorCobrar cuenta = cuentaPorCobrarService.obtenerPorId(id)
                    .orElseThrow(() -> new IllegalArgumentException("ID de Cuenta no válido:" + id));
            
            // Solo se puede anular si no está PAGADA
            if (cuenta.getEstado() == CuentaPorCobrar.EstadoCuenta.PAGADA) {
                redirectAttributes.addFlashAttribute("errorMessage", "No se puede anular una cuenta que ya está PAGADA.");
            } else {
                cuenta.setEstado(CuentaPorCobrar.EstadoCuenta.ANULADA);
                cuentaPorCobrarService.guardar(cuenta);
                redirectAttributes.addFlashAttribute("successMessage", "Cuenta por Cobrar anulada exitosamente.");
            }
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error al anular: " + e.getMessage());
        }
        return "redirect:/cuentas-por-cobrar";
    }

    // --- 6. MÉTODOS CRUD BÁSICOS (Mantenidos para funcionalidad completa) ---

    @GetMapping("/nuevo")
    public String mostrarFormularioDeCreacion(Model model) {
        // Necesitas cargar las ventas y los clientes aquí.
        List<Venta> listaVentas = ventaRepository.findAll();
        List<Cliente> listaClientes = clienteRepository.findAll();
        
        model.addAttribute("cuenta", new CuentaPorCobrar());
        model.addAttribute("listaVentas", listaVentas);
        model.addAttribute("listaClientes", listaClientes);
        model.addAttribute("pageTitle", "Nueva Cuenta por Cobrar");
        return "cuentas-por-cobrar/formulario";
    }
    
    @PostMapping("/guardar")
    public String guardarCuenta(@ModelAttribute("cuenta") CuentaPorCobrar cuenta, RedirectAttributes redirectAttributes) {
        // Aquí deberías tener una validación más robusta
        cuentaPorCobrarService.guardar(cuenta);
        redirectAttributes.addFlashAttribute("successMessage", "Cuenta por Cobrar guardada exitosamente.");
        return "redirect:/cuentas-por-cobrar";
    }
}