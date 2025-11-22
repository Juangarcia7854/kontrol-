package com.tusistema.sistemaventas.controller;

import com.tusistema.sistemaventas.model.Compra;
import com.tusistema.sistemaventas.model.Proveedor;
import com.tusistema.sistemaventas.repository.ProveedorRepository;
import com.tusistema.sistemaventas.service.CompraService;
import com.tusistema.sistemaventas.service.ProductoService; // ✅ Importación de ProductoService
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/compras")
public class CompraController {

    @Autowired
    private CompraService compraService;

    @Autowired
    private ProveedorRepository proveedorRepository;

    // --- AGREGANDO: INYECCIÓN DEL PRODUCTOSERVICE ---
    @Autowired 
    private ProductoService productoService; 
    // ------------------------------------------------

    @GetMapping
    public String listarCompras(Model model) {
        List<Compra> compras = compraService.listarTodas();
        
        List<String> proveedorIds = compras.stream()
                                         .map(compra -> compra.getProveedorId())
                                         .collect(Collectors.toList());

        List<Proveedor> proveedores = proveedorRepository.findAllById(proveedorIds);
        Map<String, Proveedor> proveedoresMap = proveedores.stream()
                                                         .collect(Collectors.toMap(Proveedor::getId, p -> p));

        model.addAttribute("compras", compras);
        model.addAttribute("proveedoresMap", proveedoresMap);
        model.addAttribute("pageTitle", "Gestión de Compras");
        return "compras/lista";
    }

    @GetMapping("/{id}")
    public String verDetallesCompra(@PathVariable("id") String id, Model model) {
        Compra compra = compraService.obtenerPorId(id)
            .orElseThrow(() -> new RuntimeException("Compra no encontrada con ID: " + id));

        Proveedor proveedor = proveedorRepository.findById(compra.getProveedorId())
                                .orElse(null);

        model.addAttribute("compra", compra);
        model.addAttribute("proveedor", proveedor);
        model.addAttribute("pageTitle", "Detalles de Compra");
        
        return "compras/detalles";
    }

    // --- AGREGANDO Y CORRIGIENDO: MÉTODO MOSTRAR FORMULARIO ---
    @GetMapping("/nuevo")
    public String mostrarFormulario(Model model) {
        model.addAttribute("compra", new Compra());
        model.addAttribute("listaProveedores", proveedorRepository.findAll());
        
        // Se llama al método correcto obtenerTodosLosProductos()
        model.addAttribute("listaProductos", productoService.obtenerTodosLosProductos()); 
        
        model.addAttribute("pageTitle", "Registrar Nueva Compra");
        return "compras/formulario";
    }
    // ----------------------------------------------------------

    @PostMapping("/guardar")
    public String guardarCompra(@ModelAttribute("compra") Compra compra) {
        compraService.guardar(compra);
        return "redirect:/compras";
    }
}