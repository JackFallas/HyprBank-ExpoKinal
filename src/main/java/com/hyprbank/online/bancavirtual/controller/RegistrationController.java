package com.hyprbank.online.bancavirtual.controller;

// Importaciones de DTOs
import com.hyprbank.online.bancavirtual.dto.RegistroRequest; // Importar el DTO de registro con el nuevo nombre

// Importaciones de Servicios
import com.hyprbank.online.bancavirtual.service.UsuarioService; // Importar la interfaz de servicio de usuario

// Importaciones de Spring Framework
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/*
 * Controlador de Spring MVC para la gestion del registro de nuevos usuarios.
 *
 * Esta clase maneja las solicitudes relacionadas con la visualizacion y el envio
 * del formulario de registro de usuarios.
 *
 * La anotacion @Controller indica que esta clase es un componente de controlador de Spring MVC,
 * que principalmente devuelve nombres de vistas.
 * @RequestMapping("/registro") define la ruta base para todos los endpoints de este controlador.
 */
@Controller
@RequestMapping("/registro")
public class RegistrationController { // Renombrado a RegistrationController

    private final UsuarioService usuarioService;

    /*
     * Constructor para la inyeccion de dependencias.
     * Spring inyectara la instancia de UsuarioService.
     */
    public RegistrationController(UsuarioService usuarioService) {
        // No es necesario llamar a super() explicitamente en este caso
        this.usuarioService = usuarioService;
    }

    /**
     * Anota un metodo para vincular un parametro o retornar un objeto
     * a un atributo del modelo, utilizado en formularios HTML.
     * En este caso, prepara un nuevo objeto {@link RegistroRequest}
     * para el formulario de registro, llamado "usuario" en la vista.
     *
     * @return Una nueva instancia de {@link RegistroRequest}.
     */
    @ModelAttribute("usuario")
    public RegistroRequest retornarNuevoUsuarioRegistroDTO() { // Renombrado el metodo para claridad
        return new RegistroRequest();
    }

    /**
     * Maneja las solicitudes GET a la ruta base "/registro".
     * Muestra el formulario de registro de usuario.
     *
     * @return El nombre de la vista "registro".
     */
    @GetMapping
    public String mostrarFormularioDeRegistro() {
        return "registro";
    }

    /**
     * Maneja las solicitudes POST enviadas al formulario de registro.
     * Recibe los datos del formulario en un {@link RegistroRequest} y guarda el nuevo usuario.
     * Despues de un registro exitoso, redirige a la misma pagina con un parametro de exito.
     *
     * @param registroDTO El {@link RegistroRequest} que contiene los datos enviados desde el formulario.
     * @return Una cadena de redireccion a la pagina de registro con un parametro "exito".
     */
    @PostMapping
    public String registrarCuentaDeUsuario(@ModelAttribute("usuario") RegistroRequest registroDTO) { // Usando el DTO con el nuevo nombre
        usuarioService.guardar(registroDTO);
        return "redirect:/registro?exito";
    }
}