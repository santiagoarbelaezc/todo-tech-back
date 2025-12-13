package co.todotech.exception.usuario;

public class UsuarioProtectedException extends RuntimeException {
    public UsuarioProtectedException(String message) {
        super(message);
    }

    public UsuarioProtectedException(String message, Throwable cause) {
        super(message, cause);
    }
}