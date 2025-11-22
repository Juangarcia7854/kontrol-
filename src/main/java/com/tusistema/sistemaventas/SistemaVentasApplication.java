package com.tusistema.sistemaventas;

import com.tusistema.sistemaventas.model.Inventario;
import com.tusistema.sistemaventas.model.Producto;
import com.tusistema.sistemaventas.repository.InventarioRepository;
import com.tusistema.sistemaventas.repository.ProductoRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate; // <-- IMPORT AÑADIDO

import java.util.List;

@SpringBootApplication
public class SistemaVentasApplication {

    public static void main(String[] args) {
        SpringApplication.run(SistemaVentasApplication.class, args);
    }

    // ===================================
    // == BEAN NUEVO PARA LLAMADAS HTTP ==
    // ===================================
    /**
     * Bean para poder inyectar RestTemplate y hacer llamadas HTTP
     * al servicio de predicción Python.
     * @return Una instancia de RestTemplate.
     */
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
    // ===================================

    /**
     * Este método se ejecuta una sola vez al iniciar la aplicación.
     * Su propósito es asegurarse de que cada producto en el sistema tenga
     * un registro de inventario correspondiente.
     */
    @Bean
    CommandLineRunner inicializarInventario(ProductoRepository productoRepo, InventarioRepository inventarioRepo) {
        return args -> {
            System.out.println("✅ Verificando registros de inventario...");
            List<Producto> productos = productoRepo.findAll();
            int creados = 0;

            for (Producto producto : productos) {
                // Revisa si ya existe un registro de inventario para este producto
                if (inventarioRepo.findByProductoId(producto.getId()).isEmpty()) {
                    // ✅ CAMBIO: Se inicia el stock en 100 en lugar de 0.
                    Inventario nuevoInventario = new Inventario(producto.getId(), 100);
                    inventarioRepo.save(nuevoInventario);
                    creados++;
                }
            }
            if (creados > 0) {
                System.out.println("✅ Se crearon " + creados + " nuevos registros de inventario con stock inicial de 100.");
            } else {
                System.out.println("✅ Todos los productos ya tienen su registro de inventario.");
            }
        };
    }
}