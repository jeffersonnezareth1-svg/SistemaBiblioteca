package modelo;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Devolución de un préstamo (tabla {@code devolucion}).
 *
 * Depende de {@link Prestamo}: cada préstamo tiene como máximo una
 * devolución. Los días de retraso y la multa los calcula
 * {@link controlador.DevolucionController}. También guarda datos del
 * préstamo (libro, lector y fechas) para mostrarlos en la vista.
 *
 * @author Guashpa Gualli Jefferson Nezareth
 * @version 1.0
 */
public class Devolucion {

    /** El libro vuelve en buen estado. */
    public static final String BUENO = "BUENO";

    /** El libro vuelve dañado. */
    public static final String DANADO = "DANADO";

    /** El lector perdió el libro. */
    public static final String PERDIDO = "PERDIDO";

    /** Identificador único (clave primaria). */
    private int idDevolucion;

    /** Préstamo que se devuelve (clave foránea, única). */
    private int idPrestamo;

    /** Fecha en que se devolvió el libro. */
    private LocalDate fechaDevolucion;

    /** Estado en que vuelve el libro. */
    private String estadoLibro;

    /** Días de retraso respecto a la fecha límite. */
    private int diasRetraso;

    /** Multa a pagar en dólares. */
    private BigDecimal multa;

    /** Observación opcional. */
    private String observacion;

    /** Título del libro prestado (dato de consulta). */
    private String tituloLibro;

    /** Nombre del lector (dato de consulta). */
    private String nombreLector;

    /** Fecha del préstamo (dato de consulta). */
    private LocalDate fechaPrestamo;

    /** Fecha límite del préstamo (dato de consulta). */
    private LocalDate fechaLimite;

    /**
     * Crea un objeto vacío.
     */
    public Devolucion() {
    }

    /**
     * Devuelve el dato: identificador de la devolución.
     *
     * @return identificador de la devolución
     */
    public int getIdDevolucion() {
        return idDevolucion;
    }

    /**
     * Asigna el dato: identificador de la devolución.
     *
     * @param idDevolucion identificador de la devolución
     */
    public void setIdDevolucion(int idDevolucion) {
        this.idDevolucion = idDevolucion;
    }

    /**
     * Devuelve el dato: id del préstamo devuelto.
     *
     * @return id del préstamo devuelto
     */
    public int getIdPrestamo() {
        return idPrestamo;
    }

    /**
     * Asigna el dato: id del préstamo devuelto.
     *
     * @param idPrestamo id del préstamo devuelto
     */
    public void setIdPrestamo(int idPrestamo) {
        this.idPrestamo = idPrestamo;
    }

    /**
     * Devuelve el dato: fecha de devolución.
     *
     * @return fecha de devolución
     */
    public LocalDate getFechaDevolucion() {
        return fechaDevolucion;
    }

    /**
     * Asigna el dato: fecha de devolución.
     *
     * @param fechaDevolucion fecha de devolución
     */
    public void setFechaDevolucion(LocalDate fechaDevolucion) {
        this.fechaDevolucion = fechaDevolucion;
    }

    /**
     * Devuelve el dato: estado del libro: BUENO, DANADO o PERDIDO.
     *
     * @return estado del libro: BUENO, DANADO o PERDIDO
     */
    public String getEstadoLibro() {
        return estadoLibro;
    }

    /**
     * Asigna el dato: estado del libro: BUENO, DANADO o PERDIDO.
     *
     * @param estadoLibro estado del libro: BUENO, DANADO o PERDIDO
     */
    public void setEstadoLibro(String estadoLibro) {
        this.estadoLibro = estadoLibro;
    }

    /**
     * Devuelve el dato: días de retraso.
     *
     * @return días de retraso
     */
    public int getDiasRetraso() {
        return diasRetraso;
    }

    /**
     * Asigna el dato: días de retraso.
     *
     * @param diasRetraso días de retraso
     */
    public void setDiasRetraso(int diasRetraso) {
        this.diasRetraso = diasRetraso;
    }

    /**
     * Devuelve el dato: multa en dólares.
     *
     * @return multa en dólares
     */
    public BigDecimal getMulta() {
        return multa;
    }

    /**
     * Asigna el dato: multa en dólares.
     *
     * @param multa multa en dólares
     */
    public void setMulta(BigDecimal multa) {
        this.multa = multa;
    }

    /**
     * Devuelve el dato: observación o {@code null}.
     *
     * @return observación o {@code null}
     */
    public String getObservacion() {
        return observacion;
    }

    /**
     * Asigna el dato: observación o {@code null}.
     *
     * @param observacion observación o {@code null}
     */
    public void setObservacion(String observacion) {
        this.observacion = observacion;
    }

    /**
     * Devuelve el dato: título del libro (solo para mostrar).
     *
     * @return título del libro (solo para mostrar)
     */
    public String getTituloLibro() {
        return tituloLibro;
    }

    /**
     * Asigna el dato: título del libro (solo para mostrar).
     *
     * @param tituloLibro título del libro (solo para mostrar)
     */
    public void setTituloLibro(String tituloLibro) {
        this.tituloLibro = tituloLibro;
    }

    /**
     * Devuelve el dato: nombre del lector (solo para mostrar).
     *
     * @return nombre del lector (solo para mostrar)
     */
    public String getNombreLector() {
        return nombreLector;
    }

    /**
     * Asigna el dato: nombre del lector (solo para mostrar).
     *
     * @param nombreLector nombre del lector (solo para mostrar)
     */
    public void setNombreLector(String nombreLector) {
        this.nombreLector = nombreLector;
    }

    /**
     * Devuelve el dato: fecha del préstamo (solo para mostrar).
     *
     * @return fecha del préstamo (solo para mostrar)
     */
    public LocalDate getFechaPrestamo() {
        return fechaPrestamo;
    }

    /**
     * Asigna el dato: fecha del préstamo (solo para mostrar).
     *
     * @param fechaPrestamo fecha del préstamo (solo para mostrar)
     */
    public void setFechaPrestamo(LocalDate fechaPrestamo) {
        this.fechaPrestamo = fechaPrestamo;
    }

    /**
     * Devuelve el dato: fecha límite del préstamo (solo para mostrar).
     *
     * @return fecha límite del préstamo (solo para mostrar)
     */
    public LocalDate getFechaLimite() {
        return fechaLimite;
    }

    /**
     * Asigna el dato: fecha límite del préstamo (solo para mostrar).
     *
     * @param fechaLimite fecha límite del préstamo (solo para mostrar)
     */
    public void setFechaLimite(LocalDate fechaLimite) {
        this.fechaLimite = fechaLimite;
    }

    /**
     * Texto descriptivo de la devolución.
     *
     * @return número de devolución y título del libro
     */
    @Override
    public String toString() {
        return "Devolución N° " + idDevolucion + " - " + tituloLibro;
    }
}
