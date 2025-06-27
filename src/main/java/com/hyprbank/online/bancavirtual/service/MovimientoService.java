package com.hyprbank.online.bancavirtual.service;

// Importaciones de Entidades y Enums
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

// Importaciones de Spring Framework
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

// Importaciones de Java Utilities
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/*
 * Clase de Servicio para la gestion de movimientos bancarios.
 *
 * Esta clase encapsula la logica de negocio relacionada con la consulta y manipulacion de movimientos.
 * Interactua con los repositorios de Movimiento, Cuenta y Usuario para realizar operaciones complejas.
 *
 * La anotacion @Service indica que esta clase es un componente de servicio de Spring,
 * adecuado para contener la logica de negocio.
 */

@Service
public class MovimientoService {

    private final MovimientoRepository movimientoRepository;
    private final UsuarioRepository usuarioRepository;
    private final CuentaRepository cuentaRepository;

    /*
     * Constructor para inyeccion de dependencias.
     * Spring se encargara de proporcionar las instancias de los repositorios.
     */
    @Autowired
    public MovimientoService(MovimientoRepository movimientoRepository, UsuarioRepository usuarioRepository, CuentaRepository cuentaRepository) {
        this.movimientoRepository = movimientoRepository;
        this.usuarioRepository = usuarioRepository;
        this.cuentaRepository = cuentaRepository;
    }

    /**
     * Obtiene todos los movimientos para un usuario dado, aplicando filtros opcionales de fecha y tipo.
     * Los movimientos se recopilan de todas las cuentas asociadas al usuario y se ordenan por fecha descendente.
     *
     * @param usuarioId El ID del usuario para el cual se desean obtener los movimientos.
     * @param fechaInicio Fecha de inicio para el filtro de rango (opcional). Los movimientos seran incluidos si su fecha es posterior o igual a esta.
     * @param fechaFin Fecha fin para el filtro de rango (opcional). Los movimientos seran incluidos si su fecha es anterior o igual a esta.
     * @param tipo Tipo de movimiento (INGRESO o EGRESO, opcional). Los movimientos se filtraran por este tipo.
     * @return Una lista de MovimientoDTOs que representan los movimientos del usuario que cumplen con los criterios de filtro.
     * @throws RuntimeException si el usuario no es encontrado con el ID proporcionado.
     */
    public List<MovimientoDTO> getMovimientosByUsuarioId(Long usuarioId, LocalDate fechaInicio, LocalDate fechaFin, TipoMovimiento tipo) {
        // Buscar el usuario por su ID; si no se encuentra, lanzar una excepcion.
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + usuarioId));

        // Obtener todas las cuentas asociadas al usuario para buscar sus movimientos.
        List<Cuenta> cuentasDelUsuario = cuentaRepository.findByUsuario(usuario);

        // Si el usuario no tiene cuentas, no puede tener movimientos, por lo que se devuelve una lista vacia.
        if (cuentasDelUsuario.isEmpty()) {
            return List.of(); // Devuelve una lista inmutable vacia.
        }

        // Recopilar movimientos de todas las cuentas del usuario, aplicando los filtros dinamicamente.
        // Se utiliza flatMap para aplanar las listas de movimientos de cada cuenta en una sola lista.
        List<Movimiento> movimientos = cuentasDelUsuario.stream()
                .flatMap(cuenta -> {
                    // Logica de filtrado dinamico usando los metodos de repositorio adecuados.
                    if (fechaInicio != null && fechaFin != null && tipo != null) {
                        return movimientoRepository.findByCuentaIdAndTipoAndFechaBetweenOrderByFechaDesc(cuenta.getId(), tipo, fechaInicio, fechaFin).stream();
                    } else if (fechaInicio != null && fechaFin != null) {
                        return movimientoRepository.findByCuentaIdAndFechaBetweenOrderByFechaDesc(cuenta.getId(), fechaInicio, fechaFin).stream();
                    } else if (tipo != null) {
                        return movimientoRepository.findByCuentaIdAndTipoOrderByFechaDesc(cuenta.getId(), tipo).stream();
                    } else {
                        // Si no hay filtros especificos, obtener todos los movimientos de la cuenta.
                        return movimientoRepository.findByCuentaIdOrderByFechaDesc(cuenta.getId()).stream();
                    }
                })
                .sorted((m1, m2) -> m2.getFecha().compareTo(m1.getFecha())) // Ordenar todos los movimientos recopilados por fecha descendente.
                .collect(Collectors.toList());

        // Convertir las entidades Movimiento a objetos MovimientoDTO para la capa de presentacion.
        return movimientos.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * Metodo auxiliar para convertir una entidad Movimiento a un MovimientoDTO.
     * Este metodo se encarga de mapear los campos relevantes de la entidad al DTO.
     *
     * @param movimiento La entidad Movimiento a convertir.
     * @return El MovimientoDTO resultante con los datos mapeados.
     */
    private MovimientoDTO convertToDto(Movimiento movimiento) {
        MovimientoDTO dto = new MovimientoDTO();
        dto.setId(movimiento.getId());
        dto.setCuentaId(movimiento.getCuenta().getId());
        dto.setNumeroCuenta(movimiento.getCuenta().getNumeroCuenta());
        dto.setFecha(movimiento.getFecha());
        dto.setDescripcion(movimiento.getDescripcion());
        dto.setTipo(movimiento.getTipo());
        dto.setMonto(movimiento.getMonto());
        return dto;
    }

    // Mas adelante, aqui se podrian anadir metodos para crear, actualizar o eliminar movimientos (ej. al hacer una transferencia o pago de servicio)
}