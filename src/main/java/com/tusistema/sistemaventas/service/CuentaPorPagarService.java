package com.tusistema.sistemaventas.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.tusistema.sistemaventas.model.CuentaPorPagar;
import com.tusistema.sistemaventas.repository.CuentaPorPagarRepository;
import com.tusistema.sistemaventas.dto.PagoCuentaPorPagarDTO; // IMPORTANTE: Nuevo import

import java.math.BigDecimal; // IMPORTANTE: Nuevo import
import java.util.List;
import java.util.Optional;

@Service
public class CuentaPorPagarService {

    @Autowired
    private CuentaPorPagarRepository cuentaPorPagarRepository;

    public List<CuentaPorPagar> listarTodas() {
        return cuentaPorPagarRepository.findAll();
    }

    public Optional<CuentaPorPagar> obtenerPorId(String id) {
        return cuentaPorPagarRepository.findById(id);
    }

    public CuentaPorPagar guardar(CuentaPorPagar cuentaPorPagar) {
        return cuentaPorPagarRepository.save(cuentaPorPagar);
    }

    public void eliminar(String id) {
        cuentaPorPagarRepository.deleteById(id);
    }
    
    // =========================================================
    //               MÉTODO NUEVO PARA REGISTRAR PAGO
    // =========================================================
    public void registrarPago(PagoCuentaPorPagarDTO pagoDto) throws IllegalArgumentException {
        // 1. Obtener la cuenta
        CuentaPorPagar cuenta = obtenerPorId(pagoDto.getCuentaId())
            .orElseThrow(() -> new IllegalArgumentException("Cuenta por Pagar no encontrada."));

        // 2. Validar que la cuenta no esté ANULADA o PAGADA
        if (cuenta.getEstado() == CuentaPorPagar.EstadoCuenta.ANULADA ||
            cuenta.getEstado() == CuentaPorPagar.EstadoCuenta.PAGADA) {
             throw new IllegalArgumentException("No se puede registrar pagos para una cuenta en estado " + cuenta.getEstado().name() + ".");
        }

        // 3. Calcular el saldo actual
        BigDecimal saldoPendiente = cuenta.getMontoTotal().subtract(cuenta.getMontoPagado());

        // 4. Validar que el monto no exceda el saldo
        if (pagoDto.getMonto().compareTo(saldoPendiente) > 0) {
            throw new IllegalArgumentException("El monto del pago (" + pagoDto.getMonto() 
                                                + ") excede el saldo pendiente (" + saldoPendiente + ").");
        }
        
        // 5. Actualizar el monto pagado
        BigDecimal nuevoMontoPagado = cuenta.getMontoPagado().add(pagoDto.getMonto());
        cuenta.setMontoPagado(nuevoMontoPagado);

        // 6. Determinar el nuevo estado
        if (nuevoMontoPagado.compareTo(cuenta.getMontoTotal()) >= 0) {
            cuenta.setEstado(CuentaPorPagar.EstadoCuenta.PAGADA);
        }
        // Si no se pagó el total, el estado (PENDIENTE o VENCIDA) se mantiene

        // 7. Guardar la actualización
        guardar(cuenta);
    }
}