package controlador;

import dao.DevolucionDAO;
import dao.PrestamoDAO;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import modelo.Devolucion;
import modelo.Prestamo;
import util.Fechas;
import util.Respuesta;

/**
 * Controlador del módulo Devolución (depende de Préstamo).
 *
 * <p>Valida los datos del formulario con un método pequeño por campo, calcula
 * los días de retraso y la multa, y aplica las reglas de negocio:</p>
 * <ul>
 * <li>Solo se devuelve un préstamo ACTIVO o VENCIDO que no tenga devolución.</li>
 * <li>La fecha de devolución no puede ser futura ni anterior al préstamo.</li>
 * <li>Multa: {@link #MULTA_POR_DIA} por día de retraso, más
 * {@link #RECARGO_DANADO} si el libro vuelve dañado o
 * {@link #RECARGO_PERDIDO} si se perdió.</li>
 * <li>Si el libro se perdió, el ejemplar no vuelve al stock.</li>
 * </ul>
 *
 * <ul>
 * <li>C: {@link #registrarDevolucion}</li>
 * <li>R: {@link #listarDevoluciones()} y {@link #buscarDevoluciones(String)}</li>
 * <li>U: {@link #actualizarDevolucion}</li>
 * <li>D: {@link #eliminarDevolucion(int)}</li>
 * </ul>
 *
 * @author Guashpa Gualli Jefferson Nezareth
 * @version 1.0
 */
public class DevolucionController {

    /** Estados posibles del libro al devolverlo. */
    public static final List<String> ESTADOS_LIBRO =
            Arrays.asList(Devolucion.BUENO, Devolucion.DANADO, Devolucion.PERDIDO);

    /** Multa por cada día de retraso (USD). */
    public static final BigDecimal MULTA_POR_DIA = new BigDecimal("0.50");
    /** Recargo si el libro vuelve dañado (USD). */
    public static final BigDecimal RECARGO_DANADO = new BigDecimal("5.00");
    /** Recargo si el libro se perdió (USD). */
    public static final BigDecimal RECARGO_PERDIDO = new BigDecimal("20.00");

    /** Nombre del campo préstamo en el formulario. */
    public static final String CAMPO_PRESTAMO = "prestamo";
    /** Nombre del campo fecha de devolución en el formulario. */
    public static final String CAMPO_FECHA = "fechaDevolucion";
    /** Nombre del campo estado del libro en el formulario. */
    public static final String CAMPO_ESTADO = "estadoLibro";
    /** Nombre del campo observación en el formulario. */
    public static final String CAMPO_OBSERVACION = "observacion";

    private final DevolucionDAO dao;
    private final PrestamoDAO prestamoDao;

    /**
     * Crea el controlador con los DAO reales.
     */
    public DevolucionController() {
        this(new DevolucionDAO(), new PrestamoDAO());
    }

    /**
     * Crea el controlador con DAO dados (útil en pruebas unitarias).
     *
     * @param dao DAO de devoluciones
     * @param prestamoDao DAO de préstamos
     */
    public DevolucionController(DevolucionDAO dao, PrestamoDAO prestamoDao) {
        this.dao = dao;
        this.prestamoDao = prestamoDao;
    }

    /**
     * Lista todas las devoluciones.
     *
     * @return lista de devoluciones
     * @throws SQLException si ocurre un error con la base de datos
     */
    public List<Devolucion> listarDevoluciones() throws SQLException {
        return dao.listar();
    }

    /**
     * Busca devoluciones por libro, lector o estado del libro. Si el texto
     * está vacío devuelve todas.
     *
     * @param texto texto a buscar
     * @return devoluciones que coinciden
     * @throws SQLException si ocurre un error con la base de datos
     */
    public List<Devolucion> buscarDevoluciones(String texto) throws SQLException {
        if (texto == null || texto.isBlank()) {
            return dao.listar();
        }
        return dao.buscar(texto.trim());
    }

    /**
     * Lista los préstamos pendientes de devolución, para el combo del
     * formulario.
     *
     * @return préstamos ACTIVO o VENCIDO sin devolución
     * @throws SQLException si ocurre un error con la base de datos
     */
    public List<Prestamo> listarPrestamosPendientes() throws SQLException {
        return prestamoDao.listarPendientes();
    }

    /**
     * Obtiene un préstamo por su id (para mostrarlo al modificar).
     *
     * @param idPrestamo identificador del préstamo
     * @return el préstamo o {@code null}
     * @throws SQLException si ocurre un error con la base de datos
     */
    public Prestamo obtenerPrestamo(int idPrestamo) throws SQLException {
        return prestamoDao.obtenerPorId(idPrestamo);
    }

    /**
     * Registra la devolución de un préstamo.
     *
     * @param idPrestamo préstamo seleccionado (0 si no se seleccionó)
     * @param fechaDevolucion fecha {@code dd/MM/yyyy}
     * @param estadoLibro BUENO, DANADO o PERDIDO
     * @param observacion observación (opcional)
     * @return respuesta exitosa (con la multa) o el error encontrado
     */
    public Respuesta registrarDevolucion(int idPrestamo, String fechaDevolucion, String estadoLibro,
            String observacion) {
        String error = validarPrestamo(idPrestamo);
        if (error != null) {
            return Respuesta.error(error, CAMPO_PRESTAMO);
        }
        try {
            Prestamo p = prestamoDao.obtenerPorId(idPrestamo);
            Respuesta regla = verificarPrestamoPendiente(p);
            if (regla != null) {
                return regla;
            }
            Respuesta validacion = validarCampos(fechaDevolucion, estadoLibro, observacion, p.getFechaPrestamo());
            if (!validacion.isExitosa()) {
                return validacion;
            }
            Devolucion d = crearDevolucion(0, p, fechaDevolucion, estadoLibro, observacion);
            boolean vuelveAlStock = !Devolucion.PERDIDO.equals(d.getEstadoLibro());
            if (dao.insertar(d, p.getIdLibro(), vuelveAlStock)) {
                return Respuesta.ok("Devolución registrada. Días de retraso: " + d.getDiasRetraso()
                        + ". Multa: $" + d.getMulta());
            }
            return Respuesta.error("No se pudo registrar la devolución");
        } catch (SQLException e) {
            return Respuesta.error("Error de base de datos: " + e.getMessage());
        }
    }

    /**
     * Modifica la fecha, el estado del libro o la observación de una
     * devolución. Recalcula días de retraso y multa.
     *
     * @param idDevolucion devolución a modificar
     * @param fechaDevolucion fecha {@code dd/MM/yyyy}
     * @param estadoLibro BUENO, DANADO o PERDIDO
     * @param observacion observación (opcional)
     * @return respuesta exitosa o el error encontrado
     */
    public Respuesta actualizarDevolucion(int idDevolucion, String fechaDevolucion, String estadoLibro,
            String observacion) {
        if (idDevolucion <= 0) {
            return Respuesta.error("Debe seleccionar una devolución para modificar");
        }
        try {
            Devolucion actual = dao.obtenerPorId(idDevolucion);
            if (actual == null) {
                return Respuesta.error("La devolución no existe");
            }
            Prestamo p = prestamoDao.obtenerPorId(actual.getIdPrestamo());
            Respuesta validacion = validarCampos(fechaDevolucion, estadoLibro, observacion, p.getFechaPrestamo());
            if (!validacion.isExitosa()) {
                return validacion;
            }
            Devolucion d = crearDevolucion(idDevolucion, p, fechaDevolucion, estadoLibro, observacion);
            int ajuste = calcularAjusteStock(actual.getEstadoLibro(), d.getEstadoLibro());
            if (dao.actualizar(d, p.getIdLibro(), ajuste)) {
                return Respuesta.ok("Devolución actualizada. Multa: $" + d.getMulta());
            }
            return Respuesta.error("No se pudo actualizar: el libro no tiene ejemplares para descontar");
        } catch (SQLException e) {
            return Respuesta.error("Error de base de datos: " + e.getMessage());
        }
    }

    /**
     * Elimina una devolución. El préstamo vuelve a quedar pendiente (ACTIVO o
     * VENCIDO) y, si el ejemplar había vuelto al stock, se descuenta.
     *
     * @param idDevolucion identificador de la devolución
     * @return respuesta exitosa o el motivo por el que no se eliminó
     */
    public Respuesta eliminarDevolucion(int idDevolucion) {
        if (idDevolucion <= 0) {
            return Respuesta.error("Debe seleccionar una devolución para eliminar");
        }
        try {
            Devolucion d = dao.obtenerPorId(idDevolucion);
            if (d == null) {
                return Respuesta.error("La devolución no existe");
            }
            Prestamo p = prestamoDao.obtenerPorId(d.getIdPrestamo());
            boolean restarStock = !Devolucion.PERDIDO.equals(d.getEstadoLibro());
            if (dao.eliminar(d, p.getIdLibro(), restarStock)) {
                return Respuesta.ok("Devolución eliminada. El préstamo vuelve a estar pendiente");
            }
            return Respuesta.error("No se puede eliminar: el ejemplar ya se volvió a prestar (stock 0)");
        } catch (SQLException e) {
            return Respuesta.error("Error de base de datos: " + e.getMessage());
        }
    }

    /**
     * Regla: el préstamo debe existir, no estar DEVUELTO y no tener
     * devolución.
     *
     * @param p préstamo leído de la base (puede ser {@code null})
     * @return respuesta de error, o {@code null} si se puede devolver
     * @throws SQLException si ocurre un error con la base de datos
     */
    public Respuesta verificarPrestamoPendiente(Prestamo p) throws SQLException {
        if (p == null) {
            return Respuesta.error("El préstamo no existe", CAMPO_PRESTAMO);
        }
        if (Prestamo.DEVUELTO.equals(p.getEstado()) || dao.existePorPrestamo(p.getIdPrestamo())) {
            return Respuesta.error("El préstamo ya tiene una devolución registrada", CAMPO_PRESTAMO);
        }
        return null;
    }

    /**
     * Valida los campos del formulario (excepto el préstamo) y devuelve el
     * primer error.
     *
     * @param fechaDevolucion fecha escrita
     * @param estadoLibro estado seleccionado
     * @param observacion observación
     * @param fechaPrestamo fecha del préstamo que se devuelve
     * @return respuesta exitosa si todo es válido, o el error con su campo
     */
    public Respuesta validarCampos(String fechaDevolucion, String estadoLibro, String observacion,
            LocalDate fechaPrestamo) {
        String error = validarFechaDevolucion(fechaDevolucion, fechaPrestamo);
        if (error != null) {
            return Respuesta.error(error, CAMPO_FECHA);
        }
        error = validarEstadoLibro(estadoLibro);
        if (error != null) {
            return Respuesta.error(error, CAMPO_ESTADO);
        }
        error = validarObservacion(observacion);
        if (error != null) {
            return Respuesta.error(error, CAMPO_OBSERVACION);
        }
        return Respuesta.ok("Datos válidos");
    }

    /**
     * Valida que se haya seleccionado un préstamo.
     *
     * @param idPrestamo id del préstamo (0 o negativo si no se seleccionó)
     * @return mensaje de error o {@code null} si es válido
     */
    public String validarPrestamo(int idPrestamo) {
        return idPrestamo <= 0 ? "Debe seleccionar un préstamo" : null;
    }

    /**
     * Valida la fecha de devolución: obligatoria, formato
     * {@code dd/MM/yyyy}, no futura y no anterior a la fecha del préstamo.
     *
     * @param fechaDevolucion fecha escrita
     * @param fechaPrestamo fecha del préstamo
     * @return mensaje de error o {@code null} si es válida
     */
    public String validarFechaDevolucion(String fechaDevolucion, LocalDate fechaPrestamo) {
        if (fechaDevolucion == null || fechaDevolucion.isBlank()) {
            return "La fecha de devolución es obligatoria";
        }
        LocalDate fecha = Fechas.convertir(fechaDevolucion);
        if (fecha == null) {
            return "La fecha de devolución debe tener el formato " + Fechas.PATRON + " y ser una fecha real";
        }
        if (fecha.isAfter(LocalDate.now())) {
            return "La fecha de devolución no puede ser futura";
        }
        if (fecha.isBefore(fechaPrestamo)) {
            return "La fecha de devolución no puede ser anterior a la fecha del préstamo ("
                    + Fechas.formatear(fechaPrestamo) + ")";
        }
        return null;
    }

    /**
     * Valida el estado del libro: obligatorio y debe ser BUENO, DANADO o
     * PERDIDO.
     *
     * @param estadoLibro estado seleccionado
     * @return mensaje de error o {@code null} si es válido
     */
    public String validarEstadoLibro(String estadoLibro) {
        if (estadoLibro == null || estadoLibro.isBlank()) {
            return "Debe seleccionar el estado del libro";
        }
        if (!ESTADOS_LIBRO.contains(estadoLibro.trim())) {
            return "El estado del libro no es válido";
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
     * Calcula los días de retraso de una devolución.
     *
     * @param fechaLimite fecha límite del préstamo
     * @param fechaDevolucion fecha en que se devolvió
     * @return días después de la fecha límite (0 si se devolvió a tiempo)
     */
    public int calcularDiasRetraso(LocalDate fechaLimite, LocalDate fechaDevolucion) {
        long dias = ChronoUnit.DAYS.between(fechaLimite, fechaDevolucion);
        return dias > 0 ? (int) dias : 0;
    }

    /**
     * Calcula la multa: días de retraso por {@link #MULTA_POR_DIA}, más el
     * recargo según el estado del libro.
     *
     * @param diasRetraso días de retraso (0 o más)
     * @param estadoLibro BUENO, DANADO o PERDIDO
     * @return multa en dólares con 2 decimales
     */
    public BigDecimal calcularMulta(int diasRetraso, String estadoLibro) {
        BigDecimal multa = MULTA_POR_DIA.multiply(BigDecimal.valueOf(diasRetraso));
        if (Devolucion.DANADO.equals(estadoLibro)) {
            multa = multa.add(RECARGO_DANADO);
        } else if (Devolucion.PERDIDO.equals(estadoLibro)) {
            multa = multa.add(RECARGO_PERDIDO);
        }
        return multa.setScale(2);
    }

    /**
     * Calcula cómo cambia el stock al modificar el estado del libro: si deja
     * de estar perdido vuelve un ejemplar (+1); si pasa a perdido se
     * descuenta (-1).
     *
     * @param estadoAnterior estado guardado antes
     * @param estadoNuevo estado nuevo
     * @return +1, -1 o 0
     */
    public int calcularAjusteStock(String estadoAnterior, String estadoNuevo) {
        boolean antesPerdido = Devolucion.PERDIDO.equals(estadoAnterior);
        boolean ahoraPerdido = Devolucion.PERDIDO.equals(estadoNuevo);
        if (antesPerdido && !ahoraPerdido) {
            return 1;
        }
        if (!antesPerdido && ahoraPerdido) {
            return -1;
        }
        return 0;
    }

    /**
     * Construye una {@link Devolucion} con los datos ya validados y los
     * cálculos de retraso y multa.
     *
     * @param idDevolucion identificador (0 para una nueva)
     * @param p préstamo que se devuelve
     * @param fechaDevolucion fecha de devolución
     * @param estadoLibro estado del libro
     * @param observacion observación (puede estar vacía)
     * @return devolución lista para guardar
     */
    private Devolucion crearDevolucion(int idDevolucion, Prestamo p, String fechaDevolucion,
            String estadoLibro, String observacion) {
        Devolucion d = new Devolucion();
        d.setIdDevolucion(idDevolucion);
        d.setIdPrestamo(p.getIdPrestamo());
        d.setFechaDevolucion(Fechas.convertir(fechaDevolucion));
        d.setEstadoLibro(estadoLibro.trim());
        d.setDiasRetraso(calcularDiasRetraso(p.getFechaLimite(), d.getFechaDevolucion()));
        d.setMulta(calcularMulta(d.getDiasRetraso(), d.getEstadoLibro()));
        d.setObservacion(observacion == null || observacion.isBlank() ? null : observacion.trim());
        return d;
    }
}
