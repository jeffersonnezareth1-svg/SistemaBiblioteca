package vista;

import controlador.PrestamoController;
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
import modelo.Prestamo;
import util.Fechas;
import util.Respuesta;

/**
 * Panel principal del módulo Préstamo: tabla de préstamos con búsqueda (R)
 * y botones para registrar (C), modificar (U) y eliminar (D). Los préstamos
 * VENCIDOS se muestran en rojo.
 *
 * @author Guashpa Gualli Jefferson Nezareth
 * @version 1.0
 */
public class PrestamoCRUDPanel extends JPanel implements Recargable {

    private static final String[] COLUMNAS = {
        "N°", "Libro", "Lector", "Fecha préstamo", "Fecha límite", "Estado", "Observación"};

    /** Controlador que valida y ejecuta las operaciones. */
    private final PrestamoController controller = new PrestamoController();
    /** Préstamos que se muestran actualmente en la tabla. */
    private final List<Prestamo> prestamosMostrados = new ArrayList<>();

    /** Campo de texto para buscar. */
    private final JTextField txtBuscar = new JTextField(25);
    /** Botón buscar. */
    private final JButton btnBuscar = Estilos.boton("Buscar", "Buscar por libro, lector, cédula o estado");
    /** Botón mostrar todos. */
    private final JButton btnMostrarTodos = Estilos.boton("Mostrar todos", "Limpiar la búsqueda");
    /** Botón nuevo. */
    private final JButton btnNuevo = Estilos.boton("Nuevo", "Registrar un préstamo nuevo");
    /** Botón editar. */
    private final JButton btnEditar = Estilos.boton("Editar", "Modificar el préstamo seleccionado");
    /** Botón eliminar. */
    private final JButton btnEliminar = Estilos.boton("Eliminar", "Eliminar el préstamo seleccionado");
    /** Etiqueta con el total de préstamos mostrados. */
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
    /** Tabla con los préstamos. */
    private final JTable tabla = new JTable(modelo);

    /**
     * Construye el panel y carga los préstamos.
     */
    public PrestamoCRUDPanel() {
        super(new BorderLayout(0, 10));
        initComponents();
        cargarPrestamos(null);
    }

    /**
     * Crea y organiza los componentes del panel.
     */
    private void initComponents() {
        setBackground(Estilos.FONDO);
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        txtBuscar.setName("txtBuscar");
        tabla.setName("tblPrestamos");
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabla.setAutoCreateRowSorter(true);
        tabla.setRowHeight(24);
        tabla.getColumnModel().getColumn(0).setPreferredWidth(40);
        tabla.getColumnModel().getColumn(1).setPreferredWidth(220);
        tabla.getColumnModel().getColumn(2).setPreferredWidth(170);
        tabla.getColumnModel().getColumn(6).setPreferredWidth(170);
        tabla.setDefaultRenderer(String.class, new EstadoRenderer());

        JPanel norte = new JPanel(new BorderLayout());
        norte.setOpaque(false);
        norte.add(Estilos.titulo("Gestión de Préstamos"), BorderLayout.NORTH);
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

        btnBuscar.addActionListener(e -> cargarPrestamos(txtBuscar.getText()));
        txtBuscar.addActionListener(e -> cargarPrestamos(txtBuscar.getText()));
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
     * Limpia la búsqueda y vuelve a cargar todos los préstamos (los
     * estados ACTIVO/VENCIDO se recalculan con la fecha actual).
     */
    @Override
    public void recargar() {
        txtBuscar.setText("");
        cargarPrestamos(null);
    }

    /**
     * Carga en la tabla los préstamos que coinciden con el texto.
     *
     * @param texto texto a buscar, o {@code null} para listar todos
     */
    private void cargarPrestamos(String texto) {
        try {
            List<Prestamo> prestamos = controller.buscarPrestamos(texto);
            llenarTabla(prestamos);
            if (prestamos.isEmpty() && texto != null && !texto.isBlank()) {
                JOptionPane.showMessageDialog(this, "No se encontraron préstamos con: " + texto.trim(),
                        "Buscar", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (SQLException e) {
            llenarTabla(new ArrayList<>());
            JOptionPane.showMessageDialog(this, "No se pudo cargar los préstamos: " + e.getMessage(),
                    "Error de base de datos", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Reemplaza el contenido de la tabla.
     *
     * @param prestamos préstamos a mostrar
     */
    private void llenarTabla(List<Prestamo> prestamos) {
        prestamosMostrados.clear();
        prestamosMostrados.addAll(prestamos);
        modelo.setRowCount(0);
        for (Prestamo p : prestamos) {
            modelo.addRow(new Object[]{p.getIdPrestamo(), p.getTituloLibro(), p.getNombreLector(),
                Fechas.formatear(p.getFechaPrestamo()), Fechas.formatear(p.getFechaLimite()),
                p.getEstado(), p.getObservacion() == null ? "" : p.getObservacion()});
        }
        lblTotal.setText("Total: " + prestamos.size() + " préstamo(s)");
    }

    /**
     * Devuelve el préstamo seleccionado en la tabla.
     *
     * @return préstamo seleccionado o {@code null}
     */
    private Prestamo prestamoSeleccionado() {
        int fila = tabla.getSelectedRow();
        if (fila < 0) {
            return null;
        }
        return prestamosMostrados.get(tabla.convertRowIndexToModel(fila));
    }

    /**
     * Abre el diálogo de edición para el préstamo seleccionado.
     */
    private void editarSeleccionado() {
        Prestamo prestamo = prestamoSeleccionado();
        if (prestamo == null) {
            JOptionPane.showMessageDialog(this, "Seleccione un préstamo de la tabla",
                    "Editar", JOptionPane.WARNING_MESSAGE);
            return;
        }
        abrirDialogo(prestamo);
    }

    /**
     * Abre el diálogo de registro o edición y recarga si hubo cambios.
     *
     * @param prestamo préstamo a editar, o {@code null} para uno nuevo
     */
    private void abrirDialogo(Prestamo prestamo) {
        PrestamoDialog dialogo = new PrestamoDialog(SwingUtilities.getWindowAncestor(this), controller, prestamo);
        dialogo.setVisible(true);
        if (dialogo.isGuardado()) {
            recargar();
        }
    }

    /**
     * Elimina el préstamo seleccionado, previa confirmación.
     */
    private void eliminarSeleccionado() {
        Prestamo prestamo = prestamoSeleccionado();
        if (prestamo == null) {
            JOptionPane.showMessageDialog(this, "Seleccione un préstamo de la tabla",
                    "Eliminar", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int op = JOptionPane.showConfirmDialog(this,
                "¿Desea eliminar el préstamo N° " + prestamo.getIdPrestamo()
                + " del libro \"" + prestamo.getTituloLibro() + "\"?",
                "Confirmar eliminación", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (op != JOptionPane.YES_OPTION) {
            return;
        }
        Respuesta r = controller.eliminarPrestamo(prestamo.getIdPrestamo());
        JOptionPane.showMessageDialog(this, r.getMensaje(), "Eliminar",
                r.isExitosa() ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.ERROR_MESSAGE);
        if (r.isExitosa()) {
            recargar();
        }
    }
}
