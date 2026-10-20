package modelo;

/**
 * Usuario que puede iniciar sesión en el sistema.
 *
 * <p>Los usuarios se insertan directamente en la base de datos; el sistema
 * solo los lee para autenticar (no existe CRUD de usuarios). La contraseña no
 * se carga en este objeto.</p>
 *
 * @author Guashpa Gualli Jefferson Nezareth
 * @version 1.0
 */
public class Usuario {

    /** Identificador único (clave primaria). */
    private int idUsuario;

    /** Nombre con el que inicia sesión. */
    private String nombreUsuario;

    /** Nombre completo. */
    private String nombres;

    /** Rol: ADMINISTRADOR o BIBLIOTECARIO. */
    private String rol;

    /** Indica si puede ingresar al sistema. */
    private boolean activo;

    /**
     * Crea un usuario vacío.
     */
    public Usuario() {
    }

    /**
     * Crea un usuario con todos sus datos.
     *
     * @param idUsuario identificador
     * @param nombreUsuario nombre con el que inicia sesión
     * @param nombres nombre completo
     * @param rol ADMINISTRADOR o BIBLIOTECARIO
     * @param activo {@code true} si puede ingresar al sistema
     */
    public Usuario(int idUsuario, String nombreUsuario, String nombres, String rol, boolean activo) {
        this.idUsuario = idUsuario;
        this.nombreUsuario = nombreUsuario;
        this.nombres = nombres;
        this.rol = rol;
        this.activo = activo;
    }

    /**
     * Devuelve el dato: identificador del usuario.
     *
     * @return identificador del usuario
     */
    public int getIdUsuario() {
        return idUsuario;
    }

    /**
     * Asigna el dato: identificador del usuario.
     *
     * @param idUsuario identificador del usuario
     */
    public void setIdUsuario(int idUsuario) {
        this.idUsuario = idUsuario;
    }

    /**
     * Devuelve el dato: nombre con el que inicia sesión.
     *
     * @return nombre con el que inicia sesión
     */
    public String getNombreUsuario() {
        return nombreUsuario;
    }

    /**
     * Asigna el dato: nombre con el que inicia sesión.
     *
     * @param nombreUsuario nombre con el que inicia sesión
     */
    public void setNombreUsuario(String nombreUsuario) {
        this.nombreUsuario = nombreUsuario;
    }

    /**
     * Devuelve el dato: nombre completo.
     *
     * @return nombre completo
     */
    public String getNombres() {
        return nombres;
    }

    /**
     * Asigna el dato: nombre completo.
     *
     * @param nombres nombre completo
     */
    public void setNombres(String nombres) {
        this.nombres = nombres;
    }

    /**
     * Devuelve el dato: rol del usuario.
     *
     * @return rol del usuario
     */
    public String getRol() {
        return rol;
    }

    /**
     * Asigna el dato: rol del usuario.
     *
     * @param rol rol del usuario
     */
    public void setRol(String rol) {
        this.rol = rol;
    }

    /**
     * Devuelve el dato: {@code true} si el usuario está activo.
     *
     * @return {@code true} si el usuario está activo
     */
    public boolean isActivo() {
        return activo;
    }

    /**
     * Asigna el dato: estado del usuario.
     *
     * @param activo estado del usuario
     */
    public void setActivo(boolean activo) {
        this.activo = activo;
    }
}
