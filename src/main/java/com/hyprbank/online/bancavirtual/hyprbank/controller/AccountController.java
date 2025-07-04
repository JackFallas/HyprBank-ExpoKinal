package com.hyprbank.online.bancavirtual.hyprbank.controller;

import com.hyprbank.online.bancavirtual.hyprbank.dto.AccountDTO;
import com.hyprbank.online.bancavirtual.hyprbank.model.Account;
import com.hyprbank.online.bancavirtual.hyprbank.model.User;
import com.hyprbank.online.bancavirtual.hyprbank.repository.AccountRepository;
import com.hyprbank.online.bancavirtual.hyprbank.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/accounts") // Ruta base para las APIs de cuentas
public class AccountController {

    private final AccountRepository accountRepository;
    private final UserRepository userRepository;

    @Autowired
    public AccountController(AccountRepository accountRepository, UserRepository userRepository) {
        this.accountRepository = accountRepository;
        this.userRepository = userRepository;
    }

    /**
     * Endpoint para obtener todas las cuentas asociadas al usuario autenticado.
     * Utilizado por la vista de usuario para mostrar "Mis Cuentas".
     *
     * @param userDetails Objeto UserDetails inyectado por Spring Security.
     * @return ResponseEntity con una lista de AccountDTOs.
     */
    @GetMapping("/me") // Endpoint específico para las cuentas del usuario autenticado
    public ResponseEntity<List<AccountDTO>> getUserAccounts(@AuthenticationPrincipal UserDetails userDetails) {
        String userEmail = userDetails.getUsername();
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado para el email: " + userEmail));

        List<Account> accounts = accountRepository.findByUser(user);
        List<AccountDTO> accountDTOs = accounts.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());

        return new ResponseEntity<>(accountDTOs, HttpStatus.OK);
    }

    /**
     * Endpoint para obtener todas las cuentas del sistema.
     * Este endpoint es para uso del administrador.
     *
     * @return ResponseEntity con una lista de AccountDTOs de todas las cuentas.
     */
    @GetMapping("/all") // Endpoint para que el administrador obtenga todas las cuentas
    public ResponseEntity<List<AccountDTO>> getAllAccounts() {
        List<Account> accounts = accountRepository.findAll();
        List<AccountDTO> accountDTOs = accounts.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        return new ResponseEntity<>(accountDTOs, HttpStatus.OK);
    }

    /**
     * NUEVO ENDPOINT: Busca una cuenta por su número de cuenta.
     * Este endpoint es para uso del administrador en la funcionalidad de depósito.
     *
     * @param accountNumber El número de cuenta a buscar.
     * @return ResponseEntity con el AccountDTO de la cuenta encontrada, o 404 Not Found si no existe.
     */
    @GetMapping("/number/{accountNumber}") // Nuevo endpoint para buscar por número de cuenta
    public ResponseEntity<AccountDTO> getAccountByNumber(@PathVariable String accountNumber) {
        Optional<Account> accountOptional = accountRepository.findByAccountNumber(accountNumber);

        if (accountOptional.isPresent()) {
            Account account = accountOptional.get();
            // Mapear la entidad Account a un AccountDTO para la respuesta
            AccountDTO accountDTO = convertToDto(account);
            return ResponseEntity.ok(accountDTO);
        } else {
            // Si la cuenta no se encuentra, devuelve un 404 Not Found
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Método auxiliar para convertir una entidad Account a un AccountDTO.
     *
     * @param account La entidad Account a convertir.
     * @return El AccountDTO resultante.
     */
    private AccountDTO convertToDto(Account account) {
        AccountDTO dto = new AccountDTO();
        dto.setId(account.getId());
        dto.setAccountNumber(account.getAccountNumber());
        dto.setAccountType(account.getAccountType());
        dto.setBalance(account.getBalance());
        dto.setStatus(account.getStatus());
        dto.setCreationDate(account.getCreationDate());
        if (account.getUser() != null) {
            dto.setUserId(account.getUser().getId());
            // MODIFICADO: Asignar firstName a userName y lastName a userLastName por separado
            dto.setUserName(account.getUser().getFirstName());
            dto.setUserLastName(account.getUser().getLastName());
        }
        return dto;
    }
}