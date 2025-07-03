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
import jakarta.servlet.http.HttpServletRequest; // Usar jakarta.servlet
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import org.springframework.security.core.userdetails.UsernameNotFoundException;

/*
 * Componente de Spring que escucha eventos de autenticacion para fines de auditoria.
 *
 * Registra los intentos de login exitosos y fallidos, incluyendo detalles como la fecha/hora,
 * el tipo de acceso y la direccion IP del cliente.
 */
@Component // Indica a Spring que esta clase es un componente gestionado.
public class AuditLoginEventListener {

    private final UserAccessRepository userAccessRepository; // Nombre de campo actualizado
    private final UserService userService; // Nombre de campo actualizado

    /*
     * Constructor para la inyeccion de dependencias.
     * Spring automaticamente inyectara las implementaciones de los repositorios y servicios necesarios.
     */
    public AuditLoginEventListener(UserAccessRepository userAccessRepository, UserService userService) { // Nombres de parametros actualizados
        this.userAccessRepository = userAccessRepository; // Nombre de campo actualizado
        this.userService = userService; // Nombre de campo actualizado
    }

    /**
     * Escucha y maneja el evento de autenticacion exitosa ({@link AuthenticationSuccessEvent}).
     * Registra un evento de "LOGIN_EXITOSO" para el usuario que ha iniciado sesion correctamente.
     *
     * @param event El evento de autenticacion exitosa.
     */
    @EventListener
    public void handleAuthenticationSuccess(AuthenticationSuccessEvent event) {
        // Obtiene el nombre de usuario (email) del objeto UserDetails, que es el 'principal' del evento.
        String username = ((UserDetails) event.getAuthentication().getPrincipal()).getUsername();

        // Busca el objeto User real en la base de datos usando el email.
        User user = userService.findByEmail(username); // Nombre de variable y metodo actualizado

        // Obtiene la direccion IP del cliente que realizo la solicitud.
        String ipAddress = getClientIpAddress();

        if (user != null) {
            // Usa el patrón Builder para crear la instancia de UserAccess
            UserAccess userAccess = UserAccess.builder() // Nombre de clase y variable actualizado
                .user(user) // Nombre de campo actualizado
                .accessDateTime(LocalDateTime.now()) // Nombre de campo actualizado
                .accessType("LOGIN_SUCCESS") // Valor actualizado
                .ipAddress(ipAddress)
                .attemptedUsername(username) // <--- Añadido: Registra el nombre de usuario que inició sesión
                .build();
            userAccessRepository.save(userAccess); // Nombre de repositorio y variable actualizado
        }
    }

    /**
     * Escucha y maneja el evento de autenticacion fallida por credenciales incorrectas ({@link AuthenticationFailureBadCredentialsEvent}).
     * Registra un evento de "LOGIN_FALLIDO" para el intento de inicio de sesion.
     *
     * @param event El evento de autenticacion fallida.
     */
    @EventListener
    public void handleAuthenticationFailure(AuthenticationFailureBadCredentialsEvent event) {
        // Obtiene el nombre de usuario (email) que se intento usar para el login fallido.
        // El 'principal' en un evento de fallo puede ser un String si no se autentico ningun UserDetails.
        String usernameAttempted = (String) event.getAuthentication().getPrincipal();

        // Intenta buscar el usuario en la base de datos. Puede ser nulo si el email no existe.
        User user = null; // Nombre de variable actualizado
        try {
            user = userService.findByEmail(usernameAttempted); // Nombre de metodo actualizado
        } catch (UsernameNotFoundException e) {
            // No hacer nada, el usuario simplemente no existe, y el registro se hara sin relacionar al usuario.
            // Esto es correcto si 'user_id' es nullable en la DB.
        }

        // Obtiene la direccion IP del cliente.
        String ipAddress = getClientIpAddress();

        // Crea y guarda un nuevo registro de UserAccess para el intento fallido.
        // Si 'user' es nulo, el registro de acceso quedara sin un usuario especifico,
        // lo cual es util para auditar intentos contra cuentas inexistentes.
        UserAccess userAccess = UserAccess.builder() // Nombre de clase y variable actualizado
            .user(user) // Puede ser null si el usuario no existe (gracias a nullable=true en la entidad)
            .accessDateTime(LocalDateTime.now()) // Nombre de campo actualizado
            .accessType("LOGIN_FAILED") // Valor actualizado
            .ipAddress(ipAddress)
            .attemptedUsername(usernameAttempted) // <--- Añadido: Registra el nombre de usuario que se intentó usar
            .build();
        userAccessRepository.save(userAccess); // Nombre de repositorio y variable actualizado
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
