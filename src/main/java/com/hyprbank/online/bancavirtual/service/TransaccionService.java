package com.hyprbank.online.bancavirtual.service;

// Importaciones de Entidades
import com.hyprbank.online.bancavirtual.model.Cuenta;
import com.hyprbank.online.bancavirtual.model.Movimiento;
import com.hyprbank.online.bancavirtual.model.Movimiento.TipoMovimiento;
import com.hyprbank.online.bancavirtual.model.Usuario;

// Importaciones de Repositorios
import com.hyprbank.online.bancavirtual.repository.CuentaRepository;
import com.hyprbank.online.bancavirtual.repository.MovimientoRepository;
import com.hyprbank.online.bancavirtual.repository.UsuarioRepository;

// Importaciones de DTOs
import com.hyprbank.online.bancavirtual.dto.MovimientoDTO;
import com.hyprbank.online.bancavirtual.dto.MovimientoRequest; 
import com.hyprbank.online.bancavirtual.dto.TransferenciaRequest; 
import com.hyprbank.online.bancavirtual.dto.TransferenciaExternaRequest; 
import com.hyprbank.online.bancavirtual.dto.TransferenciaExternaResponse;
// Importaciones de Spring Framework
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// Importaciones de Java Utilities
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

// Importaciones de Logging
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * Clase de Servicio para la gestion de transacciones bancarias.
 *
 * Esta clase encapsula la logica de negocio para realizar depositos, retiros y transferencias
 * (internas entre cuentas del mismo usuario y externas entre diferentes cuentas del sistema).
 * Asegura la atomicidad de las operaciones mediante transacciones.
 *
 * La anotacion @Service indica que esta clase es un componente de servicio de Spring.
 */
@Service
public class TransaccionService {

    private static final Logger logger = LoggerFactory.getLogger(TransaccionService.class);

    private final CuentaRepository cuentaRepository;
    private final MovimientoRepository movimientoRepository;
    private final UsuarioRepository usuarioRepository;

    /*
     * Constructor para inyeccion de dependencias.
     * Spring inyectara las instancias de los repositorios necesarios.
     */
    @Autowired
    public TransaccionService(CuentaRepository cuentaRepository, MovimientoRepository movimientoRepository, UsuarioRepository usuarioRepository) {
        this.cuentaRepository = cuentaRepository;
        this.movimientoRepository = movimientoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    /**
     * Mapea una entidad {@link Movimiento} a un {@link MovimientoDTO}.
     * Este metodo auxiliar es crucial para transformar los datos del modelo de persistencia
     * a un formato seguro y adecuado para ser expuesto en la capa de presentacion (ej. a traves de una API REST).
     *
     * @param movimiento La entidad Movimiento a ser mapeada.
     * @return Un {@link MovimientoDTO} con los datos relevantes del movimiento, o {@code null} si la entidad de entrada es nula.
     */
    private MovimientoDTO mapMovimientoToDTO(Movimiento movimiento) {
        if (movimiento == null) {
            return null;
        }
        MovimientoDTO dto = new MovimientoDTO();
        dto.setId(movimiento.getId());
        if (movimiento.getCuenta() != null) {
            dto.setCuentaId(movimiento.getCuenta().getId());
            dto.setNumeroCuenta(movimiento.getCuenta().getNumeroCuenta());
        } else {
            // Caso teorico donde la cuenta podria ser nula, aunque no deberia ocurrir con movimientos persistidos.
            dto.setCuentaId(null);
            dto.setNumeroCuenta("Cuenta Desconocida");
        }
        dto.setFecha(movimiento.getFecha());
        dto.setDescripcion(movimiento.getDescripcion());
        dto.setTipo(movimiento.getTipo());
        dto.setMonto(movimiento.getMonto());
        return dto;
    }


    /**
     * Realiza un deposito en una cuenta especifica del usuario autenticado.
     * Esta operacion incrementa el saldo de la cuenta y registra un movimiento de tipo INGRESO.
     *
     * @param request DTO con los detalles del deposito (numero de cuenta de destino, monto, descripcion).
     * @param usuarioId ID del usuario autenticado que realiza el deposito.
     * @return El objeto {@link Movimiento} registrado para el deposito.
     * @throws RuntimeException Si el usuario no es encontrado.
     * @throws IllegalArgumentException Si la cuenta de destino no es encontrada, no pertenece al usuario autenticado,
     * o si el monto del deposito no es positivo.
     */
    @Transactional
    public Movimiento realizarDeposito(MovimientoRequest request, Long usuarioId) {
        logger.info("Iniciando deposito para usuario ID: {} en cuenta: {} con monto: {}", usuarioId, request.getNumeroCuenta(), request.getMonto());

        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + usuarioId));

        Cuenta cuenta = cuentaRepository.findByNumeroCuentaAndUsuario(request.getNumeroCuenta(), usuario)
                .orElseThrow(() -> new IllegalArgumentException("Cuenta de destino no encontrada o no pertenece al usuario."));

        if (request.getMonto().compareTo(BigDecimal.ZERO) <= 0) {
            logger.warn("Intento de deposito con monto no positivo para cuenta: {}", request.getNumeroCuenta());
            throw new IllegalArgumentException("El monto del deposito debe ser positivo.");
        }

        cuenta.setSaldo(cuenta.getSaldo().add(request.getMonto()));
        cuentaRepository.save(cuenta);

        Movimiento movimiento = new Movimiento();
        movimiento.setCuenta(cuenta);
        movimiento.setFecha(LocalDate.now());
        movimiento.setDescripcion(request.getDescripcion() != null && !request.getDescripcion().trim().isEmpty() ? request.getDescripcion() : "Depósito");
        movimiento.setTipo(TipoMovimiento.INGRESO);
        movimiento.setMonto(request.getMonto());

        logger.info("Deposito exitoso en cuenta: {} para usuario ID: {}. Nuevo saldo: {}", cuenta.getNumeroCuenta(), usuarioId, cuenta.getSaldo());
        return movimientoRepository.save(movimiento);
    }

    /**
     * Realiza un retiro de una cuenta especifica del usuario autenticado.
     * Esta operacion decrementa el saldo de la cuenta y registra un movimiento de tipo EGRESO.
     *
     * @param request DTO con los detalles del retiro (numero de cuenta de origen, monto, descripcion).
     * @param usuarioId ID del usuario autenticado que realiza el retiro.
     * @return El objeto {@link Movimiento} registrado para el retiro.
     * @throws RuntimeException Si el usuario no es encontrado.
     * @throws IllegalArgumentException Si la cuenta de origen no es encontrada, no pertenece al usuario autenticado,
     * si el monto no es positivo, o si hay fondos insuficientes en la cuenta.
     */
    @Transactional
    public Movimiento realizarRetiro(MovimientoRequest request, Long usuarioId) {
        logger.info("Iniciando retiro para usuario ID: {} de cuenta: {} con monto: {}", usuarioId, request.getNumeroCuenta(), request.getMonto());

        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + usuarioId));

        Cuenta cuenta = cuentaRepository.findByNumeroCuentaAndUsuario(request.getNumeroCuenta(), usuario)
                .orElseThrow(() -> new IllegalArgumentException("Cuenta de origen no encontrada o no pertenece al usuario."));

        if (request.getMonto().compareTo(BigDecimal.ZERO) <= 0) {
            logger.warn("Intento de retiro con monto no positivo para cuenta: {}", request.getNumeroCuenta());
            throw new IllegalArgumentException("El monto del retiro debe ser positivo.");
        }

        if (cuenta.getSaldo().compareTo(request.getMonto()) < 0) {
            logger.warn("Fondos insuficientes para retiro de cuenta: {}. Saldo: {}, Monto solicitado: {}", cuenta.getNumeroCuenta(), cuenta.getSaldo(), request.getMonto());
            throw new IllegalArgumentException("Fondos insuficientes en la cuenta: " + cuenta.getNumeroCuenta());
        }

        cuenta.setSaldo(cuenta.getSaldo().subtract(request.getMonto()));
        cuentaRepository.save(cuenta);

        Movimiento movimiento = new Movimiento();
        movimiento.setCuenta(cuenta);
        movimiento.setFecha(LocalDate.now());
        movimiento.setDescripcion(request.getDescripcion() != null && !request.getDescripcion().trim().isEmpty() ? request.getDescripcion() : "Retiro");
        movimiento.setTipo(TipoMovimiento.EGRESO);
        movimiento.setMonto(request.getMonto());

        logger.info("Retiro exitoso de cuenta: {} para usuario ID: {}. Nuevo saldo: {}", cuenta.getNumeroCuenta(), usuarioId, cuenta.getSaldo());
        return movimientoRepository.save(movimiento);
    }

    /**
     * Realiza una transferencia de dinero entre dos cuentas del MISMO usuario autenticado.
     * Esta operacion implica un debito de la cuenta de origen y un credito a la cuenta de destino,
     * registrando dos movimientos (uno de egreso y uno de ingreso).
     *
     * @param request DTO con los detalles de la transferencia (numeros de cuenta origen y destino, monto, descripcion).
     * @param usuarioId ID del usuario autenticado que realiza la transferencia.
     * @return Una lista de los dos objetos {@link Movimiento} registrados (el de egreso y el de ingreso).
     * @throws RuntimeException Si el usuario no es encontrado.
     * @throws IllegalArgumentException Si las cuentas de origen o destino no son encontradas, no pertenecen al usuario autenticado,
     * si las cuentas de origen y destino son las mismas, el monto no es positivo,
     * o si hay fondos insuficientes en la cuenta de origen.
     */

     /*NO ESTOY SEGURO SI VA ASI */
    @Transactional
    public List<Movimiento> realizarTransferencia(TransferenciaRequest request, Long usuarioId) {
        logger.info("Iniciando transferencia interna para usuario ID: {} de cuenta: {} a cuenta: {} con monto: {}",
                usuarioId, request.getNumeroCuentaOrigen(), request.getNumeroCuentaDestino(), request.getMonto());

        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + usuarioId));

        if (request.getNumeroCuentaOrigen().equals(request.getNumeroCuentaDestino())) {
            logger.warn("Intento de transferencia a la misma cuenta: {}", request.getNumeroCuentaOrigen());
            throw new IllegalArgumentException("La cuenta de origen y destino no pueden ser la misma.");
        }

        Cuenta cuentaOrigen = cuentaRepository.findByNumeroCuentaAndUsuario(request.getNumeroCuentaOrigen(), usuario)
                .orElseThrow(() -> new IllegalArgumentException("Cuenta de origen no encontrada o no pertenece al usuario."));

        Cuenta cuentaDestino = cuentaRepository.findByNumeroCuentaAndUsuario(request.getNumeroCuentaDestino(), usuario)
                .orElseThrow(() -> new IllegalArgumentException("Cuenta de destino no encontrada o no pertenece al usuario."));

        if (request.getMonto().compareTo(BigDecimal.ZERO) <= 0) {
            logger.warn("Intento de transferencia con monto no positivo para cuenta origen: {}", request.getNumeroCuentaOrigen());
            throw new IllegalArgumentException("El monto de la transferencia debe ser positivo.");
        }

        if (cuentaOrigen.getSaldo().compareTo(request.getMonto()) < 0) {
            logger.warn("Fondos insuficientes para transferencia de cuenta: {}. Saldo: {}, Monto solicitado: {}", cuentaOrigen.getNumeroCuenta(), cuentaOrigen.getSaldo(), request.getMonto());
            throw new IllegalArgumentException("Fondos insuficientes en la cuenta de origen: " + cuentaOrigen.getNumeroCuenta());
        }

        cuentaOrigen.setSaldo(cuentaOrigen.getSaldo().subtract(request.getMonto()));
        cuentaDestino.setSaldo(cuentaDestino.getSaldo().add(request.getMonto()));

        cuentaRepository.save(cuentaOrigen);
        cuentaRepository.save(cuentaDestino);

        Movimiento movimientoOrigen = new Movimiento();
        movimientoOrigen.setCuenta(cuentaOrigen);
        movimientoOrigen.setFecha(LocalDate.now());
        movimientoOrigen.setDescripcion(
                request.getDescripcion() != null && !request.getDescripcion().trim().isEmpty() ? request.getDescripcion() : "Transferencia saliente a " + cuentaDestino.getNumeroCuenta()
        );
        movimientoOrigen.setTipo(TipoMovimiento.EGRESO);
        movimientoOrigen.setMonto(request.getMonto());
        movimientoOrigen = movimientoRepository.save(movimientoOrigen);

        Movimiento movimientoDestino = new Movimiento();
        movimientoDestino.setCuenta(cuentaDestino);
        movimientoDestino.setFecha(LocalDate.now());
        movimientoDestino.setDescripcion(
                request.getDescripcion() != null && !request.getDescripcion().trim().isEmpty() ? request.getDescripcion() : "Transferencia entrante de " + cuentaOrigen.getNumeroCuenta()
        );
        movimientoDestino.setTipo(TipoMovimiento.INGRESO);
        movimientoDestino.setMonto(request.getMonto());
        movimientoDestino = movimientoRepository.save(movimientoDestino);

        logger.info("Transferencia interna exitosa de cuenta: {} a cuenta: {} para usuario ID: {}", request.getNumeroCuentaOrigen(), request.getNumeroCuentaDestino(), usuarioId);
        return List.of(movimientoOrigen, movimientoDestino);
    }

    /**
     * Procesa una transferencia desde una cuenta del usuario autenticado a otra cuenta
     * que puede pertenecer a otro usuario dentro del mismo sistema bancario.
     * Esta operacion implica un debito en la cuenta de origen y un credito en la cuenta de destino,
     * registrando dos movimientos y proporcionando un DTO de respuesta detallado.
     *
     * @param request DTO con los detalles de la transferencia externa (cuenta origen, cuenta destino, monto, datos de destino).
     * @param usuarioOrigenId ID del usuario que inicia la transferencia (propietario de la cuenta de origen).
     * @return {@link TransferenciaExternaResponseDTO} con el mensaje de exito, el nuevo saldo de la cuenta de origen
     * y el ultimo movimiento de egreso registrado, ademas de una lista de movimientos recientes de la cuenta origen.
     * @throws RuntimeException Si el usuario de origen no es encontrado.
     * @throws IllegalArgumentException Si alguna de las cuentas (origen o destino) no existe,
     * si la cuenta de origen no pertenece al usuario autenticado,
     * si la cuenta de origen y destino son la misma,
     * si el monto no es positivo, o si hay fondos insuficientes en la cuenta de origen.
     */
    @Transactional
    public TransferenciaExternaResponse procesarTransferenciaExterna(TransferenciaExternaRequest request, Long usuarioOrigenId) {
        logger.info("Iniciando procesamiento de transferencia externa para usuario ID: {} desde cuenta: {} a cuenta: {} con monto: {}",
                usuarioOrigenId, request.getCuentaOrigen(), request.getCuentaDestino(), request.getMonto());

        Usuario usuarioOrigen = usuarioRepository.findById(usuarioOrigenId)
                .orElseThrow(() -> {
                    logger.error("Usuario no encontrado con ID: {}", usuarioOrigenId);
                    return new RuntimeException("Usuario no encontrado con ID: " + usuarioOrigenId);
                });

        Cuenta cuentaOrigen = cuentaRepository.findByNumeroCuentaAndUsuario(request.getCuentaOrigen(), usuarioOrigen)
                .orElseThrow(() -> {
                    logger.error("Cuenta de origen {} no encontrada o no pertenece al usuario ID: {}", request.getCuentaOrigen(), usuarioOrigenId);
                    return new IllegalArgumentException("Cuenta de origen no encontrada o no pertenece al usuario autenticado.");
                });

        Cuenta cuentaDestino = cuentaRepository.findByNumeroCuenta(request.getCuentaDestino())
                .orElseThrow(() -> {
                    logger.error("Cuenta de destino {} no existe en el sistema.", request.getCuentaDestino());
                    return new IllegalArgumentException("La cuenta de destino especificada no existe en el sistema.");
                });

        if (cuentaOrigen.getNumeroCuenta().equals(cuentaDestino.getNumeroCuenta())) {
            logger.warn("Intento de transferencia externa a la misma cuenta: {}", request.getCuentaOrigen());
            throw new IllegalArgumentException("La cuenta de origen y la cuenta de destino no pueden ser la misma.");
        }

        if (request.getMonto().compareTo(BigDecimal.ZERO) <= 0) {
            logger.warn("Intento de transferencia externa con monto no positivo: {}", request.getMonto());
            throw new IllegalArgumentException("El monto de la transferencia debe ser positivo.");
        }

        if (cuentaOrigen.getSaldo().compareTo(request.getMonto()) < 0) {
            logger.warn("Fondos insuficientes para transferencia externa de cuenta: {}. Saldo: {}, Monto solicitado: {}",
                    cuentaOrigen.getNumeroCuenta(), cuentaOrigen.getSaldo(), request.getMonto());
            throw new IllegalArgumentException("Fondos insuficientes en la cuenta de origen para la transferencia.");
        }

        // Realizar debito en la cuenta de origen
        cuentaOrigen.setSaldo(cuentaOrigen.getSaldo().subtract(request.getMonto()));
        cuentaRepository.save(cuentaOrigen);
        logger.debug("Debito de {} realizado en cuenta origen {}. Nuevo saldo: {}", request.getMonto(), cuentaOrigen.getNumeroCuenta(), cuentaOrigen.getSaldo());


        Movimiento egreso = new Movimiento();
        egreso.setCuenta(cuentaOrigen);
        egreso.setTipo(TipoMovimiento.EGRESO);
        egreso.setMonto(request.getMonto());
        egreso.setFecha(LocalDate.now());
        egreso.setDescripcion(String.format("Transferencia a %s (%s, Banco: %s). %s",
                request.getNombreDestino(),
                request.getCuentaDestino(),
                request.getBancoDestino(),
                request.getDescripcion() != null && !request.getDescripcion().isEmpty() ? "Motivo: " + request.getDescripcion() : ""));
        egreso = movimientoRepository.save(egreso);
        logger.debug("Movimiento de egreso registrado: {}", egreso.getId());

        // Realizar credito en la cuenta de destino
        cuentaDestino.setSaldo(cuentaDestino.getSaldo().add(request.getMonto()));
        cuentaRepository.save(cuentaDestino);
        logger.debug("Credito de {} realizado en cuenta destino {}. Nuevo saldo: {}", request.getMonto(), cuentaDestino.getNumeroCuenta(), cuentaDestino.getSaldo());

        Movimiento ingreso = new Movimiento();
        ingreso.setCuenta(cuentaDestino);
        ingreso.setTipo(TipoMovimiento.INGRESO);
        ingreso.setMonto(request.getMonto());
        ingreso.setFecha(LocalDate.now());
        String nombreRemitente = usuarioOrigen.getNombre() + " " + usuarioOrigen.getApellido();
        ingreso.setDescripcion(String.format("Transferencia recibida de %s (Cuenta: %s). %s",
                nombreRemitente,
                request.getCuentaOrigen(),
                request.getDescripcion() != null && !request.getDescripcion().isEmpty() ? "Motivo: " + request.getDescripcion() : ""));
        ingreso = movimientoRepository.save(ingreso);
        logger.debug("Movimiento de ingreso registrado: {}", ingreso.getId());

        TransferenciaExternaResponse responseDTO = new TransferenciaExternaResponse();
        responseDTO.setMensaje("Transferencia externa realizada con exito.");
        responseDTO.setNuevoSaldoCuentaOrigen(cuentaOrigen.getSaldo());

        responseDTO.setUltimoMovimientoOrigen(mapMovimientoToDTO(egreso));

        List<Movimiento> movimientosRecientes = movimientoRepository.findByCuentaIdOrderByFechaDesc(cuentaOrigen.getId());
        responseDTO.setMovimientosRecientes(
            movimientosRecientes.stream()
                .map(this::mapMovimientoToDTO)
                .collect(Collectors.toList())
        );
        logger.info("Transferencia externa completada con exito entre {} y {}. Nuevo saldo origen: {}", request.getCuentaOrigen(), request.getCuentaDestino(), cuentaOrigen.getSaldo());
        return responseDTO;
    }
}