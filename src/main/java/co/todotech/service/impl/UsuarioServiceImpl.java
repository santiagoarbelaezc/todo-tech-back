package co.todotech.service.impl;

import co.todotech.exception.usuario.*;
import co.todotech.mapper.UsuarioMapper;
import co.todotech.model.dto.usuario.LoginResponse;
import co.todotech.model.dto.usuario.UsuarioDto;
import co.todotech.model.entities.Usuario;
import co.todotech.model.enums.TipoUsuario;
import co.todotech.repository.UsuarioRepository;
import co.todotech.security.JwtUtil;
import co.todotech.security.TokenBlacklistService;
import co.todotech.service.UsuarioService;
import co.todotech.utils.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UsuarioServiceImpl implements UsuarioService {

    private final UsuarioMapper usuarioMapper;
    private final UsuarioRepository usuarioRepository;
    private final EmailService emailService;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    private final TokenBlacklistService tokenBlacklistService;

    // Propiedades del admin desde application.properties
    @Value("${admin.username}")
    private String adminUsername;

    @Value("${admin.cedula}")
    private String adminCedula;

    @Value("${admin.email}")
    private String adminEmail;

    // Propiedades para el admin de prueba que no puede eliminar
    @Value("${admin.prueba.cedula:300000001}")
    private String adminPruebaCedula;

    @Value("${admin.prueba.username:adminprueba}")
    private String adminPruebaUsername;

    @Value("${admin.prueba.email:prueba.admin999@gmail.com}")
    private String adminPruebaEmail;

    @Override
    public LoginResponse login(String nombreUsuario, String contrasena) {
        log.info("=== INICIO LOGIN ===");
        log.info("Usuario intentando login: {}", nombreUsuario);

        Usuario usuario = usuarioRepository.findByNombreUsuario(nombreUsuario)
                .orElseThrow(() -> new UsuarioNotFoundException("Usuario no encontrado"));

        log.info("Usuario encontrado: {} - Email: {}", usuario.getNombreUsuario(), usuario.getCorreo());

        if (!passwordEncoder.matches(contrasena, usuario.getContrasena())) {
            throw new AuthenticationException("Contraseña incorrecta");
        }

        if (!usuario.isEstado()) {
            throw new UsuarioEstadoException("Usuario inactivo. Contacte al administrador");
        }

        String token = jwtUtil.generateToken(
                usuario.getNombreUsuario(),
                usuario.getId(),
                usuario.getTipoUsuario().name()
        );

        if (usuario.getTipoUsuario().name().equals("ADMIN")) {
            log.info("Usuario es ADMIN - enviando notificación SOLO a: {}", usuario.getCorreo());
            notificarIngresoAdmin(usuario);
        }

        log.info("=== FIN LOGIN EXITOSO ===");

        return new LoginResponse(
                token,
                "Bearer",
                usuario.getId(),
                usuario.getNombreUsuario(),
                usuario.getNombre(),
                usuario.getTipoUsuario(),
                "Login exitoso"
        );
    }

    @Override
    @Transactional(readOnly = true)
    public void solicitarRecordatorioContrasena(String correo) {
        log.info("=== INICIO RECORDATORIO CONTRASEÑA ===");
        log.info("Correo solicitante: {}", correo);

        if (correo == null || correo.trim().isEmpty()) {
            throw new UsuarioBusinessException("El correo electrónico es requerido");
        }

        // No permitir recordatorio para los admins
        if (correo.equals(adminEmail) || correo.equals(adminPruebaEmail)) {
            throw new UsuarioBusinessException("El administrador no puede solicitar recordatorio de contraseña");
        }

        List<TipoUsuario> tiposPermitidos = Arrays.asList(
                TipoUsuario.VENDEDOR,
                TipoUsuario.CAJERO,
                TipoUsuario.DESPACHADOR
        );

        Usuario usuario = usuarioRepository.findByCorreoAndTipoUsuarioIn(correo, tiposPermitidos)
                .orElseThrow(() -> new UsuarioNotFoundException("No se encontró un usuario activo con ese correo electrónico o no tiene permisos para solicitar recordatorio"));

        log.info("Usuario encontrado para recordatorio: {} - Email: {}", usuario.getNombreUsuario(), usuario.getCorreo());

        if (!usuario.isEstado()) {
            throw new UsuarioEstadoException("El usuario está inactivo. Contacte al administrador");
        }

        try {
            log.info("Enviando recordatorio ÚNICAMENTE a: {}", usuario.getCorreo());

            emailService.sendPasswordReminder(
                    usuario.getCorreo(),
                    usuario.getNombre(),
                    usuario.getNombreUsuario(),
                    "Por razones de seguridad, contacte al administrador para restablecer su contraseña"
            );

            log.info("Recordatorio enviado exitosamente SOLO a: {}", usuario.getCorreo());
            log.info("=== FIN RECORDATORIO CONTRASEÑA ===");

        } catch (Exception e) {
            log.error("Error al enviar recordatorio de contraseña a {}: {}", correo, e.getMessage());
            throw new EmailException("Error al enviar el recordatorio por correo: " + e.getMessage());
        }
    }

    @Override
    public UsuarioDto obtenerUsuarioPorId(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new UsuarioNotFoundException("Usuario no encontrado con ID: " + id));

        // Proteger al admin principal de ser obtenido
        if (esUsuarioAdminPrincipal(usuario)) {
            throw new UsuarioProtectedException("No se puede acceder al usuario administrador principal");
        }

        return usuarioMapper.toDto(usuario);
    }

    @Override
    public UsuarioDto obtenerUsuarioPorCedula(String cedula) {
        // Proteger la cédula del admin principal
        if (cedula.equals(adminCedula)) {
            throw new UsuarioProtectedException("No se puede acceder al usuario administrador principal");
        }

        Usuario usuario = usuarioRepository.findByCedula(cedula)
                .orElseThrow(() -> new UsuarioNotFoundException("Usuario no encontrado con cédula: " + cedula));
        return usuarioMapper.toDto(usuario);
    }

    private void notificarIngresoAdmin(Usuario admin) {
        try {
            String fechaHora = LocalDateTime.now()
                    .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

            emailService.sendAdminLoginNotification(
                    admin.getCorreo(),
                    admin.getNombre(),
                    fechaHora
            );

            log.info("Notificación de ingreso enviada al admin: {}", admin.getNombreUsuario());
        } catch (Exception e) {
            log.error("Error al enviar notificación de ingreso al admin {}: {}",
                    admin.getNombreUsuario(), e.getMessage());
            // No lanzamos excepción aquí para no afectar el login
        }
    }

    @Override
    @Transactional
    public void cambiarEstadoUsuario(Long id, boolean estado) {
        // 1. Obtener usuario autenticado PRIMERO
        Usuario usuarioAutenticado = obtenerUsuarioAutenticado();

        // 2. BLOQUEAR admin de prueba INMEDIATAMENTE
        if (esUsuarioAdminPrueba(usuarioAutenticado)) {
            log.error("🚫 ADMIN PRUEBA BLOQUEADO - {} intentó cambiar estado de usuario",
                    usuarioAutenticado.getNombreUsuario());
            throw new UsuarioNoAutorizadoException(
                    "El administrador de prueba '" + usuarioAutenticado.getNombreUsuario() +
                            "' no tiene permisos para cambiar estados de usuario."
            );
        }

        // 3. Obtener usuario a modificar
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new UsuarioNotFoundException("Usuario no encontrado con ID: " + id));

        // 4. Proteger al admin principal
        if (esUsuarioAdminPrincipal(usuario)) {
            throw new UsuarioProtectedException("No se puede modificar el estado del administrador principal");
        }

        // 5. Si intenta desactivar otro ADMIN, verificar permisos
        if (usuario.getTipoUsuario() == TipoUsuario.ADMIN && !estado) {
            // Solo el admin principal puede desactivar otros admins
            if (!esUsuarioAdminPrincipal(usuarioAutenticado)) {
                throw new UsuarioNoAutorizadoException(
                        "Solo el administrador principal puede desactivar otros administradores."
                );
            }
        }

        usuario.setEstado(estado);
        usuarioRepository.save(usuario);

        log.info("Usuario {} cambió estado de usuario {} a: {}",
                usuarioAutenticado.getNombreUsuario(),
                usuario.getNombreUsuario(),
                estado ? "ACTIVO" : "INACTIVO");
    }

    @Override
    @Transactional
    public void crearUsuario(UsuarioDto dto) {
        log.info("Creando usuario: {}", dto.getNombreUsuario());

        // Obtener el usuario autenticado que está haciendo la petición
        Usuario usuarioAutenticado = obtenerUsuarioAutenticado();

        // Verificar permisos para crear administradores
        if (dto.getTipoUsuario() == TipoUsuario.ADMIN) {
            // Solo el admin principal puede crear otros administradores
            if (!esUsuarioAdminPrincipal(usuarioAutenticado)) {
                throw new UsuarioNoAutorizadoException("No está autorizado para crear usuarios administradores. Solo el administrador principal puede crear otros administradores.");
            }
            log.info("Admin principal {} está creando un nuevo administrador", usuarioAutenticado.getNombreUsuario());
        }

        // No permitir crear usuario con credenciales del admin principal o de prueba
        if (dto.getCedula().equals(adminCedula) || dto.getCedula().equals(adminPruebaCedula)) {
            throw new UsuarioProtectedException("No se puede usar una cédula de administrador reservada");
        }

        if (dto.getCorreo().equals(adminEmail) || dto.getCorreo().equals(adminPruebaEmail)) {
            throw new UsuarioProtectedException("No se puede usar un correo de administrador reservado");
        }

        if (dto.getNombreUsuario().equals(adminUsername) || dto.getNombreUsuario().equals(adminPruebaUsername)) {
            throw new UsuarioProtectedException("No se puede usar un nombre de usuario de administrador reservado");
        }

        if (usuarioRepository.existsByCedula(dto.getCedula())) {
            throw new UsuarioDuplicateException("Ya existe un usuario con la cédula: " + dto.getCedula());
        }

        if (usuarioRepository.existsByCorreo(dto.getCorreo())) {
            throw new UsuarioDuplicateException("Ya existe un usuario con el correo: " + dto.getCorreo());
        }

        if (usuarioRepository.existsByNombreUsuario(dto.getNombreUsuario())) {
            throw new UsuarioDuplicateException("Ya existe un usuario con el nombre de usuario: " + dto.getNombreUsuario());
        }

        // Validar que la contraseña no sea nula o vacía al crear usuario
        if (dto.getContrasena() == null || dto.getContrasena().trim().isEmpty()) {
            throw new UsuarioBusinessException("La contraseña es requerida para crear un usuario");
        }

        Usuario usuario = usuarioMapper.toEntity(dto);
        usuario.setEstado(true);

        // ENCRIPTAR LA CONTRASEÑA ANTES DE GUARDAR
        String contrasenaEncriptada = passwordEncoder.encode(dto.getContrasena());
        usuario.setContrasena(contrasenaEncriptada);

        usuarioRepository.save(usuario);
        log.info("Usuario {} creado exitosamente por: {}",
                usuario.getNombreUsuario(),
                usuarioAutenticado.getNombreUsuario());
    }

    @Override
    @Transactional
    public void actualizarUsuario(Long id, UsuarioDto dto) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new UsuarioNotFoundException("Usuario no encontrado con ID: " + id));

        // Proteger al admin principal de actualizaciones
        if (esUsuarioAdminPrincipal(usuario)) {
            throw new UsuarioProtectedException("No se puede modificar al administrador principal");
        }

        // Obtener el usuario autenticado
        Usuario usuarioAutenticado = obtenerUsuarioAutenticado();

        // Si se intenta cambiar el tipo a ADMIN, verificar permisos
        if (dto.getTipoUsuario() == TipoUsuario.ADMIN && usuario.getTipoUsuario() != TipoUsuario.ADMIN) {
            // Solo el admin principal puede convertir usuarios en administradores
            if (!esUsuarioAdminPrincipal(usuarioAutenticado)) {
                throw new UsuarioNoAutorizadoException("No está autorizado para convertir usuarios en administradores. Solo el administrador principal puede hacerlo.");
            }
            log.info("Admin principal {} está convirtiendo al usuario {} en administrador",
                    usuarioAutenticado.getNombreUsuario(), usuario.getNombreUsuario());
        }

        // Verificar si la cédula/correo ya existen en otros usuarios
        if (!usuario.getCedula().equals(dto.getCedula()) &&
                usuarioRepository.existsByCedulaAndIdNot(dto.getCedula(), id)) {
            throw new UsuarioDuplicateException("Ya existe otro usuario con la cédula: " + dto.getCedula());
        }

        if (!usuario.getCorreo().equals(dto.getCorreo()) &&
                usuarioRepository.existsByCorreoAndIdNot(dto.getCorreo(), id)) {
            throw new UsuarioDuplicateException("Ya existe otro usuario con el correo: " + dto.getCorreo());
        }

        if (!usuario.getNombreUsuario().equals(dto.getNombreUsuario()) &&
                usuarioRepository.existsByNombreUsuarioAndIdNot(dto.getNombreUsuario(), id)) {
            throw new UsuarioDuplicateException("Ya existe otro usuario con el nombre de usuario: " + dto.getNombreUsuario());
        }

        // Validar que no se intente usar credenciales de admins reservados
        if (dto.getCedula().equals(adminCedula) || dto.getCedula().equals(adminPruebaCedula)) {
            throw new UsuarioProtectedException("No se puede usar una cédula de administrador reservada");
        }

        if (dto.getCorreo().equals(adminEmail) || dto.getCorreo().equals(adminPruebaEmail)) {
            throw new UsuarioProtectedException("No se puede usar un correo de administrador reservado");
        }

        if (dto.getNombreUsuario().equals(adminUsername) || dto.getNombreUsuario().equals(adminPruebaUsername)) {
            throw new UsuarioProtectedException("No se puede usar un nombre de usuario de administrador reservado");
        }

        // Actualizar campos EXCEPTO la contraseña
        usuarioMapper.updateUsuarioFromDto(dto, usuario);
        usuario.setEstado(dto.getEstado());

        // Manejar cambio de contraseña SOLO si se solicita explícitamente
        if (Boolean.TRUE.equals(dto.getCambiarContrasena())) {
            if (dto.getContrasena() != null && !dto.getContrasena().trim().isEmpty()) {
                String contrasenaEncriptada = passwordEncoder.encode(dto.getContrasena());
                usuario.setContrasena(contrasenaEncriptada);
                log.info("Contraseña actualizada para usuario ID: {}", id);
            } else {
                throw new UsuarioBusinessException("Se solicitó cambiar contraseña pero no se proporcionó una nueva contraseña");
            }
        }

        usuarioRepository.save(usuario);
        log.info("Usuario actualizado exitosamente: {}", usuario.getNombreUsuario());
    }

    @Override
    @Transactional
    public void eliminarUsuario(Long id) {
        log.info("=== INICIANDO ELIMINACIÓN DE USUARIO ===");

        // ✅ **PASO 1: OBTENER Y VALIDAR USUARIO AUTENTICADO PRIMERO**
        Usuario usuarioAutenticado = obtenerUsuarioAutenticado();

        log.info("Usuario autenticado: {} (ID: {}, Tipo: {}, Cédula: {})",
                usuarioAutenticado.getNombreUsuario(),
                usuarioAutenticado.getId(),
                usuarioAutenticado.getTipoUsuario(),
                usuarioAutenticado.getCedula());

        // ✅ **PASO 2: BLOQUEAR INMEDIATAMENTE AL ADMIN DE PRUEBA**
        if (esUsuarioAdminPrueba(usuarioAutenticado)) {
            log.error("🚫🚫🚫 BLOQUEO ADMIN PRUEBA - {} intentó eliminar usuario",
                    usuarioAutenticado.getNombreUsuario());
            throw new UsuarioNoAutorizadoException(
                    "ACCESO DENEGADO: El administrador de prueba '" +
                            usuarioAutenticado.getNombreUsuario() +
                            "' NO tiene permisos para eliminar usuarios. " +
                            "Esta cuenta es solo para consultas y operaciones básicas."
            );
        }

        // ✅ **PASO 3: Solo ahora obtener el usuario a eliminar**
        Usuario usuarioAEliminar = usuarioRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("❌ USUARIO_NO_ENCONTRADO - ID: {}", id);
                    return new UsuarioNotFoundException("Usuario no encontrado con ID: " + id);
                });

        log.info("Usuario a eliminar: {} (ID: {}, Tipo: {})",
                usuarioAEliminar.getNombreUsuario(),
                usuarioAEliminar.getId(),
                usuarioAEliminar.getTipoUsuario());

        // 4. Proteger al admin principal de ser eliminado
        if (esUsuarioAdminPrincipal(usuarioAEliminar)) {
            log.warn("⚠️ INTENTO_ELIMINAR_ADMIN_PRINCIPAL - Usuario {} intentó eliminar al admin principal",
                    usuarioAutenticado.getNombreUsuario());
            throw new UsuarioProtectedException("No se puede eliminar al administrador principal");
        }

        // 5. Validar que no se elimine a sí mismo
        if (usuarioAEliminar.getId().equals(usuarioAutenticado.getId())) {
            log.warn("⚠️ INTENTO_AUTOELIMINACION - Usuario {} intentó eliminarse a sí mismo",
                    usuarioAutenticado.getNombreUsuario());
            throw new UsuarioNoAutorizadoException("No puede eliminarse a sí mismo.");
        }

        // 6. Validar permisos jerárquicos (solo queda validar para admins principales)
        if (usuarioAEliminar.getTipoUsuario() == TipoUsuario.ADMIN) {
            // Solo el admin principal puede eliminar a otros administradores
            if (!esUsuarioAdminPrincipal(usuarioAutenticado)) {
                log.warn("⚠️ PERMISOS_INSUFICIENTES - Usuario {} intentó eliminar admin {} sin permisos",
                        usuarioAutenticado.getNombreUsuario(), usuarioAEliminar.getNombreUsuario());
                throw new UsuarioNoAutorizadoException(
                        "No está autorizado para eliminar usuarios administradores. " +
                                "Solo el administrador principal puede eliminar otros administradores."
                );
            }
            log.info("✅ Admin principal eliminando otro admin");
        } else {
            // Para VENDEDOR/CAJERO/DESPACHADOR, verificar que sea ADMIN
            if (usuarioAutenticado.getTipoUsuario() != TipoUsuario.ADMIN) {
                log.warn("⚠️ NO_ES_ADMIN - Usuario {} (no admin) intentó eliminar usuario {}",
                        usuarioAutenticado.getNombreUsuario(), usuarioAEliminar.getNombreUsuario());
                throw new UsuarioNoAutorizadoException(
                        "No está autorizado para eliminar usuarios. " +
                                "Solo los administradores pueden eliminar usuarios."
                );
            }
            log.info("✅ Admin eliminando usuario no-admin");
        }

        // 7. Advertencia sobre posibles FK constraints
        log.warn("⚠️ ADVERTENCIA - El usuario {} puede tener datos asociados (órdenes de venta, etc.).",
                usuarioAEliminar.getNombreUsuario());

        // 8. Intentar eliminar (manejar FK constraints)
        try {
            usuarioRepository.delete(usuarioAEliminar);
            log.info("✅ ELIMINACIÓN_EXITOSA - Usuario {} eliminado por {}",
                    usuarioAEliminar.getNombreUsuario(), usuarioAutenticado.getNombreUsuario());

        } catch (Exception e) {
            log.error("❌ ERROR_ELIMINACION_FK - No se puede eliminar usuario {}: {}",
                    usuarioAEliminar.getNombreUsuario(), e.getMessage());

            // Identificar si es error de FK
            if (e.getMessage().contains("violates foreign key constraint") ||
                    e.getMessage().contains("is still referenced")) {
                throw new UsuarioBusinessException(
                        "No se puede eliminar el usuario '" + usuarioAEliminar.getNombreUsuario() +
                                "' porque tiene datos asociados (órdenes de venta, etc.). " +
                                "Recomendación: Desactive el usuario usando el endpoint de cambio de estado " +
                                "(PATCH /usuarios/" + id + "/estado?estado=false)."
                );
            }

            // Otro tipo de error
            throw new UsuarioBusinessException("Error al eliminar usuario: " + e.getMessage());
        }

        log.info("=== FIN ELIMINACIÓN DE USUARIO ===");
    }

    @Override
    public List<UsuarioDto> obtenerUsuariosPorTipo(TipoUsuario tipoUsuario) {
        List<Usuario> usuarios = usuarioRepository.findByTipoUsuario(tipoUsuario);
        if (usuarios.isEmpty()) {
            throw new UsuarioNotFoundException("No se encontraron usuarios del tipo: " + tipoUsuario);
        }

        // Filtrar el admin principal de los resultados
        return usuarios.stream()
                .filter(usuario -> !esUsuarioAdminPrincipal(usuario))
                .map(usuarioMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<UsuarioDto> buscarUsuariosPorNombre(String nombre) {
        List<Usuario> usuarios = usuarioRepository.findByNombreContainingIgnoreCase(nombre);
        if (usuarios.isEmpty()) {
            throw new UsuarioNotFoundException("No se encontraron usuarios con el nombre: " + nombre);
        }

        // Filtrar el admin principal de los resultados
        return usuarios.stream()
                .filter(usuario -> !esUsuarioAdminPrincipal(usuario))
                .map(usuarioMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<UsuarioDto> buscarUsuariosPorCedula(String cedula) {
        // Si buscan la cédula del admin principal, retornar lista vacía
        if (cedula.equals(adminCedula)) {
            return Collections.emptyList();
        }

        List<Usuario> usuarios = usuarioRepository.findByCedulaContaining(cedula);
        if (usuarios.isEmpty()) {
            throw new UsuarioNotFoundException("No se encontraron usuarios con la cédula: " + cedula);
        }

        // Filtrar cualquier coincidencia con admin principal
        return usuarios.stream()
                .filter(usuario -> !esUsuarioAdminPrincipal(usuario))
                .map(usuarioMapper::toDtoSafe)
                .collect(Collectors.toList());
    }

    @Override
    public List<UsuarioDto> obtenerTodosLosUsuarios() {
        log.info("=== INICIANDO obtenerTodosLosUsuarios ===");

        try {
            log.debug("Buscando todos los usuarios en repository...");
            List<Usuario> usuariosEntities = usuarioRepository.findAll();
            log.info("Número de usuarios encontrados en BD: {}", usuariosEntities.size());

            if (usuariosEntities.isEmpty()) {
                log.warn("No se encontraron usuarios en la base de datos");
                return Collections.emptyList();
            }

            log.debug("Iniciando mapeo de entities a DTOs...");
            List<UsuarioDto> usuariosDto = usuariosEntities.stream()
                    // EXCLUIR AL USUARIO ADMIN PRINCIPAL
                    .filter(usuario -> !esUsuarioAdminPrincipal(usuario))
                    .map(usuario -> {
                        log.trace("Mapeando usuario ID: {}, Nombre: {}", usuario.getId(), usuario.getNombre());
                        log.trace("Contraseña en Entity: {}", usuario.getContrasena());

                        UsuarioDto dto = usuarioMapper.toDtoSafe(usuario);

                        log.trace("DTO mapeado - ID: {}, Contraseña en DTO: {}",
                                dto.getId(), dto.getContrasena());
                        log.trace("DTO completo: {}", dto.toString());

                        return dto;
                    })
                    .collect(Collectors.toList());

            log.info("Mapeo completado. Total DTOs generados: {}", usuariosDto.size());
            log.info("Usuario admin principal excluido de los resultados");

            // Log final de verificación
            usuariosDto.forEach(dto -> {
                log.debug("DTO final - ID: {}, Nombre: {}, Contraseña: {}",
                        dto.getId(), dto.getNombre(), dto.getContrasena());
            });

            log.info("=== FINALIZANDO obtenerTodosLosUsuarios ===");
            return usuariosDto;

        } catch (Exception e) {
            log.error("Error en obtenerTodosLosUsuarios: {}", e.getMessage(), e);
            throw new UsuarioBusinessException("Error al obtener la lista de usuarios: " + e.getMessage());
        }
    }

    @Override
    public List<UsuarioDto> obtenerUsuariosActivos() {
        return usuarioRepository.findByEstado(true).stream()
                // Excluir al admin principal de los resultados
                .filter(usuario -> !esUsuarioAdminPrincipal(usuario))
                .map(usuarioMapper::toDtoSafe)
                .collect(Collectors.toList());
    }

    @Override
    public List<UsuarioDto> obtenerUsuariosInactivos() {
        return usuarioRepository.findByEstado(false).stream()
                // Excluir al admin principal de los resultados
                .filter(usuario -> !esUsuarioAdminPrincipal(usuario))
                .map(usuarioMapper::toDtoSafe)
                .collect(Collectors.toList());
    }

    @Override
    public List<UsuarioDto> obtenerUsuariosPorFechaCreacion(LocalDateTime fechaInicio, LocalDateTime fechaFin) {
        List<Usuario> usuarios = usuarioRepository.findByFechaCreacionBetween(fechaInicio, fechaFin);
        if (usuarios.isEmpty()) {
            throw new UsuarioNotFoundException("No se encontraron usuarios en el rango de fechas especificado");
        }

        // Filtrar el admin principal de los resultados
        return usuarios.stream()
                .filter(usuario -> !esUsuarioAdminPrincipal(usuario))
                .map(usuarioMapper::toDtoSafe)
                .collect(Collectors.toList());
    }

    @Override
    public List<UsuarioDto> obtenerUsuariosCreadosDespuesDe(LocalDateTime fecha) {
        List<Usuario> usuarios = usuarioRepository.findByFechaCreacionAfter(fecha);
        if (usuarios.isEmpty()) {
            throw new UsuarioNotFoundException("No se encontraron usuarios creados después de: " + fecha);
        }

        // Filtrar el admin principal de los resultados
        return usuarios.stream()
                .filter(usuario -> !esUsuarioAdminPrincipal(usuario))
                .map(usuarioMapper::toDtoSafe)
                .collect(Collectors.toList());
    }

    @Override
    public List<UsuarioDto> obtenerUsuariosCreadosAntesDe(LocalDateTime fecha) {
        List<Usuario> usuarios = usuarioRepository.findByFechaCreacionBefore(fecha);
        if (usuarios.isEmpty()) {
            throw new UsuarioNotFoundException("No se encontraron usuarios creados antes de: " + fecha);
        }

        // Filtrar el admin principal de los resultados
        return usuarios.stream()
                .filter(usuario -> !esUsuarioAdminPrincipal(usuario))
                .map(usuarioMapper::toDtoSafe)
                .collect(Collectors.toList());
    }

    /**
     * Método auxiliar para verificar si un usuario es el administrador principal (del .env)
     */
    private boolean esUsuarioAdminPrincipal(Usuario usuario) {
        return usuario.getCedula().equals(adminCedula) &&
                usuario.getCorreo().equals(adminEmail) &&
                usuario.getNombreUsuario().equals(adminUsername);
    }

    /**
     * Método auxiliar para verificar si un usuario es el administrador de prueba
     */
    /**
     * Método auxiliar para verificar si un usuario es el administrador de prueba
     */
    private boolean esUsuarioAdminPrueba(Usuario usuario) {
        // Verificar por múltiples criterios (username y email son más confiables)
        boolean esPrueba = usuario.getNombreUsuario().equals(adminPruebaUsername)
                && usuario.getCorreo().equals(adminPruebaEmail);

        // Log para debug
        log.debug("Validando si es admin prueba - Username: {} (esperado: {}), Email: {} (esperado: {}), Resultado: {}",
                usuario.getNombreUsuario(), adminPruebaUsername,
                usuario.getCorreo(), adminPruebaEmail,
                esPrueba);

        return esPrueba;
    }

    /**
     * Método MEJORADO para obtener el usuario autenticado actualmente
     */
    private Usuario obtenerUsuarioAutenticado() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication == null) {
                log.error("❌ NO_HAY_AUTENTICACION - No hay información de autenticación en el contexto");
                throw new UsuarioBusinessException("No se pudo identificar al usuario autenticado");
            }

            // Verificar que esté autenticado
            if (!authentication.isAuthenticated()) {
                log.error("❌ USUARIO_NO_AUTENTICADO - El usuario no está autenticado");
                throw new UsuarioBusinessException("Usuario no autenticado");
            }

            String username;
            Object principal = authentication.getPrincipal();

            if (principal instanceof UserDetails) {
                username = ((UserDetails) principal).getUsername();
            } else if (principal instanceof String) {
                username = (String) principal;

                // Si es "anonymousUser" significa que no hay usuario autenticado
                if ("anonymousUser".equals(username)) {
                    log.error("❌ USUARIO_ANONIMO - Intento de acceso con usuario anónimo");
                    throw new UsuarioBusinessException("Acceso no autorizado - usuario anónimo");
                }
            } else {
                log.error("❌ TIPO_PRINCIPAL_DESCONOCIDO - Tipo de principal no reconocido: {}",
                        principal.getClass().getName());
                throw new UsuarioBusinessException("Error al identificar el usuario autenticado");
            }

            // Buscar usuario en base de datos
            Usuario usuario = usuarioRepository.findByNombreUsuario(username)
                    .orElseThrow(() -> {
                        log.error("❌ USUARIO_NO_ENCONTRADO - Usuario del token no encontrado en BD: {}", username);
                        return new UsuarioNotFoundException("Usuario autenticado no encontrado en el sistema");
                    });

            log.debug("✅ USUARIO_AUTENTICADO_IDENTIFICADO - Usuario autenticado: {} (ID: {})",
                    usuario.getNombreUsuario(), usuario.getId());

            return usuario;

        } catch (UsuarioNotFoundException | UsuarioBusinessException e) {
            // Relanzar excepciones específicas
            throw e;
        } catch (Exception e) {
            log.error("❌ ERROR_OBTENIENDO_USUARIO_AUTENTICADO - Error inesperado: {}", e.getMessage(), e);
            throw new UsuarioBusinessException("Error al obtener información del usuario autenticado: " + e.getMessage());
        }
    }

    public boolean esAdminConPermisosEliminacion() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication == null || !authentication.isAuthenticated()) {
                // Esto NO lanza AccessDeniedException de Spring
                throw new co.todotech.exception.security.CustomAccessDeniedException(
                        "Usuario no autenticado"
                );
            }

            String username = authentication.getName();

            // ✅ **BLOQUEO ESPECÍFICO PARA ADMIN PRUEBA**
            if (username.equals(adminPruebaUsername)) {
                log.warn("🚫 ADMIN PRUEBA BLOQUEADO - Usuario: {}", username);
                throw new co.todotech.exception.security.CustomAccessDeniedException(
                        "El administrador de prueba '" + username +
                                "' no tiene permisos para eliminar usuarios. " +
                                "Esta cuenta es solo para consultas y operaciones básicas. " +
                                "Use el endpoint de cambio de estado (/usuarios/{id}/estado) para desactivar usuarios."
                );
            }

            Usuario usuarioAutenticado = usuarioRepository.findByNombreUsuario(username)
                    .orElseThrow(() -> new UsuarioNotFoundException("Usuario no encontrado"));

            // ✅ **DOBLE VALIDACIÓN POR EMAIL**
            if (usuarioAutenticado.getCorreo().equals(adminPruebaEmail)) {
                throw new co.todotech.exception.security.CustomAccessDeniedException(
                        "Cuenta de administrador de prueba detectada. " +
                                "Operación de eliminación no permitida. " +
                                "Contacte al administrador principal si necesita eliminar un usuario."
                );
            }

            // Solo admins principales pueden eliminar
            boolean tienePermisos = usuarioAutenticado.getTipoUsuario() == TipoUsuario.ADMIN
                    && esUsuarioAdminPrincipal(usuarioAutenticado);

            if (!tienePermisos) {
                throw new co.todotech.exception.security.CustomAccessDeniedException(
                        "No tiene permisos para eliminar usuarios. " +
                                "Solo el administrador principal puede eliminar usuarios."
                );
            }

            return true;

        } catch (co.todotech.exception.security.CustomAccessDeniedException e) {
            // Relanzar para que GlobalExceptionHandler la capture
            throw e;
        } catch (UsuarioNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error validando permisos eliminación: {}", e.getMessage());
            throw new UsuarioBusinessException("Error al validar permisos: " + e.getMessage());
        }
    }

    /**
     * Método PÚBLICO para que Spring Security pueda usarlo en @PreAuthorize
     * Verifica si el usuario autenticado actual puede crear administradores
     */
    public boolean puedeCrearAdministradores() {
        try {
            Usuario usuarioAutenticado = obtenerUsuarioAutenticado();

            // Solo el admin principal puede crear otros administradores
            return esUsuarioAdminPrincipal(usuarioAutenticado);

        } catch (Exception e) {
            log.error("❌ ERROR_VALIDANDO_PERMISOS_CREACION_ADMIN - Error al validar permisos para crear administradores: {}", e.getMessage());
            return false;
        }
    }
}