package modelo;

/**
 * Lector o socio de la biblioteca (tabla {@code lector}). Entidad
 * independiente; los préstamos dependen de ella.
 *
 * @author Guashpa Gualli Jefferson Nezareth
 * @version 1.0
 */
public class Lector {

    /** Estado de un lector que puede pedir préstamos. */
    public static final String ACTIVO = "ACTIVO";
    /** Estado de un lector que no puede pedir préstamos. */
    public static final String SUSPENDIDO = "SUSPENDIDO";

    /** Identificador único (clave primaria). */
    private int idLector;

    /** Cédula de identidad ecuatoriana (10 dígitos). */
    private String cedula;

    /** Nombres del lector. */
    private String nombres;

    /** Apellidos del lector. */
    private String apellidos;

    /** Correo electrónico (único). */
    private String correo;

    /** Teléfono celular de 10 dígitos. */
    private String telefono;

    /** Dirección domiciliaria; opcional. */
    private String direccion;

    /** Estado: ACTIVO o SUSPENDIDO. */
    private String estado;

    /**
     * Crea un lector vacío.
     */
    public Lector() {
    }

    /**
     * Crea un lector con todos sus datos.
     *
     * @param idLector identificador del lector
     * @param cedula cédula ecuatoriana de 10 dígitos
     * @param nombres nombres
     * @param apellidos apellidos
     * @param correo correo electrónico
     * @param telefono teléfono celular
     * @param direccion dirección o {@code null}
     * @param estado estado: ACTIVO o SUSPENDIDO
     */
    public Lector(int idLector, String cedula, String nombres, String apellidos,
            String correo, String telefono, String direccion, String estado) {
        this.idLector = idLector;
        this.cedula = cedula;
        this.nombres = nombres;
        this.apellidos = apellidos;
        this.correo = correo;
        this.telefono = telefono;
        this.direccion = direccion;
        this.estado = estado;
    }

    /**
     * Devuelve el dato: identificador del lector.
     *
     * @return identificador del lector
     */
    public int getIdLector() {
        return idLector;
    }

    /**
     * Asigna el dato: identificador del lector.
     *
     * @param idLector identificador del lector
     */
    public void setIdLector(int idLector) {
        this.idLector = idLector;
    }

    /**
     * Devuelve el dato: cédula ecuatoriana de 10 dígitos.
     *
     * @return cédula ecuatoriana de 10 dígitos
     */
    public String getCedula() {
        return cedula;
    }

    /**
     * Asigna el dato: cédula ecuatoriana de 10 dígitos.
     *
     * @param cedula cédula ecuatoriana de 10 dígitos
     */
    public void setCedula(String cedula) {
        this.cedula = cedula;
    }

    /**
     * Devuelve el dato: nombres.
     *
     * @return nombres
     */
    public String getNombres() {
        return nombres;
    }

    /**
     * Asigna el dato: nombres.
     *
     * @param nombres nombres
     */
    public void setNombres(String nombres) {
        this.nombres = nombres;
    }

    /**
     * Devuelve el dato: apellidos.
     *
     * @return apellidos
     */
    public String getApellidos() {
        return apellidos;
    }

    /**
     * Asigna el dato: apellidos.
     *
     * @param apellidos apellidos
     */
    public void setApellidos(String apellidos) {
        this.apellidos = apellidos;
    }

    /**
     * Devuelve el dato: correo electrónico.
     *
     * @return correo electrónico
     */
    public String getCorreo() {
        return correo;
    }

    /**
     * Asigna el dato: correo electrónico.
     *
     * @param correo correo electrónico
     */
    public void setCorreo(String correo) {
        this.correo = correo;
    }

    /**
     * Devuelve el dato: teléfono celular.
     *
     * @return teléfono celular
     */
    public String getTelefono() {
        return telefono;
    }

    /**
     * Asigna el dato: teléfono celular.
     *
     * @param telefono teléfono celular
     */
    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    /**
     * Devuelve el dato: dirección o {@code null}.
     *
     * @return dirección o {@code null}
     */
    public String getDireccion() {
        return direccion;
    }

    /**
     * Asigna el dato: dirección o {@code null}.
     *
     * @param direccion dirección o {@code null}
     */
    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    /**
     * Devuelve el dato: estado: ACTIVO o SUSPENDIDO.
     *
     * @return estado: ACTIVO o SUSPENDIDO
     */
    public String getEstado() {
        return estado;
    }

    /**
     * Asigna el dato: estado: ACTIVO o SUSPENDIDO.
     *
     * @param estado estado: ACTIVO o SUSPENDIDO
     */
    public void setEstado(String estado) {
        this.estado = estado;
    }

    /**
     * Texto usado en listas y combos (por ejemplo, en el módulo Préstamo).
     *
     * @return cédula y nombre completo
     */
    @Override
    public String toString() {
        return cedula + " - " + nombres + " " + apellidos;
    }
}
