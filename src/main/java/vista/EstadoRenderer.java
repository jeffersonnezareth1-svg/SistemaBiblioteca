package vista;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

/**
 * Pinta con color las celdas de texto que contienen un estado, para que se
 * reconozcan rápido en las tablas de Lectores, Préstamos y Devoluciones:
 * VENCIDO, PERDIDO y SUSPENDIDO en rojo, DANADO en naranja, ACTIVO en verde y
 * DEVUELTO en gris.
 *
 * @author Guashpa Gualli Jefferson Nezareth
 * @version 1.0
 */
public class EstadoRenderer extends DefaultTableCellRenderer {

    private static final Color ROJO = new Color(178, 34, 34);
    private static final Color NARANJA = new Color(205, 110, 0);
    private static final Color VERDE = new Color(30, 120, 60);
    private static final Color GRIS = new Color(110, 110, 110);

    /**
     * Crea el renderizador.
     */
    public EstadoRenderer() {
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
            boolean hasFocus, int row, int column) {
        Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        Color color = colorDe(value == null ? "" : value.toString());
        c.setFont(c.getFont().deriveFont(color == null ? Font.PLAIN : Font.BOLD));
        if (!isSelected) {
            c.setForeground(color == null ? table.getForeground() : color);
        }
        return c;
    }

    /**
     * Devuelve el color asociado a un estado.
     *
     * @param texto texto de la celda
     * @return color del estado, o {@code null} si el texto no es un estado
     */
    private Color colorDe(String texto) {
        switch (texto) {
            case "VENCIDO":
            case "PERDIDO":
            case "SUSPENDIDO":
                return ROJO;
            case "DANADO":
                return NARANJA;
            case "ACTIVO":
                return VERDE;
            case "DEVUELTO":
                return GRIS;
            default:
                return null;
        }
    }
}
