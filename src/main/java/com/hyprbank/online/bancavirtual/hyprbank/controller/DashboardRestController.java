package com.hyprbank.online.bancavirtual.hyprbank.controller;

// Importaciones de Entidades
import com.hyprbank.online.bancavirtual.hyprbank.model.Account;
import com.hyprbank.online.bancavirtual.hyprbank.model.User;

// Importaciones de Repositorios
import com.hyprbank.online.bancavirtual.hyprbank.repository.AccountRepository;
import com.hyprbank.online.bancavirtual.hyprbank.repository.UserRepository;

// Importaciones de DTOs
import com.hyprbank.online.bancavirtual.hyprbank.dto.AccountDTO;
import com.hyprbank.online.bancavirtual.hyprbank.dto.DashboardUserDTO; // Importa DashboardUserDTO

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
// import java.time.LocalDateTime; // ELIMINADO: No usada directamente aqui
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/*
 * Controlador REST para el Dashboard del Usuario.
 *
 * Proporciona endpoints para que los usuarios autenticados puedan acceder a la información
 * de su dashboard, como sus cuentas bancarias y un resumen de su saldo total.
 *
 * La anotación @RestController combina @Controller y @ResponseBody, indicando que las
 * respuestas de los métodos se serializarán directamente al cuerpo de la respuesta HTTP.
 * @RequestMapping("/api/dashboard") define la ruta base para todos los endpoints de este controlador.
 */
@RestController
@RequestMapping("/api/dashboard")
public class DashboardRestController {

    private final UserRepository userRepository;
    private final AccountRepository accountRepository; // Mantener si se usa en otros métodos, si no, eliminar

    @Autowired
    public DashboardRestController(UserRepository userRepository, AccountRepository accountRepository) {
        this.userRepository = userRepository;
        this.accountRepository = accountRepository; // Inicializar el repositorio
    }

    /**
     * Obtiene los datos del dashboard para el usuario autenticado.
     * Incluye información del usuario y sus cuentas asociadas.
     *
     * @return ResponseEntity con un {@link DashboardUserDTO} que contiene los datos del dashboard.
     */
    @GetMapping("/user")
    public ResponseEntity<DashboardUserDTO> getUserDashboard() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = authentication.getName(); // Obtiene el email del usuario autenticado

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado."));

        List<Account> accounts = accountRepository.findByUser(user);

        BigDecimal totalBalance = accounts.stream()
                .map(Account::getBalance)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<AccountDTO> accountsDTO = accounts.stream()
                                           .map(account -> {
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

        DashboardUserDTO dashboardUserDTO = DashboardUserDTO.builder()
                .id(user.getId())
                .fullName(user.getFirstName() + " " + user.getLastName())
                .email(user.getEmail())
                .totalBalance(totalBalance)
                .accounts(accountsDTO)
                .build();

        // Devolver una respuesta exitosa con la lista de DTOs.
        return ResponseEntity.ok(dashboardUserDTO);
    }
}