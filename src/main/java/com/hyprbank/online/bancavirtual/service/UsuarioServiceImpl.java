package com.hyprbank.online.bancavirtual.service;

import com.hyprbank.online.bancavirtual.dto.RegistroRequest; // DTO renombrado
import com.hyprbank.online.bancavirtual.model.Usuario;
import com.hyprbank.online.bancavirtual.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/*
 * Implementacion de la interfaz {@link UsuarioService}.
 *
 * Esta clase provee la logica de negocio para la gestion de usuarios,
 * incluyendo el registro, la consulta y la integracion con Spring Security para la autenticacion.
 */
@Service
public class UsuarioServiceImpl implements UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    /*
     * Constructor para inyeccion de dependencias.
     * Spring inyectara las instancias de UsuarioRepository y BCryptPasswordEncoder.
     */
    @Autowired
    public UsuarioServiceImpl(UsuarioRepository usuarioRepository, BCryptPasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Guarda un nuevo usuario en el sistema.
     * Se encarga de mapear los datos del DTO a la entidad Usuario,
     * encriptar la contrasena y guardar la entidad en la base de datos.
     *
     * @param registroDTO El {@link RegistroRequest} con la informacion del usuario a registrar.
     * @return La entidad {@link Usuario} guardada.
     * @throws IllegalArgumentException Si ya existe un usuario registrado con el email proporcionado.
     */
    @Override
    public Usuario guardar(RegistroRequest registroDTO) { // Usando el DTO renombrado
        // Validacion para evitar emails duplicados
        if (usuarioRepository.findByEmail(registroDTO.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Ya existe un usuario registrado con este email: " + registroDTO.getEmail());
        }

        Usuario usuario = new Usuario();
        usuario.setNombre(registroDTO.getNombre());
        usuario.setApellido(registroDTO.getApellido());
        usuario.setEmail(registroDTO.getEmail());
        usuario.setPassword(passwordEncoder.encode(registroDTO.getPassword())); // Encriptar la contrasena
        // Si tienes logica de roles por defecto, la anadirias aqui.
        // usuario.setRoles(new HashSet<>(Arrays.asList(new Rol("ROLE_USER")))); // Ejemplo: Asignar rol por defecto

        return usuarioRepository.save(usuario);
    }

    /**
     * Carga los detalles de un usuario por su nombre de usuario (email en este caso).
     * Este metodo es invocado por Spring Security durante el proceso de autenticacion.
     *
     * @param email La direccion de correo electronico del usuario.
     * @return Un objeto {@link UserDetails} que representa al usuario autenticado.
     * @throws UsernameNotFoundException Si el usuario no es encontrado con el email proporcionado.
     */
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado con email: " + email));
    }

    /**
     * Lista todos los usuarios existentes en la base de datos.
     *
     * @return Una {@link List} de entidades {@link Usuario}.
     */
    @Override
    public List<Usuario> listarUsuarios() {
        return usuarioRepository.findAll();
    }

    /**
     * Busca un usuario por su direccion de correo electronico.
     * Proporciona la entidad {@link Usuario} directamente o lanza una excepcion si no se encuentra.
     *
     * @param email La direccion de correo electronico del usuario.
     * @return La entidad {@link Usuario} si se encuentra.
     * @throws UsernameNotFoundException Si no se encuentra un usuario con el email proporcionado.
     */
    @Override
    public Usuario buscarPorEmail(String email) throws UsernameNotFoundException {
        return usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado con email: " + email));
    }
}