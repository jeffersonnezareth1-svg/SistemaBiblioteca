package util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Proporciona las conexiones JDBC hacia la base de datos {@code bd_biblioteca}.
 *
 * <p>Todos los DAO del sistema obtienen su conexión desde esta clase, de modo
 * que los datos de acceso (URL, usuario y contraseña) se configuran en un solo
 * lugar. Antes de ejecutar el sistema, cada integrante debe ajustar
 * {@link #USUARIO} y {@link #CONTRASENA} según su instalación de MySQL.</p>
 *
 * @author Guashpa Gualli Jefferson Nezareth
 * @version 1.0
 */
public final class Conexion {

    /** URL JDBC de la base de datos del sistema. */
    private static final String URL = "jdbc:mysql://localhost:3306/bd_biblioteca"
            + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=America/Guayaquil";

    /** Usuario de MySQL. */
    private static final String USUARIO = "root";

    /** Contraseña del usuario de MySQL (cambiarla según cada equipo). */
    private static final String CONTRASENA = "";

    /**
     * Constructor privado: la clase solo expone métodos estáticos.
     */
    private Conexion() {
    }

    /**
     * Abre una nueva conexión con la base de datos.
     *
     * <p>Quien la solicita es responsable de cerrarla, idealmente con
     * {@code try-with-resources}.</p>
     *
     * @return conexión abierta con {@code bd_biblioteca}
     * @throws SQLException si el servidor no responde o las credenciales son
     * incorrectas
     */
    public static Connection getConexion() throws SQLException {
        return DriverManager.getConnection(URL, USUARIO, CONTRASENA);
    }

    /**
     * Comprueba si es posible conectarse a la base de datos.
     *
     * @return {@code true} si la conexión se abrió correctamente
     */
    public static boolean probarConexion() {
        try (Connection cn = getConexion()) {
            return cn.isValid(2);
        } catch (SQLException e) {
            return false;
        }
    }
}
