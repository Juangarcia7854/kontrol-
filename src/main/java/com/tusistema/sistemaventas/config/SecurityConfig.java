package com.tusistema.sistemaventas.config;

import com.tusistema.sistemaventas.service.UserDetailsServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository; // <-- IMPORT AÑADIDO

// @Configuration: Indica que esta clase contiene definiciones de "Beans" (componentes de configuración).
// @EnableWebSecurity: Activa la seguridad web de Spring Security en el proyecto.
// @EnableMethodSecurity: Permite usar anotaciones como @PreAuthorize en tus servicios o controladores
// para proteger métodos específicos (ej: solo ADMIN puede borrar usuarios).
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    // 1. Definimos el encriptador de contraseñas.
    // BCrypt es el estándar actual. Transforma "123456" en algo ilegible como "$2a$10$..."
    // Esto asegura que ni siquiera el administrador de la BD pueda leer las claves.
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // 2. Configuración del proveedor de autenticación.
    // Conecta tu servicio de usuarios (UserDetailsServiceImpl) con el encriptador de contraseñas.
    // Es el encargado de verificar si el usuario existe y si la contraseña coincide.
    @Bean
    public DaoAuthenticationProvider authenticationProvider(UserDetailsServiceImpl userDetailsService, PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder);
        return authProvider;
    }

    // 3. AuthenticationManager: Es el componente principal que gestiona el proceso de login.
    // Lo exponemos como Bean por si necesitamos usarlo manualmente en algún controlador personalizado.
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    // ============================================
    // == BEAN NUEVO AÑADIDO ==
    // ============================================
    /**
     * Define explícitamente el repositorio que guarda la sesión de seguridad (quién está logueado).
     * Normalmente Spring lo hace solo, pero definirlo explícitamente ayuda a resolver problemas
     * donde el usuario se "desloguea" solo o la sesión no persiste entre peticiones.
     */
    @Bean
    public HttpSessionSecurityContextRepository securityContextRepository() {
        return new HttpSessionSecurityContextRepository();
    }
    // ============================================

    // 4. SecurityFilterChain: Aquí se definen las reglas de acceso HTTP (URLs).
    // Es el "portero" de la discoteca que decide quién pasa y a dónde.
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // Desactivamos CSRF (Cross-Site Request Forgery).
            // A veces se desactiva para facilitar el desarrollo o APIs, aunque en producción web se recomienda activarlo.
            .csrf(AbstractHttpConfigurer::disable) 

            // Configuración de autorización de rutas
            .authorizeHttpRequests(authorize -> authorize
                // A) Rutas PÚBLICAS (permitimos entrar a cualquiera sin login)
                // Incluye archivos estáticos (css, js, imágenes), página de login, registro y errores.
                .requestMatchers(
                    "/",
                    "/css/**",
                    "/js/**",
                    "/images/**",
                    "/webjars/**",
                    "/login",
                    "/register",
                    "/demo/start", // Ruta Demo pública
                    "/error"
                ).permitAll()

                // B) Rutas PROTEGIDAS (requieren estar logueado)
                // Todas las rutas funcionales del sistema (ventas, productos, admin) requieren autenticación.
                .requestMatchers(
                    "/dashboard", "/ventas/**", "/productos/**", "/clientes/**",
                    "/devoluciones/**", "/proveedores/**", "/reportes/**", "/admin/usuarios/**",
                    "/configuraciones/**", "/inventario/**", "/prediccion/**",
                    "/cuentas-por-cobrar/**", "/cuentas-por-pagar/**", "/compras/**"
                ).authenticated()

                // C) Cualquier otra ruta que se nos haya olvidado listar arriba TAMBIÉN requiere login.
                .anyRequest().authenticated()
            )
            
            // 5. Configuración del Formulario de Login
            .formLogin(form -> form
                .loginPage("/login") // Usamos nuestra propia página HTML en lugar de la fea por defecto de Spring.
                .loginProcessingUrl("/login") // URL donde se envía el formulario (POST).
                .defaultSuccessUrl("/dashboard", true) // Si el login es correcto, ir al Dashboard.
                .failureUrl("/login?error=true") // Si falla, volver al login con un parámetro de error.
                .permitAll() // Permitir que cualquiera vea la página de login.
            )
            
            // 6. Configuración del Logout (Cerrar sesión)
            .logout(logout -> logout
                .logoutUrl("/logout") // URL para cerrar sesión.
                .logoutSuccessUrl("/login?logout") // A dónde ir tras salir.
                .permitAll()
            );

        return http.build();
    }
}