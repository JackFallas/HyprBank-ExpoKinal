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
import org.springframework.security.core.userdetails.UserDetailsService; // Necesario para el DaoAuthenticationProvider
import com.hyprbank.online.bancavirtual.hyprbank.config.RolAccess;

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

    private final RolAccess rolAccess;

    // Ya no inyectamos UserDetailsService directamente en el constructor de SecurityConfiguration.
    // Esto es clave para romper el ciclo de dependencias.
    public SecurityConfiguration(RolAccess rolAccess) {
        this.rolAccess = rolAccess;
    }

    /**
     * Define el bean para el codificador de contraseñas (BCryptPasswordEncoder).
     *
     * @return Una instancia de {@link BCryptPasswordEncoder}.
     */
    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Configura el proveedor de autenticación DAO.
     * Este bean ahora toma UserDetailsService y BCryptPasswordEncoder como parámetros.
     * Spring se encargará de inyectar estos beans cuando cree este proveedor,
     * lo que ayuda a evitar el ciclo de dependencias.
     *
     * @param userDetailsService El servicio de detalles de usuario (UserServiceImpl).
     * @param passwordEncoder El codificador de contraseñas.
     * @return Una instancia de {@link DaoAuthenticationProvider}.
     */
    @Bean
    public DaoAuthenticationProvider authenticationProvider(UserDetailsService userDetailsService, BCryptPasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider auth = new DaoAuthenticationProvider();
        auth.setUserDetailsService(userDetailsService);
        auth.setPasswordEncoder(passwordEncoder);
        return auth;
    }

    /**
     * Configura la cadena de filtros de seguridad HTTP.
     * Define las reglas de autorización para diferentes rutas, la página de login,
     * el manejo de logout y la integración con el proveedor de autenticación.
     *
     * @param http El objeto {@link HttpSecurity} para configurar la seguridad.
     * @param authenticationProvider El proveedor de autenticación DAO, que Spring inyectará.
     * @return La cadena de filtros de seguridad configurada.
     * @throws Exception Si ocurre un error durante la configuración.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, DaoAuthenticationProvider authenticationProvider) throws Exception {
        http
            .csrf(csrf -> csrf.disable()) // Deshabilita CSRF para simplificar, considera habilitarlo en producción
            .cors(cors -> cors.configurationSource(corsConfigurationSource())) // Habilita CORS con la configuración definida
            .authorizeHttpRequests(authorize -> authorize
                .requestMatchers(
                    // Rutas permitidas para todos (sin autenticación)
                    new AntPathRequestMatcher("/css/**"),     // Permitir acceso a recursos CSS
                    new AntPathRequestMatcher("/js/**"),      // Permitir acceso a recursos JS
                    new AntPathRequestMatcher("/images/**"),  // Permitir acceso a recursos de imágenes
                    new AntPathRequestMatcher("/"),           // Permitir acceso a la página de inicio (index.html)
                    new AntPathRequestMatcher("/login**")     // Permitir acceso a la página de login (login.html)
                ).permitAll() // Permitir a todos acceder a estas rutas
                .requestMatchers(new AntPathRequestMatcher("/dashboard/admin**")).hasRole("ADMIN") // Solo ADMIN puede acceder al dashboard de admin
                .requestMatchers(new AntPathRequestMatcher("/dashboard/user**")).hasAnyRole("USER", "ADMIN") // USER y ADMIN pueden acceder al dashboard de usuario
                .anyRequest().authenticated() // Cualquier otra solicitud requiere autenticación
            )
            .formLogin(form -> form
                .loginPage("/login") // Especifica la página de login personalizada
                .successHandler(rolAccess) // Usa el handler personalizado para redirección post-login
                .permitAll() // Permitir a todos acceder al formulario de login
            )
            .logout(logout -> logout
                .invalidateHttpSession(true) // Invalida la sesión HTTP
                .clearAuthentication(true) // Limpia la autenticación
                .logoutRequestMatcher(new AntPathRequestMatcher("/logout")) // URL para cerrar sesión
                .logoutSuccessUrl("/login?logout") // Redirige aquí después de cerrar sesión
                .permitAll() // Permite a todos acceder al proceso de logout
            )
            // IMPORTANTE: Añadir el proveedor de autenticación a HttpSecurity
            .authenticationProvider(authenticationProvider);

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
