package dao;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import modelo.Devolucion;
import util.Conexion;

/**
 * Acceso a datos de la tabla {@code devolucion}.
 *
 * <p>Registrar, modificar y eliminar una devolución también cambia el estado
 * del préstamo y el stock del libro, por eso se ejecutan dentro de una
 * transacción. Las validaciones y cálculos están en
 * {@link controlador.DevolucionController}.</p>
 *
 * @author Guashpa Gualli Jefferson Nezareth
 * @version 1.0
 */
public class DevolucionDAO {

    private static final String SELECT_BASE =
            "SELECT d.id_devolucion, d.id_prestamo, d.fecha_devolucion, d.estado_libro, d.dias_retraso, "
            + "d.multa, d.observacion, p.fecha_prestamo, p.fecha_limite, l.titulo, "
            + "CONCAT(r.nombres, ' ', r.apellidos) AS lector "
            + "FROM devolucion d "
            + "JOIN prestamo p ON p.id_prestamo = d.id_prestamo "
            + "JOIN libro l ON l.id_libro = p.id_libro "
            + "JOIN lector r ON r.id_lector = p.id_lector ";

    private static final String ORDEN = " ORDER BY d.fecha_devolucion DESC, d.id_devolucion DESC";

    private static final String SQL_LISTAR = SELECT_BASE + ORDEN;

    private static final String SQL_BUSCAR = SELECT_BASE
            + "WHERE l.titulo LIKE ? OR r.nombres LIKE ? OR r.apellidos LIKE ? OR d.estado_libro LIKE ?"
            + ORDEN;

    private static final String SQL_OBTENER = SELECT_BASE + "WHERE d.id_devolucion = ?";

    private static final String SQL_INSERTAR =
            "INSERT INTO devolucion (id_prestamo, fecha_devolucion, estado_libro, dias_retraso, multa, "
            + "observacion) VALUES (?, ?, ?, ?, ?, ?)";

    private static final String SQL_ACTUALIZAR =
            "UPDATE devolucion SET fecha_devolucion = ?, estado_libro = ?, dias_retraso = ?, multa = ?, "
            + "observacion = ? WHERE id_devolucion = ?";

    private static final String SQL_ELIMINAR =
            "DELETE FROM devolucion WHERE id_devolucion = ?";

    private static final String SQL_PRESTAMO_DEVUELTO =
            "UPDATE prestamo SET estado = 'DEVUELTO' WHERE id_prestamo = ?";

    private static final String SQL_PRESTAMO_PENDIENTE =
            "UPDATE prestamo SET estado = CASE WHEN fecha_limite < CURDATE() THEN 'VENCIDO' "
            + "ELSE 'ACTIVO' END WHERE id_prestamo = ?";

    private static final String SQL_SUMAR_STOCK =
            "UPDATE libro SET stock = stock + 1 WHERE id_libro = ?";

    private static final String SQL_RESTAR_STOCK =
            "UPDATE libro SET stock = stock - 1 WHERE id_libro = ? AND stock > 0";

    private static final String SQL_EXISTE_PRESTAMO =
            "SELECT COUNT(*) FROM devolucion WHERE id_prestamo = ?";

    /**
     * Crea el DAO de devoluciones.
     */
    public DevolucionDAO() {
    }

    /**
     * Lista todas las devoluciones, de la más reciente a la más antigua.
     *
     * @return lista de devoluciones
     * @throws SQLException si ocurre un error con la base de datos
     */
    public List<Devolucion> listar() throws SQLException {
        try (Connection cn = Conexion.getConexion();
                PreparedStatement ps = cn.prepareStatement(SQL_LISTAR);
                ResultSet rs = ps.executeQuery()) {
            return leerLista(rs);
        }
    }

    /**
     * Busca devoluciones por título del libro, nombre del lector o estado del
     * libro.
     *
     * @param texto texto a buscar
     * @return devoluciones que coinciden
     * @throws SQLException si ocurre un error con la base de datos
     */
    public List<Devolucion> buscar(String texto) throws SQLException {
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
     * Obtiene una devolución por su identificador.
     *
     * @param idDevolucion identificador de la devolución
     * @return la devolución, o {@code null} si no existe
     * @throws SQLException si ocurre un error con la base de datos
     */
    public Devolucion obtenerPorId(int idDevolucion) throws SQLException {
        try (Connection cn = Conexion.getConexion();
                PreparedStatement ps = cn.prepareStatement(SQL_OBTENER)) {
            ps.setInt(1, idDevolucion);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? leerDevolucion(rs) : null;
            }
        }
    }

    /**
     * Indica si el préstamo ya tiene una devolución.
     *
     * @param idPrestamo identificador del préstamo
     * @return {@code true} si ya existe una devolución para ese préstamo
     * @throws SQLException si ocurre un error con la base de datos
     */
    public boolean existePorPrestamo(int idPrestamo) throws SQLException {
        try (Connection cn = Conexion.getConexion();
                PreparedStatement ps = cn.prepareStatement(SQL_EXISTE_PRESTAMO)) {
            ps.setInt(1, idPrestamo);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    /**
     * Registra una devolución: inserta la fila, marca el préstamo como
     * DEVUELTO y, si el libro no se perdió, suma el ejemplar al stock.
     *
     * @param d devolución a registrar
     * @param idLibro libro del préstamo
     * @param sumarStock {@code true} si el ejemplar vuelve al stock
     * @return {@code true} si se registró
     * @throws SQLException si ocurre un error con la base de datos
     */
    public boolean insertar(Devolucion d, int idLibro, boolean sumarStock) throws SQLException {
        try (Connection cn = Conexion.getConexion()) {
            cn.setAutoCommit(false);
            try {
                boolean ok;
                try (PreparedStatement ps = cn.prepareStatement(SQL_INSERTAR)) {
                    ps.setInt(1, d.getIdPrestamo());
                    ps.setDate(2, Date.valueOf(d.getFechaDevolucion()));
                    ps.setString(3, d.getEstadoLibro());
                    ps.setInt(4, d.getDiasRetraso());
                    ps.setBigDecimal(5, d.getMulta());
                    ps.setString(6, d.getObservacion());
                    ok = ps.executeUpdate() == 1;
                }
                ok = ok && ejecutar(cn, SQL_PRESTAMO_DEVUELTO, d.getIdPrestamo()) == 1;
                if (ok && sumarStock) {
                    ok = ejecutar(cn, SQL_SUMAR_STOCK, idLibro) == 1;
                }
                terminar(cn, ok);
                return ok;
            } catch (SQLException e) {
                cn.rollback();
                throw e;
            }
        }
    }

    /**
     * Modifica una devolución y ajusta el stock si el libro pasa a perdido o
     * deja de estarlo.
     *
     * @param d devolución con los datos nuevos
     * @param idLibro libro del préstamo
     * @param ajusteStock +1 para sumar un ejemplar, -1 para restarlo, 0 sin
     * cambios
     * @return {@code true} si se modificó; {@code false} si no había stock
     * para restar (no se guarda nada)
     * @throws SQLException si ocurre un error con la base de datos
     */
    public boolean actualizar(Devolucion d, int idLibro, int ajusteStock) throws SQLException {
        try (Connection cn = Conexion.getConexion()) {
            cn.setAutoCommit(false);
            try {
                boolean ok;
                try (PreparedStatement ps = cn.prepareStatement(SQL_ACTUALIZAR)) {
                    ps.setDate(1, Date.valueOf(d.getFechaDevolucion()));
                    ps.setString(2, d.getEstadoLibro());
                    ps.setInt(3, d.getDiasRetraso());
                    ps.setBigDecimal(4, d.getMulta());
                    ps.setString(5, d.getObservacion());
                    ps.setInt(6, d.getIdDevolucion());
                    ok = ps.executeUpdate() == 1;
                }
                ok = ok && ajustarStock(cn, idLibro, ajusteStock);
                terminar(cn, ok);
                return ok;
            } catch (SQLException e) {
                cn.rollback();
                throw e;
            }
        }
    }

    /**
     * Elimina una devolución: el préstamo vuelve a ACTIVO o VENCIDO y, si el
     * ejemplar había vuelto al stock, se descuenta otra vez.
     *
     * @param d devolución a eliminar
     * @param idLibro libro del préstamo
     * @param restarStock {@code true} si hay que descontar el ejemplar
     * @return {@code true} si se eliminó; {@code false} si no había stock
     * para descontar (no se guarda nada)
     * @throws SQLException si ocurre un error con la base de datos
     */
    public boolean eliminar(Devolucion d, int idLibro, boolean restarStock) throws SQLException {
        try (Connection cn = Conexion.getConexion()) {
            cn.setAutoCommit(false);
            try {
                boolean ok = ejecutar(cn, SQL_ELIMINAR, d.getIdDevolucion()) == 1
                        && ejecutar(cn, SQL_PRESTAMO_PENDIENTE, d.getIdPrestamo()) == 1;
                if (ok && restarStock) {
                    ok = ejecutar(cn, SQL_RESTAR_STOCK, idLibro) == 1;
                }
                terminar(cn, ok);
                return ok;
            } catch (SQLException e) {
                cn.rollback();
                throw e;
            }
        }
    }

    /**
     * Suma o resta un ejemplar del stock dentro de una transacción.
     *
     * @param cn conexión de la transacción
     * @param idLibro libro a ajustar
     * @param ajuste +1, -1 o 0
     * @return {@code true} si el ajuste se pudo hacer
     * @throws SQLException si falla la sentencia
     */
    private boolean ajustarStock(Connection cn, int idLibro, int ajuste) throws SQLException {
        if (ajuste > 0) {
            return ejecutar(cn, SQL_SUMAR_STOCK, idLibro) == 1;
        }
        if (ajuste < 0) {
            return ejecutar(cn, SQL_RESTAR_STOCK, idLibro) == 1;
        }
        return true;
    }

    /**
     * Ejecuta una sentencia de actualización con un único parámetro entero.
     *
     * @param cn conexión (de la transacción en curso)
     * @param sql sentencia a ejecutar
     * @param id valor del parámetro
     * @return filas afectadas
     * @throws SQLException si falla la sentencia
     */
    private int ejecutar(Connection cn, String sql, int id) throws SQLException {
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate();
        }
    }

    /**
     * Confirma o revierte la transacción.
     *
     * @param cn conexión de la transacción
     * @param ok {@code true} para confirmar, {@code false} para revertir
     * @throws SQLException si falla el commit o el rollback
     */
    private void terminar(Connection cn, boolean ok) throws SQLException {
        if (ok) {
            cn.commit();
        } else {
            cn.rollback();
        }
    }

    /**
     * Convierte todas las filas de un ResultSet en devoluciones.
     *
     * @param rs resultado de la consulta
     * @return lista de devoluciones
     * @throws SQLException si falla la lectura
     */
    private List<Devolucion> leerLista(ResultSet rs) throws SQLException {
        List<Devolucion> lista = new ArrayList<>();
        while (rs.next()) {
            lista.add(leerDevolucion(rs));
        }
        return lista;
    }

    /**
     * Convierte la fila actual de un ResultSet en una devolución.
     *
     * @param rs resultado posicionado en una fila
     * @return devolución con los datos de la fila
     * @throws SQLException si falla la lectura
     */
    private Devolucion leerDevolucion(ResultSet rs) throws SQLException {
        Devolucion d = new Devolucion();
        d.setIdDevolucion(rs.getInt("id_devolucion"));
        d.setIdPrestamo(rs.getInt("id_prestamo"));
        d.setFechaDevolucion(rs.getDate("fecha_devolucion").toLocalDate());
        d.setEstadoLibro(rs.getString("estado_libro"));
        d.setDiasRetraso(rs.getInt("dias_retraso"));
        d.setMulta(rs.getBigDecimal("multa"));
        d.setObservacion(rs.getString("observacion"));
        d.setFechaPrestamo(rs.getDate("fecha_prestamo").toLocalDate());
        d.setFechaLimite(rs.getDate("fecha_limite").toLocalDate());
        d.setTituloLibro(rs.getString("titulo"));
        d.setNombreLector(rs.getString("lector"));
        return d;
    }
}
