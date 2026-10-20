package vista;

/**
 * Panel de módulo que vuelve a leer sus datos de la base cuando el usuario lo
 * abre desde el menú. Así, por ejemplo, el stock de Libros se actualiza
 * después de registrar un préstamo.
 *
 * @author Guashpa Gualli Jefferson Nezareth
 * @version 1.0
 */
public interface Recargable {

    /**
     * Vuelve a cargar los datos del panel.
     */
    void recargar();
}
