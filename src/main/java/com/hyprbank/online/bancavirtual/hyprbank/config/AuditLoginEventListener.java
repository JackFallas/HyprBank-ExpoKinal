package com.hyprbank.online.bancavirtual.hyprbank.config;

// Importaciones de Entidades
import com.hyprbank.online.bancavirtual.hyprbank.model.UserAccess;
import com.hyprbank.online.bancavirtual.hyprbank.model.User;

// Importaciones de Repositorios
import com.hyprbank.online.bancavirtual.hyprbank.repository.UserAccessRepository;

// Importaciones de Servicios
import com.hyprbank.online.bancavirtual.hyprbank.service.UserService;

// Importaciones de Spring Framework
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

// Importaciones de Java IO y Utilidades de Spring para HTTP Request
import java.time.LocalDateTime;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import org.springframework.security.core.userdetails.UsernameNotFoundException;

/*
 * Componente de Spring que escucha eventos de autenticacion para fines de auditoria.
 *
 * Registra los intentos de login exitosos y fallidos, incluyendo detalles como la fecha/hora,
 * el tipo de acceso y la direccion IP del cliente.
 */
@Component
public class AuditLoginEventListener {

    private final UserAccessRepository userAccessRepository;
    private final UserService userService;

    /*
     * Constructor para la inyeccion de dependencias.
     * Spring automaticamente inyectara las implementaciones de los repositorios y servicios necesarios.
     */
    public AuditLoginEventListener(UserAccessRepository userAccessRepository, UserService userService) {
        this.userAccessRepository = userAccessRepository;
        this.userService = userService;
    }

    /**
     * Escucha y maneja el evento de autenticacion exitosa ({@link AuthenticationSuccessEvent}).
     * Registra un evento de "LOGIN_SUCCESS" para el usuario que ha iniciado sesion correctamente.
     *
     * @param event El evento de autenticacion exitosa.
     */
    @EventListener
    public void handleAuthenticationSuccess(AuthenticationSuccessEvent event) {
        String username = ((UserDetails) event.getAuthentication().getPrincipal()).getUsername();

        User user = userService.findByEmail(username);

        String ipAddress = getClientIpAddress();

        if (user != null) { // Asegurarse de que el usuario no sea nulo antes de registrar el acceso
            UserAccess userAccess = UserAccess.builder()
                .user(user)
                .accessDateTime(LocalDateTime.now())
                .accessType("LOGIN_SUCCESS")
                .ipAddress(ipAddress)
                .build();
            userAccessRepository.save(userAccess);
        }
    }

    /**
     * Escucha y maneja el evento de autenticacion fallida por credenciales incorrectas ({@link AuthenticationFailureBadCredentialsEvent}).
     * Registra un evento de "LOGIN_FAILED" para el intento de inicio de sesion,
     * solo si el usuario existe en la base de datos.
     *
     * @param event El evento de autenticacion fallida.
     */
    @EventListener
    public void handleAuthenticationFailure(AuthenticationFailureBadCredentialsEvent event) {
        String username = (String) event.getAuthentication().getPrincipal();

        User user = null;
        try {
            user = userService.findByEmail(username);
        } catch (UsernameNotFoundException e) {
            // Si el usuario no existe, 'user' permanece null.
            // No registramos el acceso fallido si el usuario no existe para evitar el error 'user_id cannot be null'.
            return; // <--- ¡Añadido! Salir del metodo si el usuario no existe.
        }

        // Si llegamos aqui, significa que el usuario fue encontrado, pero las credenciales eran incorrectas.
        String ipAddress = getClientIpAddress();

        UserAccess userAccess = UserAccess.builder()
            .user(user) // Ahora 'user' nunca sera null aqui
            .accessDateTime(LocalDateTime.now())
            .accessType("LOGIN_FAILED")
            .ipAddress(ipAddress)
            .build();
        userAccessRepository.save(userAccess);
    }

    /**
     * Metodo auxiliar para obtener la direccion IP del cliente desde la solicitud HTTP actual.
     * Considera encabezados comunes como 'X-Forwarded-For' para casos donde la aplicacion
     * esta detras de un proxy o balanceador de carga.
     *
     * @return La direccion IP del cliente o "N/A (No Request Context)" si no hay una solicitud HTTP activa.
     */
    private String getClientIpAddress() {
        try {
            HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();

            String ipAddress = request.getHeader("X-Forwarded-For");
            if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
                ipAddress = request.getHeader("Proxy-Client-IP");
            }
            if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
                ipAddress = request.getHeader("WL-Proxy-Client-IP");
            }
            if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
                ipAddress = request.getRemoteAddr();
            }
            return ipAddress != null ? ipAddress : "UNKNOWN";
        } catch (IllegalStateException e) {
            return "N/A (No Request Context)";
        }
    }
}