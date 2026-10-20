package util;

/**
 * Resultado de una operación de la capa controlador.
 *
 * <p>Indica si la operación fue exitosa, el mensaje que se muestra al usuario
 * y, cuando hay un error de validación, el nombre del campo que lo produjo
 * para que la vista pueda enfocarlo.</p>
 *
 * @author Guashpa Gualli Jefferson Nezareth
 * @version 1.0
 */
public final class Respuesta {

    private final boolean exitosa;
    private final String mensaje;
    private final String campo;

    /**
     * Crea una respuesta. Se usa a través de {@link #ok(String)} y
     * {@link #error(String, String)}.
     *
     * @param exitosa {@code true} si la operación se completó
     * @param mensaje mensaje para el usuario
     * @param campo campo relacionado con el error, o {@code null}
     */
    private Respuesta(boolean exitosa, String mensaje, String campo) {
        this.exitosa = exitosa;
        this.mensaje = mensaje;
        this.campo = campo;
    }

    /**
     * Crea una respuesta exitosa.
     *
     * @param mensaje mensaje de confirmación
     * @return respuesta exitosa
     */
    public static Respuesta ok(String mensaje) {
        return new Respuesta(true, mensaje, null);
    }

    /**
     * Crea una respuesta de error asociada a un campo del formulario.
     *
     * @param mensaje descripción del error
     * @param campo nombre del campo que causó el error
     * @return respuesta con error
     */
    public static Respuesta error(String mensaje, String campo) {
        return new Respuesta(false, mensaje, campo);
    }

    /**
     * Crea una respuesta de error general (no asociada a un campo).
     *
     * @param mensaje descripción del error
     * @return respuesta con error
     */
    public static Respuesta error(String mensaje) {
        return new Respuesta(false, mensaje, null);
    }

    /**
     * Indica si la operación fue exitosa.
     *
     * @return {@code true} si fue exitosa
     */
    public boolean isExitosa() {
        return exitosa;
    }

    /**
     * Devuelve el mensaje para el usuario.
     *
     * @return mensaje de éxito o de error
     */
    public String getMensaje() {
        return mensaje;
    }

    /**
     * Devuelve el campo asociado al error.
     *
     * @return nombre del campo o {@code null} si no aplica
     */
    public String getCampo() {
        return campo;
    }

    @Override
    public String toString() {
        return (exitosa ? "OK: " : "ERROR: ") + mensaje;
    }
}
