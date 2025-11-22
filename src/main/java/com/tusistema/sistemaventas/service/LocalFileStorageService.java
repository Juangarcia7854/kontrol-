package com.tusistema.sistemaventas.service;

import com.tusistema.sistemaventas.config.AppProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import jakarta.annotation.PostConstruct; // Importación correcta
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.*;

@Service
public class LocalFileStorageService implements FileStorageService {

    private static final Logger logger = LoggerFactory.getLogger(LocalFileStorageService.class);
    private final Path fileStorageLocation;
    // Guarda la ruta base donde se sirven las imágenes, si decides servirlas directamente
    private final String imageServePath = "/api/productos/images/";

    @Autowired
    public LocalFileStorageService(AppProperties appProperties) {
        // Obtiene la ruta del directorio de subida desde las propiedades
        this.fileStorageLocation = Paths.get(appProperties.getUploadDir())
                .toAbsolutePath().normalize();
        logger.info("Directorio de subida de archivos configurado en: {}", this.fileStorageLocation.toString());
    }

    @PostConstruct
    public void init() {
        // Crea el directorio de subida si no existe al iniciar la aplicación
        try {
            Files.createDirectories(this.fileStorageLocation);
            logger.info("Directorio de subida creado o ya existente.");
        } catch (Exception ex) {
            logger.error("No se pudo crear el directorio de subida: {}", this.fileStorageLocation.toString(), ex);
            throw new RuntimeException("No se pudo crear el directorio donde se guardarán los archivos subidos.", ex);
        }
    }

    @Override
    public String storeFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
             logger.warn("Intento de guardar un archivo nulo o vacío.");
            throw new IllegalArgumentException("El archivo no puede estar vacío.");
        }
        // Normalizar nombre de archivo y crear uno único
        String originalFileName = StringUtils.cleanPath(file.getOriginalFilename());
        String uniqueFileName = System.currentTimeMillis() + "_" + originalFileName.replaceAll("[^a-zA-Z0-9.\\-]", "_"); // Nombre único y limpio

        try {
            // Verificar si el nombre de archivo es inválido
            if (uniqueFileName.contains("..")) {
                throw new RuntimeException("Nombre de archivo inválido: " + uniqueFileName);
            }

            // Copiar archivo a la ubicación destino (reemplazando si existe)
            Path targetLocation = this.fileStorageLocation.resolve(uniqueFileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
            logger.info("Archivo guardado exitosamente como: {}", uniqueFileName);
            return uniqueFileName; // Devuelve solo el nombre único
        } catch (IOException ex) {
            logger.error("No se pudo guardar el archivo '{}': {}", uniqueFileName, ex.getMessage(), ex);
            throw new RuntimeException("No se pudo guardar el archivo " + uniqueFileName + ". Por favor, inténtelo de nuevo.", ex);
        }
    }

     @Override
    public String getFileUrl(String fileName) {
         if (fileName == null || fileName.isBlank()) {
             return null; // O una URL de imagen por defecto
         }
        // Construye la URL completa usando la ruta base donde se sirven las imágenes
        // Asegúrate de que el endpoint /api/productos/images/{filename:.+} exista en tu controller
        return ServletUriComponentsBuilder.fromCurrentContextPath()
                 .path(this.imageServePath) // Usa la ruta definida
                 .path(fileName)
                 .toUriString();
    }

    @Override
    public Resource loadFileAsResource(String fileName) {
        try {
            Path filePath = this.fileStorageLocation.resolve(fileName).normalize();
            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists() && resource.isReadable()) {
                return resource;
            } else {
                 logger.warn("Archivo no encontrado o no legible en la ruta: {}", filePath.toString());
                throw new RuntimeException("Archivo no encontrado: " + fileName);
            }
        } catch (MalformedURLException ex) {
            logger.error("URL mal formada al intentar cargar el archivo: {}", fileName, ex);
            throw new RuntimeException("Archivo no encontrado: " + fileName, ex);
        }
    }

    @Override
    public void deleteFileByName(String fileName) {
        if (fileName == null || fileName.isBlank()) {
            logger.warn("Intento de eliminar archivo con nombre nulo o vacío.");
            return;
        }
        try {
            Path filePath = this.fileStorageLocation.resolve(fileName).normalize();
            if(Files.exists(filePath)) {
                Files.delete(filePath);
                logger.info("Archivo eliminado exitosamente: {}", fileName);
            } else {
                 logger.warn("Intento de eliminar archivo no existente: {}", fileName);
            }
        } catch (IOException ex) {
            logger.error("No se pudo eliminar el archivo '{}': {}", fileName, ex.getMessage(), ex);
            // Puedes decidir si lanzar una excepción o solo loggear el error
            // throw new RuntimeException("No se pudo eliminar el archivo " + fileName, ex);
        }
    }

    @Override
    public void deleteFileByUrl(String fileUrl) {
        if (fileUrl == null || fileUrl.isBlank()) {
             logger.warn("Intento de eliminar archivo con URL nula o vacía.");
            return;
        }
        try {
            // Intenta extraer el nombre del archivo de la URL
            String fileName = fileUrl.substring(fileUrl.lastIndexOf('/') + 1);
            if (!fileName.isBlank()) {
                deleteFileByName(fileName);
            } else {
                 logger.warn("No se pudo extraer un nombre de archivo válido de la URL: {}", fileUrl);
            }
        } catch (Exception e) {
             logger.error("Error al intentar extraer nombre de archivo de la URL '{}' para eliminarlo: {}", fileUrl, e.getMessage());
        }
    }
}