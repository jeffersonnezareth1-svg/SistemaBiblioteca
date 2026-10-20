package modelo;

import java.time.LocalDate;

/**
 * Préstamo de un libro a un lector (tabla {@code prestamo}).
 *
 * Depende de {@link Libro} y {@link Lector}. Además de las columnas de la
 * tabla, guarda el título del libro y el nombre del lector para mostrarlos
 * en la tabla de la vista sin hacer otra consulta.
 *
 * @author Guashpa Gualli Jefferson Nezareth
 * @version 1.0
 */
public class Prestamo {

    /** Préstamo vigente: el libro no se ha devuelto y la fecha límite no ha pasado. */
    public static final String ACTIVO = "ACTIVO";

    /** Préstamo cuya fecha límite ya pasó sin devolución. */
    public static final String VENCIDO = "VENCIDO";

    /** Préstamo con devolución registrada. */
    public static final String DEVUELTO = "DEVUELTO";

    /** Identificador único (clave primaria). */
    private int idPrestamo;

    /** Libro prestado (clave foránea). */
    private int idLibro;

    /** Lector que recibe el libro (clave foránea). */
    private int idLector;

    /** Fecha en que se entregó el libro. */
    private LocalDate fechaPrestamo;

    /** Fecha máxima para devolver el libro. */
    private LocalDate fechaLimite;

    /** Estado del préstamo. */
    private String estado;

    /** Observación opcional. */
    private String observacion;

    /** Título del libro (dato de consulta, no se guarda). */
    private String tituloLibro;

    /** Nombre del lector (dato de consulta, no se guarda). */
    private String nombreLector;

    /**
     * Crea un objeto vacío.
     */
    public Prestamo() {
    }

    /**
     * Devuelve el dato: identificador del préstamo.
     *
     * @return identificador del préstamo
     */
    public int getIdPrestamo() {
        return idPrestamo;
    }

    /**
     * Asigna el dato: identificador del préstamo.
     *
     * @param idPrestamo identificador del préstamo
     */
    public void setIdPrestamo(int idPrestamo) {
        this.idPrestamo = idPrestamo;
    }

    /**
     * Devuelve el dato: id del libro prestado.
     *
     * @return id del libro prestado
     */
    public int getIdLibro() {
        return idLibro;
    }

    /**
     * Asigna el dato: id del libro prestado.
     *
     * @param idLibro id del libro prestado
     */
    public void setIdLibro(int idLibro) {
        this.idLibro = idLibro;
    }

    /**
     * Devuelve el dato: id del lector.
     *
     * @return id del lector
     */
    public int getIdLector() {
        return idLector;
    }

    /**
     * Asigna el dato: id del lector.
     *
     * @param idLector id del lector
     */
    public void setIdLector(int idLector) {
        this.idLector = idLector;
    }

    /**
     * Devuelve el dato: fecha del préstamo.
     *
     * @return fecha del préstamo
     */
    public LocalDate getFechaPrestamo() {
        return fechaPrestamo;
    }

    /**
     * Asigna el dato: fecha del préstamo.
     *
     * @param fechaPrestamo fecha del préstamo
     */
    public void setFechaPrestamo(LocalDate fechaPrestamo) {
        this.fechaPrestamo = fechaPrestamo;
    }

    /**
     * Devuelve el dato: fecha límite de devolución.
     *
     * @return fecha límite de devolución
     */
    public LocalDate getFechaLimite() {
        return fechaLimite;
    }

    /**
     * Asigna el dato: fecha límite de devolución.
     *
     * @param fechaLimite fecha límite de devolución
     */
    public void setFechaLimite(LocalDate fechaLimite) {
        this.fechaLimite = fechaLimite;
    }

    /**
     * Devuelve el dato: estado: ACTIVO, VENCIDO o DEVUELTO.
     *
     * @return estado: ACTIVO, VENCIDO o DEVUELTO
     */
    public String getEstado() {
        return estado;
    }

    /**
     * Asigna el dato: estado: ACTIVO, VENCIDO o DEVUELTO.
     *
     * @param estado estado: ACTIVO, VENCIDO o DEVUELTO
     */
    public void setEstado(String estado) {
        this.estado = estado;
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
     * Devuelve el dato: nombre completo del lector (solo para mostrar).
     *
     * @return nombre completo del lector (solo para mostrar)
     */
    public String getNombreLector() {
        return nombreLector;
    }

    /**
     * Asigna el dato: nombre completo del lector (solo para mostrar).
     *
     * @param nombreLector nombre completo del lector (solo para mostrar)
     */
    public void setNombreLector(String nombreLector) {
        this.nombreLector = nombreLector;
    }

    /**
     * Texto usado en el combo del módulo Devolución.
     *
     * @return número de préstamo, título del libro y lector
     */
    @Override
    public String toString() {
        return "N° " + idPrestamo + " - " + tituloLibro + " - " + nombreLector;
    }
}
