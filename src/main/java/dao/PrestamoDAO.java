package dao;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import modelo.Prestamo;
import util.Conexion;

/**
 * Acceso a datos de la tabla {@code prestamo}.
 *
 * <p>Registrar, modificar y eliminar un préstamo también ajusta el stock del
 * libro; por eso esas operaciones se ejecutan dentro de una transacción
 * (o se guardan todos los cambios, o ninguno). Las validaciones y reglas de
 * negocio están en {@link controlador.PrestamoController}.</p>
 *
 * @author Guashpa Gualli Jefferson Nezareth
 * @version 1.0
 */
public class PrestamoDAO {

    private static final String SELECT_BASE =
            "SELECT p.id_prestamo, p.id_libro, p.id_lector, p.fecha_prestamo, p.fecha_limite, "
            + "p.estado, p.observacion, l.titulo, CONCAT(r.nombres, ' ', r.apellidos) AS lector "
            + "FROM prestamo p "
            + "JOIN libro l ON l.id_libro = p.id_libro "
            + "JOIN lector r ON r.id_lector = p.id_lector ";

    private static final String ORDEN = " ORDER BY p.fecha_prestamo DESC, p.id_prestamo DESC";

    private static final String SQL_LISTAR = SELECT_BASE + ORDEN;

    private static final String SQL_BUSCAR = SELECT_BASE
            + "WHERE l.titulo LIKE ? OR r.nombres LIKE ? OR r.apellidos LIKE ? OR r.cedula LIKE ? "
            + "OR p.estado LIKE ?" + ORDEN;

    private static final String SQL_PENDIENTES = SELECT_BASE
            + "WHERE p.estado IN ('ACTIVO', 'VENCIDO') "
            + "AND NOT EXISTS (SELECT 1 FROM devolucion d WHERE d.id_prestamo = p.id_prestamo)" + ORDEN;

    private static final String SQL_OBTENER = SELECT_BASE + "WHERE p.id_prestamo = ?";

    private static final String SQL_MARCAR_VENCIDOS =
            "UPDATE prestamo SET estado = 'VENCIDO' WHERE estado = 'ACTIVO' AND fecha_limite < CURDATE()";

    private static final String SQL_MARCAR_ACTIVOS =
            "UPDATE prestamo SET estado = 'ACTIVO' WHERE estado = 'VENCIDO' AND fecha_limite >= CURDATE()";

    private static final String SQL_INSERTAR =
            "INSERT INTO prestamo (id_libro, id_lector, fecha_prestamo, fecha_limite, estado, observacion) "
            + "VALUES (?, ?, ?, ?, ?, ?)";

    private static final String SQL_ACTUALIZAR =
            "UPDATE prestamo SET id_libro = ?, id_lector = ?, fecha_prestamo = ?, fecha_limite = ?, "
            + "estado = ?, observacion = ? WHERE id_prestamo = ?";

    private static final String SQL_ELIMINAR =
            "DELETE FROM prestamo WHERE id_prestamo = ?";

    private static final String SQL_RESTAR_STOCK =
            "UPDATE libro SET stock = stock - 1 WHERE id_libro = ? AND stock > 0";

    private static final String SQL_SUMAR_STOCK =
            "UPDATE libro SET stock = stock + 1 WHERE id_libro = ?";

    private static final String SQL_STOCK =
            "SELECT stock FROM libro WHERE id_libro = ?";

    private static final String SQL_ESTADO_LECTOR =
            "SELECT estado FROM lector WHERE id_lector = ?";

    private static final String SQL_CONTAR_PENDIENTES =
            "SELECT COUNT(*) FROM prestamo WHERE id_lector = ? AND estado IN ('ACTIVO', 'VENCIDO') "
            + "AND id_prestamo <> ?";

    private static final String SQL_TIENE_DEVOLUCION =
            "SELECT COUNT(*) FROM devolucion WHERE id_prestamo = ?";

    /**
     * Crea el DAO de préstamos.
     */
    public PrestamoDAO() {
    }

    /**
     * Lista todos los préstamos, del más reciente al más antiguo. Antes de
     * listar, actualiza los estados ACTIVO/VENCIDO según la fecha actual.
     *
     * @return lista de préstamos
     * @throws SQLException si ocurre un error con la base de datos
     */
    public List<Prestamo> listar() throws SQLException {
        actualizarEstados();
        try (Connection cn = Conexion.getConexion();
                PreparedStatement ps = cn.prepareStatement(SQL_LISTAR);
                ResultSet rs = ps.executeQuery()) {
            return leerLista(rs);
        }
    }

    /**
     * Busca préstamos por título del libro, nombre o cédula del lector, o
     * estado.
     *
     * @param texto texto a buscar
     * @return préstamos que coinciden
     * @throws SQLException si ocurre un error con la base de datos
     */
    public List<Prestamo> buscar(String texto) throws SQLException {
        actualizarEstados();
        String patron = "%" + texto + "%";
        try (Connection cn = Conexion.getConexion();
                PreparedStatement ps = cn.prepareStatement(SQL_BUSCAR)) {
            for (int i = 1; i <= 5; i++) {
                ps.setString(i, patron);
            }
            try (ResultSet rs = ps.executeQuery()) {
                return leerLista(rs);
            }
        }
    }

    /**
     * Lista los préstamos que aún no se devuelven (ACTIVO o VENCIDO y sin
     * devolución). Lo usa el módulo Devolución.
     *
     * @return préstamos pendientes de devolución
     * @throws SQLException si ocurre un error con la base de datos
     */
    public List<Prestamo> listarPendientes() throws SQLException {
        actualizarEstados();
        try (Connection cn = Conexion.getConexion();
                PreparedStatement ps = cn.prepareStatement(SQL_PENDIENTES);
                ResultSet rs = ps.executeQuery()) {
            return leerLista(rs);
        }
    }

    /**
     * Obtiene un préstamo por su identificador.
     *
     * @param idPrestamo identificador del préstamo
     * @return el préstamo, o {@code null} si no existe
     * @throws SQLException si ocurre un error con la base de datos
     */
    public Prestamo obtenerPorId(int idPrestamo) throws SQLException {
        try (Connection cn = Conexion.getConexion();
                PreparedStatement ps = cn.prepareStatement(SQL_OBTENER)) {
            ps.setInt(1, idPrestamo);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? leerPrestamo(rs) : null;
            }
        }
    }

    /**
     * Marca como VENCIDO los préstamos activos cuya fecha límite ya pasó, y
     * como ACTIVO los vencidos cuya fecha límite se amplió.
     *
     * @throws SQLException si ocurre un error con la base de datos
     */
    public void actualizarEstados() throws SQLException {
        try (Connection cn = Conexion.getConexion();
                PreparedStatement ps1 = cn.prepareStatement(SQL_MARCAR_VENCIDOS);
                PreparedStatement ps2 = cn.prepareStatement(SQL_MARCAR_ACTIVOS)) {
            ps1.executeUpdate();
            ps2.executeUpdate();
        }
    }

    /**
     * Registra un préstamo y descuenta un ejemplar del stock del libro, en
     * una sola transacción.
     *
     * @param p préstamo a registrar
     * @return {@code true} si se registró; {@code false} si el libro ya no
     * tenía stock (no se guarda nada)
     * @throws SQLException si ocurre un error con la base de datos
     */
    public boolean insertar(Prestamo p) throws SQLException {
        try (Connection cn = Conexion.getConexion()) {
            cn.setAutoCommit(false);
            try {
                boolean ok = ejecutar(cn, SQL_RESTAR_STOCK, p.getIdLibro()) == 1;
                if (ok) {
                    try (PreparedStatement ps = cn.prepareStatement(SQL_INSERTAR)) {
                        asignarParametros(ps, p);
                        ok = ps.executeUpdate() == 1;
                    }
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
     * Modifica un préstamo. Si cambió el libro, devuelve un ejemplar al libro
     * anterior y descuenta uno del libro nuevo, en una sola transacción.
     *
     * @param p préstamo con los datos nuevos
     * @param idLibroAnterior libro que tenía el préstamo antes de modificarlo
     * @return {@code true} si se modificó; {@code false} si el préstamo no
     * existe o el libro nuevo no tiene stock (no se guarda nada)
     * @throws SQLException si ocurre un error con la base de datos
     */
    public boolean actualizar(Prestamo p, int idLibroAnterior) throws SQLException {
        try (Connection cn = Conexion.getConexion()) {
            cn.setAutoCommit(false);
            try {
                boolean ok = true;
                if (p.getIdLibro() != idLibroAnterior) {
                    ok = ejecutar(cn, SQL_RESTAR_STOCK, p.getIdLibro()) == 1
                            && ejecutar(cn, SQL_SUMAR_STOCK, idLibroAnterior) == 1;
                }
                if (ok) {
                    try (PreparedStatement ps = cn.prepareStatement(SQL_ACTUALIZAR)) {
                        asignarParametros(ps, p);
                        ps.setInt(7, p.getIdPrestamo());
                        ok = ps.executeUpdate() == 1;
                    }
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
     * Elimina un préstamo. Si el libro no había sido devuelto, suma el
     * ejemplar de vuelta al stock, en una sola transacción.
     *
     * @param p préstamo a eliminar (se usan su id, libro y estado)
     * @return {@code true} si se eliminó
     * @throws SQLException si ocurre un error con la base de datos
     */
    public boolean eliminar(Prestamo p) throws SQLException {
        try (Connection cn = Conexion.getConexion()) {
            cn.setAutoCommit(false);
            try {
                boolean ok = ejecutar(cn, SQL_ELIMINAR, p.getIdPrestamo()) == 1;
                if (ok && !Prestamo.DEVUELTO.equals(p.getEstado())) {
                    ok = ejecutar(cn, SQL_SUMAR_STOCK, p.getIdLibro()) == 1;
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
     * Devuelve el stock actual de un libro.
     *
     * @param idLibro identificador del libro
     * @return ejemplares disponibles, o -1 si el libro no existe
     * @throws SQLException si ocurre un error con la base de datos
     */
    public int stockLibro(int idLibro) throws SQLException {
        try (Connection cn = Conexion.getConexion();
                PreparedStatement ps = cn.prepareStatement(SQL_STOCK)) {
            ps.setInt(1, idLibro);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : -1;
            }
        }
    }

    /**
     * Devuelve el estado de un lector.
     *
     * @param idLector identificador del lector
     * @return ACTIVO o SUSPENDIDO, o {@code null} si el lector no existe
     * @throws SQLException si ocurre un error con la base de datos
     */
    public String estadoLector(int idLector) throws SQLException {
        try (Connection cn = Conexion.getConexion();
                PreparedStatement ps = cn.prepareStatement(SQL_ESTADO_LECTOR)) {
            ps.setInt(1, idLector);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getString(1) : null;
            }
        }
    }

    /**
     * Cuenta los préstamos sin devolver (ACTIVO o VENCIDO) de un lector.
     *
     * @param idLector identificador del lector
     * @param idPrestamoExcluir préstamo que no se cuenta (el que se edita; 0
     * al registrar)
     * @return número de préstamos pendientes
     * @throws SQLException si ocurre un error con la base de datos
     */
    public int contarPendientesLector(int idLector, int idPrestamoExcluir) throws SQLException {
        try (Connection cn = Conexion.getConexion();
                PreparedStatement ps = cn.prepareStatement(SQL_CONTAR_PENDIENTES)) {
            ps.setInt(1, idLector);
            ps.setInt(2, idPrestamoExcluir);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    /**
     * Indica si el préstamo ya tiene una devolución registrada.
     *
     * @param idPrestamo identificador del préstamo
     * @return {@code true} si tiene devolución
     * @throws SQLException si ocurre un error con la base de datos
     */
    public boolean tieneDevolucion(int idPrestamo) throws SQLException {
        try (Connection cn = Conexion.getConexion();
                PreparedStatement ps = cn.prepareStatement(SQL_TIENE_DEVOLUCION)) {
            ps.setInt(1, idPrestamo);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
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
     * Asigna los seis campos del préstamo a una sentencia INSERT o UPDATE.
     *
     * @param ps sentencia preparada
     * @param p préstamo con los datos
     * @throws SQLException si falla la asignación
     */
    private void asignarParametros(PreparedStatement ps, Prestamo p) throws SQLException {
        ps.setInt(1, p.getIdLibro());
        ps.setInt(2, p.getIdLector());
        ps.setDate(3, Date.valueOf(p.getFechaPrestamo()));
        ps.setDate(4, Date.valueOf(p.getFechaLimite()));
        ps.setString(5, p.getEstado());
        ps.setString(6, p.getObservacion());
    }

    /**
     * Convierte todas las filas de un ResultSet en préstamos.
     *
     * @param rs resultado de la consulta
     * @return lista de préstamos
     * @throws SQLException si falla la lectura
     */
    private List<Prestamo> leerLista(ResultSet rs) throws SQLException {
        List<Prestamo> lista = new ArrayList<>();
        while (rs.next()) {
            lista.add(leerPrestamo(rs));
        }
        return lista;
    }

    /**
     * Convierte la fila actual de un ResultSet en un préstamo.
     *
     * @param rs resultado posicionado en una fila
     * @return préstamo con los datos de la fila
     * @throws SQLException si falla la lectura
     */
    private Prestamo leerPrestamo(ResultSet rs) throws SQLException {
        Prestamo p = new Prestamo();
        p.setIdPrestamo(rs.getInt("id_prestamo"));
        p.setIdLibro(rs.getInt("id_libro"));
        p.setIdLector(rs.getInt("id_lector"));
        p.setFechaPrestamo(rs.getDate("fecha_prestamo").toLocalDate());
        p.setFechaLimite(rs.getDate("fecha_limite").toLocalDate());
        p.setEstado(rs.getString("estado"));
        p.setObservacion(rs.getString("observacion"));
        p.setTituloLibro(rs.getString("titulo"));
        p.setNombreLector(rs.getString("lector"));
        return p;
    }
}
