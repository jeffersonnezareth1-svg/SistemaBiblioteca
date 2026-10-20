package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import modelo.Usuario;
import util.Conexion;

/**
 * Acceso a la tabla {@code usuario}. Solo realiza lecturas para el login.
 *
 * @author Guashpa Gualli Jefferson Nezareth
 * @version 1.0
 */
public class UsuarioDAO {

    /**
     * Crea el DAO de usuarios.
     */
    public UsuarioDAO() {
    }

    private static final String SQL_AUTENTICAR =
            "SELECT id_usuario, nombre_usuario, nombres, rol, activo "
            + "FROM usuario WHERE nombre_usuario = ? AND contrasena = ?";

    /**
     * Busca un usuario cuyo nombre y hash de contraseña coincidan.
     *
     * @param nombreUsuario nombre de usuario escrito en el login
     * @param hashContrasena contraseña convertida a SHA-256
     * @return el usuario encontrado (activo o no), o {@code null} si las
     * credenciales no coinciden
     * @throws SQLException si ocurre un error con la base de datos
     */
    public Usuario autenticar(String nombreUsuario, String hashContrasena) throws SQLException {
        try (Connection cn = Conexion.getConexion();
                PreparedStatement ps = cn.prepareStatement(SQL_AUTENTICAR)) {
            ps.setString(1, nombreUsuario);
            ps.setString(2, hashContrasena);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Usuario(
                            rs.getInt("id_usuario"),
                            rs.getString("nombre_usuario"),
                            rs.getString("nombres"),
                            rs.getString("rol"),
                            rs.getBoolean("activo"));
                }
                return null;
            }
        }
    }
}
