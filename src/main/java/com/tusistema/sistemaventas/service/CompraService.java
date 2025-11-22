package com.tusistema.sistemaventas.service;

import com.tusistema.sistemaventas.model.Compra;
import com.tusistema.sistemaventas.model.CuentaPorPagar;
import com.tusistema.sistemaventas.repository.CompraRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class CompraService {

    @Autowired
    private CompraRepository compraRepository;

    // --- PASO 1: Inyectamos el servicio de Cuentas por Pagar ---
    // CompraService ahora necesita poder hablar con CuentaPorPagarService.
    @Autowired
    private CuentaPorPagarService cuentaPorPagarService;

    public List<Compra> listarTodas() {
        return compraRepository.findAll();
    }

    public Optional<Compra> obtenerPorId(String id) {
        return compraRepository.findById(id);
    }

    // --- PASO 2: Modificamos el método guardar ---
    public Compra guardar(Compra compra) {
        // Primero, guardamos la compra para que obtenga un ID
        Compra compraGuardada = compraRepository.save(compra);

        // Luego, creamos y poblamos la Cuenta por Pagar
        CuentaPorPagar nuevaCuenta = new CuentaPorPagar();
        nuevaCuenta.setCompraId(compraGuardada.getId());
        nuevaCuenta.setProveedorId(compraGuardada.getProveedorId());
        nuevaCuenta.setMontoTotal(compraGuardada.getTotal());
        nuevaCuenta.setMontoPagado(BigDecimal.ZERO); // Por defecto, no se ha pagado nada
        nuevaCuenta.setEstado(CuentaPorPagar.EstadoCuenta.PENDIENTE);
        nuevaCuenta.setFechaDeVencimiento(LocalDate.now().plusDays(30)); // Vence en 30 días

        // Finalmente, guardamos la nueva Cuenta por Pagar
        cuentaPorPagarService.guardar(nuevaCuenta);

        return compraGuardada;
    }

    public void eliminar(String id) {
        // En un futuro, aquí deberíamos validar si se puede eliminar
        // una compra que ya tiene una cuenta por pagar asociada.
        compraRepository.deleteById(id);
    }
}