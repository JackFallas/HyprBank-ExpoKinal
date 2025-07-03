package com.hyprbank.online.bancavirtual.hyprbank.config;

// Importaciones de Spring Security
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.core.userdetails.UserDetailsService;

// Importaciones para CORS
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import java.util.Arrays;

/*
 * Clase de configuración principal para Spring Security.
 *
 * Define las reglas de autenticación y autorización, la configuración de la página de login,
 * el manejo de logout, la configuración de CORS y la codificación de contraseñas.
 *
 * @Configuration indica que esta clase contiene definiciones de beans de Spring.
 * @EnableWebSecurity habilita la integración de Spring Security en la aplicación web.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfiguration {

    /**
     * Define el bean para el codificador de contraseñas.
     * Se recomienda BCryptPasswordEncoder para encriptar contraseñas de forma segura.
     *
     * @return Una instancia de {@link BCryptPasswordEncoder}.
     */
    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Configura el proveedor de autenticación DAO (Data Access Object).
     * Este proveedor utiliza el {@link UserDetailsService} (que tu UserService implementa)
     * para cargar los detalles del usuario y el {@link BCryptPasswordEncoder} para verificar la contraseña.
     *
     * @param userDetailsService Spring inyectará automáticamente tu implementación de UserDetailsService.
     * @return Una instancia de {@link DaoAuthenticationProvider}.
     */
    @Bean
    public DaoAuthenticationProvider authenticationProvider(UserDetailsService userDetailsService) {
        DaoAuthenticationProvider auth = new DaoAuthenticationProvider();
        auth.setUserDetailsService(userDetailsService);
        auth.setPasswordEncoder(passwordEncoder());
        return auth;
    }

    /**
     * Define la cadena de filtros de seguridad HTTP.
     * En esta configuración se establecen las reglas de autorización para las diferentes URLs,
     * la configuración del formulario de login y el manejo del logout.
     *
     * @param http El objeto HttpSecurity para configurar la seguridad.
     * @return Una instancia de {@link SecurityFilterChain}.
     * @throws Exception Si ocurre un error durante la configuración.
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable()) // Deshabilita CSRF (considera habilitarlo en producción con formularios)
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .authorizeHttpRequests(authorize -> authorize
                // 1. **Rutas públicas (permitAll()):**
                // Estas rutas son accesibles sin autenticación.
                // Hemos ELIMINADO "/" de aquí para que sea una ruta protegida.
                .requestMatchers(
                    "/login",                // La URL de tu página de login (GET)
                    "/login**",              // Captura /login?error, /login?logout, etc.
                    "/register",             // Tu página de registro (GET)
                    "/register**",           // Captura /register?success, etc.
                    "/css/**",               // Recursos estáticos
                    "/js/**",
                    "/img/**",
                    "/webjars/**"
                ).permitAll()                // Permite acceso sin autenticación a estas rutas

                // 2. **Rutas protegidas (authenticated()):**
                // CUALQUIER OTRA SOLICITUD requiere autenticación.
                // Esto incluye la raíz "/", lo que forzará la redirección al login.
                .anyRequest().authenticated()
            )
            // Configuración del formulario de login
            .formLogin(form -> form
                .loginPage("/login")             // Especifica la URL de tu página de login
                .loginProcessingUrl("/login")    // URL a la que se envían las credenciales del formulario
                .defaultSuccessUrl("/dashboard", true) // Redirige a /dashboard después de un login exitoso
                .failureUrl("/login?error")      // Redirige a /login?error si el login falla
                .permitAll()                     // Permite acceso a la página de login y al proceso de autenticación
            )
            // Configuración del logout
            .logout(logout -> logout
                .invalidateHttpSession(true)
                .clearAuthentication(true)
                .logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
                .logoutSuccessUrl("/login?logout") // Redirige a /login?logout después de cerrar sesión
                .permitAll()
            );

        return http.build();
    }

    /**
     * Define la configuración de CORS (Cross-Origin Resource Sharing).
     * Permite especificar qué orígenes, métodos y cabeceras están permitidos para solicitudes de origen cruzado.
     *
     * @return Una instancia de {@link CorsConfigurationSource}.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList(
            "http://127.0.0.1:5500",
            "http://localhost:5500",
            "http://localhost:8080",
            "http://localhost:8081"
        ));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
