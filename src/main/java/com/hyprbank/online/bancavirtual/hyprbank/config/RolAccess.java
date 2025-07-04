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
        // redirige a la página de usuario por defecto.
        // Esto previene un bucle de redireccionamiento si /dashboard no tiene una regla específica
        // o si el usuario no tiene un rol esperado.
        response.sendRedirect("/dashboard/user"); // Redirección por defecto a dashboard de usuario
    }
}