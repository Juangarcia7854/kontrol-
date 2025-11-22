package com.tusistema.sistemaventas.service;

import com.tusistema.sistemaventas.model.*;
import com.tusistema.sistemaventas.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import weka.classifiers.trees.J48;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instances;
import weka.core.SerializationHelper;

import org.springframework.data.domain.Sort; 
import org.springframework.scheduling.annotation.Async; 

import java.io.File;
// --- IMPORTS PARA MANEJO DE HILOS (CONCURRENCIA) ---
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
// ---------------------------------------------------

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
public class ModelTrainingService {

    // Inyección de repositorios: Necesitamos acceso total a la BD para "aprender" del pasado.
    @Autowired private ProductoRepository productoRepository;
    @Autowired private InventarioRepository inventarioRepository;
    @Autowired private VentaRepository ventaRepository;
    @Autowired private DevolucionRepository devolucionRepository;

    // Definimos las posibles respuestas de la IA (Clases).
    private List<String> claseSiNo = Arrays.asList("SI", "NO");
    
    // Ruta donde se guardarán los archivos .model generados.
    private String modelPath = System.getProperty("user.home") + "/kontrolplus_models/";
    
    // Categorías conocidas para el entrenamiento.
    private List<String> categoriasDefinidas = Arrays.asList("Electronica", "Ropa", "Hogar", "Otros"); 

    // Método auxiliar privado para guardar el archivo físico (.model) en el disco duro.
    private void guardarModeloInternal(J48 modelo, String nombreArchivo) throws Exception {
        File modelDir = new File(modelPath);
        // Si la carpeta no existe, la crea.
        if (!modelDir.exists()) {
            modelDir.mkdirs(); 
        }
        String fullPath = modelPath + nombreArchivo;
        // Escribe el objeto binario en el disco.
        SerializationHelper.write(fullPath, modelo);
        System.out.println("Modelo guardado en: " + fullPath);
    }

    // --- 1. ENTRENAR MODELO: VENTA RÁPIDA ---
    // @Async permite que este método corra en un hilo separado. 
    // Así, si tarda 10 segundos entrenando, no congela la pantalla del usuario.
    @Async 
    public Future<String> entrenarModeloVentaRapida() throws Exception {
        
        // 1. Definimos la estructura de la tabla de datos para Weka (Atributos).
        ArrayList<Attribute> attrs = new ArrayList<>();
        attrs.add(new Attribute("stock_actual"));   // Numérico
        attrs.add(new Attribute("ventas_7_dias"));  // Numérico
        attrs.add(new Attribute("precio"));         // Numérico
        attrs.add(new Attribute("clase_venta_rapida", claseSiNo)); // Nominal (La respuesta que buscamos)
        
        // Creamos el contenedor de datos (Dataset) vacío.
        Instances data = new Instances("VentaRapidaData", attrs, 0);
        // Le decimos que la última columna es la que queremos predecir (la Clase).
        data.setClassIndex(attrs.size() - 1);

        // Obtenemos todas las ventas de los últimos 7 días en un Mapa para acceso rápido.
        Map<String, Integer> ventas7dMap = getVentasAgregadas(7);

        // 2. Llenamos el dataset con ejemplos reales de la base de datos.
        for (Producto p : productoRepository.findAll()) {
            Inventario inv = inventarioRepository.findByProductoId(p.getId()).orElse(new Inventario(p.getId(), 0));
            
            int stock = inv.getCantidad();
            int ventas7d = ventas7dMap.getOrDefault(p.getId(), 0);
            double precio = p.getPrecio().doubleValue();
            
            // 3. ETIQUETADO AUTOMÁTICO (La parte "Supervisor"):
            // Aquí definimos la regla de negocio para enseñar a la IA qué consideramos "Venta Rápida".
            // Regla: Si hay stock Y (se vendieron más de 5 O el stock es muy bajo comparado con ventas)...
            String clase = (stock > 0 && (ventas7d > 5 || stock < (ventas7d * 2))) ? "SI" : "NO";
            
            // Agregamos esta "fila" de ejemplo al dataset.
            data.add(new DenseInstance(1.0, new double[]{stock, ventas7d, precio, claseSiNo.indexOf(clase)}));
        }
        
        // Si no hay datos, abortamos para no crear un modelo vacío.
        if (data.isEmpty()) return CompletableFuture.completedFuture("Entrenamiento VENTA RÁPIDA OMITIDO: No hay productos.");
        
        // 4. CONSTRUCCIÓN DEL MODELO (J48 es un árbol de decisión C4.5).
        J48 j48 = new J48(); 
        j48.buildClassifier(data); // Aquí ocurre la magia matemática.
        
        // Guardamos el resultado en disco.
        guardarModeloInternal(j48, "j48_ventarapida.model");
        
        // Retornamos un mensaje encapsulado en Future (promesa de resultado).
        return CompletableFuture.completedFuture("Modelo VENTA RÁPIDA entrenado con " + data.size() + " productos.");
    }

    // --- 2. ENTRENAR MODELO: RIESGO DE DEVOLUCIÓN ---
    @Async
    public Future<String> entrenarModeloDevolucion() throws Exception {
        // Definición de atributos (Notar que usamos categorías aquí).
        ArrayList<Attribute> attrs = new ArrayList<>();
        attrs.add(new Attribute("precio"));
        attrs.add(new Attribute("categoria_producto", this.categoriasDefinidas));
        attrs.add(new Attribute("tasa_devolucion_historica"));
        attrs.add(new Attribute("clase_devolucion", claseSiNo));
        
        Instances data = new Instances("DevolucionData", attrs, 0);
        data.setClassIndex(attrs.size() - 1);

        // Obtenemos historial largo (365 días) para tener datos significativos.
        Map<String, Integer> ventasMap = getVentasAgregadas(365); 
        Map<String, Integer> devMap = getDevolucionesAgregadas(365); 

        for (Producto p : productoRepository.findAll()) {
            double precio = p.getPrecio().doubleValue();
            String cat = p.getCategoria();
            
            // Manejo de categoría desconocida para evitar crash en Weka.
            double valCat = categoriasDefinidas.indexOf(cat);
            if (valCat == -1) valCat = categoriasDefinidas.indexOf("Otros");

            int totalVendido = ventasMap.getOrDefault(p.getId(), 0);
            int totalDevuelto = devMap.getOrDefault(p.getId(), 0);
            
            // Cálculo de tasa: Si vendí 100 y me devolvieron 15, tasa = 0.15.
            double tasaDevolucion = (totalVendido == 0) ? 0.0 : (double) totalDevuelto / totalVendido;

            // Regla de Etiquetado: Si devuelven más del 10% de las veces, es "SI" (Riesgoso).
            String clase = (tasaDevolucion > 0.10) ? "SI" : "NO";
            
            data.add(new DenseInstance(1.0, new double[]{precio, valCat, tasaDevolucion, claseSiNo.indexOf(clase)}));
        }
        
        if (data.isEmpty()) return CompletableFuture.completedFuture("Entrenamiento DEVOLUCIÓN OMITIDO: No hay productos.");
        
        J48 j48 = new J48(); j48.buildClassifier(data);
        guardarModeloInternal(j48, "j48_devolucion.model");
        
        return CompletableFuture.completedFuture("Modelo DEVOLUCIÓN entrenado con " + data.size() + " productos.");
    }

    // --- 3. ENTRENAR MODELO: NECESIDAD DE PROMOCIÓN ---
    @Async
    public Future<String> entrenarModeloPromocion() throws Exception {
        ArrayList<Attribute> attrs = new ArrayList<>();
        attrs.add(new Attribute("stock_actual"));
        attrs.add(new Attribute("ventas_30_dias"));
        attrs.add(new Attribute("dias_sin_ventas")); // Recencia
        attrs.add(new Attribute("clase_promocion", claseSiNo));
        
        Instances data = new Instances("PromocionData", attrs, 0);
        data.setClassIndex(attrs.size() - 1);

        Map<String, Integer> ventas30dMap = getVentasAgregadas(30);
        Map<String, Long> diasSinVentaMap = getDiasDesdeUltimaVenta(); 

        for (Producto p : productoRepository.findAll()) {
            Inventario inv = inventarioRepository.findByProductoId(p.getId()).orElse(new Inventario(p.getId(), 0));
            int stock = inv.getCantidad();
            int ventas30d = ventas30dMap.getOrDefault(p.getId(), 0);
            long diasSinVentas = diasSinVentaMap.getOrDefault(p.getId(), 999L); 

            // Regla de Etiquetado: 
            // Si hay mucho stock (>20), se vende poco (<5 al mes) Y lleva tiempo parado (>30 días) -> SI PROMOCIONAR.
            String clase = (stock > 20 && ventas30d < 5 && diasSinVentas > 30) ? "SI" : "NO";
            
            data.add(new DenseInstance(1.0, new double[]{stock, ventas30d, diasSinVentas, claseSiNo.indexOf(clase)}));
        }
        
        if (data.isEmpty()) return CompletableFuture.completedFuture("Entrenamiento PROMOCIÓN OMITIDO: No hay productos.");
        
        J48 j48 = new J48(); j48.buildClassifier(data);
        guardarModeloInternal(j48, "j48_promocion.model");
        
        return CompletableFuture.completedFuture("Modelo PROMOCIÓN entrenado con " + data.size() + " productos.");
    }


    // ===================================
    // --- MÉTODOS AYUDANTES (HELPERS) ---
    // Estos métodos transforman datos crudos de Mongol en Mapas Java rápidos de leer.
    // ===================================

    // Suma todas las ventas por producto en los últimos X días.
    public Map<String, Integer> getVentasAgregadas(int dias) {
        List<Venta> ventas = ventaRepository.findByFechaVentaBetween(LocalDateTime.now().minusDays(dias), LocalDateTime.now());
        Map<String, Integer> mapaVentas = new HashMap<>();
        for (Venta v : ventas) {
            if (v.getDetalles() != null) { 
                for (DetalleVenta d : v.getDetalles()) {
                    // .merge suma la cantidad nueva a la que ya existía en el mapa.
                    mapaVentas.merge(d.getProductoId(), d.getCantidad(), Integer::sum);
                }
            }
        }
        return mapaVentas;
    }

    // Suma todas las devoluciones por producto.
    public Map<String, Integer> getDevolucionesAgregadas(int dias) {
        List<Devolucion> devoluciones = devolucionRepository.findByFechaDevolucionBetween(LocalDateTime.now().minusDays(dias), LocalDateTime.now());
        Map<String, Integer> mapaDevoluciones = new HashMap<>();
        
        for (Devolucion dev : devoluciones) {
            if (dev.getDetalles() != null) {
                for (DetalleDevolucion d : dev.getDetalles()) { 
                    int cantidad = (d.getCantidadDevuelta() != null) ? d.getCantidadDevuelta() : 0;
                    mapaDevoluciones.merge(d.getProductoId(), cantidad, Integer::sum); 
                }
            }
        }
        return mapaDevoluciones;
    }

    // Calcula cuántos días han pasado desde la última venta de cada producto.
    public Map<String, Long> getDiasDesdeUltimaVenta() {
        Map<String, Long> mapaDias = new HashMap<>();
        // Traemos ventas ordenadas de la más reciente a la más antigua.
        List<Venta> ventas = ventaRepository.findAll(Sort.by(Sort.Direction.DESC, "fechaVenta"));
        Map<String, LocalDate> ultimaVentaMap = new HashMap<>();
        
        // Llenamos el mapa solo con la primera aparición (la más reciente) de cada producto.
        for (Venta v : ventas) {
            if (v.getDetalles() != null) {
                for (DetalleVenta d : v.getDetalles()) {
                    if (!ultimaVentaMap.containsKey(d.getProductoId())) {
                        ultimaVentaMap.put(d.getProductoId(), v.getFechaVenta().toLocalDate());
                    }
                }
            }
        }
        // Calculamos la diferencia en días contra la fecha de hoy.
        for (Map.Entry<String, LocalDate> entry : ultimaVentaMap.entrySet()) {
            mapaDias.put(entry.getKey(), ChronoUnit.DAYS.between(entry.getValue(), LocalDate.now()));
        }
        return mapaDias;
    }
}