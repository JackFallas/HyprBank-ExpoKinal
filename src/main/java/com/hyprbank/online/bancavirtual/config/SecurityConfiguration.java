package com.hyprbank.online.bancavirtual.config; // Nuevo paquete para la configuracion de seguridad

// Importaciones de Servicios
import com.hyprbank.online.bancavirtual.service.UsuarioService; // Importa la interfaz de servicio de usuario

// Importaciones de Spring Security
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

// Importaciones para CORS
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import java.util.Arrays; // Necesario para Arrays.asList

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

    private final UsuarioService usuarioService;

    /*
     * Constructor para la inyeccion de dependencias.
     * Spring inyectara la instancia de UsuarioService.
     */
    @Autowired
    public SecurityConfiguration(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
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
     * Este proveedor utiliza el {@link UsuarioService} (que implementa UserDetailsService)
     * para cargar los detalles del usuario y el {@link BCryptPasswordEncoder} para verificar la contrasena.
     *
     * @return Una instancia de {@link DaoAuthenticationProvider}.
     */
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider auth = new DaoAuthenticationProvider();
        auth.setUserDetailsService(usuarioService); // Usa tu implementacion de UserDetailsService
        auth.setPasswordEncoder(passwordEncoder()); // Usa el codificador de contrasenas definido
        return auth;
    }

    /**
     * Configura el AuthenticationManager para utilizar el proveedor de autenticacion definido.
     *
     * @param http El objeto HttpSecurity proporcionado por Spring Security.
     * @return Una instancia de {@link AuthenticationManager}.
     * @throws Exception Si ocurre un error durante la configuracion.
     */
    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
        return http.getSharedObject(AuthenticationManagerBuilder.class)
                .authenticationProvider(authenticationProvider())
                .build();
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
            .csrf(csrf -> csrf.disable()) // Deshabilita CSRF para peticiones API, considerar habilitarlo para web
            .cors(cors -> cors.configurationSource(corsConfigurationSource())) // Habilita y configura CORS
            .authorizeHttpRequests(authorize -> authorize
                .requestMatchers(
                    "/",          // Pagina principal
                    "/login",     // Pagina de login
                    "/registro**", // Pagina de registro y sus recursos (ej. /registro?exito)
                    "/js/**",     // Recursos JavaScript estaticos
                    "/css/**",    // Recursos CSS estaticos
                    "/img/**",    // Recursos de imagenes estaticos
                    "/api/usuarios/registrar" // Endpoint de registro de usuarios (publico)
                ).permitAll() // Estas rutas son accesibles sin autenticacion
                // Rutas que requieren autenticacion.
                .requestMatchers("/Usuario.html", "/api/dashboard/**", "/api/movimientos/**", "/api/transacciones/**").authenticated()
                .anyRequest().authenticated() // CUALQUIER OTRA RUTA requiere autenticacion
            )
            .formLogin(form -> form
                .loginPage("/login") // Define la URL de la pagina de login personalizada
                .loginProcessingUrl("/login") // Define la URL a la que se envian las credenciales de login
                .defaultSuccessUrl("/Usuario.html", true) // Redirige a /Usuario.html despues de un login exitoso
                .permitAll() // Permite acceso a la pagina de login y el proceso de autenticacion
            )
            .logout(logout -> logout
                .invalidateHttpSession(true) // Invalida la sesion HTTP al cerrar sesion
                .clearAuthentication(true) // Limpia la autenticacion de Spring Security
                .logoutRequestMatcher(new AntPathRequestMatcher("/logout")) // URL para cerrar sesion
                .logoutSuccessUrl("/login?logout") // Redirige a /login?logout despues de cerrar sesion
                .permitAll() // Permite el acceso al proceso de logout
            );

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
