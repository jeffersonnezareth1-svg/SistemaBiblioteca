package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import modelo.Libro;
import util.Conexion;

/**
 * Acceso a datos de la tabla {@code libro}.
 *
 * <p>Contiene únicamente sentencias SQL; las validaciones y reglas de negocio
 * están en {@link controlador.LibroController}. Cada método abre y cierra su
 * propia conexión.</p>
 *
 * @author Guashpa Gualli Jefferson Nezareth
 * @version 1.0
 */
public class LibroDAO {

    /**
     * Crea el DAO de libros.
     */
    public LibroDAO() {
    }

    private static final String COLUMNAS =
            "id_libro, isbn, titulo, editorial, anio_publicacion, genero, numero_paginas, stock";

    private static final String SQL_LISTAR =
            "SELECT " + COLUMNAS + " FROM libro ORDER BY titulo";

    private static final String SQL_BUSCAR =
            "SELECT " + COLUMNAS + " FROM libro "
            + "WHERE isbn LIKE ? OR titulo LIKE ? OR editorial LIKE ? OR genero LIKE ? "
            + "ORDER BY titulo";

    private static final String SQL_OBTENER =
            "SELECT " + COLUMNAS + " FROM libro WHERE id_libro = ?";

    private static final String SQL_INSERTAR =
            "INSERT INTO libro (isbn, titulo, editorial, anio_publicacion, genero, numero_paginas, stock) "
            + "VALUES (?, ?, ?, ?, ?, ?, ?)";

    private static final String SQL_ACTUALIZAR =
            "UPDATE libro SET isbn = ?, titulo = ?, editorial = ?, anio_publicacion = ?, "
            + "genero = ?, numero_paginas = ?, stock = ? WHERE id_libro = ?";

    private static final String SQL_ELIMINAR =
            "DELETE FROM libro WHERE id_libro = ?";

    private static final String SQL_EXISTE_ISBN =
            "SELECT COUNT(*) FROM libro WHERE isbn = ? AND id_libro <> ?";

    private static final String SQL_TIENE_PRESTAMOS =
            "SELECT COUNT(*) FROM prestamo WHERE id_libro = ?";

    /**
     * Lista todos los libros ordenados por título.
     *
     * @return lista de libros (vacía si no hay registros)
     * @throws SQLException si ocurre un error con la base de datos
     */
    public List<Libro> listar() throws SQLException {
        try (Connection cn = Conexion.getConexion();
                PreparedStatement ps = cn.prepareStatement(SQL_LISTAR);
                ResultSet rs = ps.executeQuery()) {
            return leerLista(rs);
        }
    }

    /**
     * Busca libros cuyo ISBN, título, editorial o género contenga el texto.
     *
     * @param texto texto a buscar
     * @return libros que coinciden
     * @throws SQLException si ocurre un error con la base de datos
     */
    public List<Libro> buscar(String texto) throws SQLException {
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
     * Obtiene un libro por su identificador.
     *
     * @param idLibro identificador del libro
     * @return el libro, o {@code null} si no existe
     * @throws SQLException si ocurre un error con la base de datos
     */
    public Libro obtenerPorId(int idLibro) throws SQLException {
        try (Connection cn = Conexion.getConexion();
                PreparedStatement ps = cn.prepareStatement(SQL_OBTENER)) {
            ps.setInt(1, idLibro);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? leerLibro(rs) : null;
            }
        }
    }

    /**
     * Inserta un libro nuevo.
     *
     * @param libro libro a registrar (su id se ignora)
     * @return {@code true} si se insertó una fila
     * @throws SQLException si ocurre un error con la base de datos
     */
    public boolean insertar(Libro libro) throws SQLException {
        try (Connection cn = Conexion.getConexion();
                PreparedStatement ps = cn.prepareStatement(SQL_INSERTAR)) {
            asignarParametros(ps, libro);
            return ps.executeUpdate() == 1;
        }
    }

    /**
     * Actualiza los datos de un libro existente.
     *
     * @param libro libro con los datos nuevos y su id
     * @return {@code true} si se actualizó una fila
     * @throws SQLException si ocurre un error con la base de datos
     */
    public boolean actualizar(Libro libro) throws SQLException {
        try (Connection cn = Conexion.getConexion();
                PreparedStatement ps = cn.prepareStatement(SQL_ACTUALIZAR)) {
            asignarParametros(ps, libro);
            ps.setInt(8, libro.getIdLibro());
            return ps.executeUpdate() == 1;
        }
    }

    /**
     * Elimina un libro.
     *
     * @param idLibro identificador del libro
     * @return {@code true} si se eliminó una fila
     * @throws SQLException si ocurre un error con la base de datos
     */
    public boolean eliminar(int idLibro) throws SQLException {
        try (Connection cn = Conexion.getConexion();
                PreparedStatement ps = cn.prepareStatement(SQL_ELIMINAR)) {
            ps.setInt(1, idLibro);
            return ps.executeUpdate() == 1;
        }
    }

    /**
     * Indica si un ISBN ya está registrado en otro libro.
     *
     * @param isbn ISBN a verificar
     * @param idExcluir id del libro que se está editando (0 al registrar)
     * @return {@code true} si otro libro ya usa ese ISBN
     * @throws SQLException si ocurre un error con la base de datos
     */
    public boolean existeIsbn(String isbn, int idExcluir) throws SQLException {
        try (Connection cn = Conexion.getConexion();
                PreparedStatement ps = cn.prepareStatement(SQL_EXISTE_ISBN)) {
            ps.setString(1, isbn);
            ps.setInt(2, idExcluir);
            return contar(ps) > 0;
        }
    }

    /**
     * Indica si el libro tiene préstamos registrados (activos o históricos).
     *
     * @param idLibro identificador del libro
     * @return {@code true} si existe al menos un préstamo del libro
     * @throws SQLException si ocurre un error con la base de datos
     */
    public boolean tienePrestamos(int idLibro) throws SQLException {
        try (Connection cn = Conexion.getConexion();
                PreparedStatement ps = cn.prepareStatement(SQL_TIENE_PRESTAMOS)) {
            ps.setInt(1, idLibro);
            return contar(ps) > 0;
        }
    }

    /**
     * Asigna los siete campos del libro a una sentencia INSERT o UPDATE.
     *
     * @param ps sentencia preparada
     * @param libro libro con los datos
     * @throws SQLException si falla la asignación
     */
    private void asignarParametros(PreparedStatement ps, Libro libro) throws SQLException {
        ps.setString(1, libro.getIsbn());
        ps.setString(2, libro.getTitulo());
        ps.setString(3, libro.getEditorial());
        ps.setInt(4, libro.getAnioPublicacion());
        ps.setString(5, libro.getGenero());
        ps.setInt(6, libro.getNumeroPaginas());
        ps.setInt(7, libro.getStock());
    }

    /**
     * Ejecuta una consulta {@code SELECT COUNT(*)} y devuelve el conteo.
     *
     * @param ps sentencia preparada con sus parámetros
     * @return número de filas contadas
     * @throws SQLException si falla la consulta
     */
    private int contar(PreparedStatement ps) throws SQLException {
        try (ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    /**
     * Convierte todas las filas de un ResultSet en libros.
     *
     * @param rs resultado de la consulta
     * @return lista de libros
     * @throws SQLException si falla la lectura
     */
    private List<Libro> leerLista(ResultSet rs) throws SQLException {
        List<Libro> libros = new ArrayList<>();
        while (rs.next()) {
            libros.add(leerLibro(rs));
        }
        return libros;
    }

    /**
     * Convierte la fila actual de un ResultSet en un libro.
     *
     * @param rs resultado posicionado en una fila
     * @return libro con los datos de la fila
     * @throws SQLException si falla la lectura
     */
    private Libro leerLibro(ResultSet rs) throws SQLException {
        return new Libro(
                rs.getInt("id_libro"),
                rs.getString("isbn"),
                rs.getString("titulo"),
                rs.getString("editorial"),
                rs.getInt("anio_publicacion"),
                rs.getString("genero"),
                rs.getInt("numero_paginas"),
                rs.getInt("stock"));
    }
}
