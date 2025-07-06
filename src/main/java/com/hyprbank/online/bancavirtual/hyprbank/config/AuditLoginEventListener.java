package com.hyprbank.online.bancavirtual.hyprbank.config;

// Importaciones de Entidades
import com.hyprbank.online.bancavirtual.hyprbank.model.UserAccess;
import com.hyprbank.online.bancavirtual.hyprbank.model.User;

// Importaciones de Repositorios
import com.hyprbank.online.bancavirtual.hyprbank.repository.UserAccessRepository;
import com.hyprbank.online.bancavirtual.hyprbank.repository.UserRepository; // Importar UserRepository

// Importaciones de Servicios
// import com.hyprbank.online.bancavirtual.hyprbank.service.UserService; // ELIMINADO: Ya no se inyecta directamente

// Importaciones de Spring Framework
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired; // Necesario para inyección

// Importaciones de Java IO y Utilidades de Spring para HTTP Request
import java.time.LocalDateTime;
import jakarta.servlet.http.HttpServletRequest; // Usar jakarta.servlet
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import org.springframework.security.core.userdetails.UsernameNotFoundException;

/*
 * Componente de Spring que escucha eventos de autenticacion para fines de auditoria.
 *
 * Registra los intentos de login exitosos y fallidos, incluyendo detalles como la fecha/hora...
 */
@Component
public class AuditLoginEventListener {

    private final UserAccessRepository userAccessRepository;
    private final UserRepository userRepository; // Inyectar UserRepository directamente

    @Autowired
    public AuditLoginEventListener(UserAccessRepository userAccessRepository, UserRepository userRepository) {
        this.userAccessRepository = userAccessRepository;
        this.userRepository = userRepository;
    }

    /**
     * Escucha eventos de autenticación exitosa.
     * Registra un acceso exitoso en la tabla de auditoría.
     *
     * @param event El evento de autenticación exitosa.
     */
    @EventListener
    public void handleAuthenticationSuccess(AuthenticationSuccessEvent event) {
        String username = event.getAuthentication().getName(); // Generalmente el email
        String ipAddress = getClientIpAddress();

        User user = userRepository.findByEmail(username).orElse(null); // Buscar el usuario

        UserAccess userAccess = UserAccess.builder()
                .user(user) // Puede ser null si el usuario no se encuentra (ej. si se borró después del login)
                .accessDateTime(LocalDateTime.now())
                .accessType("LOGIN_SUCCESS")
                .ipAddress(ipAddress)
                .attemptedUsername(username) // Guardar el nombre de usuario intentado
                .build();
        userAccessRepository.save(userAccess);
        System.out.println("Login exitoso registrado para: " + username + " desde IP: " + ipAddress);
    }

    /**
     * Escucha eventos de autenticación fallida (credenciales inválidas).
     * Registra un intento de acceso fallido en la tabla de auditoría.
     *
     * @param event El evento de autenticación fallida.
     */
    @EventListener
    public void handleAuthenticationFailure(AuthenticationFailureBadCredentialsEvent event) {
        String username = (String) event.getAuthentication().getPrincipal(); // Obtener el nombre de usuario intentado
        String ipAddress = getClientIpAddress();

        // Intentar buscar el usuario si existe, aunque el login haya fallado
        User user = userRepository.findByEmail(username).orElse(null);

        UserAccess userAccess = UserAccess.builder()
                .user(user) // Puede ser null si el usuario no existe
                .accessDateTime(LocalDateTime.now())
                .accessType("LOGIN_FAILED")
                .ipAddress(ipAddress)
                .attemptedUsername(username) // Guardar el nombre de usuario intentado
                .build();
        userAccessRepository.save(userAccess);
        System.err.println("Login fallido registrado para: " + username + " desde IP: " + ipAddress + " - Causa: " + event.getException().getMessage());
    }

    /**
     * Obtiene la dirección IP del cliente que realiza la solicitud HTTP activa.
     */
    private String getClientIpAddress() {
        try {
            // Intenta obtener el objeto HttpServletRequest del contexto de la solicitud actual.
            HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();

            // Prioriza los encabezados que indican la IP real en entornos de proxy.
            String ipAddress = request.getHeader("X-Forwarded-For");
            if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
                ipAddress = request.getHeader("Proxy-Client-IP");
            }
            if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
                ipAddress = request.getHeader("WL-Proxy-Client-IP");
            }
            if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
                ipAddress = request.getRemoteAddr(); // IP directa si no hay proxies.
            }
            return ipAddress != null ? ipAddress : "UNKNOWN"; // Retorna la IP o "UNKNOWN" si no se encuentra.
        } catch (IllegalStateException e) {
            // Esto puede ocurrir si se intenta acceder al RequestContext fuera del ambito de una solicitud HTTP.
            // Por ejemplo, si el evento se dispara en un hilo que no esta asociado con una solicitud.
            return "N/A (No Request Context)";
        }
    }
}