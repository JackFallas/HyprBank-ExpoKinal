package com.hyprbank.online.bancavirtual.hyprbank.config;

 import jakarta.servlet.ServletException;
    import jakarta.servlet.http.HttpServletRequest;
    import jakarta.servlet.http.HttpServletResponse;
    import org.springframework.security.core.Authentication;
    import org.springframework.security.core.GrantedAuthority;
    import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
    import org.springframework.stereotype.Component;

    import java.io.IOException;

    @Component
    public class RolAccess implements AuthenticationSuccessHandler {

        @Override
        public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                            Authentication authentication) throws IOException, ServletException {

            for (GrantedAuthority auth : authentication.getAuthorities()) {
                String role = auth.getAuthority(); // Obtiene el nombre del rol (ej. "ROLE_ADMIN", "ROLE_USER")
                if (role.equals("ROLE_ADMIN")) {
                    response.sendRedirect("/dashboard/admin"); // Redirige a una URL específica para admin
                    return;
                } else if (role.equals("ROLE_USER")) {
                    response.sendRedirect("/dashboard/user"); // Redirige a una URL específica para usuario
                    return;
                }
            }

            // Si por alguna razón el usuario autenticado no tiene ninguno de los roles esperados,
            // puedes definir una redirección por defecto.
            // Por ejemplo, redirigir a un dashboard genérico o de vuelta al login con un mensaje.
            response.sendRedirect("/dashboard"); // Redirección por defecto si no hay rol específico
        }
    }
    
