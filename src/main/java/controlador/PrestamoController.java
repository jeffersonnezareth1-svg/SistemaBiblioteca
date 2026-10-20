package controlador;

import dao.LectorDAO;
import dao.LibroDAO;
import dao.PrestamoDAO;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import modelo.Lector;
import modelo.Libro;
import modelo.Prestamo;
import util.Fechas;
import util.Respuesta;

/**
 * Controlador del módulo Préstamo (depende de Libro y Lector).
 *
 * <p>Valida los datos del formulario con un método pequeño por campo y
 * aplica las reglas de negocio:</p>
 * <ul>
 * <li>El libro debe tener stock disponible.</li>
 * <li>El lector debe estar ACTIVO.</li>
 * <li>Un lector puede tener como máximo {@value #MAX_PENDIENTES} préstamos sin
 * devolver.</li>
 * <li>La fecha límite no puede superar {@value #DIAS_MAXIMOS} días desde la
 * fecha del préstamo.</li>
 * <li>No se modifica un préstamo devuelto ni se elimina uno que tenga
 * devolución.</li>
 * </ul>
 *
 * <ul>
 * <li>C: {@link #registrarPrestamo}</li>
 * <li>R: {@link #listarPrestamos()} y {@link #buscarPrestamos(String)}</li>
 * <li>U: {@link #actualizarPrestamo}</li>
 * <li>D: {@link #eliminarPrestamo(int)}</li>
 * </ul>
 *
 * @author Guashpa Gualli Jefferson Nezareth
 * @version 1.0
 */
public class PrestamoController {

    /** Máximo de préstamos sin devolver por lector. */
    public static final int MAX_PENDIENTES = 3;
    /** Máximo de días entre la fecha del préstamo y la fecha límite. */
    public static final int DIAS_MAXIMOS = 30;

    /** Nombre del campo libro en el formulario. */
    public static final String CAMPO_LIBRO = "libro";
    /** Nombre del campo lector en el formulario. */
    public static final String CAMPO_LECTOR = "lector";
    /** Nombre del campo fecha de préstamo en el formulario. */
    public static final String CAMPO_FECHA_PRESTAMO = "fechaPrestamo";
    /** Nombre del campo fecha límite en el formulario. */
    public static final String CAMPO_FECHA_LIMITE = "fechaLimite";
    /** Nombre del campo observación en el formulario. */
    public static final String CAMPO_OBSERVACION = "observacion";

    private final PrestamoDAO dao;
    private final LibroDAO libroDao;
    private final LectorDAO lectorDao;

    /**
     * Crea el controlador con los DAO reales.
     */
    public PrestamoController() {
        this(new PrestamoDAO(), new LibroDAO(), new LectorDAO());
    }

    /**
     * Crea el controlador con DAO dados (útil en pruebas unitarias).
     *
     * @param dao DAO de préstamos
     * @param libroDao DAO de libros (para el combo de libros)
     * @param lectorDao DAO de lectores (para el combo de lectores)
     */
    public PrestamoController(PrestamoDAO dao, LibroDAO libroDao, LectorDAO lectorDao) {
        this.dao = dao;
        this.libroDao = libroDao;
        this.lectorDao = lectorDao;
    }

    /**
     * Lista todos los préstamos.
     *
     * @return lista de préstamos
     * @throws SQLException si ocurre un error con la base de datos
     */
    public List<Prestamo> listarPrestamos() throws SQLException {
        return dao.listar();
    }

    /**
     * Busca préstamos por libro, lector, cédula o estado. Si el texto está
     * vacío devuelve todos.
     *
     * @param texto texto a buscar
     * @return préstamos que coinciden
     * @throws SQLException si ocurre un error con la base de datos
     */
    public List<Prestamo> buscarPrestamos(String texto) throws SQLException {
        if (texto == null || texto.isBlank()) {
            return dao.listar();
        }
        return dao.buscar(texto.trim());
    }

    /**
     * Lista los libros para el combo del formulario.
     *
     * @return todos los libros
     * @throws SQLException si ocurre un error con la base de datos
     */
    public List<Libro> listarLibros() throws SQLException {
        return libroDao.listar();
    }

    /**
     * Lista los lectores para el combo del formulario.
     *
     * @return todos los lectores
     * @throws SQLException si ocurre un error con la base de datos
     */
    public List<Lector> listarLectores() throws SQLException {
        return lectorDao.listar();
    }

    /**
     * Registra un préstamo nuevo y descuenta un ejemplar del libro.
     *
     * @param idLibro libro seleccionado (0 si no se seleccionó)
     * @param idLector lector seleccionado (0 si no se seleccionó)
     * @param fechaPrestamo fecha {@code dd/MM/yyyy}
     * @param fechaLimite fecha {@code dd/MM/yyyy}
     * @param observacion observación (opcional)
     * @return respuesta exitosa o el error encontrado
     */
    public Respuesta registrarPrestamo(int idLibro, int idLector, String fechaPrestamo,
            String fechaLimite, String observacion) {
        Respuesta validacion = validarCampos(idLibro, idLector, fechaPrestamo, fechaLimite, observacion);
        if (!validacion.isExitosa()) {
            return validacion;
        }
        Prestamo p = crearPrestamo(0, idLibro, idLector, fechaPrestamo, fechaLimite, observacion);
        try {
            Respuesta regla = verificarStock(idLibro);
            if (regla == null) {
                regla = verificarLector(idLector, 0);
            }
            if (regla != null) {
                return regla;
            }
            if (dao.insertar(p)) {
                return Respuesta.ok("Préstamo registrado correctamente");
            }
            return Respuesta.error("No se pudo registrar: el libro se quedó sin stock", CAMPO_LIBRO);
        } catch (SQLException e) {
            return Respuesta.error("Error de base de datos: " + e.getMessage());
        }
    }

    /**
     * Modifica un préstamo que todavía no ha sido devuelto.
     *
     * @param idPrestamo préstamo a modificar
     * @param idLibro libro seleccionado
     * @param idLector lector seleccionado
     * @param fechaPrestamo fecha {@code dd/MM/yyyy}
     * @param fechaLimite fecha {@code dd/MM/yyyy}
     * @param observacion observación (opcional)
     * @return respuesta exitosa o el error encontrado
     */
    public Respuesta actualizarPrestamo(int idPrestamo, int idLibro, int idLector, String fechaPrestamo,
            String fechaLimite, String observacion) {
        if (idPrestamo <= 0) {
            return Respuesta.error("Debe seleccionar un préstamo para modificar");
        }
        Respuesta validacion = validarCampos(idLibro, idLector, fechaPrestamo, fechaLimite, observacion);
        if (!validacion.isExitosa()) {
            return validacion;
        }
        try {
            Prestamo actual = dao.obtenerPorId(idPrestamo);
            if (actual == null) {
                return Respuesta.error("El préstamo no existe");
            }
            if (Prestamo.DEVUELTO.equals(actual.getEstado())) {
                return Respuesta.error("No se puede modificar un préstamo que ya fue devuelto");
            }
            Respuesta regla = verificarCambios(actual, idLibro, idLector);
            if (regla != null) {
                return regla;
            }
            Prestamo p = crearPrestamo(idPrestamo, idLibro, idLector, fechaPrestamo, fechaLimite, observacion);
            if (dao.actualizar(p, actual.getIdLibro())) {
                return Respuesta.ok("Préstamo actualizado correctamente");
            }
            return Respuesta.error("No se pudo actualizar el préstamo");
        } catch (SQLException e) {
            return Respuesta.error("Error de base de datos: " + e.getMessage());
        }
    }

    /**
     * Elimina un préstamo que no tenga devolución y devuelve el ejemplar al
     * stock si aún no se había devuelto.
     *
     * @param idPrestamo identificador del préstamo
     * @return respuesta exitosa o el motivo por el que no se eliminó
     */
    public Respuesta eliminarPrestamo(int idPrestamo) {
        if (idPrestamo <= 0) {
            return Respuesta.error("Debe seleccionar un préstamo para eliminar");
        }
        try {
            Prestamo p = dao.obtenerPorId(idPrestamo);
            if (p == null) {
                return Respuesta.error("El préstamo no existe");
            }
            if (dao.tieneDevolucion(idPrestamo)) {
                return Respuesta.error("No se puede eliminar: el préstamo tiene una devolución registrada");
            }
            if (dao.eliminar(p)) {
                return Respuesta.ok("Préstamo eliminado correctamente");
            }
            return Respuesta.error("No se pudo eliminar el préstamo");
        } catch (SQLException e) {
            return Respuesta.error("Error de base de datos: " + e.getMessage());
        }
    }

    /**
     * Revisa las reglas que dependen de lo que cambió al modificar: stock si
     * cambia el libro y estado/límite del lector si cambia el lector.
     *
     * @param actual préstamo tal como está en la base
     * @param idLibro libro nuevo
     * @param idLector lector nuevo
     * @return respuesta de error, o {@code null} si se cumplen las reglas
     * @throws SQLException si ocurre un error con la base de datos
     */
    private Respuesta verificarCambios(Prestamo actual, int idLibro, int idLector) throws SQLException {
        if (idLibro != actual.getIdLibro()) {
            Respuesta r = verificarStock(idLibro);
            if (r != null) {
                return r;
            }
        }
        if (idLector != actual.getIdLector()) {
            return verificarLector(idLector, actual.getIdPrestamo());
        }
        return null;
    }

    /**
     * Regla: el libro debe tener al menos un ejemplar disponible.
     *
     * @param idLibro libro a prestar
     * @return respuesta de error, o {@code null} si hay stock
     * @throws SQLException si ocurre un error con la base de datos
     */
    public Respuesta verificarStock(int idLibro) throws SQLException {
        int stock = dao.stockLibro(idLibro);
        if (stock < 0) {
            return Respuesta.error("El libro seleccionado no existe", CAMPO_LIBRO);
        }
        if (stock == 0) {
            return Respuesta.error("El libro no tiene ejemplares disponibles (stock 0)", CAMPO_LIBRO);
        }
        return null;
    }

    /**
     * Regla: el lector debe estar ACTIVO y tener menos de
     * {@value #MAX_PENDIENTES} préstamos sin devolver.
     *
     * @param idLector lector que recibe el libro
     * @param idPrestamoExcluir préstamo que no se cuenta (0 al registrar)
     * @return respuesta de error, o {@code null} si se cumplen las reglas
     * @throws SQLException si ocurre un error con la base de datos
     */
    public Respuesta verificarLector(int idLector, int idPrestamoExcluir) throws SQLException {
        String estado = dao.estadoLector(idLector);
        if (estado == null) {
            return Respuesta.error("El lector seleccionado no existe", CAMPO_LECTOR);
        }
        if (!Lector.ACTIVO.equals(estado)) {
            return Respuesta.error("El lector está SUSPENDIDO y no puede recibir préstamos", CAMPO_LECTOR);
        }
        if (dao.contarPendientesLector(idLector, idPrestamoExcluir) >= MAX_PENDIENTES) {
            return Respuesta.error("El lector ya tiene " + MAX_PENDIENTES
                    + " préstamos sin devolver", CAMPO_LECTOR);
        }
        return null;
    }

    /**
     * Valida todos los campos en orden y devuelve el primer error.
     *
     * @param idLibro libro seleccionado
     * @param idLector lector seleccionado
     * @param fechaPrestamo fecha del préstamo
     * @param fechaLimite fecha límite
     * @param observacion observación
     * @return respuesta exitosa si todo es válido, o el error con su campo
     */
    public Respuesta validarCampos(int idLibro, int idLector, String fechaPrestamo,
            String fechaLimite, String observacion) {
        String error = validarLibro(idLibro);
        if (error != null) {
            return Respuesta.error(error, CAMPO_LIBRO);
        }
        error = validarLector(idLector);
        if (error != null) {
            return Respuesta.error(error, CAMPO_LECTOR);
        }
        error = validarFechaPrestamo(fechaPrestamo);
        if (error != null) {
            return Respuesta.error(error, CAMPO_FECHA_PRESTAMO);
        }
        error = validarFechaLimite(fechaPrestamo, fechaLimite);
        if (error != null) {
            return Respuesta.error(error, CAMPO_FECHA_LIMITE);
        }
        error = validarObservacion(observacion);
        if (error != null) {
            return Respuesta.error(error, CAMPO_OBSERVACION);
        }
        return Respuesta.ok("Datos válidos");
    }

    /**
     * Valida que se haya seleccionado un libro.
     *
     * @param idLibro id del libro (0 o negativo si no se seleccionó)
     * @return mensaje de error o {@code null} si es válido
     */
    public String validarLibro(int idLibro) {
        return idLibro <= 0 ? "Debe seleccionar un libro" : null;
    }

    /**
     * Valida que se haya seleccionado un lector.
     *
     * @param idLector id del lector (0 o negativo si no se seleccionó)
     * @return mensaje de error o {@code null} si es válido
     */
    public String validarLector(int idLector) {
        return idLector <= 0 ? "Debe seleccionar un lector" : null;
    }

    /**
     * Valida la fecha del préstamo: obligatoria, formato {@code dd/MM/yyyy},
     * fecha real y no futura.
     *
     * @param fechaPrestamo fecha escrita
     * @return mensaje de error o {@code null} si es válida
     */
    public String validarFechaPrestamo(String fechaPrestamo) {
        if (fechaPrestamo == null || fechaPrestamo.isBlank()) {
            return "La fecha del préstamo es obligatoria";
        }
        LocalDate fecha = Fechas.convertir(fechaPrestamo);
        if (fecha == null) {
            return "La fecha del préstamo debe tener el formato " + Fechas.PATRON + " y ser una fecha real";
        }
        if (fecha.isAfter(LocalDate.now())) {
            return "La fecha del préstamo no puede ser futura";
        }
        return null;
    }

    /**
     * Valida la fecha límite: obligatoria, formato {@code dd/MM/yyyy}, no
     * anterior a la fecha del préstamo y como máximo {@value #DIAS_MAXIMOS}
     * días después. Se asume que la fecha del préstamo ya es válida.
     *
     * @param fechaPrestamo fecha del préstamo (ya validada)
     * @param fechaLimite fecha límite escrita
     * @return mensaje de error o {@code null} si es válida
     */
    public String validarFechaLimite(String fechaPrestamo, String fechaLimite) {
        if (fechaLimite == null || fechaLimite.isBlank()) {
            return "La fecha límite es obligatoria";
        }
        LocalDate limite = Fechas.convertir(fechaLimite);
        if (limite == null) {
            return "La fecha límite debe tener el formato " + Fechas.PATRON + " y ser una fecha real";
        }
        LocalDate inicio = Fechas.convertir(fechaPrestamo);
        if (limite.isBefore(inicio)) {
            return "La fecha límite no puede ser anterior a la fecha del préstamo";
        }
        if (limite.isAfter(inicio.plusDays(DIAS_MAXIMOS))) {
            return "La fecha límite no puede superar " + DIAS_MAXIMOS + " días desde el préstamo";
        }
        return null;
    }

    /**
     * Valida la observación: opcional, máximo 255 caracteres.
     *
     * @param observacion observación escrita
     * @return mensaje de error o {@code null} si es válida
     */
    public String validarObservacion(String observacion) {
        if (observacion != null && observacion.trim().length() > 255) {
            return "La observación no puede superar 255 caracteres";
        }
        return null;
    }

    /**
     * Calcula el estado de un préstamo sin devolver según su fecha límite.
     *
     * @param fechaLimite fecha límite del préstamo
     * @return VENCIDO si la fecha límite ya pasó; ACTIVO en otro caso
     */
    public String calcularEstado(LocalDate fechaLimite) {
        return fechaLimite.isBefore(LocalDate.now()) ? Prestamo.VENCIDO : Prestamo.ACTIVO;
    }

    /**
     * Construye un {@link Prestamo} con los datos ya validados.
     *
     * @param idPrestamo identificador (0 para uno nuevo)
     * @param idLibro libro
     * @param idLector lector
     * @param fechaPrestamo fecha del préstamo
     * @param fechaLimite fecha límite
     * @param observacion observación (puede estar vacía)
     * @return préstamo listo para guardar
     */
    private Prestamo crearPrestamo(int idPrestamo, int idLibro, int idLector, String fechaPrestamo,
            String fechaLimite, String observacion) {
        Prestamo p = new Prestamo();
        p.setIdPrestamo(idPrestamo);
        p.setIdLibro(idLibro);
        p.setIdLector(idLector);
        p.setFechaPrestamo(Fechas.convertir(fechaPrestamo));
        p.setFechaLimite(Fechas.convertir(fechaLimite));
        p.setEstado(calcularEstado(p.getFechaLimite()));
        p.setObservacion(observacion == null || observacion.isBlank() ? null : observacion.trim());
        return p;
    }
}
