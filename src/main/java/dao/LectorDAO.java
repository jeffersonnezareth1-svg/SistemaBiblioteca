package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import modelo.Lector;
import util.Conexion;

/**
 * Acceso a datos de la tabla {@code lector}.
 *
 * <p>Contiene únicamente sentencias SQL; las validaciones están en
 * {@link controlador.LectorController}.</p>
 *
 * @author Guashpa Gualli Jefferson Nezareth
 * @version 1.0
 */
public class LectorDAO {

    private static final String COLUMNAS =
            "id_lector, cedula, nombres, apellidos, correo, telefono, direccion, estado";

    private static final String SQL_LISTAR =
            "SELECT " + COLUMNAS + " FROM lector ORDER BY apellidos, nombres";

    private static final String SQL_LISTAR_ACTIVOS =
            "SELECT " + COLUMNAS + " FROM lector WHERE estado = 'ACTIVO' ORDER BY apellidos, nombres";

    private static final String SQL_BUSCAR =
            "SELECT " + COLUMNAS + " FROM lector "
            + "WHERE cedula LIKE ? OR nombres LIKE ? OR apellidos LIKE ? OR correo LIKE ? "
            + "ORDER BY apellidos, nombres";

    private static final String SQL_OBTENER =
            "SELECT " + COLUMNAS + " FROM lector WHERE id_lector = ?";

    private static final String SQL_INSERTAR =
            "INSERT INTO lector (cedula, nombres, apellidos, correo, telefono, direccion, estado) "
            + "VALUES (?, ?, ?, ?, ?, ?, ?)";

    private static final String SQL_ACTUALIZAR =
            "UPDATE lector SET cedula = ?, nombres = ?, apellidos = ?, correo = ?, telefono = ?, "
            + "direccion = ?, estado = ? WHERE id_lector = ?";

    private static final String SQL_ELIMINAR =
            "DELETE FROM lector WHERE id_lector = ?";

    private static final String SQL_EXISTE_CEDULA =
            "SELECT COUNT(*) FROM lector WHERE cedula = ? AND id_lector <> ?";

    private static final String SQL_EXISTE_CORREO =
            "SELECT COUNT(*) FROM lector WHERE LOWER(correo) = LOWER(?) AND id_lector <> ?";

    private static final String SQL_TIENE_PRESTAMOS =
            "SELECT COUNT(*) FROM prestamo WHERE id_lector = ?";

    /**
     * Crea el DAO de lectores.
     */
    public LectorDAO() {
    }

    /**
     * Lista todos los lectores ordenados por apellido.
     *
     * @return lista de lectores
     * @throws SQLException si ocurre un error con la base de datos
     */
    public List<Lector> listar() throws SQLException {
        return consultarLista(SQL_LISTAR);
    }

    /**
     * Lista solo los lectores en estado ACTIVO (los que pueden pedir
     * préstamos).
     *
     * @return lista de lectores activos
     * @throws SQLException si ocurre un error con la base de datos
     */
    public List<Lector> listarActivos() throws SQLException {
        return consultarLista(SQL_LISTAR_ACTIVOS);
    }

    /**
     * Busca lectores por cédula, nombres, apellidos o correo.
     *
     * @param texto texto a buscar
     * @return lectores que coinciden
     * @throws SQLException si ocurre un error con la base de datos
     */
    public List<Lector> buscar(String texto) throws SQLException {
        String patron = "%" + texto + "%";
        try (Connection cn = Conexion.getConexion();
                PreparedStatement ps = cn.prepareStatement(SQL_BUSCAR)) {
            for (int i = 1; i <= 4; i++) {
                ps.setString(i, patron);
            }
            try (ResultSet rs = ps.executeQuery()) {
                return leerLista(rs);
            }
        }
    }

    /**
     * Obtiene un lector por su identificador.
     *
     * @param idLector identificador del lector
     * @return el lector, o {@code null} si no existe
     * @throws SQLException si ocurre un error con la base de datos
     */
    public Lector obtenerPorId(int idLector) throws SQLException {
        try (Connection cn = Conexion.getConexion();
                PreparedStatement ps = cn.prepareStatement(SQL_OBTENER)) {
            ps.setInt(1, idLector);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? leerLector(rs) : null;
            }
        }
    }

    /**
     * Inserta un lector nuevo.
     *
     * @param lector lector a registrar
     * @return {@code true} si se insertó una fila
     * @throws SQLException si ocurre un error con la base de datos
     */
    public boolean insertar(Lector lector) throws SQLException {
        try (Connection cn = Conexion.getConexion();
                PreparedStatement ps = cn.prepareStatement(SQL_INSERTAR)) {
            asignarParametros(ps, lector);
            return ps.executeUpdate() == 1;
        }
    }

    /**
     * Actualiza un lector existente.
     *
     * @param lector lector con los datos nuevos y su id
     * @return {@code true} si se actualizó una fila
     * @throws SQLException si ocurre un error con la base de datos
     */
    public boolean actualizar(Lector lector) throws SQLException {
        try (Connection cn = Conexion.getConexion();
                PreparedStatement ps = cn.prepareStatement(SQL_ACTUALIZAR)) {
            asignarParametros(ps, lector);
            ps.setInt(8, lector.getIdLector());
            return ps.executeUpdate() == 1;
        }
    }

    /**
     * Elimina un lector.
     *
     * @param idLector identificador del lector
     * @return {@code true} si se eliminó una fila
     * @throws SQLException si ocurre un error con la base de datos
     */
    public boolean eliminar(int idLector) throws SQLException {
        try (Connection cn = Conexion.getConexion();
                PreparedStatement ps = cn.prepareStatement(SQL_ELIMINAR)) {
            ps.setInt(1, idLector);
            return ps.executeUpdate() == 1;
        }
    }

    /**
     * Indica si otra persona ya tiene registrada la cédula.
     *
     * @param cedula cédula a verificar
     * @param idExcluir id del lector que se edita (0 al registrar)
     * @return {@code true} si la cédula ya existe
     * @throws SQLException si ocurre un error con la base de datos
     */
    public boolean existeCedula(String cedula, int idExcluir) throws SQLException {
        return contar(SQL_EXISTE_CEDULA, cedula, idExcluir) > 0;
    }

    /**
     * Indica si otra persona ya tiene registrado el correo.
     *
     * @param correo correo a verificar
     * @param idExcluir id del lector que se edita (0 al registrar)
     * @return {@code true} si el correo ya existe
     * @throws SQLException si ocurre un error con la base de datos
     */
    public boolean existeCorreo(String correo, int idExcluir) throws SQLException {
        return contar(SQL_EXISTE_CORREO, correo, idExcluir) > 0;
    }

    /**
     * Indica si el lector tiene préstamos registrados.
     *
     * @param idLector identificador del lector
     * @return {@code true} si tiene al menos un préstamo
     * @throws SQLException si ocurre un error con la base de datos
     */
    public boolean tienePrestamos(int idLector) throws SQLException {
        try (Connection cn = Conexion.getConexion();
                PreparedStatement ps = cn.prepareStatement(SQL_TIENE_PRESTAMOS)) {
            ps.setInt(1, idLector);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    /**
     * Ejecuta un conteo con un texto y un id a excluir.
     *
     * @param sql consulta {@code SELECT COUNT(*)}
     * @param valor valor de texto a comparar
     * @param idExcluir id a excluir
     * @return número de filas encontradas
     * @throws SQLException si falla la consulta
     */
    private int contar(String sql, String valor, int idExcluir) throws SQLException {
        try (Connection cn = Conexion.getConexion();
                PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, valor);
            ps.setInt(2, idExcluir);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    /**
     * Ejecuta una consulta sin parámetros que devuelve lectores.
     *
     * @param sql consulta a ejecutar
     * @return lectores encontrados
     * @throws SQLException si falla la consulta
     */
    private List<Lector> consultarLista(String sql) throws SQLException {
        try (Connection cn = Conexion.getConexion();
                PreparedStatement ps = cn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            return leerLista(rs);
        }
    }

    /**
     * Asigna los siete campos del lector a una sentencia INSERT o UPDATE.
     *
     * @param ps sentencia preparada
     * @param lector lector con los datos
     * @throws SQLException si falla la asignación
     */
    private void asignarParametros(PreparedStatement ps, Lector lector) throws SQLException {
        ps.setString(1, lector.getCedula());
        ps.setString(2, lector.getNombres());
        ps.setString(3, lector.getApellidos());
        ps.setString(4, lector.getCorreo());
        ps.setString(5, lector.getTelefono());
        ps.setString(6, lector.getDireccion());
        ps.setString(7, lector.getEstado());
    }

    /**
     * Convierte todas las filas de un ResultSet en lectores.
     *
     * @param rs resultado de la consulta
     * @return lista de lectores
     * @throws SQLException si falla la lectura
     */
    private List<Lector> leerLista(ResultSet rs) throws SQLException {
        List<Lector> lectores = new ArrayList<>();
        while (rs.next()) {
            lectores.add(leerLector(rs));
        }
        return lectores;
    }

    /**
     * Convierte la fila actual de un ResultSet en un lector.
     *
     * @param rs resultado posicionado en una fila
     * @return lector con los datos de la fila
     * @throws SQLException si falla la lectura
     */
    private Lector leerLector(ResultSet rs) throws SQLException {
        return new Lector(
                rs.getInt("id_lector"),
                rs.getString("cedula"),
                rs.getString("nombres"),
                rs.getString("apellidos"),
                rs.getString("correo"),
                rs.getString("telefono"),
                rs.getString("direccion"),
                rs.getString("estado"));
    }
}
