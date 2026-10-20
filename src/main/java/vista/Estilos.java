package vista;

import java.awt.Color;
import java.awt.Font;
import javax.swing.JButton;
import javax.swing.JLabel;

/**
 * Colores, fuentes y ayudas visuales compartidas por todas las ventanas, para
 * que los cinco módulos tengan el mismo aspecto.
 *
 * @author Guashpa Gualli Jefferson Nezareth
 * @version 1.0
 */
public final class Estilos {

    /** Color principal (encabezados y menú lateral). */
    public static final Color PRIMARIO = new Color(31, 58, 96);
    /** Color de los botones del menú lateral al pasar el mouse. */
    public static final Color PRIMARIO_CLARO = new Color(52, 86, 133);
    /** Color de fondo de los paneles de contenido. */
    public static final Color FONDO = new Color(245, 247, 250);
    /** Color para acciones destructivas (eliminar). */
    public static final Color PELIGRO = new Color(178, 34, 34);

    /** Fuente de los títulos de cada pantalla. */
    public static final Font TITULO = new Font("SansSerif", Font.BOLD, 20);
    /** Fuente normal de etiquetas y campos. */
    public static final Font NORMAL = new Font("SansSerif", Font.PLAIN, 13);

    /**
     * Constructor privado: la clase solo expone constantes y métodos
     * estáticos.
     */
    private Estilos() {
    }

    /**
     * Crea la etiqueta de título de una pantalla.
     *
     * @param texto texto del título
     * @return etiqueta con estilo de título
     */
    public static JLabel titulo(String texto) {
        JLabel lbl = new JLabel(texto);
        lbl.setFont(TITULO);
        lbl.setForeground(PRIMARIO);
        return lbl;
    }

    /**
     * Crea un botón con texto e indicación (tooltip).
     *
     * @param texto texto del botón
     * @param ayuda texto de ayuda al pasar el mouse
     * @return botón creado
     */
    public static JButton boton(String texto, String ayuda) {
        JButton btn = new JButton(texto);
        btn.setFont(NORMAL);
        btn.setToolTipText(ayuda);
        btn.setFocusPainted(false);
        return btn;
    }
}
