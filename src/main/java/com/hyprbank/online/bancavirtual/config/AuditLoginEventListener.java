package com.hyprbank.online.bancavirtual.config; // Nuevo paquete para los componentes de configuracion

// Importaciones de Entidades
import com.hyprbank.online.bancavirtual.model.AccesoUsuario;
import com.hyprbank.online.bancavirtual.model.Usuario;

// Importaciones de Repositorios
import com.hyprbank.online.bancavirtual.repository.AccesoUsuarioRepository;

// Importaciones de Servicios
import com.hyprbank.online.bancavirtual.service.UsuarioService; // Importa la interfaz de servicio de usuario

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

    private final AccesoUsuarioRepository accesoUsuarioRepository; // Ajuste de nombre de Repositorio
    private final UsuarioService usuarioService; // Inyecta la interfaz UsuarioService

    /*
     * Constructor para la inyeccion de dependencias.
     * Spring automaticamente inyectara las implementaciones de los repositorios y servicios necesarios.
     */
    public AuditLoginEventListener(AccesoUsuarioRepository accesoUsuarioRepository, UsuarioService usuarioService) {
        this.accesoUsuarioRepository = accesoUsuarioRepository;
        this.usuarioService = usuarioService;
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

        // Busca el objeto Usuario real en la base de datos usando el email.
        Usuario usuario = usuarioService.buscarPorEmail(username);

        // Obtiene la direccion IP del cliente que realizo la solicitud.
        String ipAddress = getClientIpAddress();

        if (usuario != null) {
        // Usa el patrón Builder para crear la instancia de AccesoUsuario
        AccesoUsuario acceso = AccesoUsuario.builder()
            .usuario(usuario)
            .fechaHoraAcceso(LocalDateTime.now())
            .tipoAcceso("LOGIN_EXITOSO")
            .ipAddress(ipAddress)
            .build(); // No necesitas pasar el ID aquí, la DB lo generará.
        accesoUsuarioRepository.save(acceso);
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
        String username = (String) event.getAuthentication().getPrincipal();

        // Intenta buscar el usuario en la base de datos. Puede ser nulo si el email no existe.
        Usuario usuario = null;
        try {
            usuario = usuarioService.buscarPorEmail(username);
        } catch (UsernameNotFoundException e) {
            // No hacer nada, el usuario simplemente no existe, y el registro se hara sin relacionar al usuario.
        }

        // Obtiene la direccion IP del cliente.
        String ipAddress = getClientIpAddress();

        // Crea y guarda un nuevo registro de AccesoUsuario para el intento fallido.
        // Si 'usuario' es nulo, el registro de acceso quedara sin un usuario especifico,
        // lo cual es util para auditar intentos contra cuentas inexistentes.
        AccesoUsuario acceso = AccesoUsuario.builder()
        .usuario(usuario)
        .fechaHoraAcceso(LocalDateTime.now())
        .tipoAcceso("LOGIN_FALLIDO")
        .ipAddress(ipAddress)
        .build();
        accesoUsuarioRepository.save(acceso);
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
