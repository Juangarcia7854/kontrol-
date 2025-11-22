package com.tusistema.sistemaventas.service;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {

    /**
     * Guarda un archivo subido.
     * @param file El archivo MultipartFile subido.
     * @return El nombre único asignado al archivo guardado.
     */
    String storeFile(MultipartFile file);

    /**
     * Obtiene la URL completa para acceder a un archivo guardado.
     * @param fileName El nombre único del archivo.
     * @return La URL completa (String).
     */
    String getFileUrl(String fileName);

    /**
     * Carga un archivo como un recurso (para servirlo directamente).
     * @param fileName El nombre único del archivo.
     * @return El Resource del archivo.
     */
    Resource loadFileAsResource(String fileName);

    /**
     * Elimina un archivo basado en su nombre único.
     * @param fileName El nombre único del archivo a eliminar.
     */
    void deleteFileByName(String fileName);

     /**
     * Elimina un archivo basado en su URL completa.
     * Intenta extraer el nombre del archivo de la URL.
     * @param fileUrl La URL completa del archivo a eliminar.
     */
    void deleteFileByUrl(String fileUrl);
}