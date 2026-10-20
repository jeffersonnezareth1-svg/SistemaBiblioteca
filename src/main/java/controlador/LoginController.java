package controlador;

import dao.UsuarioDAO;
import java.sql.SQLException;
import modelo.Usuario;
import util.Respuesta;
import util.Seguridad;
import util.Sesion;

/**
 * Controlador del inicio de sesión.
 *
 * <p>Valida los datos escritos en el login, consulta la tabla {@code usuario}
 * mediante {@link UsuarioDAO} y, si las credenciales son correctas y el
 * usuario está activo, registra la sesión en {@link Sesion}.</p>
 *
 * @author Guashpa Gualli Jefferson Nezareth
 * @version 1.0
 */
public class LoginController {

    /** Nombre del campo usuario, para enfocar el error en la vista. */
    public static final String CAMPO_USUARIO = "usuario";
    /** Nombre del campo contraseña, para enfocar el error en la vista. */
    public static final String CAMPO_CONTRASENA = "contrasena";

    private final UsuarioDAO dao;

    /**
     * Crea el controlador con el DAO real.
     */
    public LoginController() {
        this(new UsuarioDAO());
    }

    /**
     * Crea el controlador con un DAO dado (útil en pruebas unitarias).
     *
     * @param dao DAO de usuarios
     */
    public LoginController(UsuarioDAO dao) {
        this.dao = dao;
    }

    /**
     * Intenta iniciar sesión con las credenciales dadas.
     *
     * @param nombreUsuario nombre de usuario escrito
     * @param contrasena contraseña escrita
     * @return respuesta exitosa con mensaje de bienvenida, o el error
     * correspondiente
     */
    public Respuesta iniciarSesion(String nombreUsuario, String contrasena) {
        String error = validarUsuario(nombreUsuario);
        if (error != null) {
            return Respuesta.error(error, CAMPO_USUARIO);
        }
        error = validarContrasena(contrasena);
        if (error != null) {
            return Respuesta.error(error, CAMPO_CONTRASENA);
        }
        try {
            Usuario usuario = dao.autenticar(nombreUsuario.trim(), Seguridad.sha256(contrasena));
            if (usuario == null) {
                return Respuesta.error("Usuario o contraseña incorrectos", CAMPO_CONTRASENA);
            }
            if (!usuario.isActivo()) {
                return Respuesta.error("El usuario está inactivo. Contacte al administrador");
            }
            Sesion.iniciar(usuario);
            return Respuesta.ok("Bienvenido, " + usuario.getNombres());
        } catch (SQLException e) {
            return Respuesta.error("No se pudo conectar con la base de datos: " + e.getMessage());
        }
    }

    /**
     * Valida el nombre de usuario.
     *
     * @param nombreUsuario texto escrito
     * @return mensaje de error o {@code null} si es válido
     */
    public String validarUsuario(String nombreUsuario) {
        if (nombreUsuario == null || nombreUsuario.isBlank()) {
            return "El usuario es obligatorio";
        }
        if (nombreUsuario.trim().contains(" ")) {
            return "El usuario no debe contener espacios";
        }
        if (nombreUsuario.trim().length() > 30) {
            return "El usuario no puede superar 30 caracteres";
        }
        return null;
    }

    /**
     * Valida la contraseña.
     *
     * @param contrasena texto escrito
     * @return mensaje de error o {@code null} si es válida
     */
    public String validarContrasena(String contrasena) {
        if (contrasena == null || contrasena.isEmpty()) {
            return "La contraseña es obligatoria";
        }
        if (contrasena.length() < 6) {
            return "La contraseña debe tener al menos 6 caracteres";
        }
        return null;
    }

    /**
     * Cierra la sesión del usuario actual.
     */
    public void cerrarSesion() {
        Sesion.cerrar();
    }
}
