package vista;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 * Punto de entrada del Sistema de Gestión de Biblioteca.
 *
 * <p>Aplica el aspecto visual Nimbus y abre la ventana de inicio de sesión.</p>
 *
 * @author Guashpa Gualli Jefferson Nezareth
 * @version 1.0
 */
public final class SistemaBiblioteca {

    /**
     * Constructor privado: la clase solo contiene el método {@code main}.
     */
    private SistemaBiblioteca() {
    }

    /**
     * Inicia la aplicación.
     *
     * @param args argumentos de la línea de comandos (no se usan)
     */
    public static void main(String[] args) {
        aplicarAspecto();
        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
    }

    /**
     * Aplica el aspecto Nimbus si está disponible; si no, se usa el aspecto
     * por defecto de Java.
     */
    private static void aplicarAspecto() {
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    return;
                }
            }
        } catch (ReflectiveOperationException | javax.swing.UnsupportedLookAndFeelException e) {
            System.err.println("No se pudo aplicar el aspecto Nimbus: " + e.getMessage());
        }
    }
}
