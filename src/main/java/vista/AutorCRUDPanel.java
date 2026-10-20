package vista;

import controlador.AutorController;
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
import modelo.Autor;
import util.Fechas;
import util.Respuesta;

/**
 * Panel principal del módulo Autor: tabla de autores con búsqueda (R) y
 * botones para registrar (C), modificar (U) y eliminar (D).
 *
 * @author Guashpa Gualli Jefferson Nezareth
 * @version 1.0
 */
public class AutorCRUDPanel extends JPanel implements Recargable {

    private static final String[] COLUMNAS = {
        "ID", "Nombres", "Apellidos", "Nacionalidad", "Fecha de nacimiento", "Biografía"};

    /** Controlador que valida y ejecuta las operaciones. */
    private final AutorController controller = new AutorController();
    /** Autores que se muestran actualmente en la tabla. */
    private final List<Autor> autoresMostrados = new ArrayList<>();

    /** Campo de texto para buscar. */
    private final JTextField txtBuscar = new JTextField(25);
    /** Botón buscar. */
    private final JButton btnBuscar = Estilos.boton("Buscar", "Buscar por nombre, apellido o nacionalidad");
    /** Botón mostrar todos. */
    private final JButton btnMostrarTodos = Estilos.boton("Mostrar todos", "Limpiar la búsqueda");
    /** Botón nuevo. */
    private final JButton btnNuevo = Estilos.boton("Nuevo", "Registrar un autor nuevo");
    /** Botón editar. */
    private final JButton btnEditar = Estilos.boton("Editar", "Modificar el autor seleccionado");
    /** Botón eliminar. */
    private final JButton btnEliminar = Estilos.boton("Eliminar", "Eliminar el autor seleccionado");
    /** Etiqueta con el total de autores mostrados. */
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
    /** Tabla con los autores. */
    private final JTable tabla = new JTable(modelo);

    /**
     * Construye el panel y carga los autores.
     */
    public AutorCRUDPanel() {
        super(new BorderLayout(0, 10));
        initComponents();
        cargarAutores(null);
    }

    /**
     * Crea y organiza los componentes del panel.
     */
    private void initComponents() {
        setBackground(Estilos.FONDO);
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        txtBuscar.setName("txtBuscar");
        tabla.setName("tblAutores");
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabla.setAutoCreateRowSorter(true);
        tabla.setRowHeight(24);
        tabla.getColumnModel().getColumn(0).setPreferredWidth(40);
        tabla.getColumnModel().getColumn(5).setPreferredWidth(280);

        JPanel norte = new JPanel(new BorderLayout());
        norte.setOpaque(false);
        norte.add(Estilos.titulo("Gestión de Autores"), BorderLayout.NORTH);
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

        btnBuscar.addActionListener(e -> cargarAutores(txtBuscar.getText()));
        txtBuscar.addActionListener(e -> cargarAutores(txtBuscar.getText()));
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
     * Limpia la búsqueda y vuelve a cargar todos los autores.
     */
    @Override
    public void recargar() {
        txtBuscar.setText("");
        cargarAutores(null);
    }

    /**
     * Carga en la tabla los autores que coinciden con el texto.
     *
     * @param texto texto a buscar, o {@code null} para listar todos
     */
    private void cargarAutores(String texto) {
        try {
            List<Autor> autores = controller.buscarAutores(texto);
            llenarTabla(autores);
            if (autores.isEmpty() && texto != null && !texto.isBlank()) {
                JOptionPane.showMessageDialog(this, "No se encontraron autores con: " + texto.trim(),
                        "Buscar", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (SQLException e) {
            llenarTabla(new ArrayList<>());
            JOptionPane.showMessageDialog(this, "No se pudo cargar los autores: " + e.getMessage(),
                    "Error de base de datos", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Reemplaza el contenido de la tabla.
     *
     * @param autores autores a mostrar
     */
    private void llenarTabla(List<Autor> autores) {
        autoresMostrados.clear();
        autoresMostrados.addAll(autores);
        modelo.setRowCount(0);
        for (Autor a : autores) {
            modelo.addRow(new Object[]{a.getIdAutor(), a.getNombres(), a.getApellidos(),
                a.getNacionalidad(), Fechas.formatear(a.getFechaNacimiento()),
                a.getBiografia() == null ? "" : a.getBiografia()});
        }
        lblTotal.setText("Total: " + autores.size() + " autor(es)");
    }

    /**
     * Devuelve el autor seleccionado en la tabla.
     *
     * @return autor seleccionado o {@code null}
     */
    private Autor autorSeleccionado() {
        int fila = tabla.getSelectedRow();
        if (fila < 0) {
            return null;
        }
        return autoresMostrados.get(tabla.convertRowIndexToModel(fila));
    }

    /**
     * Abre el diálogo de edición para el autor seleccionado.
     */
    private void editarSeleccionado() {
        Autor autor = autorSeleccionado();
        if (autor == null) {
            JOptionPane.showMessageDialog(this, "Seleccione un autor de la tabla",
                    "Editar", JOptionPane.WARNING_MESSAGE);
            return;
        }
        abrirDialogo(autor);
    }

    /**
     * Abre el diálogo de registro o edición y recarga si hubo cambios.
     *
     * @param autor autor a editar, o {@code null} para uno nuevo
     */
    private void abrirDialogo(Autor autor) {
        AutorDialog dialogo = new AutorDialog(SwingUtilities.getWindowAncestor(this), controller, autor);
        dialogo.setVisible(true);
        if (dialogo.isGuardado()) {
            recargar();
        }
    }

    /**
     * Elimina el autor seleccionado, previa confirmación.
     */
    private void eliminarSeleccionado() {
        Autor autor = autorSeleccionado();
        if (autor == null) {
            JOptionPane.showMessageDialog(this, "Seleccione un autor de la tabla",
                    "Eliminar", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int op = JOptionPane.showConfirmDialog(this,
                "¿Desea eliminar al autor \"" + autor + "\"?",
                "Confirmar eliminación", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (op != JOptionPane.YES_OPTION) {
            return;
        }
        Respuesta r = controller.eliminarAutor(autor.getIdAutor());
        JOptionPane.showMessageDialog(this, r.getMensaje(), "Eliminar",
                r.isExitosa() ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.ERROR_MESSAGE);
        if (r.isExitosa()) {
            recargar();
        }
    }
}
