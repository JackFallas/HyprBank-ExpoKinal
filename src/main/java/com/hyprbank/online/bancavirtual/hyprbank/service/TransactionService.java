package com.hyprbank.online.bancavirtual.hyprbank.service;

// Importaciones de Entidades
import com.hyprbank.online.bancavirtual.hyprbank.model.Movement;
import com.hyprbank.online.bancavirtual.hyprbank.model.User;

// Importaciones de DTOs
import com.hyprbank.online.bancavirtual.hyprbank.dto.MovementRequest;
import com.hyprbank.online.bancavirtual.hyprbank.dto.TransferRequest;
import com.hyprbank.online.bancavirtual.hyprbank.dto.ExternalTransferRequest;
import com.hyprbank.online.bancavirtual.hyprbank.dto.ExternalTransferResponse;

// Importaciones de Java Utilities
import java.util.List;

/*
 * Interfaz de Servicio para la gestion de transacciones bancarias.
 *
 * Define las operaciones de negocio relacionadas con depositos, retiros y transferencias.
 * Esta interfaz abstrae la logica de negocio de la capa de controladores.
 */
public interface TransactionService {

    

    /**
     * Realiza un deposito en una cuenta especifica del usuario.
     *
     * @param request El DTO con los detalles del deposito.
     * @param userId El ID del usuario que realiza el deposito.
     * @return La entidad {@link Movement} creada para el deposito.
     * @throws IllegalArgumentException Si la cuenta no es encontrada o no pertenece al usuario.
     */
    Movement performDeposit(MovementRequest request, Long userId);

    /**
     * Realiza un deposito en una cuenta especifica, sin validar la pertenencia al usuario autenticado.
     * Este metodo esta diseñado para ser usado por administradores.
     *
     * @param request El DTO con los detalles del deposito (numero de cuenta, monto, descripcion).
     * @return La entidad {@link Movement} creada para el deposito.
     * @throws IllegalArgumentException Si la cuenta no es encontrada.
     */
    Movement performAdminDeposit(MovementRequest request); // NUEVO MÉTODO

    /**
     * Realiza un retiro de una cuenta especifica del usuario.
     *
     * @param request El DTO con los detalles del retiro.
     * @param userId El ID del usuario que realiza el retiro.
     * @return La entidad {@link Movement} creada para el retiro.
     * @throws IllegalArgumentException Si la cuenta no es encontrada, no pertenece al usuario, o el saldo es insuficiente.
     */
    Movement performWithdrawal(MovementRequest request, Long userId);

    /**
     * Realiza una transferencia de dinero entre dos cuentas del mismo usuario.
     *
     * @param request El DTO con los detalles de la transferencia (cuenta origen, cuenta destino, monto).
     * @param userId El ID del usuario que realiza la transferencia.
     * @return Una lista de entidades {@link Movement} creadas para la transferencia (debito y credito).
     * @throws IllegalArgumentException Si alguna cuenta no es encontrada, no pertenece al usuario, o el saldo es insuficiente.
     */
    List<Movement> performTransfer(TransferRequest request, Long userId);

    /**
     * NUEVO MÉTODO: Realiza una transferencia de dinero entre una cuenta del usuario autenticado
     * y una cuenta de OTRO usuario dentro del mismo banco.
     *
     * @param request El DTO con los detalles de la transferencia (número de cuenta origen, número de cuenta destino, monto, descripción).
     * @param userId El ID del usuario que realiza la transferencia (dueño de la cuenta de origen).
     * @return Una lista de entidades {@link Movement} creadas (debito en origen, credito en destino).
     * @throws IllegalArgumentException Si la cuenta de origen no es encontrada o no pertenece al usuario,
     * si la cuenta de destino no es encontrada, o el saldo es insuficiente.
     */
    List<Movement> performInternalTransferToOtherUser(TransferRequest request, Long userId); // Nuevo método

    /**
     * Procesa una transferencia a una cuenta externa (a un tercero).
     *
     * @param request El DTO con los detalles de la transferencia externa.
     * @param userId El ID del usuario que realiza la transferencia.
     * @return Un {@link ExternalTransferResponse} con el resultado de la operacion.
     * @throws IllegalArgumentException Si la cuenta de origen no es encontrada, no pertenece al usuario, o el saldo es insuficiente.
     */
    ExternalTransferResponse processExternalTransfer(ExternalTransferRequest request, Long userId);
}