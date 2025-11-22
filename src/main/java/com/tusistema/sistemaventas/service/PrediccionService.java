package com.tusistema.sistemaventas.service;

import org.springframework.stereotype.Service;
import weka.classifiers.trees.J48;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instances;
import weka.core.SerializationHelper;
import weka.core.Utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

// Marca esta clase como un Servicio de Spring para inyectarla en otros lados (como en el Controller).
@Service
public class PrediccionService {

    // Clase interna (DTO simple) para empaquetar la respuesta de la IA.
    // Devuelve QUÉ predijo (SI/NO) y con QUÉ seguridad (confianza 0.0 - 1.0).
    public static class ResultadoIA {
        public String valor; // Predicción: "SI" o "NO"
        public double confianza; // Certeza: ej. 0.95 (95%)
        
        public ResultadoIA(String valor, double confianza) {
            this.valor = valor;
            this.confianza = confianza;
        }
    }

    // Lista de posibles respuestas de la IA (SI/NO) para los atributos de clase.
    private List<String> claseSiNo = Arrays.asList("SI", "NO");
    
    // Ruta donde se guardan los archivos .model entrenados (en la carpeta del usuario).
    private String modelPath = System.getProperty("user.home") + "/kontrolplus_models/";

    // Variables para mantener los modelos en memoria RAM y no cargarlos de disco cada vez.
    private J48 modelVentaRapida;
    private J48 modelDevolucion;
    private J48 modelPromocion;
    
    // Estructuras que definen cómo son los datos (nombres y tipos de columnas) para Weka.
    private ArrayList<Attribute> attrsVentaRapida;
    private ArrayList<Attribute> attrsDevolucion;
    private ArrayList<Attribute> attrsPromocion;
    
    // Categorías fijas que el modelo conoce. Si llega una nueva, se tratará como "Otros".
    private List<String> categoriasDefinidas = Arrays.asList("Electronica", "Ropa", "Hogar", "Otros");

    // Constructor: Al iniciar la app, definimos la estructura de los datos (cabeceras).
    public PrediccionService() {
        definirAtributosVentaRapida();
        definirAtributosDevolucion();
        definirAtributosPromocion();
    }

    // Método útil para desarrollo: Borra los modelos de RAM para obligar a recargarlos del disco
    // (útil si acabas de re-entrenar un modelo y quieres que la app lo note sin reiniciar).
    public void clearModelCache() {
        this.modelVentaRapida = null;
        this.modelDevolucion = null;
        this.modelPromocion = null;
        System.out.println("Caché de modelos limpiado. Se recargarán en la próxima predicción.");
    }

    // Método privado que lee el archivo .model físico del disco y lo convierte en un objeto J48.
    private J48 cargarModeloInternal(String nombreArchivo) {
        File modelFile = new File(modelPath + nombreArchivo);
        // Si no existe el archivo, retornamos null (la predicción fallará controladamente).
        if (!modelFile.exists()) return null;
        try (InputStream is = new FileInputStream(modelFile)) {
            // SerializationHelper es la herramienta de Weka para leer objetos guardados.
            return (J48) SerializationHelper.read(is);
        } catch (Exception e) {
            return null;
        }
    }

    // --- MÉTODOS PÚBLICOS DE PREDICCIÓN ---

    // Caso 1: Venta Rápida
    public ResultadoIA predecirVentaRapida(int stock, int ventas7d, double precio) throws Exception {
        // Lazy loading: Si el modelo no está en RAM, intenta cargarlo.
        if (modelVentaRapida == null) modelVentaRapida = cargarModeloInternal("j48_ventarapida.model");
        if (modelVentaRapida == null) return new ResultadoIA("Error: Modelo no cargado", 0.0);
        
        // Prepara los datos numéricos en un array en el orden exacto que espera Weka.
        double[] valores = {stock, ventas7d, precio};
        // Llama al método genérico "predecir".
        return predecir(valores, modelVentaRapida, attrsVentaRapida);
    }

    // Caso 2: Riesgo de Devolución
    public ResultadoIA predecirDevolucion(double precio, String categoria, double tasaDevolucion) throws Exception {
        if (modelDevolucion == null) modelDevolucion = cargarModeloInternal("j48_devolucion.model");
        if (modelDevolucion == null) return new ResultadoIA("Error: Modelo no cargado", 0.0);
        
        // Weka no entiende Strings directos en la predicción, necesita el índice numérico de la lista.
        double valCategoria = this.categoriasDefinidas.indexOf(categoria);
        // Si la categoría no está en la lista conocida, usa "Otros" para evitar error.
        if (valCategoria == -1) valCategoria = this.categoriasDefinidas.indexOf("Otros");
        
        double[] valores = {precio, valCategoria, tasaDevolucion};
        return predecir(valores, modelDevolucion, attrsDevolucion);
    }

    // Caso 3: Necesidad de Promoción
    public ResultadoIA predecirPromocion(int stock, int ventas30d, int diasSinVentas) throws Exception {
        if (modelPromocion == null) modelPromocion = cargarModeloInternal("j48_promocion.model");
        if (modelPromocion == null) return new ResultadoIA("Error: Modelo no cargado", 0.0);
        
        double[] valores = {stock, ventas30d, diasSinVentas};
        return predecir(valores, modelPromocion, attrsPromocion);
    }

    // --- MÉTODO GENÉRICO CENTRAL (CORE) ---
    // Este método hace el trabajo pesado común para cualquier modelo.
    private ResultadoIA predecir(double[] valores, J48 modelo, ArrayList<Attribute> atributos) throws Exception {
        // 1. Crea un dataset vacío con la estructura (cabeceras) definida.
        Instances dataset = new Instances("Prediccion", atributos, 0);
        // Indica que el último atributo es el que queremos predecir (la Clase: SI/NO).
        dataset.setClassIndex(atributos.size() - 1);

        // 2. Llena un array con los valores que recibimos.
        double[] instanceValues = new double[dataset.numAttributes()];
        for (int i = 0; i < valores.length; i++) {
            instanceValues[i] = valores[i];
        }
        // El último valor (la respuesta) es desconocido, así que ponemos "Missing".
        instanceValues[dataset.numAttributes() - 1] = Utils.missingValue(); 

        // 3. Crea la instancia (la fila de datos) y la asocia al dataset.
        DenseInstance instancia = new DenseInstance(1.0, instanceValues);
        instancia.setDataset(dataset);

        // 4. CLASIFICAR: El modelo evalúa la instancia y devuelve un índice (ej: 0 para SI, 1 para NO).
        double resultadoIndex = modelo.classifyInstance(instancia);
        // Traduce ese índice (0 o 1) al texto real ("SI" o "NO").
        String valorPredicho = atributos.get(atributos.size() - 1).value((int) resultadoIndex);

        // 5. CALCULAR CONFIANZA: Obtiene la probabilidad estadística.
        double[] dist = modelo.distributionForInstance(instancia);
        // dist[0] es probabilidad de SI, dist[1] es probabilidad de NO.
        double confianza = dist[(int) resultadoIndex]; // Tomamos la del ganador.

        return new ResultadoIA(valorPredicho, confianza);
    }
    
    // --- DEFINICIONES DE ESTRUCTURA (METADATA) ---
    // Estos métodos construyen las cabeceras que Weka necesita para entender los datos.
    // Es CRUCIAL que el orden aquí sea IDÉNTICO al orden en que se entrenó el modelo.
    
    private void definirAtributosVentaRapida() {
        attrsVentaRapida = new ArrayList<>();
        attrsVentaRapida.add(new Attribute("stock_actual"));
        attrsVentaRapida.add(new Attribute("ventas_7_dias"));
        attrsVentaRapida.add(new Attribute("precio"));
        // El atributo clase es Nominal (lista fija de valores SI/NO).
        attrsVentaRapida.add(new Attribute("clase_venta_rapida", claseSiNo));
    }
    
    private void definirAtributosDevolucion() {
        attrsDevolucion = new ArrayList<>();
        attrsDevolucion.add(new Attribute("precio"));
        // Atributo Nominal (lista de categorías).
        attrsDevolucion.add(new Attribute("categoria_producto", this.categoriasDefinidas));
        attrsDevolucion.add(new Attribute("tasa_devolucion_historica"));
        attrsDevolucion.add(new Attribute("clase_devolucion", claseSiNo));
    }
    
    private void definirAtributosPromocion() {
        attrsPromocion = new ArrayList<>();
        attrsPromocion.add(new Attribute("stock_actual"));
        attrsPromocion.add(new Attribute("ventas_30_dias"));
        attrsPromocion.add(new Attribute("dias_sin_ventas"));
        attrsPromocion.add(new Attribute("clase_promocion", claseSiNo));
    }
}