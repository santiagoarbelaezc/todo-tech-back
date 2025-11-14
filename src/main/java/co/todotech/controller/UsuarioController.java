package co.todotech.controller;

import co.todotech.model.dto.MensajeDto;
import co.todotech.model.dto.usuario.LoginResponse;
import co.todotech.model.dto.usuario.UsuarioDto;
import co.todotech.model.enums.TipoUsuario;
import co.todotech.security.JwtUtil;
import co.todotech.security.TokenBlacklistService;
import co.todotech.service.UsuarioService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;
    private final TokenBlacklistService tokenBlacklistService;
    private final JwtUtil jwtUtil;

    // Login - PÚBLICO
    @PostMapping("/login")
    public ResponseEntity<MensajeDto<LoginResponse>> login(
            @RequestParam("nombreUsuario") String nombreUsuario,
            @RequestParam("contrasena") String contrasena) {
        try {
            log.info("🔐 USER_LOGIN - Iniciando login para usuario: {}", nombreUsuario);

            LoginResponse loginResponse = usuarioService.login(nombreUsuario, contrasena);

            log.info("✅ USER_LOGIN_SUCCESS - Login exitoso para usuario: {}, tipo: {}",
                    nombreUsuario, loginResponse.getNombre());

            return ResponseEntity.ok(new MensajeDto<>(false, "Login exitoso", loginResponse));
        } catch (Exception e) {
            log.error("❌ USER_LOGIN_ERROR - Error en login para usuario: {}, error: {}",
                    nombreUsuario, e.getMessage());
            return ResponseEntity.badRequest().body(new MensajeDto<>(true, e.getMessage(), null));
        }
    }

    // Crear usuario - SOLO ADMIN
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MensajeDto<String>> crearUsuario(@RequestBody UsuarioDto dto) {
        try {
            log.info("👤 USER_CREATE - Iniciando creación de usuario: {}", dto.getNombreUsuario());

            usuarioService.crearUsuario(dto);

            log.info("✅ USER_CREATE_SUCCESS - Usuario creado exitosamente: {}", dto.getNombreUsuario());

            return ResponseEntity.ok(new MensajeDto<>(false, "Usuario creado exitosamente"));
        } catch (Exception e) {
            log.error("❌ USER_CREATE_ERROR - Error creando usuario: {}, error: {}",
                    dto.getNombreUsuario(), e.getMessage());
            return ResponseEntity.badRequest().body(new MensajeDto<>(true, e.getMessage()));
        }
    }

    // Obtener todos los usuarios - SOLO ADMIN
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MensajeDto<List<UsuarioDto>>> obtenerTodosLosUsuarios() {
        try {
            log.info("🔍 USER_QUERY - Consultando todos los usuarios");

            List<UsuarioDto> usuarios = usuarioService.obtenerTodosLosUsuarios();

            log.info("✅ USER_QUERY_SUCCESS - Todos los usuarios obtenidos: cantidad={}", usuarios.size());

            return ResponseEntity.ok(new MensajeDto<>(false, "Usuarios obtenidos exitosamente", usuarios));
        } catch (Exception e) {
            log.error("❌ USER_QUERY_ERROR - Error obteniendo todos los usuarios: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new MensajeDto<>(true, e.getMessage(), null));
        }
    }

    // Obtener usuarios activos - SOLO ADMIN
    @GetMapping("/activos")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MensajeDto<List<UsuarioDto>>> obtenerUsuariosActivos() {
        try {
            log.info("🔍 USER_QUERY - Consultando usuarios activos");

            List<UsuarioDto> usuarios = usuarioService.obtenerUsuariosActivos();

            log.info("✅ USER_QUERY_SUCCESS - Usuarios activos obtenidos: cantidad={}", usuarios.size());

            return ResponseEntity.ok(new MensajeDto<>(false, "Usuarios activos obtenidos exitosamente", usuarios));
        } catch (Exception e) {
            log.error("❌ USER_QUERY_ERROR - Error obteniendo usuarios activos: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new MensajeDto<>(true, e.getMessage(), null));
        }
    }

    // Obtener usuarios inactivos - SOLO ADMIN
    @GetMapping("/inactivos")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MensajeDto<List<UsuarioDto>>> obtenerUsuariosInactivos() {
        try {
            log.info("🔍 USER_QUERY - Consultando usuarios inactivos");

            List<UsuarioDto> usuarios = usuarioService.obtenerUsuariosInactivos();

            log.info("✅ USER_QUERY_SUCCESS - Usuarios inactivos obtenidos: cantidad={}", usuarios.size());

            return ResponseEntity.ok(new MensajeDto<>(false, "Usuarios inactivos obtenidos exitosamente", usuarios));
        } catch (Exception e) {
            log.error("❌ USER_QUERY_ERROR - Error obteniendo usuarios inactivos: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new MensajeDto<>(true, e.getMessage(), null));
        }
    }

    // Cambiar estado de usuario - SOLO ADMIN
    @PatchMapping("/{id}/estado")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MensajeDto<String>> cambiarEstadoUsuario(
            @PathVariable("id") Long id,
            @RequestParam("estado") boolean estado) {
        try {
            log.info("🔄 USER_STATUS_UPDATE - Cambiando estado de usuario ID: {} a {}", id, estado);

            usuarioService.cambiarEstadoUsuario(id, estado);

            String mensaje = estado ? "Usuario activado exitosamente" : "Usuario desactivado exitosamente";

            log.info("✅ USER_STATUS_UPDATE_SUCCESS - Estado cambiado exitosamente: id={}, nuevoEstado={}",
                    id, estado);

            return ResponseEntity.ok(new MensajeDto<>(false, mensaje));
        } catch (Exception e) {
            log.error("❌ USER_STATUS_UPDATE_ERROR - Error cambiando estado usuario ID: {}, error: {}",
                    id, e.getMessage());
            return ResponseEntity.badRequest().body(new MensajeDto<>(true, e.getMessage()));
        }
    }

    // Obtener usuario por ID - AUTENTICADO (propio usuario o admin)
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<MensajeDto<UsuarioDto>> obtenerUsuarioPorId(@PathVariable("id") Long id) {
        try {
            log.info("🔍 USER_QUERY - Consultando usuario por ID: {}", id);

            UsuarioDto usuarioDto = usuarioService.obtenerUsuarioPorId(id);

            log.info("✅ USER_QUERY_SUCCESS - Usuario encontrado: id={}, nombre={}",
                    id, usuarioDto.getNombreUsuario());

            return ResponseEntity.ok(new MensajeDto<>(false, "Usuario encontrado", usuarioDto));
        } catch (Exception e) {
            log.error("❌ USER_QUERY_ERROR - Error consultando usuario ID: {}, error: {}", id, e.getMessage());
            return ResponseEntity.badRequest().body(new MensajeDto<>(true, e.getMessage(), null));
        }
    }

    // Actualizar usuario - AUTENTICADO (propio usuario o admin)
    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<MensajeDto<String>> actualizarUsuario(
            @PathVariable("id") Long id,
            @RequestBody UsuarioDto dto) {
        try {
            log.info("👤 USER_UPDATE - Iniciando actualización de usuario ID: {}", id);

            usuarioService.actualizarUsuario(id, dto);

            log.info("✅ USER_UPDATE_SUCCESS - Usuario actualizado exitosamente: id={}", id);

            return ResponseEntity.ok(new MensajeDto<>(false, "Usuario actualizado exitosamente"));
        } catch (Exception e) {
            log.error("❌ USER_UPDATE_ERROR - Error actualizando usuario ID: {}, error: {}", id, e.getMessage());
            return ResponseEntity.badRequest().body(new MensajeDto<>(true, e.getMessage()));
        }
    }

    // Eliminar usuario - SOLO ADMIN
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MensajeDto<String>> eliminarUsuario(@PathVariable("id") Long id) {
        try {
            log.info("👤 USER_DELETE - Iniciando eliminación de usuario ID: {}", id);

            usuarioService.eliminarUsuario(id);

            log.info("✅ USER_DELETE_SUCCESS - Usuario eliminado exitosamente: id={}", id);

            return ResponseEntity.ok(new MensajeDto<>(false, "Usuario eliminado exitosamente"));
        } catch (Exception e) {
            log.error("❌ USER_DELETE_ERROR - Error eliminando usuario ID: {}, error: {}", id, e.getMessage());
            return ResponseEntity.badRequest().body(new MensajeDto<>(true, e.getMessage()));
        }
    }

    // Obtener usuarios por tipo - SOLO ADMIN
    @GetMapping("/tipo/{tipoUsuario}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MensajeDto<List<UsuarioDto>>> obtenerUsuariosPorTipo(
            @PathVariable("tipoUsuario") TipoUsuario tipoUsuario) {
        try {
            log.info("🔍 USER_QUERY - Consultando usuarios por tipo: {}", tipoUsuario);

            List<UsuarioDto> usuarios = usuarioService.obtenerUsuariosPorTipo(tipoUsuario);

            log.info("✅ USER_QUERY_SUCCESS - Usuarios por tipo encontrados: tipo={}, cantidad={}",
                    tipoUsuario, usuarios.size());

            return ResponseEntity.ok(new MensajeDto<>(false,
                    "Usuarios del tipo " + tipoUsuario + " obtenidos exitosamente", usuarios));
        } catch (Exception e) {
            log.error("❌ USER_QUERY_ERROR - Error consultando usuarios por tipo: {}, error: {}",
                    tipoUsuario, e.getMessage());
            return ResponseEntity.badRequest().body(new MensajeDto<>(true, e.getMessage(), null));
        }
    }

    // Buscar usuarios por nombre - SOLO ADMIN
    @GetMapping("/buscar/nombre")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MensajeDto<List<UsuarioDto>>> buscarUsuariosPorNombre(
            @RequestParam("nombre") String nombre) {
        try {
            log.info("🔍 USER_SEARCH - Buscando usuarios por nombre: {}", nombre);

            List<UsuarioDto> usuarios = usuarioService.buscarUsuariosPorNombre(nombre);

            log.info("✅ USER_SEARCH_SUCCESS - Usuarios encontrados por nombre: nombre={}, cantidad={}",
                    nombre, usuarios.size());

            return ResponseEntity.ok(new MensajeDto<>(false,
                    "Usuarios encontrados con el nombre: " + nombre, usuarios));
        } catch (Exception e) {
            log.error("❌ USER_SEARCH_ERROR - Error buscando usuarios por nombre: {}, error: {}",
                    nombre, e.getMessage());
            return ResponseEntity.badRequest().body(new MensajeDto<>(true, e.getMessage(), null));
        }
    }

    // Buscar usuarios por cédula - SOLO ADMIN
    @GetMapping("/buscar/cedula")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MensajeDto<List<UsuarioDto>>> buscarUsuariosPorCedula(
            @RequestParam("cedula") String cedula) {
        try {
            log.info("🔍 USER_SEARCH - Buscando usuarios por cédula: {}", cedula);

            List<UsuarioDto> usuarios = usuarioService.buscarUsuariosPorCedula(cedula);

            log.info("✅ USER_SEARCH_SUCCESS - Usuarios encontrados por cédula: cedula={}, cantidad={}",
                    cedula, usuarios.size());

            return ResponseEntity.ok(new MensajeDto<>(false,
                    "Usuarios encontrados con la cédula: " + cedula, usuarios));
        } catch (Exception e) {
            log.error("❌ USER_SEARCH_ERROR - Error buscando usuarios por cédula: {}, error: {}",
                    cedula, e.getMessage());
            return ResponseEntity.badRequest().body(new MensajeDto<>(true, e.getMessage(), null));
        }
    }

    // Obtener usuarios por rango de fechas - SOLO ADMIN
    @GetMapping("/fecha-creacion")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MensajeDto<List<UsuarioDto>>> obtenerUsuariosPorFechaCreacion(
            @RequestParam("fechaInicio") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaInicio,
            @RequestParam("fechaFin") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaFin) {
        try {
            log.info("🔍 USER_QUERY - Consultando usuarios por rango de fechas: {} a {}", fechaInicio, fechaFin);

            List<UsuarioDto> usuarios = usuarioService.obtenerUsuariosPorFechaCreacion(fechaInicio, fechaFin);

            log.info("✅ USER_QUERY_SUCCESS - Usuarios encontrados en rango: inicio={}, fin={}, cantidad={}",
                    fechaInicio, fechaFin, usuarios.size());

            return ResponseEntity.ok(new MensajeDto<>(false,
                    "Usuarios encontrados en el rango de fechas", usuarios));
        } catch (Exception e) {
            log.error("❌ USER_QUERY_ERROR - Error consultando usuarios por rango: {}-{}, error: {}",
                    fechaInicio, fechaFin, e.getMessage());
            return ResponseEntity.badRequest().body(new MensajeDto<>(true, e.getMessage(), null));
        }
    }

    // Obtener usuarios creados después de una fecha - SOLO ADMIN
    @GetMapping("/fecha-creacion/despues")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MensajeDto<List<UsuarioDto>>> obtenerUsuariosCreadosDespuesDe(
            @RequestParam("fecha") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fecha) {
        try {
            log.info("🔍 USER_QUERY - Consultando usuarios creados después de: {}", fecha);

            List<UsuarioDto> usuarios = usuarioService.obtenerUsuariosCreadosDespuesDe(fecha);

            log.info("✅ USER_QUERY_SUCCESS - Usuarios creados después de fecha: fecha={}, cantidad={}",
                    fecha, usuarios.size());

            return ResponseEntity.ok(new MensajeDto<>(false,
                    "Usuarios creados después de " + fecha, usuarios));
        } catch (Exception e) {
            log.error("❌ USER_QUERY_ERROR - Error consultando usuarios después de fecha: {}, error: {}",
                    fecha, e.getMessage());
            return ResponseEntity.badRequest().body(new MensajeDto<>(true, e.getMessage(), null));
        }
    }

    // Obtener usuarios creados antes de una fecha - SOLO ADMIN
    @GetMapping("/fecha-creacion/antes")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MensajeDto<List<UsuarioDto>>> obtenerUsuariosCreadosAntesDe(
            @RequestParam("fecha") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fecha) {
        try {
            log.info("🔍 USER_QUERY - Consultando usuarios creados antes de: {}", fecha);

            List<UsuarioDto> usuarios = usuarioService.obtenerUsuariosCreadosAntesDe(fecha);

            log.info("✅ USER_QUERY_SUCCESS - Usuarios creados antes de fecha: fecha={}, cantidad={}",
                    fecha, usuarios.size());

            return ResponseEntity.ok(new MensajeDto<>(false,
                    "Usuarios creados antes de " + fecha, usuarios));
        } catch (Exception e) {
            log.error("❌ USER_QUERY_ERROR - Error consultando usuarios antes de fecha: {}, error: {}",
                    fecha, e.getMessage());
            return ResponseEntity.badRequest().body(new MensajeDto<>(true, e.getMessage(), null));
        }
    }

    // Recordatorio de contraseña - PÚBLICO
    @PostMapping("/recordar-contrasena")
    public ResponseEntity<MensajeDto<String>> solicitarRecordatorioContrasena(
            @RequestParam("correo") String correo) {
        try {
            log.info("📧 PASSWORD_REMINDER - Solicitando recordatorio de contraseña para correo: {}", correo);

            usuarioService.solicitarRecordatorioContrasena(correo);

            log.info("✅ PASSWORD_REMINDER_SUCCESS - Recordatorio enviado exitosamente a: {}", correo);

            return ResponseEntity.ok(new MensajeDto<>(false,
                    "Se ha enviado un recordatorio de contraseña a tu correo electrónico"));
        } catch (Exception e) {
            log.error("❌ PASSWORD_REMINDER_ERROR - Error enviando recordatorio a: {}, error: {}",
                    correo, e.getMessage());
            return ResponseEntity.badRequest().body(new MensajeDto<>(true, e.getMessage()));
        }
    }

    // Logout - AUTENTICADO
    @PostMapping("/logout")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<MensajeDto<String>> logout(HttpServletRequest request) {
        try {
            log.info("🚪 USER_LOGOUT - Iniciando proceso de logout");

            String header = request.getHeader("Authorization");

            if (header != null && header.startsWith("Bearer ")) {
                String token = header.substring(7);

                // Validar que el token sea válido antes de blacklistear
                if (!jwtUtil.validateToken(token)) {
                    log.warn("⚠️ USER_LOGOUT_WARN - Token inválido o expirado");
                    return ResponseEntity.badRequest()
                            .body(new MensajeDto<>(true, "Token inválido o expirado"));
                }

                // Verificar que el token no esté ya blacklisteado
                if (tokenBlacklistService.isTokenBlacklisted(token)) {
                    log.info("ℹ️ USER_LOGOUT_INFO - Sesión ya estaba cerrada previamente");
                    return ResponseEntity.ok(new MensajeDto<>(false, "Sesión ya estaba cerrada"));
                }

                tokenBlacklistService.blacklistToken(token);

                // Limpiar el contexto de seguridad
                SecurityContextHolder.clearContext();

                log.info("✅ USER_LOGOUT_SUCCESS - Sesión cerrada exitosamente");

                return ResponseEntity.ok(new MensajeDto<>(false, "Sesión cerrada exitosamente"));
            } else {
                log.warn("⚠️ USER_LOGOUT_WARN - Token no proporcionado en formato Bearer");
                return ResponseEntity.badRequest()
                        .body(new MensajeDto<>(true, "Token no proporcionado en formato Bearer"));
            }
        } catch (Exception e) {
            log.error("❌ USER_LOGOUT_ERROR - Error al cerrar sesión: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(new MensajeDto<>(true, "Error al cerrar sesión: " + e.getMessage()));
        }
    }
}