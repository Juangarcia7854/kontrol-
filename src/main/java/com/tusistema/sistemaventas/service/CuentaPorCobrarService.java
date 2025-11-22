package com.tusistema.sistemaventas.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.tusistema.sistemaventas.model.CuentaPorCobrar;
import com.tusistema.sistemaventas.repository.CuentaPorCobrarRepository;
import com.tusistema.sistemaventas.dto.FiltroCuentasCobrarDTO;
import com.tusistema.sistemaventas.dto.AbonoCuentaCobrarDTO; 

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.Arrays;

@Service
public class CuentaPorCobrarService {

    @Autowired
    private CuentaPorCobrarRepository cuentaPorCobrarRepository;

    public List<CuentaPorCobrar> listarTodas() {
        return cuentaPorCobrarRepository.findAll();
    }

    public Optional<CuentaPorCobrar> obtenerPorId(String id) {
        return cuentaPorCobrarRepository.findById(id);
    }

    public CuentaPorCobrar guardar(CuentaPorCobrar cuentaPorCobrar) {
        return cuentaPorCobrarRepository.save(cuentaPorCobrar);
    }

    public void eliminar(String id) {
        cuentaPorCobrarRepository.deleteById(id);
    }
    
    // **[ACTUALIZADO]** Consulta Filtrada (Corregido)
    public Page<CuentaPorCobrar> listarCuentasFiltradas(FiltroCuentasCobrarDTO filtro) {
        String term = (filtro.getTerm() == null || filtro.getTerm().isEmpty()) ? "" : filtro.getTerm();
        Pageable pageable = filtro.getPageable();
        
        String[] estados;
        switch (filtro.getStatus().toUpperCase()) {
            case "ACTIVOS":
                estados = new String[]{"PENDIENTE", "VENCIDA"};
                break;
            case "INACTIVOS":
                estados = new String[]{"PAGADA", "ANULADA"};
                break;
            case "TODOS":
                estados = new String[]{"PENDIENTE", "PAGADA", "VENCIDA", "ANULADA"};
                break;
            default: 
                estados = new String[]{filtro.getStatus().toUpperCase()};
                break;
        }

        return cuentaPorCobrarRepository.findByStatusAndSearchTerm(estados, term, pageable);
    }
    
    // **[NUEVO]** Registro de Abono
    public void registrarAbono(AbonoCuentaCobrarDTO abonoDto) throws IllegalArgumentException {
        CuentaPorCobrar cuenta = obtenerPorId(abonoDto.getCuentaId())
            .orElseThrow(() -> new IllegalArgumentException("Cuenta por Cobrar no encontrada."));

        if (cuenta.getEstado() == CuentaPorCobrar.EstadoCuenta.ANULADA ||
            cuenta.getEstado() == CuentaPorCobrar.EstadoCuenta.PAGADA) {
             throw new IllegalArgumentException("No se puede abonar a una cuenta en estado " + cuenta.getEstado().name() + ".");
        }
        
        BigDecimal montoPagadoActual = cuenta.getMontoPagado() != null ? cuenta.getMontoPagado() : BigDecimal.ZERO;
        BigDecimal saldoPendiente = cuenta.getMontoTotal().subtract(montoPagadoActual);
        
        if (abonoDto.getMonto().compareTo(saldoPendiente) > 0) {
            throw new IllegalArgumentException("El monto del abono (" + abonoDto.getMonto() 
                                                + ") excede el saldo pendiente (" + saldoPendiente + ").");
        }
        
        BigDecimal nuevoMontoPagado = montoPagadoActual.add(abonoDto.getMonto());
        cuenta.setMontoPagado(nuevoMontoPagado);

        if (nuevoMontoPagado.compareTo(cuenta.getMontoTotal()) >= 0) {
            cuenta.setEstado(CuentaPorCobrar.EstadoCuenta.PAGADA);
            cuenta.setMontoPagado(cuenta.getMontoTotal()); 
        }

        guardar(cuenta);
    }
}