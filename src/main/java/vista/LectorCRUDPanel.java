package vista;

import controlador.LectorController;
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
import modelo.Lector;
import util.Respuesta;

/**
 * Panel principal del módulo Lector: tabla de lectores con búsqueda (R) y
 * botones para registrar (C), modificar (U) y eliminar (D).
 *
 * @author Guashpa Gualli Jefferson Nezareth
 * @version 1.0
 */
public class LectorCRUDPanel extends JPanel implements Recargable {

    private static final String[] COLUMNAS = {
        "ID", "Cédula", "Nombres", "Apellidos", "Correo", "Teléfono", "Dirección", "Estado"};

    /** Controlador que valida y ejecuta las operaciones. */
    private final LectorController controller = new LectorController();
    /** Lectores que se muestran actualmente en la tabla. */
    private final List<Lector> lectoresMostrados = new ArrayList<>();

    /** Campo de texto para buscar. */
    private final JTextField txtBuscar = new JTextField(25);
    /** Botón buscar. */
    private final JButton btnBuscar = Estilos.boton("Buscar", "Buscar por cédula, nombre, apellido o correo");
    /** Botón mostrar todos. */
    private final JButton btnMostrarTodos = Estilos.boton("Mostrar todos", "Limpiar la búsqueda");
    /** Botón nuevo. */
    private final JButton btnNuevo = Estilos.boton("Nuevo", "Registrar un lector nuevo");
    /** Botón editar. */
    private final JButton btnEditar = Estilos.boton("Editar", "Modificar el lector seleccionado");
    /** Botón eliminar. */
    private final JButton btnEliminar = Estilos.boton("Eliminar", "Eliminar el lector seleccionado");
    /** Etiqueta con el total de lectores mostrados. */
    private final JLabel lblTotal = new JLabel();

    /** Modelo de datos de la tabla (celdas no editables). */
    private final DefaultTableModel modelo = new DefaultTableModel(COLUMNAS, 0) {
        @Override
        public boolean isCellEditable(int fila, int columna) {
            return false;
        }

        @Override
        public Class<?> getColumnClass(int columna) {
            return columna == 0 ? Integer.class : String.class;
        }
    };
    /** Tabla con los lectores. */
    private final JTable tabla = new JTable(modelo);

    /**
     * Construye el panel y carga los lectores.
     */
    public LectorCRUDPanel() {
        super(new BorderLayout(0, 10));
        initComponents();
        cargarLectores(null);
    }

    /**
     * Crea y organiza los componentes del panel.
     */
    private void initComponents() {
        setBackground(Estilos.FONDO);
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        txtBuscar.setName("txtBuscar");
        tabla.setName("tblLectores");
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabla.setAutoCreateRowSorter(true);
        tabla.setRowHeight(24);
        tabla.getColumnModel().getColumn(0).setPreferredWidth(40);
        tabla.getColumnModel().getColumn(4).setPreferredWidth(190);
        tabla.getColumnModel().getColumn(6).setPreferredWidth(160);
        tabla.setDefaultRenderer(String.class, new EstadoRenderer());

        JPanel norte = new JPanel(new BorderLayout());
        norte.setOpaque(false);
        norte.add(Estilos.titulo("Gestión de Lectores"), BorderLayout.NORTH);
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

        btnBuscar.addActionListener(e -> cargarLectores(txtBuscar.getText()));
        txtBuscar.addActionListener(e -> cargarLectores(txtBuscar.getText()));
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
     * Limpia la búsqueda y vuelve a cargar todos los lectores.
     */
    @Override
    public void recargar() {
        txtBuscar.setText("");
        cargarLectores(null);
    }

    /**
     * Carga en la tabla los lectores que coinciden con el texto.
     *
     * @param texto texto a buscar, o {@code null} para listar todos
     */
    private void cargarLectores(String texto) {
        try {
            List<Lector> lectores = controller.buscarLectores(texto);
            llenarTabla(lectores);
            if (lectores.isEmpty() && texto != null && !texto.isBlank()) {
                JOptionPane.showMessageDialog(this, "No se encontraron lectores con: " + texto.trim(),
                        "Buscar", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (SQLException e) {
            llenarTabla(new ArrayList<>());
            JOptionPane.showMessageDialog(this, "No se pudo cargar los lectores: " + e.getMessage(),
                    "Error de base de datos", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Reemplaza el contenido de la tabla.
     *
     * @param lectores lectores a mostrar
     */
    private void llenarTabla(List<Lector> lectores) {
        lectoresMostrados.clear();
        lectoresMostrados.addAll(lectores);
        modelo.setRowCount(0);
        for (Lector l : lectores) {
            modelo.addRow(new Object[]{l.getIdLector(), l.getCedula(), l.getNombres(), l.getApellidos(),
                l.getCorreo(), l.getTelefono(), l.getDireccion() == null ? "" : l.getDireccion(),
                l.getEstado()});
        }
        lblTotal.setText("Total: " + lectores.size() + " lector(es)");
    }

    /**
     * Devuelve el lector seleccionado en la tabla.
     *
     * @return lector seleccionado o {@code null}
     */
    private Lector lectorSeleccionado() {
        int fila = tabla.getSelectedRow();
        if (fila < 0) {
            return null;
        }
        return lectoresMostrados.get(tabla.convertRowIndexToModel(fila));
    }

    /**
     * Abre el diálogo de edición para el lector seleccionado.
     */
    private void editarSeleccionado() {
        Lector lector = lectorSeleccionado();
        if (lector == null) {
            JOptionPane.showMessageDialog(this, "Seleccione un lector de la tabla",
                    "Editar", JOptionPane.WARNING_MESSAGE);
            return;
        }
        abrirDialogo(lector);
    }

    /**
     * Abre el diálogo de registro o edición y recarga si hubo cambios.
     *
     * @param lector lector a editar, o {@code null} para uno nuevo
     */
    private void abrirDialogo(Lector lector) {
        LectorDialog dialogo = new LectorDialog(SwingUtilities.getWindowAncestor(this), controller, lector);
        dialogo.setVisible(true);
        if (dialogo.isGuardado()) {
            recargar();
        }
    }

    /**
     * Elimina el lector seleccionado, previa confirmación.
     */
    private void eliminarSeleccionado() {
        Lector lector = lectorSeleccionado();
        if (lector == null) {
            JOptionPane.showMessageDialog(this, "Seleccione un lector de la tabla",
                    "Eliminar", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int op = JOptionPane.showConfirmDialog(this,
                "¿Desea eliminar al lector \"" + lector.getNombres() + " " + lector.getApellidos() + "\"?",
                "Confirmar eliminación", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (op != JOptionPane.YES_OPTION) {
            return;
        }
        Respuesta r = controller.eliminarLector(lector.getIdLector());
        JOptionPane.showMessageDialog(this, r.getMensaje(), "Eliminar",
                r.isExitosa() ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.ERROR_MESSAGE);
        if (r.isExitosa()) {
            recargar();
        }
    }
}
