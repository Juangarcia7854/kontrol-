package com.tusistema.sistemaventas.service; // Reemplaza con tu paquete

import com.tusistema.sistemaventas.model.Cliente; // Reemplaza con tu paquete
import com.tusistema.sistemaventas.repository.ClienteRepository; // Reemplaza con tu paquete
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
// Import innecesario si no se usa directamente: import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class ClienteService {

    private static final Logger logger = LoggerFactory.getLogger(ClienteService.class);
    private final ClienteRepository clienteRepository;

    @Autowired
    public ClienteService(ClienteRepository clienteRepository) {
        this.clienteRepository = clienteRepository;
    }

    @Transactional(readOnly = true)
    public List<Cliente> obtenerTodosLosClientes() {
        logger.info("Servicio: Obteniendo todos los clientes.");
        return clienteRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Cliente> obtenerClientePorId(String id) {
        logger.info("Servicio: Buscando cliente por ID: {}", id);
        return clienteRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public Optional<Cliente> obtenerClientePorNumeroDocumento(String numeroDocumento) {
        logger.info("Servicio: Buscando cliente por número de documento: {}", numeroDocumento);
        // Este método debe estar definido en ClienteRepository
        return clienteRepository.findByNumeroDocumento(numeroDocumento);
    }

    @Transactional(readOnly = true)
    public Optional<Cliente> obtenerClientePorEmail(String email) {
        logger.info("Servicio: Buscando cliente por email: {}", email);
        // Este método debe estar definido en ClienteRepository
        return clienteRepository.findByEmail(email);
    }

    @Transactional
    public Cliente guardarCliente(Cliente cliente) {
        // --- INICIO: Modificación para corregir ID ---
        // Si el ID viene como cadena vacía "" del formulario (caso de nuevo cliente),
        // lo ponemos a null para forzar a MongoDB a generar un nuevo ObjectId.
        if (cliente.getId() != null && cliente.getId().isEmpty()) {
            logger.debug("ID recibido como cadena vacía, estableciendo a null para generación automática.");
            cliente.setId(null);
        }
        // --- FIN: Modificación para corregir ID ---

        // Ahora la lógica de log funciona correctamente para nuevos vs actualizados
        if (cliente.getId() == null) {
            logger.info("Servicio: Guardando NUEVO cliente con documento: {}", cliente.getNumeroDocumento());
        } else {
            logger.info("Servicio: ACTUALIZANDO cliente con ID: {}", cliente.getId());
        }
        // Se guarda el cliente (si id es null, se genera; si tiene id, se actualiza)
        return clienteRepository.save(cliente);
    }

    @Transactional
    public void eliminarCliente(String id) {
        logger.info("Servicio: Intentando eliminar cliente con ID: {}", id);
        if (clienteRepository.existsById(id)) {
            clienteRepository.deleteById(id);
            logger.info("Servicio: Cliente eliminado exitosamente con ID: {}", id);
        } else {
            logger.warn("Servicio: No se encontró cliente para eliminar con ID: {}", id);
            // Considera lanzar una excepción personalizada si prefieres un manejo de error más explícito
            // throw new ClienteNoEncontradoException("No se encontró cliente con ID: " + id);
        }
    }

    @Transactional(readOnly = true)
    public List<Cliente> buscarPorNombreOApellido(String termino) {
        logger.info("Servicio: Buscando clientes por término: {}", termino);
        // Este método debe estar definido en ClienteRepository
        return clienteRepository.findByNombreContainingIgnoreCaseOrApellidoContainingIgnoreCase(termino, termino);
    }

    @Transactional(readOnly = true)
    public boolean existePorNumeroDocumento(String numeroDocumento) {
        logger.info("Servicio: Verificando existencia por documento: {}", numeroDocumento);
        // Este método debe estar definido en ClienteRepository
        return clienteRepository.existsByNumeroDocumento(numeroDocumento);
    }

    @Transactional(readOnly = true)
    public boolean existePorEmail(String email) {
        logger.info("Servicio: Verificando existencia por email: {}", email);
        // Este método debe estar definido en ClienteRepository
        return clienteRepository.existsByEmail(email);
    }

    // Método para el dashboard que tu DashboardController podría estar llamando
    @Transactional(readOnly = true)
    public long contarTotalClientes() {
        logger.info("Servicio: Contando el total de clientes...");
        long count = clienteRepository.count();
        logger.debug("Servicio: Total clientes encontrados: {}", count);
        return count;
    }
}