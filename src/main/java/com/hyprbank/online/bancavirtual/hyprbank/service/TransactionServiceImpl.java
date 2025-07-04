package com.hyprbank.online.bancavirtual.hyprbank.service;

// Importaciones de Entidades
import com.hyprbank.online.bancavirtual.hyprbank.model.Account;
import com.hyprbank.online.bancavirtual.hyprbank.model.Movement;
import com.hyprbank.online.bancavirtual.hyprbank.model.Movement.MovementType;
import com.hyprbank.online.bancavirtual.hyprbank.model.User;

// Importaciones de Repositorios
import com.hyprbank.online.bancavirtual.hyprbank.repository.AccountRepository;
import com.hyprbank.online.bancavirtual.hyprbank.repository.MovementRepository;
import com.hyprbank.online.bancavirtual.hyprbank.repository.UserRepository;

// Importaciones de DTOs
import com.hyprbank.online.bancavirtual.hyprbank.dto.MovementDTO;
import com.hyprbank.online.bancavirtual.hyprbank.dto.MovementRequest;
import com.hyprbank.online.bancavirtual.hyprbank.dto.TransferRequest;
import com.hyprbank.online.bancavirtual.hyprbank.dto.ExternalTransferRequest;
import com.hyprbank.online.bancavirtual.hyprbank.dto.ExternalTransferResponse;

// Importaciones de Spring Framework
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // Para gestion de transacciones
import org.springframework.data.domain.PageRequest; // AGREGADO: Importar PageRequest
import org.springframework.data.domain.Pageable; // AGREGADO: Importar Pageable

// Importaciones de Java Utilities
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

// Importaciones de Logging
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * Implementacion de la interfaz {@link TransactionService}.
 *
 * Esta clase provee la logica de negocio para la realizacion de transacciones bancarias
 * como depositos, retiros y transferencias.
 *
 * La anotacion @Service indica que esta clase es un componente de servicio de Spring.
 * La anotacion @Transactional asegura que los metodos se ejecuten dentro de una transaccion de base de datos.
 */
@Service
public class TransactionServiceImpl implements TransactionService {

    private static final Logger logger = LoggerFactory.getLogger(TransactionServiceImpl.class);

    private final AccountRepository accountRepository;
    private final MovementRepository movementRepository;
    private final UserRepository userRepository; // Inyectar UserRepository

    /*
     * Constructor para la inyeccion de dependencias.
     * Spring inyectara las instancias de AccountRepository y MovementRepository.
     */
    @Autowired
    public TransactionServiceImpl(AccountRepository accountRepository, MovementRepository movementRepository, UserRepository userRepository) {
        this.accountRepository = accountRepository;
        this.movementRepository = movementRepository;
        this.userRepository = userRepository;
    }

    /**
     * Metodo auxiliar para transformar una entidad {@link Movement} a su correspondiente {@link MovementDTO}.
     * Esto es fundamental para evitar la exposicion directa de las entidades de persistencia en la API
     * y prevenir problemas de serializacion (ej., referencias circulares).
     *
     * @param movement La entidad Movement a ser convertida.
     * @return Un {@link MovementDTO} con los datos esenciales del movimiento, o {@code null} si la entidad de entrada es nula.
     */
    private MovementDTO mapMovementToDTO(Movement movement) {
        if (movement == null) {
            return null;
        }
        MovementDTO dto = new MovementDTO();
        dto.setId(movement.getId());
        if (movement.getAccount() != null) {
            dto.setAccountId(movement.getAccount().getId());
            dto.setAccountNumber(movement.getAccount().getAccountNumber());
            dto.setBalance(movement.getAccount().getBalance()); // AHORA MovementDTO tiene 'balance'
        } else {
            dto.setAccountId(null);
            dto.setAccountNumber("Unknown Account");
            dto.setBalance(null); // Si la cuenta es desconocida, el balance también lo es
        }
        dto.setDate(movement.getDate());
        dto.setDescription(movement.getDescription());
        dto.setType(movement.getType());
        dto.setAmount(movement.getAmount());
        return dto;
    }

    /**
     * Realiza un deposito en una cuenta.
     * Este método valida que la cuenta pertenezca al usuario autenticado.
     *
     * @param request El DTO de solicitud de movimiento.
     * @param userId El ID del usuario que realiza el deposito.
     * @return La entidad Movement creada.
     * @throws IllegalArgumentException Si la cuenta no existe o no pertenece al usuario.
     */
    @Override
    @Transactional // Asegura que la operacion sea atomica
    public Movement performDeposit(MovementRequest request, Long userId) {
        // Buscar el usuario para asegurar que existe
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado."));

        // Buscar la cuenta por numero y asegurarse de que pertenece al usuario
        Account account = accountRepository.findByAccountNumberAndUser(request.getAccountNumber(), user)
                .orElseThrow(() -> new IllegalArgumentException("Cuenta no encontrada o no pertenece al usuario."));

        // Actualizar el saldo de la cuenta
        account.setBalance(account.getBalance().add(request.getAmount()));
        accountRepository.save(account);
        logger.debug("Saldo de cuenta {} actualizado a {}", account.getAccountNumber(), account.getBalance());

        // Registrar el movimiento
        Movement movement = new Movement();
        movement.setAccount(account);
        movement.setDate(LocalDate.now());
        movement.setDescription(request.getDescription() != null && !request.getDescription().isEmpty() ? request.getDescription() : "Deposito en cuenta");
        movement.setType(MovementType.INCOME);
        movement.setAmount(request.getAmount());
        movement = movementRepository.save(movement);
        logger.info("Deposito de {} en cuenta {} registrado. Nuevo saldo: {}", request.getAmount(), account.getAccountNumber(), account.getBalance());
        return movement;
    }

    /**
     * NUEVO MÉTODO: Realiza un deposito en una cuenta especifica.
     * Este método NO valida la pertenencia de la cuenta al usuario autenticado,
     * está diseñado para ser usado por administradores.
     *
     * @param request El DTO de solicitud de movimiento (con accountNumber, amount, description).
     * @return La entidad Movement creada.
     * @throws IllegalArgumentException Si la cuenta no existe.
     */
    @Override
    @Transactional // Asegura que la operacion sea atomica
    public Movement performAdminDeposit(MovementRequest request) {
        // Buscar la cuenta solo por número (sin validar pertenencia a un usuario específico)
        Account account = accountRepository.findByAccountNumber(request.getAccountNumber())
                .orElseThrow(() -> new IllegalArgumentException("Cuenta no encontrada con número: " + request.getAccountNumber()));

        // Actualizar el saldo de la cuenta
        account.setBalance(account.getBalance().add(request.getAmount()));
        accountRepository.save(account);
        logger.debug("ADMIN: Saldo de cuenta {} actualizado a {}", account.getAccountNumber(), account.getBalance());

        // Registrar el movimiento
        Movement movement = new Movement();
        movement.setAccount(account);
        movement.setDate(LocalDate.now());
        movement.setDescription(request.getDescription() != null && !request.getDescription().isEmpty() ? request.getDescription() : "Deposito por Administrador");
        movement.setType(MovementType.INCOME);
        movement.setAmount(request.getAmount());
        movement = movementRepository.save(movement);
        logger.info("ADMIN: Deposito de {} en cuenta {} registrado. Nuevo saldo: {}", request.getAmount(), account.getAccountNumber(), account.getBalance());
        return movement;
    }


    /**
     * Realiza un retiro de una cuenta.
     *
     * @param request El DTO de solicitud de movimiento.
     * @param userId El ID del usuario que realiza el retiro.
     * @return La entidad Movement creada.
     * @throws IllegalArgumentException Si la cuenta no existe, no pertenece al usuario o el saldo es insuficiente.
     */
    @Override
    @Transactional
    public Movement performWithdrawal(MovementRequest request, Long userId) {
        Account account = accountRepository.findByAccountNumberAndUser(request.getAccountNumber(), userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado.")))
                .orElseThrow(() -> new IllegalArgumentException("Cuenta no encontrada o no pertenece al usuario."));

        // Validar saldo suficiente
        if (account.getBalance().compareTo(request.getAmount()) < 0) {
            throw new IllegalArgumentException("Saldo insuficiente en la cuenta " + request.getAccountNumber());
        }

        // Actualizar el saldo
        account.setBalance(account.getBalance().subtract(request.getAmount()));
        accountRepository.save(account);
        logger.debug("Saldo de cuenta {} actualizado a {}", account.getAccountNumber(), account.getBalance());


        // Registrar el movimiento
        Movement movement = new Movement();
        movement.setAccount(account);
        movement.setDate(LocalDate.now());
        movement.setDescription(request.getDescription() != null && !request.getDescription().isEmpty() ? request.getDescription() : "Retiro de cuenta");
        movement.setType(MovementType.EXPENSE);
        movement.setAmount(request.getAmount());
        movement = movementRepository.save(movement);
        logger.info("Retiro de {} de cuenta {} registrado. Nuevo saldo: {}", request.getAmount(), account.getAccountNumber(), account.getBalance());
        return movement;
    }

    /**
     * Realiza una transferencia de dinero entre dos cuentas del MISMO usuario.
     *
     * @param request El DTO de solicitud de transferencia.
     * @param userId El ID del usuario que realiza la transferencia.
     * @return Una lista de entidades Movement creadas (debito y credito).
     * @throws IllegalArgumentException Si alguna cuenta no existe, no pertenece al usuario o el saldo es insuficiente.
     */
    @Override
    @Transactional
    public List<Movement> performTransfer(TransferRequest request, Long userId) {
        if (request.getOriginAccountNumber().equals(request.getDestinationAccountNumber())) {
            throw new IllegalArgumentException("Las cuentas de origen y destino no pueden ser la misma para una transferencia interna.");
        }

        User currentUser = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado."));

        Account originAccount = accountRepository.findByAccountNumberAndUser(request.getOriginAccountNumber(), currentUser)
                .orElseThrow(() -> new IllegalArgumentException("Cuenta de origen no encontrada o no pertenece al usuario."));

        // Modificado: Asegurarse de que la cuenta de destino también pertenece al mismo usuario
        Account destinationAccount = accountRepository.findByAccountNumberAndUser(request.getDestinationAccountNumber(), currentUser)
                .orElseThrow(() -> new IllegalArgumentException("Cuenta de destino no encontrada o no pertenece al usuario."));

        // Validar saldo suficiente en la cuenta de origen
        if (originAccount.getBalance().compareTo(request.getAmount()) < 0) {
            throw new IllegalArgumentException("Saldo insuficiente en la cuenta de origen " + request.getOriginAccountNumber());
        }

        // Actualizar saldos
        originAccount.setBalance(originAccount.getBalance().subtract(request.getAmount()));
        destinationAccount.setBalance(destinationAccount.getBalance().add(request.getAmount()));

        accountRepository.save(originAccount);
        accountRepository.save(destinationAccount);
        logger.debug("Saldos de cuentas actualizados. Origen: {}, Destino: {}", originAccount.getAccountNumber(), destinationAccount.getAccountNumber());


        List<Movement> movements = new ArrayList<>();

        // Registrar movimiento de egreso en cuenta origen
        Movement expenseMovement = new Movement();
        expenseMovement.setAccount(originAccount);
        expenseMovement.setDate(LocalDate.now());
        // Descripción más clara para transferencias internas del mismo usuario
        expenseMovement.setDescription(
            request.getDescription() != null && !request.getDescription().isEmpty() ?
            request.getDescription() :
            "Transferencia interna a " + destinationAccount.getAccountNumber()
        );
        expenseMovement.setType(MovementType.EXPENSE);
        expenseMovement.setAmount(request.getAmount());
        movements.add(movementRepository.save(expenseMovement));
        logger.debug("Movimiento de egreso registrado: {}", expenseMovement.getId());


        // Registrar movimiento de ingreso en cuenta destino
        Movement incomeMovement = new Movement();
        incomeMovement.setAccount(destinationAccount);
        incomeMovement.setDate(LocalDate.now());
        // Descripción más clara para transferencias internas del mismo usuario
        incomeMovement.setDescription(
            request.getDescription() != null && !request.getDescription().isEmpty() ?
            request.getDescription() :
            "Transferencia interna de " + originAccount.getAccountNumber()
        );
        incomeMovement.setType(MovementType.INCOME);
        incomeMovement.setAmount(request.getAmount());
        movements.add(movementRepository.save(incomeMovement));
        logger.info("Transferencia interna de {} de cuenta {} a cuenta {} registrada. Nuevo saldo origen: {}, nuevo saldo destino: {}",
                request.getAmount(), originAccount.getAccountNumber(), destinationAccount.getAccountNumber(), originAccount.getBalance(), destinationAccount.getBalance());

        return movements;
    }

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
    @Override
    @Transactional
    public List<Movement> performInternalTransferToOtherUser(TransferRequest request, Long userId) {
        if (request.getOriginAccountNumber().equals(request.getDestinationAccountNumber())) {
            throw new IllegalArgumentException("Las cuentas de origen y destino no pueden ser la misma para una transferencia.");
        }

        // 1. Validar Cuenta de Origen (debe pertenecer al usuario autenticado)
        User currentUser = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario de origen no encontrado."));

        Account originAccount = accountRepository.findByAccountNumberAndUser(request.getOriginAccountNumber(), currentUser)
                .orElseThrow(() -> new IllegalArgumentException("Cuenta de origen no encontrada o no pertenece al usuario autenticado."));

        // 2. Validar Cuenta de Destino (solo debe existir en el sistema, no necesariamente pertenecer al usuario autenticado)
        Account destinationAccount = accountRepository.findByAccountNumber(request.getDestinationAccountNumber())
                .orElseThrow(() -> new IllegalArgumentException("Cuenta de destino no encontrada en el sistema."));

        // 3. Validar saldo suficiente en la cuenta de origen
        if (originAccount.getBalance().compareTo(request.getAmount()) < 0) {
            throw new IllegalArgumentException("Saldo insuficiente en la cuenta de origen " + request.getOriginAccountNumber());
        }

        // 4. Actualizar saldos
        originAccount.setBalance(originAccount.getBalance().subtract(request.getAmount()));
        destinationAccount.setBalance(destinationAccount.getBalance().add(request.getAmount()));

        accountRepository.save(originAccount);
        accountRepository.save(destinationAccount);
        logger.debug("Transferencia entre usuarios: Saldos actualizados. Origen: {}, Destino: {}", originAccount.getAccountNumber(), destinationAccount.getAccountNumber());

        List<Movement> movements = new ArrayList<>();

        // 5. Registrar movimiento de egreso en cuenta origen (para el usuario que envía)
        Movement expenseMovement = new Movement();
        expenseMovement.setAccount(originAccount);
        expenseMovement.setDate(LocalDate.now());
        expenseMovement.setDescription(
            request.getDescription() != null && !request.getDescription().isEmpty() ?
            request.getDescription() :
            "Transferencia enviada a " + destinationAccount.getAccountNumber() + " (" + destinationAccount.getUser().getFirstName() + " " + destinationAccount.getUser().getLastName() + ")"
        );
        expenseMovement.setType(MovementType.EXPENSE);
        expenseMovement.setAmount(request.getAmount());
        movements.add(movementRepository.save(expenseMovement));
        logger.info("Transferencia enviada de {} a {} ({}). Monto: {}. Nuevo saldo origen: {}",
            originAccount.getAccountNumber(), destinationAccount.getAccountNumber(), destinationAccount.getUser().getEmail(), request.getAmount(), originAccount.getBalance());

        // 6. Registrar movimiento de ingreso en cuenta destino (para el usuario que recibe)
        Movement incomeMovement = new Movement();
        incomeMovement.setAccount(destinationAccount);
        incomeMovement.setDate(LocalDate.now());
        incomeMovement.setDescription(
            request.getDescription() != null && !request.getDescription().isEmpty() ?
            request.getDescription() :
            "Transferencia recibida de " + originAccount.getAccountNumber() + " (" + originAccount.getUser().getFirstName() + " " + originAccount.getUser().getLastName() + ")"
        );
        incomeMovement.setType(MovementType.INCOME);
        incomeMovement.setAmount(request.getAmount());
        movements.add(movementRepository.save(incomeMovement));
        logger.info("Transferencia recibida en {} de {} ({}). Monto: {}. Nuevo saldo destino: {}",
            destinationAccount.getAccountNumber(), originAccount.getAccountNumber(), originAccount.getUser().getEmail(), request.getAmount(), destinationAccount.getBalance());

        return movements;
    }


    /**
     * Procesa una transferencia a una cuenta externa (a un tercero).
     *
     * @param request El DTO de solicitud de transferencia externa.
     * @param userId El ID del usuario que realiza la transferencia.
     * @return Un ExternalTransferResponse con el resultado de la operacion.
     * @throws IllegalArgumentException Si la cuenta de origen no existe, no pertenece al usuario, o el saldo es insuficiente.
     */
    @Override
    @Transactional
    public ExternalTransferResponse processExternalTransfer(ExternalTransferRequest request, Long userId) {
        User userOrigin = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario de origen no encontrado."));

        Account originAccount = accountRepository.findByAccountNumberAndUser(request.getOriginAccountNumber(), userOrigin)
                .orElseThrow(() -> new IllegalArgumentException("Cuenta de origen no encontrada o no pertenece al usuario."));

        // Validar saldo suficiente
        if (originAccount.getBalance().compareTo(request.getAmount()) < 0) {
            throw new IllegalArgumentException("Saldo insuficiente en la cuenta de origen " + request.getOriginAccountNumber());
        }

        // Actualizar saldo de la cuenta de origen
        originAccount.setBalance(originAccount.getBalance().subtract(request.getAmount()));
        accountRepository.save(originAccount);
        logger.debug("Saldo de cuenta origen {} actualizado a {}", originAccount.getAccountNumber(), originAccount.getBalance());


        // Registrar movimiento de egreso en cuenta origen
        Movement expense = new Movement();
        expense.setAccount(originAccount);
        expense.setDate(LocalDate.now());
        expense.setDescription(String.format("Transferencia enviada a %s (Banco: %s, Cuenta: %s). %s",
                request.getDestinationName(),
                request.getDestinationBank(),
                request.getDestinationAccountNumber(),
                request.getDescription() != null && !request.getDescription().isEmpty() ? "Motivo: " + request.getDescription() : ""));
        expense.setType(MovementType.EXPENSE);
        expense.setAmount(request.getAmount());
        expense = movementRepository.save(expense);
        logger.debug("Movimiento de egreso registrado: {}", expense.getId());


        // Simular el ingreso en la cuenta destino externa (no se persiste en nuestra DB)
        // Podrias anadir logica para interactuar con un servicio externo o API aqui.
        // Por ahora, solo registramos el movimiento de ingreso "logico" para el reporte.
        // NOTA: Este movimiento 'income' se registra en la cuenta de ORIGEN para fines de auditoría del remitente,
        // pero su tipo es INCOME para reflejar la naturaleza de la transacción externa.
        // Para evitar confusión en el historial del remitente, la descripción es clave.
        Movement income = new Movement();
        income.setAccount(originAccount); // Asociamos al mismo origen para que aparezca en su historial
        income.setDate(LocalDate.now());
        String senderName = userOrigin.getFirstName() + " " + userOrigin.getLastName();
        income.setDescription(String.format("Confirmación de transferencia recibida por %s (Cuenta: %s). %s",
                request.getDestinationName(),
                request.getDestinationAccountNumber(),
                request.getDescription() != null && !request.getDescription().isEmpty() ? "Motivo: " + request.getDescription() : ""));
        income.setType(MovementType.INCOME); // Se mantiene como INCOME para reflejar que el dinero "salió" de nuestro sistema hacia otro banco
        income.setAmount(request.getAmount()); // El monto es el mismo que se envió
        income = movementRepository.save(income); // Guardar para fines de auditoria/historial del remitente
        logger.debug("Movimiento de ingreso simulado registrado: {}", income.getId());


        ExternalTransferResponse responseDTO = new ExternalTransferResponse();
        responseDTO.setMessage("Transferencia externa realizada con exito.");
        responseDTO.setNewOriginAccountBalance(originAccount.getBalance());

        responseDTO.setLastOriginMovement(mapMovementToDTO(expense));

        // LINEA QUE DA ERROR:
        List<Movement> recentMovements = movementRepository.findByAccountIdOrderByDateDesc(originAccount.getId(), PageRequest.of(0, 5));
        responseDTO.setRecentMovements(
            recentMovements.stream()
                .map(this::mapMovementToDTO)
                .collect(Collectors.toList())
        );
        logger.info("Transferencia externa completada con exito entre {} y {}. Nuevo saldo origen: {}", request.getOriginAccountNumber(), request.getDestinationAccountNumber(), originAccount.getBalance());
        return responseDTO;
    }
}
