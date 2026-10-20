package vista;

import controlador.LibroController;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Window;
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
import modelo.Libro;
import util.Respuesta;

/**
 * Panel principal del módulo Libro.
 *
 * <p>Muestra los libros en una tabla y permite: buscar (R), registrar uno
 * nuevo (C), modificar el seleccionado (U) y eliminar el seleccionado (D).
 * El registro y la modificación se hacen en {@link LibroDialog}.</p>
 *
 * @author Guashpa Gualli Jefferson Nezareth
 * @version 1.0
 */
public class LibroCRUDPanel extends JPanel implements Recargable {

    private static final String[] COLUMNAS = {
        "ID", "ISBN", "Título", "Editorial", "Año", "Género", "Páginas", "Stock"};

    /** Controlador que valida y ejecuta las operaciones. */
    private final LibroController controller = new LibroController();
    /** Libros que se muestran actualmente en la tabla (mismo orden que el modelo). */
    private final List<Libro> librosMostrados = new ArrayList<>();

    /** Campo de texto para buscar. */
    private final JTextField txtBuscar = new JTextField(25);
    /** Botón buscar. */
    private final JButton btnBuscar = Estilos.boton("Buscar", "Buscar por ISBN, título, editorial o género");
    /** Botón mostrar todos. */
    private final JButton btnMostrarTodos = Estilos.boton("Mostrar todos", "Limpiar la búsqueda");
    /** Botón nuevo. */
    private final JButton btnNuevo = Estilos.boton("Nuevo", "Registrar un libro nuevo");
    /** Botón editar. */
    private final JButton btnEditar = Estilos.boton("Editar", "Modificar el libro seleccionado");
    /** Botón eliminar. */
    private final JButton btnEliminar = Estilos.boton("Eliminar", "Eliminar el libro seleccionado");
    /** Etiqueta con el total de libros mostrados. */
    private final JLabel lblTotal = new JLabel();

    /** Modelo de datos de la tabla (celdas no editables). */
    private final DefaultTableModel modelo = new DefaultTableModel(COLUMNAS, 0) {
        @Override
        public boolean isCellEditable(int fila, int columna) {
            return false;
        }

        @Override
        public Class<?> getColumnClass(int columna) {
            return columna == 0 || columna == 4 || columna == 6 || columna == 7
                    ? Integer.class : String.class;
        }
    };
    /** Tabla con los libros. */
    private final JTable tabla = new JTable(modelo);

    /**
     * Construye el panel y carga los libros.
     */
    public LibroCRUDPanel() {
        super(new BorderLayout(0, 10));
        initComponents();
        cargarLibros(null);
    }

    /**
     * Crea y organiza los componentes del panel.
     */
    private void initComponents() {
        setBackground(Estilos.FONDO);
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        txtBuscar.setName("txtBuscar");
        tabla.setName("tblLibros");
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabla.setAutoCreateRowSorter(true);
        tabla.setRowHeight(24);
        tabla.getColumnModel().getColumn(0).setPreferredWidth(40);
        tabla.getColumnModel().getColumn(1).setPreferredWidth(120);
        tabla.getColumnModel().getColumn(2).setPreferredWidth(220);
        tabla.getColumnModel().getColumn(3).setPreferredWidth(150);

        JPanel norte = new JPanel(new BorderLayout());
        norte.setOpaque(false);
        norte.add(Estilos.titulo("Gestión de Libros"), BorderLayout.NORTH);
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

        btnBuscar.addActionListener(e -> cargarLibros(txtBuscar.getText()));
        txtBuscar.addActionListener(e -> cargarLibros(txtBuscar.getText()));
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
     * Limpia la búsqueda y vuelve a cargar todos los libros (por ejemplo,
     * para ver el stock actualizado después de un préstamo).
     */
    @Override
    public void recargar() {
        txtBuscar.setText("");
        cargarLibros(null);
    }

    /**
     * Carga en la tabla los libros que coinciden con el texto, o todos si el
     * texto está vacío.
     *
     * @param texto texto a buscar, o {@code null} para listar todos
     */
    private void cargarLibros(String texto) {
        try {
            List<Libro> libros = controller.buscarLibros(texto);
            llenarTabla(libros);
            if (libros.isEmpty() && texto != null && !texto.isBlank()) {
                JOptionPane.showMessageDialog(this, "No se encontraron libros con: " + texto.trim(),
                        "Buscar", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (SQLException e) {
            llenarTabla(new ArrayList<>());
            JOptionPane.showMessageDialog(this, "No se pudo cargar los libros: " + e.getMessage(),
                    "Error de base de datos", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Reemplaza el contenido de la tabla.
     *
     * @param libros libros a mostrar
     */
    private void llenarTabla(List<Libro> libros) {
        librosMostrados.clear();
        librosMostrados.addAll(libros);
        modelo.setRowCount(0);
        for (Libro l : libros) {
            modelo.addRow(new Object[]{l.getIdLibro(), l.getIsbn(), l.getTitulo(), l.getEditorial(),
                l.getAnioPublicacion(), l.getGenero(), l.getNumeroPaginas(), l.getStock()});
        }
        lblTotal.setText("Total: " + libros.size() + " libro(s)");
    }

    /**
     * Devuelve el libro seleccionado en la tabla, considerando el orden
     * aplicado por el usuario.
     *
     * @return libro seleccionado o {@code null} si no hay selección
     */
    private Libro libroSeleccionado() {
        int fila = tabla.getSelectedRow();
        if (fila < 0) {
            return null;
        }
        return librosMostrados.get(tabla.convertRowIndexToModel(fila));
    }

    /**
     * Abre el diálogo de edición para el libro seleccionado.
     */
    private void editarSeleccionado() {
        Libro libro = libroSeleccionado();
        if (libro == null) {
            JOptionPane.showMessageDialog(this, "Seleccione un libro de la tabla",
                    "Editar", JOptionPane.WARNING_MESSAGE);
            return;
        }
        abrirDialogo(libro);
    }

    /**
     * Abre el diálogo de registro (libro {@code null}) o de edición, y
     * recarga la tabla si se guardaron cambios.
     *
     * @param libro libro a editar, o {@code null} para uno nuevo
     */
    private void abrirDialogo(Libro libro) {
        Window ventana = SwingUtilities.getWindowAncestor(this);
        LibroDialog dialogo = new LibroDialog(ventana, controller, libro);
        dialogo.setVisible(true);
        if (dialogo.isGuardado()) {
            cargarLibros(txtBuscar.getText());
        }
    }

    /**
     * Elimina el libro seleccionado, previa confirmación.
     */
    private void eliminarSeleccionado() {
        Libro libro = libroSeleccionado();
        if (libro == null) {
            JOptionPane.showMessageDialog(this, "Seleccione un libro de la tabla",
                    "Eliminar", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int op = JOptionPane.showConfirmDialog(this,
                "¿Desea eliminar el libro \"" + libro.getTitulo() + "\"?",
                "Confirmar eliminación", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (op != JOptionPane.YES_OPTION) {
            return;
        }
        Respuesta r = controller.eliminarLibro(libro.getIdLibro());
        JOptionPane.showMessageDialog(this, r.getMensaje(), "Eliminar",
                r.isExitosa() ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.ERROR_MESSAGE);
        if (r.isExitosa()) {
            cargarLibros(txtBuscar.getText());
        }
    }
}
