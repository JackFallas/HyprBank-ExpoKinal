package com.hyprbank.online.bancavirtual.hyprbank.service;
import com.hyprbank.online.bancavirtual.hyprbank.dto.AdminMovementDTO;
import com.hyprbank.online.bancavirtual.hyprbank.model.Account;
import com.hyprbank.online.bancavirtual.hyprbank.model.Movement;
import com.hyprbank.online.bancavirtual.hyprbank.repository.AccountRepository;
import com.hyprbank.online.bancavirtual.hyprbank.repository.MovementRepository;
import com.hyprbank.online.bancavirtual.hyprbank.repository.UserRepository;
import com.hyprbank.online.bancavirtual.hyprbank.service.AdminMovementService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AdminMovementServiceImpl implements AdminMovementService {

    private final MovementRepository movementRepository;
    private final AccountRepository accountRepository; // Necesario para obtener la cuenta y el usuario
    private final UserRepository userRepository; // Opcional si ya accedes al usuario desde la cuenta

    public AdminMovementServiceImpl(MovementRepository movementRepository, AccountRepository accountRepository, UserRepository userRepository) {
        this.movementRepository = movementRepository;
        this.accountRepository = accountRepository;
        this.userRepository = userRepository;
    }

    @Override
    public List<AdminMovementDTO> getAllBankMovements() {
        List<Movement> allMovements = movementRepository.findAll(); // Obtiene todos los movimientos

        return allMovements.stream().map(movement -> {
            String userName = "Desconocido";
            String accountNumber = "N/A";

            // Asumiendo que tu entidad Movement tiene una relación con Account
            if (movement.getAccount() != null) {
                Account account = movement.getAccount(); // Obtener la cuenta directamente si ya está cargada
                // Si la cuenta no está cargada completamente (lazy loading), podrías necesitar buscarla:
                // Account account = accountRepository.findById(movement.getAccountId()).orElse(null);

                if (account != null) {
                    accountNumber = account.getAccountNumber();
                    // Asumiendo que tu entidad Account tiene una relación con User
                    if (account.getUser() != null) {
                        userName = account.getUser().getFirstName() + " " + account.getUser().getLastName();
                    }
                }
            }

            return new AdminMovementDTO(
                movement.getId(),
                movement.getDate(), // Asegúrate que el tipo de dato coincida (LocalDate o LocalDateTime)
                movement.getDescription(),
                movement.getType().name(), // Asumiendo que 'type' es un Enum (ej. MovementType.INCOME)
                movement.getAmount(),
                accountNumber,
                userName
            );
        }).collect(Collectors.toList());
    }
}