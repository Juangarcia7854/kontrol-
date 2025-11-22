package com.tusistema.sistemaventas.config;

import com.tusistema.sistemaventas.config.interceptor.GlobalDataInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.http.HttpHeaders; // <-- NUEVO IMPORT (No usado aquí, pero útil para respuestas HTTP)
import org.springframework.http.MediaType; // <-- NUEVO IMPORT (Igual que arriba)
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;
import java.util.Locale;

// @Configuration: Define que esta es una clase de configuración de Spring.
// implements WebMvcConfigurer: Nos permite personalizar la configuración por defecto de Spring MVC
// (como añadir interceptores, configurar idiomas, carpetas de recursos, etc.).
@Configuration
public class WebConfig implements WebMvcConfigurer {

    // Inyectamos el interceptor que explicamos al principio (el que carga los datos de la empresa).
    @Autowired
    private GlobalDataInterceptor globalDataInterceptor;

    // 1. Configuración de Mensajes (Traducciones/Textos)
    // Este Bean le dice a Spring dónde buscar los archivos de texto (ej: messages.properties).
    // Es útil para cambiar textos sin tocar el código Java y para tener varios idiomas.
    @Bean
    public MessageSource messageSource() {
        ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
        messageSource.setBasename("classpath:messages"); // Busca archivos que empiecen por "messages" en resources.
        messageSource.setDefaultEncoding("UTF-8"); // Soporte para tildes y ñ.
        return messageSource;
    }

    // 2. Configuración de la Región e Idioma (Locale)
    // Define cómo determinar qué idioma mostrar al usuario.
    @Bean
    public LocaleResolver localeResolver() {
        SessionLocaleResolver slr = new SessionLocaleResolver();
        // Establecemos el idioma por defecto: Español de Colombia (es_CO).
        // Esto afecta formatos de fecha (DD/MM/AAAA) y moneda ($).
        slr.setDefaultLocale(new Locale("es", "CO"));
        return slr;
    }

    // 3. Interceptor para cambiar de idioma
    // Permite cambiar el idioma pasando un parámetro en la URL.
    // Ejemplo: tuweb.com/inicio?lang=en (cambiaría a inglés).
    @Bean
    public LocaleChangeInterceptor localeChangeInterceptor() {
        LocaleChangeInterceptor lci = new LocaleChangeInterceptor();
        lci.setParamName("lang"); // El nombre del parámetro será "lang".
        return lci;
    }
    
    // ============================================

    // 4. Registro de Interceptores
    // Aquí es donde "activamos" los interceptores que hemos creado o configurado arriba.
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // Activamos el interceptor de cambio de idioma.
        registry.addInterceptor(localeChangeInterceptor());
        
        // Activamos TU interceptor de datos globales (Logo, Nombre empresa).
        registry.addInterceptor(globalDataInterceptor)
                .addPathPatterns("/**") // Se aplica a TODAS las URL de la aplicación.
                // EXCEPCIONES: No ejecutar esto para archivos estáticos (imágenes, css, js).
                // Esto mejora el rendimiento, ya que no queremos consultar la BD para mostrar una foto o un estilo.
                .excludePathPatterns("/css/**", "/js/**", "/webjars/**", "/images/**");
    }
}