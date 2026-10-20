package modelo;

import java.time.LocalDate;

/**
 * Autor de libros (tabla {@code autor}). Entidad independiente.
 *
 * @author Guashpa Gualli Jefferson Nezareth
 * @version 1.0
 */
public class Autor {

    /** Identificador único (clave primaria). */
    private int idAutor;

    /** Nombres del autor. */
    private String nombres;

    /** Apellidos del autor. */
    private String apellidos;

    /** Nacionalidad (por ejemplo, Ecuatoriana). */
    private String nacionalidad;

    /** Fecha de nacimiento; puede ser {@code null} si se desconoce. */
    private LocalDate fechaNacimiento;

    /** Biografía breve; opcional. */
    private String biografia;

    /**
     * Crea un autor vacío.
     */
    public Autor() {
    }

    /**
     * Crea un autor con todos sus datos.
     *
     * @param idAutor identificador (0 si aún no se registra)
     * @param nombres nombres
     * @param apellidos apellidos
     * @param nacionalidad nacionalidad
     * @param fechaNacimiento fecha de nacimiento o {@code null}
     * @param biografia biografía o {@code null}
     */
    public Autor(int idAutor, String nombres, String apellidos, String nacionalidad,
            LocalDate fechaNacimiento, String biografia) {
        this.idAutor = idAutor;
        this.nombres = nombres;
        this.apellidos = apellidos;
        this.nacionalidad = nacionalidad;
        this.fechaNacimiento = fechaNacimiento;
        this.biografia = biografia;
    }

    /**
     * Devuelve el identificador.
     *
     * @return identificador del autor
     */
    public int getIdAutor() {
        return idAutor;
    }

    /**
     * Asigna el identificador.
     *
     * @param idAutor identificador del autor
     */
    public void setIdAutor(int idAutor) {
        this.idAutor = idAutor;
    }

    /**
     * Devuelve los nombres.
     *
     * @return nombres
     */
    public String getNombres() {
        return nombres;
    }

    /**
     * Asigna los nombres.
     *
     * @param nombres nombres
     */
    public void setNombres(String nombres) {
        this.nombres = nombres;
    }

    /**
     * Devuelve los apellidos.
     *
     * @return apellidos
     */
    public String getApellidos() {
        return apellidos;
    }

    /**
     * Asigna los apellidos.
     *
     * @param apellidos apellidos
     */
    public void setApellidos(String apellidos) {
        this.apellidos = apellidos;
    }

    /**
     * Devuelve la nacionalidad.
     *
     * @return nacionalidad
     */
    public String getNacionalidad() {
        return nacionalidad;
    }

    /**
     * Asigna la nacionalidad.
     *
     * @param nacionalidad nacionalidad
     */
    public void setNacionalidad(String nacionalidad) {
        this.nacionalidad = nacionalidad;
    }

    /**
     * Devuelve la fecha de nacimiento.
     *
     * @return fecha de nacimiento o {@code null}
     */
    public LocalDate getFechaNacimiento() {
        return fechaNacimiento;
    }

    /**
     * Asigna la fecha de nacimiento.
     *
     * @param fechaNacimiento fecha de nacimiento o {@code null}
     */
    public void setFechaNacimiento(LocalDate fechaNacimiento) {
        this.fechaNacimiento = fechaNacimiento;
    }

    /**
     * Devuelve la biografía.
     *
     * @return biografía o {@code null}
     */
    public String getBiografia() {
        return biografia;
    }

    /**
     * Asigna la biografía.
     *
     * @param biografia biografía o {@code null}
     */
    public void setBiografia(String biografia) {
        this.biografia = biografia;
    }

    /**
     * Nombre completo del autor.
     *
     * @return nombres y apellidos
     */
    @Override
    public String toString() {
        return nombres + " " + apellidos;
    }
}
