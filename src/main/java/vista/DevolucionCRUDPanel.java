package vista;

import controlador.DevolucionController;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import modelo.Devolucion;
import util.Fechas;
import util.Respuesta;

/**
 * Panel principal del módulo Devolución: tabla de devoluciones con búsqueda
 * (R) y botones para registrar (C), modificar (U) y eliminar (D). Muestra
 * los días de retraso y la multa calculados.
 *
 * @author Guashpa Gualli Jefferson Nezareth
 * @version 1.0
 */
public class DevolucionCRUDPanel extends JPanel implements Recargable {

    private static final String[] COLUMNAS = {
        "N°", "Préstamo", "Libro", "Lector", "Fecha límite", "Fecha devolución", "Días retraso",
        "Multa ($)", "Estado libro", "Observación"};

    /** Controlador que valida y ejecuta las operaciones. */
    private final DevolucionController controller = new DevolucionController();
    /** Devoluciones que se muestran actualmente en la tabla. */
    private final List<Devolucion> devolucionesMostradas = new ArrayList<>();

    /** Campo de texto para buscar. */
    private final JTextField txtBuscar = new JTextField(25);
    /** Botón buscar. */
    private final JButton btnBuscar = Estilos.boton("Buscar", "Buscar por libro, lector o estado del libro");
    /** Botón mostrar todos. */
    private final JButton btnMostrarTodos = Estilos.boton("Mostrar todos", "Limpiar la búsqueda");
    /** Botón nuevo. */
    private final JButton btnNuevo = Estilos.boton("Nuevo", "Registrar la devolución de un préstamo");
    /** Botón editar. */
    private final JButton btnEditar = Estilos.boton("Editar", "Modificar la devolución seleccionada");
    /** Botón eliminar. */
    private final JButton btnEliminar = Estilos.boton("Eliminar", "Eliminar la devolución seleccionada");
    /** Etiqueta con el total de devoluciones mostradas. */
    private final JLabel lblTotal = new JLabel();

    /** Modelo de datos de la tabla (celdas no editables). */
    private final DefaultTableModel modelo = new DefaultTableModel(COLUMNAS, 0) {
        @Override
        public boolean isCellEditable(int fila, int columna) {
            return false;
        }

        @Override
        public Class<?> getColumnClass(int columna) {
            return columna == 0 || columna == 1 || columna == 6 ? Integer.class : String.class;
        }
    };
    /** Tabla con las devoluciones. */
    private final JTable tabla = new JTable(modelo);

    /**
     * Construye el panel y carga las devoluciones.
     */
    public DevolucionCRUDPanel() {
        super(new BorderLayout(0, 10));
        initComponents();
        cargarDevoluciones(null);
    }

    /**
     * Crea y organiza los componentes del panel.
     */
    private void initComponents() {
        setBackground(Estilos.FONDO);
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        txtBuscar.setName("txtBuscar");
        tabla.setName("tblDevoluciones");
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabla.setAutoCreateRowSorter(true);
        tabla.setRowHeight(24);
        tabla.getColumnModel().getColumn(0).setPreferredWidth(40);
        tabla.getColumnModel().getColumn(2).setPreferredWidth(200);
        tabla.getColumnModel().getColumn(3).setPreferredWidth(150);
        tabla.getColumnModel().getColumn(9).setPreferredWidth(170);
        tabla.setDefaultRenderer(String.class, new EstadoRenderer());

        JPanel norte = new JPanel(new BorderLayout());
        norte.setOpaque(false);
        norte.add(Estilos.titulo("Gestión de Devoluciones"), BorderLayout.NORTH);
        JPanel busqueda = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 8));
        busqueda.setOpaque(false);
        busqueda.add(new JLabel("Buscar:"));
        busqueda.add(txtBuscar);
        busqueda.add(btnBuscar);
        busqueda.add(btnMostrarTodos);
        norte.add(busqueda, BorderLayout.SOUTH);

        btnEliminar.setForeground(Estilos.PELIGRO);
        JPanel acciones = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        acciones.setOpaque(false);
        acciones.add(btnNuevo);
        acciones.add(btnEditar);
        acciones.add(btnEliminar);
        JPanel sur = new JPanel(new BorderLayout());
        sur.setOpaque(false);
        sur.add(acciones, BorderLayout.WEST);
        sur.add(lblTotal, BorderLayout.EAST);

        add(norte, BorderLayout.NORTH);
        add(new JScrollPane(tabla), BorderLayout.CENTER);
        add(sur, BorderLayout.SOUTH);

        btnBuscar.addActionListener(e -> cargarDevoluciones(txtBuscar.getText()));
        txtBuscar.addActionListener(e -> cargarDevoluciones(txtBuscar.getText()));
        btnMostrarTodos.addActionListener(e -> recargar());
        btnNuevo.addActionListener(e -> abrirDialogo(null));
        btnEditar.addActionListener(e -> editarSeleccionado());
        btnEliminar.addActionListener(e -> eliminarSeleccionado());
        tabla.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    editarSeleccionado();
                }
            }
        });
    }

    /**
     * Limpia la búsqueda y vuelve a cargar todas las devoluciones.
     */
    @Override
    public void recargar() {
        txtBuscar.setText("");
        cargarDevoluciones(null);
    }

    /**
     * Carga en la tabla las devoluciones que coinciden con el texto.
     *
     * @param texto texto a buscar, o {@code null} para listar todos
     */
    private void cargarDevoluciones(String texto) {
        try {
            List<Devolucion> devoluciones = controller.buscarDevoluciones(texto);
            llenarTabla(devoluciones);
            if (devoluciones.isEmpty() && texto != null && !texto.isBlank()) {
                JOptionPane.showMessageDialog(this, "No se encontraron devoluciones con: " + texto.trim(),
                        "Buscar", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (SQLException e) {
            llenarTabla(new ArrayList<>());
            JOptionPane.showMessageDialog(this, "No se pudo cargar las devoluciones: " + e.getMessage(),
                    "Error de base de datos", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Reemplaza el contenido de la tabla.
     *
     * @param devoluciones devoluciones a mostrar
     */
    private void llenarTabla(List<Devolucion> devoluciones) {
        devolucionesMostradas.clear();
        devolucionesMostradas.addAll(devoluciones);
        modelo.setRowCount(0);
        for (Devolucion d : devoluciones) {
            modelo.addRow(new Object[]{d.getIdDevolucion(), d.getIdPrestamo(), d.getTituloLibro(),
                d.getNombreLector(), Fechas.formatear(d.getFechaLimite()),
                Fechas.formatear(d.getFechaDevolucion()), d.getDiasRetraso(), d.getMulta().toPlainString(),
                d.getEstadoLibro(), d.getObservacion() == null ? "" : d.getObservacion()});
        }
        lblTotal.setText("Total: " + devoluciones.size() + " devolución(es)");
    }

    /**
     * Devuelve la devolución seleccionada en la tabla.
     *
     * @return devolución seleccionada o {@code null}
     */
    private Devolucion devolucionSeleccionada() {
        int fila = tabla.getSelectedRow();
        if (fila < 0) {
            return null;
        }
        return devolucionesMostradas.get(tabla.convertRowIndexToModel(fila));
    }

    /**
     * Abre el diálogo de edición para la devolución seleccionada.
     */
    private void editarSeleccionado() {
        Devolucion devolucion = devolucionSeleccionada();
        if (devolucion == null) {
            JOptionPane.showMessageDialog(this, "Seleccione una devolución de la tabla",
                    "Editar", JOptionPane.WARNING_MESSAGE);
            return;
        }
        abrirDialogo(devolucion);
    }

    /**
     * Abre el diálogo de registro o edición y recarga si hubo cambios.
     *
     * @param devolucion devolución a editar, o {@code null} para uno nuevo
     */
    private void abrirDialogo(Devolucion devolucion) {
        DevolucionDialog dialogo = new DevolucionDialog(SwingUtilities.getWindowAncestor(this), controller, devolucion);
        dialogo.setVisible(true);
        if (dialogo.isGuardado()) {
            recargar();
        }
    }

    /**
     * Elimina la devolución seleccionada, previa confirmación.
     */
    private void eliminarSeleccionado() {
        Devolucion devolucion = devolucionSeleccionada();
        if (devolucion == null) {
            JOptionPane.showMessageDialog(this, "Seleccione una devolución de la tabla",
                    "Eliminar", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int op = JOptionPane.showConfirmDialog(this,
                "¿Desea eliminar la devolución N° " + devolucion.getIdDevolucion()
                + "? El préstamo N° " + devolucion.getIdPrestamo() + " volverá a quedar pendiente.",
                "Confirmar eliminación", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (op != JOptionPane.YES_OPTION) {
            return;
        }
        Respuesta r = controller.eliminarDevolucion(devolucion.getIdDevolucion());
        JOptionPane.showMessageDialog(this, r.getMensaje(), "Eliminar",
                r.isExitosa() ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.ERROR_MESSAGE);
        if (r.isExitosa()) {
            recargar();
        }
    }
}
