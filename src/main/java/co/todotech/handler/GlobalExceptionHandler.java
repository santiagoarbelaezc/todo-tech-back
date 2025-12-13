package co.todotech.handler;

import co.todotech.exception.detalleorden.*;
import co.todotech.exception.ordenventa.OrdenNotFoundException;
import co.todotech.exception.producto.ProductoBusinessException;
import co.todotech.exception.producto.ProductoDuplicateException;
import co.todotech.exception.producto.ProductoNotFoundException;
import co.todotech.exception.security.CustomAccessDeniedException;
import co.todotech.exception.usuario.*;
import co.todotech.model.dto.MensajeDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ProductoNotFoundException.class)
    public ResponseEntity<MensajeDto<?>> handleProductoNotFoundException(ProductoNotFoundException ex) {
        log.warn("Producto no encontrado: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new MensajeDto<>(true, ex.getMessage()));
    }

    @ExceptionHandler(ProductoDuplicateException.class)
    public ResponseEntity<MensajeDto<?>> handleProductoDuplicateException(ProductoDuplicateException ex) {
        log.warn("Intento de duplicado: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new MensajeDto<>(true, ex.getMessage()));
    }

    @ExceptionHandler(ProductoBusinessException.class)
    public ResponseEntity<MensajeDto<?>> handleProductoBusinessException(ProductoBusinessException ex) {
        log.warn("Error de negocio: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new MensajeDto<>(true, ex.getMessage()));
    }

    // Manejadores para DetalleOrden
    @ExceptionHandler(DetalleOrdenNotFoundException.class)
    public ResponseEntity<MensajeDto<?>> handleDetalleOrdenNotFoundException(DetalleOrdenNotFoundException ex) {
        log.warn("Detalle de orden no encontrado: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new MensajeDto<>(true, ex.getMessage()));
    }

    @ExceptionHandler(DetallesNoEncontradosException.class)
    public ResponseEntity<MensajeDto<?>> handleDetallesNoEncontradosException(DetallesNoEncontradosException ex) {
        log.warn("Detalles no encontrados: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new MensajeDto<>(true, ex.getMessage()));
    }

    @ExceptionHandler(DetalleOrdenDuplicateException.class)
    public ResponseEntity<MensajeDto<?>> handleDetalleOrdenDuplicateException(DetalleOrdenDuplicateException ex) {
        log.warn("Intento de duplicado en detalle de orden: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new MensajeDto<>(true, ex.getMessage()));
    }

    @ExceptionHandler(DetalleOrdenBusinessException.class)
    public ResponseEntity<MensajeDto<?>> handleDetalleOrdenBusinessException(DetalleOrdenBusinessException ex) {
        log.warn("Error de negocio en detalle de orden: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new MensajeDto<>(true, ex.getMessage()));
    }

    @ExceptionHandler(DetalleOrdenEstadoException.class)
    public ResponseEntity<MensajeDto<?>> handleDetalleOrdenEstadoException(DetalleOrdenEstadoException ex) {
        log.warn("Error de estado en detalle de orden: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new MensajeDto<>(true, ex.getMessage()));
    }

    // 🔥 NUEVOS: Manejadores para excepciones de stock
    @ExceptionHandler(StockInsufficientException.class)
    public ResponseEntity<MensajeDto<?>> handleStockInsufficientException(StockInsufficientException ex) {
        log.warn("Stock insuficiente: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new MensajeDto<>(true, ex.getMessage()));
    }

    @ExceptionHandler(StockCriticalException.class)
    public ResponseEntity<MensajeDto<?>> handleStockCriticalException(StockCriticalException ex) {
        log.warn("Stock crítico: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new MensajeDto<>(true, ex.getMessage()));
    }

    @ExceptionHandler(ProductoNoDisponibleException.class)
    public ResponseEntity<MensajeDto<?>> handleProductoNoDisponibleException(ProductoNoDisponibleException ex) {
        log.warn("Producto no disponible: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new MensajeDto<>(true, ex.getMessage()));
    }

    // Manejador para Orden (necesario para las validaciones)
    @ExceptionHandler(OrdenNotFoundException.class)
    public ResponseEntity<MensajeDto<?>> handleOrdenNotFoundException(OrdenNotFoundException ex) {
        log.warn("Orden no encontrada: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new MensajeDto<>(true, ex.getMessage()));
    }

    // Manejadores para Usuario
    @ExceptionHandler(UsuarioNotFoundException.class)
    public ResponseEntity<MensajeDto<?>> handleUsuarioNotFoundException(UsuarioNotFoundException ex) {
        log.warn("Usuario no encontrado: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new MensajeDto<>(true, ex.getMessage()));
    }

    @ExceptionHandler(UsuarioDuplicateException.class)
    public ResponseEntity<MensajeDto<?>> handleUsuarioDuplicateException(UsuarioDuplicateException ex) {
        log.warn("Intento de duplicado de usuario: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new MensajeDto<>(true, ex.getMessage()));
    }

    @ExceptionHandler(UsuarioBusinessException.class)
    public ResponseEntity<MensajeDto<?>> handleUsuarioBusinessException(UsuarioBusinessException ex) {
        log.warn("Error de negocio en usuario: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new MensajeDto<>(true, ex.getMessage()));
    }

    @ExceptionHandler(UsuarioEstadoException.class)
    public ResponseEntity<MensajeDto<?>> handleUsuarioEstadoException(UsuarioEstadoException ex) {
        log.warn("Error de estado en usuario: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new MensajeDto<>(true, ex.getMessage()));
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<MensajeDto<?>> handleAuthenticationException(AuthenticationException ex) {
        log.warn("Error de autenticación: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new MensajeDto<>(true, ex.getMessage()));
    }

    @ExceptionHandler(EmailException.class)
    public ResponseEntity<MensajeDto<?>> handleEmailException(EmailException ex) {
        log.warn("Error en envío de email: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new MensajeDto<>(true, ex.getMessage()));
    }

    // 🔥🔥🔥 Manejadores CORREGIDOS - TODOS con BAD_REQUEST (400) 🔥🔥🔥

    @ExceptionHandler(UsuarioNoAutorizadoException.class)
    public ResponseEntity<MensajeDto<?>> handleUsuarioNoAutorizadoException(UsuarioNoAutorizadoException ex) {
        log.warn("Acceso no autorizado: {}", ex.getMessage());
        // 🔴 CAMBIADO de FORBIDDEN (403) a BAD_REQUEST (400)
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new MensajeDto<>(true, ex.getMessage()));
    }

    @ExceptionHandler(UsuarioProtectedException.class)
    public ResponseEntity<MensajeDto<?>> handleUsuarioProtectedException(UsuarioProtectedException ex) {
        log.warn("Usuario protegido: {}", ex.getMessage());
        // 🔴 CAMBIADO de FORBIDDEN (403) a BAD_REQUEST (400)
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new MensajeDto<>(true, ex.getMessage()));
    }

    @ExceptionHandler(CustomAccessDeniedException.class)
    public ResponseEntity<MensajeDto<?>> handleCustomAccessDeniedException(CustomAccessDeniedException ex) {
        // Obtener usuario actual para log
        String username = "usuario desconocido";
        try {
            org.springframework.security.core.Authentication auth =
                    org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated()) {
                username = auth.getName();
            }
        } catch (Exception e) {
            // Ignorar
        }

        log.warn("Acceso denegado personalizado para usuario '{}': {}", username, ex.getMessage());

        // 🔴 Usar BAD_REQUEST (400) para evitar que Spring Security desloguee
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new MensajeDto<>(true, ex.getMessage()));
    }

    @ExceptionHandler(org.springframework.security.access.AccessDeniedException.class)
    public ResponseEntity<MensajeDto<?>> handleSpringAccessDeniedException(
            org.springframework.security.access.AccessDeniedException ex) {

        String username = "usuario desconocido";
        try {
            org.springframework.security.core.Authentication auth =
                    org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated()) {
                username = auth.getName();
            }
        } catch (Exception e) {
            // Ignorar si no se puede obtener
        }

        String mensaje;
        if ("adminprueba".equals(username)) {
            mensaje = "El administrador de prueba 'adminprueba' no tiene permisos para esta acción. " +
                    "Esta cuenta es solo para consultas y operaciones básicas.";
        } else {
            mensaje = "Acceso denegado. No tiene permisos para realizar esta operación.";
        }

        log.warn("Acceso denegado para usuario '{}': {}", username, ex.getMessage());

        // 🔴 CAMBIADO de FORBIDDEN (403) a BAD_REQUEST (400)
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new MensajeDto<>(true, mensaje));
    }

    @ExceptionHandler(org.springframework.security.authorization.AuthorizationDeniedException.class)
    public ResponseEntity<MensajeDto<?>> handleAuthorizationDeniedException(
            org.springframework.security.authorization.AuthorizationDeniedException ex) {

        String username = "usuario desconocido";
        try {
            org.springframework.security.core.Authentication auth =
                    org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated()) {
                username = auth.getName();
            }
        } catch (Exception e) {
            // Ignorar
        }

        String mensaje;
        if ("adminprueba".equals(username)) {
            mensaje = "El administrador de prueba no puede eliminar usuarios. " +
                    "Use la opción de 'Desactivar usuario' en lugar de 'Eliminar'.";
        } else {
            mensaje = "Acceso denegado por falta de permisos.";
        }

        log.warn("Autorización denegada para usuario '{}': {}", username, ex.getMessage());

        // 🔴 Ya está como BAD_REQUEST (400) - Mantener
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new MensajeDto<>(true, mensaje));
    }

    @ExceptionHandler(org.springframework.security.core.AuthenticationException.class)
    public ResponseEntity<MensajeDto<?>> handleSpringAuthenticationException(
            org.springframework.security.core.AuthenticationException ex) {
        log.warn("Error de autenticación Spring Security: {}", ex.getMessage());
        // ✅ AuthenticationException debe mantener UNAUTHORIZED (401)
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new MensajeDto<>(true, "Error de autenticación: " + ex.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<MensajeDto<?>> handleGenericException(Exception ex) {
        log.error("Error inesperado: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new MensajeDto<>(true, "Error interno del servidor"));
    }
}