package co.todotech.exception.usuario;

public class UsuarioNoAutorizadoException extends RuntimeException {
    public UsuarioNoAutorizadoException(String message) {
        super(message);
    }

    public UsuarioNoAutorizadoException(String message, Throwable cause) {
        super(message, cause);
    }
}