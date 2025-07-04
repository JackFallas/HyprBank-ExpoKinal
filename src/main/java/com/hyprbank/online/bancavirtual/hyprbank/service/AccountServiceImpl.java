package com.hyprbank.online.bancavirtual.hyprbank.service;

import com.hyprbank.online.bancavirtual.hyprbank.dto.AccountDTO;
import com.hyprbank.online.bancavirtual.hyprbank.model.Account; // Asume que tienes esta entidad
import com.hyprbank.online.bancavirtual.hyprbank.repository.AccountRepository; // Asume que tienes este repositorio
import com.hyprbank.online.bancavirtual.hyprbank.service.AccountService;
import org.springframework.stereotype.Service;

@Service
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;

    public AccountServiceImpl(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Override
    public AccountDTO findByAccountNumber(String accountNumber) {
        return accountRepository.findByAccountNumber(accountNumber)
                .map(account -> {
                    String userName = null;
                    String userLastName = null;
                    Long userId = null; // Inicializar userId

                    // Asegúrate de que la entidad User esté cargada si la relación es LAZY
                    if (account.getUser() != null) {
                        userId = account.getUser().getId(); // Obtener el ID del usuario
                        userName = account.getUser().getFirstName();
                        userLastName = account.getUser().getLastName();
                    }

                    return new AccountDTO(
                        account.getId(),
                        account.getAccountNumber(),
                        account.getAccountType(), // Asume que AccountType es un Enum
                        account.getBalance(),
                        account.getStatus(), // Asume que Status es un Enum
                        account.getCreationDate(), // Usar el campo creationDate de la entidad
                        userId, // Pasar el userId
                        userName,
                        userLastName
                    );
                })
                .orElse(null); // Retorna null si no se encuentra la cuenta
    }
}