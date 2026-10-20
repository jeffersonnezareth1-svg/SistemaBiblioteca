package dao;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import modelo.Autor;
import util.Conexion;

/**
 * Acceso a datos de la tabla {@code autor}.
 *
 * <p>Contiene únicamente sentencias SQL; las validaciones están en
 * {@link controlador.AutorController}.</p>
 *
 * @author Guashpa Gualli Jefferson Nezareth
 * @version 1.0
 */
public class AutorDAO {

    private static final String COLUMNAS =
            "id_autor, nombres, apellidos, nacionalidad, fecha_nacimiento, biografia";

    private static final String SQL_LISTAR =
            "SELECT " + COLUMNAS + " FROM autor ORDER BY apellidos, nombres";

    private static final String SQL_BUSCAR =
            "SELECT " + COLUMNAS + " FROM autor "
            + "WHERE nombres LIKE ? OR apellidos LIKE ? OR nacionalidad LIKE ? "
            + "ORDER BY apellidos, nombres";

    private static final String SQL_OBTENER =
            "SELECT " + COLUMNAS + " FROM autor WHERE id_autor = ?";

    private static final String SQL_INSERTAR =
            "INSERT INTO autor (nombres, apellidos, nacionalidad, fecha_nacimiento, biografia) "
            + "VALUES (?, ?, ?, ?, ?)";

    private static final String SQL_ACTUALIZAR =
            "UPDATE autor SET nombres = ?, apellidos = ?, nacionalidad = ?, fecha_nacimiento = ?, "
            + "biografia = ? WHERE id_autor = ?";

    private static final String SQL_ELIMINAR =
            "DELETE FROM autor WHERE id_autor = ?";

    private static final String SQL_EXISTE_NOMBRE =
            "SELECT COUNT(*) FROM autor WHERE LOWER(nombres) = LOWER(?) "
            + "AND LOWER(apellidos) = LOWER(?) AND id_autor <> ?";

    /**
     * Crea el DAO de autores.
     */
    public AutorDAO() {
    }

    /**
     * Lista todos los autores ordenados por apellido.
     *
     * @return lista de autores
     * @throws SQLException si ocurre un error con la base de datos
     */
    public List<Autor> listar() throws SQLException {
        try (Connection cn = Conexion.getConexion();
                PreparedStatement ps = cn.prepareStatement(SQL_LISTAR);
                ResultSet rs = ps.executeQuery()) {
            return leerLista(rs);
        }
    }

    /**
     * Busca autores cuyo nombre, apellido o nacionalidad contenga el texto.
     *
     * @param texto texto a buscar
     * @return autores que coinciden
     * @throws SQLException si ocurre un error con la base de datos
     */
    public List<Autor> buscar(String texto) throws SQLException {
        String patron = "%" + texto + "%";
        try (Connection cn = Conexion.getConexion();
                PreparedStatement ps = cn.prepareStatement(SQL_BUSCAR)) {
            for (int i = 1; i <= 3; i++) {
                ps.setString(i, patron);
            }
            try (ResultSet rs = ps.executeQuery()) {
                return leerLista(rs);
            }
        }
    }

    /**
     * Obtiene un autor por su identificador.
     *
     * @param idAutor identificador del autor
     * @return el autor, o {@code null} si no existe
     * @throws SQLException si ocurre un error con la base de datos
     */
    public Autor obtenerPorId(int idAutor) throws SQLException {
        try (Connection cn = Conexion.getConexion();
                PreparedStatement ps = cn.prepareStatement(SQL_OBTENER)) {
            ps.setInt(1, idAutor);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? leerAutor(rs) : null;
            }
        }
    }

    /**
     * Inserta un autor nuevo.
     *
     * @param autor autor a registrar
     * @return {@code true} si se insertó una fila
     * @throws SQLException si ocurre un error con la base de datos
     */
    public boolean insertar(Autor autor) throws SQLException {
        try (Connection cn = Conexion.getConexion();
                PreparedStatement ps = cn.prepareStatement(SQL_INSERTAR)) {
            asignarParametros(ps, autor);
            return ps.executeUpdate() == 1;
        }
    }

    /**
     * Actualiza un autor existente.
     *
     * @param autor autor con los datos nuevos y su id
     * @return {@code true} si se actualizó una fila
     * @throws SQLException si ocurre un error con la base de datos
     */
    public boolean actualizar(Autor autor) throws SQLException {
        try (Connection cn = Conexion.getConexion();
                PreparedStatement ps = cn.prepareStatement(SQL_ACTUALIZAR)) {
            asignarParametros(ps, autor);
            ps.setInt(6, autor.getIdAutor());
            return ps.executeUpdate() == 1;
        }
    }

    /**
     * Elimina un autor.
     *
     * @param idAutor identificador del autor
     * @return {@code true} si se eliminó una fila
     * @throws SQLException si ocurre un error con la base de datos
     */
    public boolean eliminar(int idAutor) throws SQLException {
        try (Connection cn = Conexion.getConexion();
                PreparedStatement ps = cn.prepareStatement(SQL_ELIMINAR)) {
            ps.setInt(1, idAutor);
            return ps.executeUpdate() == 1;
        }
    }

    /**
     * Indica si ya existe otro autor con los mismos nombres y apellidos (sin
     * distinguir mayúsculas).
     *
     * @param nombres nombres
     * @param apellidos apellidos
     * @param idExcluir id del autor que se está editando (0 al registrar)
     * @return {@code true} si existe otro autor con ese nombre
     * @throws SQLException si ocurre un error con la base de datos
     */
    public boolean existeNombre(String nombres, String apellidos, int idExcluir) throws SQLException {
        try (Connection cn = Conexion.getConexion();
                PreparedStatement ps = cn.prepareStatement(SQL_EXISTE_NOMBRE)) {
            ps.setString(1, nombres);
            ps.setString(2, apellidos);
            ps.setInt(3, idExcluir);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    /**
     * Asigna los cinco campos del autor a una sentencia INSERT o UPDATE.
     *
     * @param ps sentencia preparada
     * @param autor autor con los datos
     * @throws SQLException si falla la asignación
     */
    private void asignarParametros(PreparedStatement ps, Autor autor) throws SQLException {
        ps.setString(1, autor.getNombres());
        ps.setString(2, autor.getApellidos());
        ps.setString(3, autor.getNacionalidad());
        if (autor.getFechaNacimiento() == null) {
            ps.setNull(4, Types.DATE);
        } else {
            ps.setDate(4, Date.valueOf(autor.getFechaNacimiento()));
        }
        ps.setString(5, autor.getBiografia());
    }

    /**
     * Convierte todas las filas de un ResultSet en autores.
     *
     * @param rs resultado de la consulta
     * @return lista de autores
     * @throws SQLException si falla la lectura
     */
    private List<Autor> leerLista(ResultSet rs) throws SQLException {
        List<Autor> autores = new ArrayList<>();
        while (rs.next()) {
            autores.add(leerAutor(rs));
        }
        return autores;
    }

    /**
     * Convierte la fila actual de un ResultSet en un autor.
     *
     * @param rs resultado posicionado en una fila
     * @return autor con los datos de la fila
     * @throws SQLException si falla la lectura
     */
    private Autor leerAutor(ResultSet rs) throws SQLException {
        Date fecha = rs.getDate("fecha_nacimiento");
        return new Autor(
                rs.getInt("id_autor"),
                rs.getString("nombres"),
                rs.getString("apellidos"),
                rs.getString("nacionalidad"),
                fecha == null ? null : fecha.toLocalDate(),
                rs.getString("biografia"));
    }
}
