package com.hyprbank.online.bancavirtual.controller;

// Importaciones de Entidades
import com.hyprbank.online.bancavirtual.model.Cuenta;
import com.hyprbank.online.bancavirtual.model.Usuario;

// Importaciones de Repositorios
import com.hyprbank.online.bancavirtual.repository.CuentaRepository;
import com.hyprbank.online.bancavirtual.repository.UsuarioRepository;

// Importaciones de DTOs
import com.hyprbank.online.bancavirtual.dto.CuentaDTO;
import com.hyprbank.online.bancavirtual.dto.DashboardUserDTO;

// Importaciones de Spring Framework
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// Importaciones de Java Utilities
import java.math.BigDecimal;
import java.time.LocalDateTime; // Necesario para la fecha de creación de CuentaDTO
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/*
 * Controlador REST para el dashboard de usuario.
 *
 * Proporciona endpoints para que los usuarios autenticados puedan acceder a su informacion
 * personal basica, el saldo total de sus cuentas y una lista detallada de sus cuentas.
 *
 * La anotacion @RestController combina @Controller y @ResponseBody, indicando que las
 * respuestas de los metodos se serializaran directamente al cuerpo de la respuesta HTTP.
 * @RequestMapping("/api/dashboard") define la ruta base para todos los endpoints de este controlador.
 */
@RestController
@RequestMapping("/api/dashboard")
public class DashboardRestController {

    private final UsuarioRepository usuarioRepository;
    private final CuentaRepository cuentaRepository;

    /*
     * Constructor para la inyeccion de dependencias.
     * Spring inyectara las instancias de UsuarioRepository y CuentaRepository.
     */
    @Autowired
    public DashboardRestController(UsuarioRepository usuarioRepository, CuentaRepository cuentaRepository) {
        this.usuarioRepository = usuarioRepository;
        this.cuentaRepository = cuentaRepository;
    }

    /**
     * Endpoint para obtener la informacion basica del usuario autenticado y
     * el saldo total combinado de todas sus cuentas.
     *
     * @return ResponseEntity con un DashboardUserDTO si el usuario es encontrado,
     * o ResponseEntity.notFound() si el usuario no existe.
     */
    @GetMapping("/me")
    public ResponseEntity<?> getAuthenticatedUserDetails() {
        // Obtener el objeto de autenticacion del contexto de seguridad de Spring.
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        // El nombre de usuario (email) se obtiene de la autenticacion.
        String userEmail = authentication.getName();

        // Buscar el usuario en la base de datos por su email.
        Optional<Usuario> usuarioOptional = usuarioRepository.findByEmail(userEmail);
        if (usuarioOptional.isEmpty()) {
            // Si el usuario no se encuentra (lo cual no deberia pasar si esta autenticado), devolver 404.
            return ResponseEntity.notFound().build();
        }
        Usuario usuario = usuarioOptional.get();

        // Calcular el saldo total sumando los saldos de todas las cuentas del usuario.
        BigDecimal saldoTotal = BigDecimal.ZERO;
        // Se accede a las cuentas del usuario directamente desde la entidad.
        // Es importante que la relacion @OneToMany en Usuario este configurada para cargar las cuentas
        // (idealmente LAZY y luego accederlas, o con EAGER si es siempre necesario).
        if (usuario.getCuentas() != null && !usuario.getCuentas().isEmpty()) {
            saldoTotal = usuario.getCuentas().stream()
                               .map(Cuenta::getSaldo)
                               .reduce(BigDecimal.ZERO, BigDecimal::add);
        }

        // Crear el DTO con la informacion resumida del dashboard.
        DashboardUserDTO userDTO = DashboardUserDTO.builder()
    .nombreCompleto(usuario.getNombre() + " " + usuario.getApellido())
    .email(usuario.getEmail())
    .saldoTotal(saldoTotal) // Asignamos el saldoTotal directamente.
    .build();

        // Devolver una respuesta exitosa con el DTO.
        return ResponseEntity.ok(userDTO);
    }

    /**
     * Endpoint para obtener una lista detallada de todas las cuentas
     * asociadas al usuario autenticado.
     *
     * @return ResponseEntity con una lista de CuentaDTOs si se encuentran cuentas,
     * o ResponseEntity.notFound() si el usuario no existe.
     */
    @GetMapping("/cuentas")
    public ResponseEntity<?> getUserAccounts() {
        // Obtener el objeto de autenticacion y el email del usuario.
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = authentication.getName();

        // Buscar el usuario en la base de datos.
        Optional<Usuario> usuarioOptional = usuarioRepository.findByEmail(userEmail);
        if (usuarioOptional.isEmpty()) {
            // Si el usuario no se encuentra, devolver 404.
            return ResponseEntity.notFound().build();
        }
        Usuario usuario = usuarioOptional.get();

        // Mapear la lista de entidades Cuenta a una lista de CuentaDTOs.
        // Se utiliza stream() para procesar la coleccion de manera funcional.
        List<CuentaDTO> cuentasDTO = usuario.getCuentas().stream()
                                           .map(cuenta -> {
                                               // Crear una nueva instancia de CuentaDTO y poblar sus campos
                                               // utilizando los getters de la entidad Cuenta.
                                               CuentaDTO dto = new CuentaDTO();
                                               dto.setId(cuenta.getId());
                                               dto.setNumeroCuenta(cuenta.getNumeroCuenta());
                                               dto.setTipoCuenta(cuenta.getTipoCuenta());
                                               dto.setSaldo(cuenta.getSaldo());
                                               dto.setEstado(cuenta.getEstado());
                                               dto.setFechaCreacion(cuenta.getFechaCreacion());

                                               // Mapear tambien la informacion del usuario propietario de la cuenta al DTO.
                                               if (cuenta.getUsuario() != null) {
                                                   dto.setUsuarioId(cuenta.getUsuario().getId());
                                                   dto.setUsuarioNombreCompleto(cuenta.getUsuario().getNombre() + " " + cuenta.getUsuario().getApellido());
                                               } else {
                                                   dto.setUsuarioId(null);
                                                   dto.setUsuarioNombreCompleto("N/A"); // Valor por defecto si el usuario es null
                                               }
                                               return dto; // Devolver el DTO mapeado.
                                           })
                                           .collect(Collectors.toList()); // Recopilar los DTOs en una lista.

        // Devolver una respuesta exitosa con la lista de DTOs.
        return ResponseEntity.ok(cuentasDTO);
    }
}
