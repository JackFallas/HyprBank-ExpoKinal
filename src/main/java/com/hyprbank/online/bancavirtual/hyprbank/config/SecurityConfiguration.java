package com.hyprbank.online.bancavirtual.hyprbank.config; 

// Importaciones de Servicios
import com.hyprbank.online.bancavirtual.hyprbank.service.UserService; 

// Importaciones de Spring Security
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

// Importaciones para CORS
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import java.util.Arrays;

/*
 * Clase de configuracion principal para Spring Security.
 *
 * Define las reglas de autenticacion y autorizacion, la configuracion de la pagina de login,
 * el manejo de logout, la configuracion de CORS y la codificacion de contrasenas.
 *
 * @Configuration indica que esta clase contiene definiciones de beans de Spring.
 * @EnableWebSecurity habilita la integracion de Spring Security en la aplicacion web.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfiguration {

    // Inyeccion de la implementacion de UserService (que tambien es UserDetailsService)
    private final UserService userService; // Nombre de campo actualizado

    @Autowired
    public SecurityConfiguration(UserService userService) { // Nombre de parametro actualizado
        this.userService = userService; // Nombre de campo actualizado
    }

    /**
     * Define el bean para el codificador de contrasenas.
     * Se recomienda BCryptPasswordEncoder para encriptar contrasenas de forma segura.
     *
     * @return Una instancia de {@link BCryptPasswordEncoder}.
     */
    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Configura el proveedor de autenticacion DAO (Data Access Object).
     * Este proveedor utiliza el {@link UserService} (que implementa UserDetailsService)
     * para cargar los detalles del usuario y el {@link BCryptPasswordEncoder} para verificar la contrasena.
     *
     * @return Una instancia de {@link DaoAuthenticationProvider}.
     */
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider auth = new DaoAuthenticationProvider();
        auth.setUserDetailsService(userService); // Usa tu implementacion de UserDetailsService
        auth.setPasswordEncoder(passwordEncoder()); // Usa el codificador de contrasenas definido
        return auth;
    }

    /**
     * Define la cadena de filtros de seguridad HTTP.
     * En esta configuracion se establecen las reglas de autorizacion para las diferentes URLs,
     * la configuracion del formulario de login y el manejo del logout.
     *
     * @param http El objeto HttpSecurity para configurar la seguridad.
     * @return Una instancia de {@link SecurityFilterChain}.
     * @throws Exception Si ocurre un error durante la configuracion.
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // Deshabilita CSRF. Para formularios Thymeleaf, Spring Security lo maneja por defecto.
            // Si no estas usando tokens CSRF en tus formularios, es necesario deshabilitarlo.
            // Para este proyecto con Thymeleaf, podrias intentar habilitarlo y ver si funciona.
            // Por ahora lo dejaremos deshabilitado para simplificar la depuracion.
            .csrf(csrf -> csrf.disable())
            // Habilita y configura CORS (Cross-Origin Resource Sharing)
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            // Define las reglas de autorizacion para las solicitudes HTTP
            .authorizeHttpRequests(authorize -> authorize
                // Rutas publicas: accesibles sin autenticacion
                .requestMatchers(
                    "/",           // Pagina principal de bienvenida (si existe)
                    "/login",      // Tu pagina de login personalizada
                    "/register**", // Tu pagina de registro y sus parametros (ej. /register?success) - Nombre de ruta actualizado
                    "/css/**",     // Recursos CSS estaticos
                    "/js/**",      // Recursos JavaScript estaticos
                    "/img/**",     // Recursos de imagenes estaticos
                    "/webjars/**"  // Si usas WebJars para librerias frontend
                ).permitAll() // Permite el acceso a estas rutas sin autenticacion
                // Rutas protegidas: requieren autenticacion.
                // Asumo que 'Usuario.html' es tu dashboard principal.
                .requestMatchers(
                    "/dashboard",  // Nueva ruta para el dashboard despues del login
                    "/user-dashboard.html", // Si esta es tu pagina de dashboard (nombre actualizado)
                    "/api/dashboard/**",
                    "/api/movements/**", // Nombre actualizado
                    "/api/transactions/**", // Nombre actualizado
                    "/api/reports/**" // Nombre actualizado
                ).authenticated() // Estas rutas requieren que el usuario este autenticado
                // Cualquier otra solicitud no especificada requiere autenticacion
                .anyRequest().authenticated()
            )
            // Configuracion del formulario de login
            .formLogin(form -> form
                .loginPage("/login") // Especifica la URL de tu pagina de login personalizada
                .loginProcessingUrl("/login") // URL a la que se enviaran las credenciales del formulario
                .defaultSuccessUrl("/dashboard", true) // Redirige a /dashboard despues de un login exitoso
                .failureUrl("/login?error") // Redirige a /login?error si el login falla
                .permitAll() // Permite acceso a la pagina de login y al proceso de autenticacion
            )
            // Configuracion del logout
            .logout(logout -> logout
                .invalidateHttpSession(true) // Invalida la sesion HTTP al cerrar sesion
                .clearAuthentication(true) // Limpia la autenticacion de Spring Security
                .logoutRequestMatcher(new AntPathRequestMatcher("/logout")) // URL para cerrar sesion
                .logoutSuccessUrl("/login?logout") // Redirige a /login?logout despues de cerrar sesion
                .permitAll() // Permite el acceso al proceso de logout
            )
            // Asegura que el DaoAuthenticationProvider personalizado sea usado
            .authenticationProvider(authenticationProvider());

        return http.build();
    }

    /**
     * Define la configuracion de CORS (Cross-Origin Resource Sharing).
     * Permite especificar que origenes, metodos y cabeceras estan permitidos para solicitudes de origen cruzado.
     *
     * @return Una instancia de {@link CorsConfigurationSource}.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        // Configura los origenes permitidos para las solicitudes de frontend
        configuration.setAllowedOrigins(Arrays.asList(
            "http://127.0.0.1:5500", // Comunes para Live Server
            "http://localhost:5500", // Comunes para Live Server
            "http://localhost:8080"  // Si el frontend corre en el mismo puerto del backend o si es un cliente web simple
        ));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS")); // Metodos HTTP permitidos
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type")); // Cabeceras permitidas
        configuration.setAllowCredentials(true); // Permite el envio de cookies de autenticacion
        configuration.setMaxAge(3600L); // Tiempo maximo de cache para resultados de preflight (en segundos)

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration); // Aplica esta configuracion CORS a todas las rutas
        return source;
    }
}