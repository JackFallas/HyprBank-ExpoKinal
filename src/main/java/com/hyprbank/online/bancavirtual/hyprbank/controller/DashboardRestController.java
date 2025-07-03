package com.hyprbank.online.bancavirtual.hyprbank.controller;

// Importaciones de Entidades
import com.hyprbank.online.bancavirtual.hyprbank.model.Account;
import com.hyprbank.online.bancavirtual.hyprbank.model.User;

// Importaciones de Repositorios
import com.hyprbank.online.bancavirtual.hyprbank.repository.AccountRepository;
import com.hyprbank.online.bancavirtual.hyprbank.repository.UserRepository;

// Importaciones de DTOs
import com.hyprbank.online.bancavirtual.hyprbank.dto.AccountDTO;
import com.hyprbank.online.bancavirtual.hyprbank.dto.DashboardUserDTO;

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
import java.time.LocalDateTime;
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

    private final UserRepository userRepository;
    private final AccountRepository accountRepository;

    /*
     * Constructor para la inyeccion de dependencias.
     * Spring inyectara las instancias de UserRepository y AccountRepository.
     */
    @Autowired
    public DashboardRestController(UserRepository userRepository, AccountRepository accountRepository) {
        this.userRepository = userRepository;
        this.accountRepository = accountRepository;
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
        Optional<User> userOptional = userRepository.findByEmail(userEmail);
        if (userOptional.isEmpty()) {
            // Si el usuario no se encuentra (lo cual no deberia pasar si esta autenticado), devolver 404.
            return ResponseEntity.notFound().build();
        }
        User user = userOptional.get();

        // Calcular el saldo total sumando los saldos de todas las cuentas del usuario.
        BigDecimal totalBalance = BigDecimal.ZERO;
        // Se accede a las cuentas del usuario directamente desde la entidad.
        // Es importante que la relacion @OneToMany en User este configurada para cargar las cuentas
        // (idealmente LAZY y luego accederlas, o con EAGER si es siempre necesario).
        if (user.getAccounts() != null && !user.getAccounts().isEmpty()) {
            totalBalance = user.getAccounts().stream()
                               .map(Account::getBalance)
                               .reduce(BigDecimal.ZERO, BigDecimal::add);
        }

        // Crear el DTO con la informacion resumida del dashboard.
        DashboardUserDTO userDTO = new DashboardUserDTO(
            user.getFirstName() + " " + user.getLastName(), // Asumiendo getFirstName y getLastName
            user.getEmail(),
            totalBalance
        );

        // Devolver una respuesta exitosa con el DTO.
        return ResponseEntity.ok(userDTO);
    }

    /**
     * Endpoint para obtener una lista detallada de todas las cuentas
     * asociadas al usuario autenticado.
     *
     * @return ResponseEntity con una lista de AccountDTOs si se encuentran cuentas,
     * o ResponseEntity.notFound() si el usuario no existe.
     */
    @GetMapping("/accounts") // Nombre de ruta actualizado
    public ResponseEntity<?> getUserAccounts() {
        // Obtener el objeto de autenticacion y el email del usuario.
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = authentication.getName();

        // Buscar el usuario en la base de datos.
        Optional<User> userOptional = userRepository.findByEmail(userEmail);
        if (userOptional.isEmpty()) {
            // Si el usuario no se encuentra, devolver 404.
            return ResponseEntity.notFound().build();
        }
        User user = userOptional.get();

        // Mapear la lista de entidades Account a una lista de AccountDTOs.
        // Se utiliza stream() para procesar la coleccion de manera funcional.
        List<AccountDTO> accountsDTO = user.getAccounts().stream()
                                           .map(account -> {
                                               // Crear una nueva instancia de AccountDTO y poblar sus campos
                                               // utilizando los getters de la entidad Account.
                                               AccountDTO dto = new AccountDTO();
                                               dto.setId(account.getId());
                                               dto.setAccountNumber(account.getAccountNumber());
                                               dto.setAccountType(account.getAccountType());
                                               dto.setBalance(account.getBalance());
                                               dto.setStatus(account.getStatus());
                                               dto.setCreationDate(account.getCreationDate());

                                               // Mapear tambien la informacion del usuario propietario de la cuenta al DTO.
                                               if (account.getUser() != null) {
                                                   dto.setUserId(account.getUser().getId());
                                                   dto.setUserName(account.getUser().getFirstName() + " " + account.getUser().getLastName());
                                               } else {
                                                   dto.setUserId(null);
                                                   dto.setUserName("N/A"); // Valor por defecto si el usuario es null
                                               }
                                               return dto; // Devolver el DTO mapeado.
                                           })
                                           .collect(Collectors.toList()); // Recopilar los DTOs en una lista.

        // Devolver una respuesta exitosa con la lista de DTOs.
        return ResponseEntity.ok(accountsDTO);
    }
}