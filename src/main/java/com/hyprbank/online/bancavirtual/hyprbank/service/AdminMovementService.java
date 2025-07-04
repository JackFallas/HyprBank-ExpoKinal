package com.hyprbank.online.bancavirtual.hyprbank.service;

import com.hyprbank.online.bancavirtual.hyprbank.dto.AdminMovementDTO;
import java.util.List;

public interface AdminMovementService {
    List<AdminMovementDTO> getAllBankMovements();
}