package com.tusistema.sistemaventas.controller;

import com.tusistema.sistemaventas.model.CuentaPorPagar;
import com.tusistema.sistemaventas.model.Proveedor;
import com.tusistema.sistemaventas.repository.ProveedorRepository;
import com.tusistema.sistemaventas.service.CuentaPorPagarService;
import com.tusistema.sistemaventas.dto.PagoCuentaPorPagarDTO; // IMPORTANTE: Nuevo import

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes; 
import org.springframework.validation.BindingResult; // IMPORTANTE: Nuevo import
import jakarta.validation.Valid; // IMPORTANTE: Nuevo import

import java.util.List;
import java.util.Map;
import java.util.Optional; 
import java.util.stream.Collectors;

@Controller
@RequestMapping("/cuentas-por-pagar")
public class CuentaPorPagarController {

    @Autowired
    private CuentaPorPagarService cuentaPorPagarService;

    @Autowired
    private ProveedorRepository proveedorRepository;

    @GetMapping
    public String listarCuentas(Model model) {
        List<CuentaPorPagar> cuentas = cuentaPorPagarService.listarTodas();
        List<String> proveedorIds = cuentas.stream().map(CuentaPorPagar::getProveedorId).collect(Collectors.toList());
        List<Proveedor> proveedores = proveedorRepository.findAllById(proveedorIds);
        Map<String, Proveedor> proveedoresMap = proveedores.stream().collect(Collectors.toMap(Proveedor::getId, p -> p));

        model.addAttribute("cuentas", cuentas);
        model.addAttribute("proveedoresMap", proveedoresMap);
        model.addAttribute("pageTitle", "Cuentas por Pagar");
        return "cuentas-por-pagar/lista";
    }

    @GetMapping("/ver/{id}") 
    public String verDetalleCuenta(@PathVariable String id, Model model, RedirectAttributes redirectAttributes) {
        Optional<CuentaPorPagar> cuentaOptional = cuentaPorPagarService.obtenerPorId(id);

        if (cuentaOptional.isPresent()) {
            CuentaPorPagar cuenta = cuentaOptional.get();
            
            Proveedor proveedor = proveedorRepository.findById(cuenta.getProveedorId()).orElse(null);

            model.addAttribute("cuenta", cuenta);
            model.addAttribute("proveedor", proveedor);
            model.addAttribute("pageTitle", "Detalle Cta. Pagar: " + cuenta.getCompraId()); 
            
            return "cuentas-por-pagar/detalle";
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "La cuenta por pagar solicitada no existe.");
            return "redirect:/cuentas-por-pagar";
        }
    }

    @GetMapping("/registrar-pago/{id}") 
    public String mostrarFormularioPago(@PathVariable String id, Model model, RedirectAttributes redirectAttributes) {
        
        Optional<CuentaPorPagar> cuentaOptional = cuentaPorPagarService.obtenerPorId(id);

        if (cuentaOptional.isPresent()) {
            CuentaPorPagar cuenta = cuentaOptional.get();
            
            if (cuenta.getEstado() == CuentaPorPagar.EstadoCuenta.PAGADA || 
                cuenta.getEstado() == CuentaPorPagar.EstadoCuenta.ANULADA) {
                redirectAttributes.addFlashAttribute("errorMessage", "No se puede registrar pagos para una cuenta en estado " + cuenta.getEstado().name() + ".");
                return "redirect:/cuentas-por-pagar/ver/" + id;
            }

            model.addAttribute("cuenta", cuenta);
            model.addAttribute("pageTitle", "Registrar Pago a Cuenta: " + cuenta.getCompraId());
            
            // Creamos un DTO para el formulario y pre-llenamos el ID de la cuenta.
            PagoCuentaPorPagarDTO pagoDTO = new PagoCuentaPorPagarDTO();
            pagoDTO.setCuentaId(cuenta.getId());
            model.addAttribute("pago", pagoDTO); 

            return "cuentas-por-pagar/form-pago"; 
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Cuenta por Pagar no encontrada.");
            return "redirect:/cuentas-por-pagar";
        }
    }
    
    // =========================================================
    //               MÉTODO NUEVO PARA PROCESAR PAGO
    //               SOLUCIÓN AL ERROR 404
    // =========================================================
    @PostMapping("/procesar-pago")
    public String procesarPago(@Valid PagoCuentaPorPagarDTO pagoDto, 
                               BindingResult bindingResult, 
                               RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            String errorMsg = bindingResult.getFieldError().getDefaultMessage();
            redirectAttributes.addFlashAttribute("errorMessage", "Error de validación en el pago: " + errorMsg);
            // Redirigimos a la vista de detalle para mostrar el error
            return "redirect:/cuentas-por-pagar/ver/" + pagoDto.getCuentaId(); 
        }

        try {
            cuentaPorPagarService.registrarPago(pagoDto);
            redirectAttributes.addFlashAttribute("successMessage", "Pago registrado exitosamente. La cuenta ha sido actualizada.");
            return "redirect:/cuentas-por-pagar/ver/" + pagoDto.getCuentaId();
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error al procesar el pago: " + e.getMessage());
            return "redirect:/cuentas-por-pagar/ver/" + pagoDto.getCuentaId(); 
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error inesperado al registrar el pago.");
            // Si hay un error grave, volvemos al listado principal
            return "redirect:/cuentas-por-pagar"; 
        }
    }


    @PostMapping("/anular/{id}")
    public String anularCuenta(@PathVariable String id) {
        CuentaPorPagar cuenta = cuentaPorPagarService.obtenerPorId(id)
                .orElseThrow(() -> new IllegalArgumentException("ID de Cuenta no válido:" + id));

        cuenta.setEstado(CuentaPorPagar.EstadoCuenta.ANULADA);
        cuentaPorPagarService.guardar(cuenta);

        return "redirect:/cuentas-por-pagar";
    }
}