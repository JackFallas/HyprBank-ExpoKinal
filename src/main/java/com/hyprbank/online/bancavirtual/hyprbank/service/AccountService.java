package com.hyprbank.online.bancavirtual.hyprbank.service;
import com.hyprbank.online.bancavirtual.hyprbank.dto.AccountDTO;

public interface AccountService {
    AccountDTO findByAccountNumber(String accountNumber);
}