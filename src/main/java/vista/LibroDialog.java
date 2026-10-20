package vista;

import controlador.LibroController;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.util.HashMap;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import modelo.Libro;
import util.Respuesta;

/**
 * Formulario modal para registrar (C) o modificar (U) un libro.
 *
 * <p>Si se construye con un libro, se abre en modo edición con sus datos
 * cargados; si se construye con {@code null}, se abre en modo registro. Las
 * validaciones las hace {@link LibroController}; este diálogo solo muestra el
 * mensaje y enfoca el campo con error.</p>
 *
 * @author Guashpa Gualli Jefferson Nezareth
 * @version 1.0
 */
public class LibroDialog extends JDialog {

    private static final String SELECCIONE = "-- Seleccione --";

    /** Controlador que valida y ejecuta las operaciones. */
    private final LibroController controller;
    /** Libro que se modifica, o null si se registra uno nuevo. */
    private final Libro libro;
    /** Indica si el libro se guardó correctamente. */
    private boolean guardado;

    /** Campo de texto del ISBN. */
    private final JTextField txtIsbn = new JTextField(20);
    /** Campo de texto del título. */
    private final JTextField txtTitulo = new JTextField(28);
    /** Campo de texto de la editorial. */
    private final JTextField txtEditorial = new JTextField(28);
    /** Campo de texto del año de publicación. */
    private final JTextField txtAnio = new JTextField(6);
    /** Lista desplegable de géneros. */
    private final JComboBox<String> cboGenero = new JComboBox<>();
    /** Campo de texto del número de páginas. */
    private final JTextField txtPaginas = new JTextField(6);
    /** Campo de texto del stock. */
    private final JTextField txtStock = new JTextField(6);
    /** Botón guardar. */
    private final JButton btnGuardar = Estilos.boton("Guardar", "Guardar los datos del libro");
    /** Botón cancelar. */
    private final JButton btnCancelar = Estilos.boton("Cancelar", "Cerrar sin guardar");
    /** Relación entre el nombre de cada campo y su componente, para enfocar errores. */
    private final Map<String, JComponent> campos = new HashMap<>();

    /**
     * Crea el formulario.
     *
     * @param owner ventana dueña del diálogo
     * @param controller controlador del módulo Libro
     * @param libro libro a modificar, o {@code null} para registrar uno nuevo
     */
    public LibroDialog(Window owner, LibroController controller, Libro libro) {
        super(owner, libro == null ? "Registrar libro" : "Modificar libro", ModalityType.APPLICATION_MODAL);
        this.controller = controller;
        this.libro = libro;
        initComponents();
        if (libro != null) {
            cargarDatos(libro);
        }
        pack();
        setResizable(false);
        setLocationRelativeTo(owner);
    }

    /**
     * Crea y organiza los componentes del formulario.
     */
    private void initComponents() {
        cboGenero.addItem(SELECCIONE);
        for (String genero : LibroController.GENEROS) {
            cboGenero.addItem(genero);
        }
        registrarCampo(LibroController.CAMPO_ISBN, txtIsbn);
        registrarCampo(LibroController.CAMPO_TITULO, txtTitulo);
        registrarCampo(LibroController.CAMPO_EDITORIAL, txtEditorial);
        registrarCampo(LibroController.CAMPO_ANIO, txtAnio);
        registrarCampo(LibroController.CAMPO_GENERO, cboGenero);
        registrarCampo(LibroController.CAMPO_PAGINAS, txtPaginas);
        registrarCampo(LibroController.CAMPO_STOCK, txtStock);

        JPanel formulario = new JPanel(new GridBagLayout());
        formulario.setBorder(BorderFactory.createEmptyBorder(15, 20, 5, 20));
        agregarFila(formulario, 0, "ISBN *:", txtIsbn);
        agregarFila(formulario, 1, "Título *:", txtTitulo);
        agregarFila(formulario, 2, "Editorial *:", txtEditorial);
        agregarFila(formulario, 3, "Año de publicación *:", txtAnio);
        agregarFila(formulario, 4, "Género *:", cboGenero);
        agregarFila(formulario, 5, "Número de páginas *:", txtPaginas);
        agregarFila(formulario, 6, "Stock (ejemplares) *:", txtStock);

        JPanel botones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 10));
        botones.add(btnGuardar);
        botones.add(btnCancelar);

        JPanel norte = new JPanel(new BorderLayout());
        norte.setBorder(BorderFactory.createEmptyBorder(12, 20, 0, 20));
        norte.add(Estilos.titulo(getTitle()), BorderLayout.NORTH);
        norte.add(new JLabel("Los campos con * son obligatorios."), BorderLayout.SOUTH);

        getContentPane().setLayout(new BorderLayout());
        add(norte, BorderLayout.NORTH);
        add(formulario, BorderLayout.CENTER);
        add(botones, BorderLayout.SOUTH);

        getRootPane().setDefaultButton(btnGuardar);
        btnGuardar.addActionListener(e -> guardar());
        btnCancelar.addActionListener(e -> dispose());
    }

    /**
     * Asigna un nombre al componente y lo asocia a su campo del controlador.
     *
     * @param nombre nombre del campo
     * @param componente componente del formulario
     */
    private void registrarCampo(String nombre, JComponent componente) {
        componente.setName(nombre);
        campos.put(nombre, componente);
    }

    /**
     * Agrega una fila (etiqueta + componente) al formulario.
     *
     * @param panel panel con GridBagLayout
     * @param fila número de fila
     * @param etiqueta texto de la etiqueta
     * @param componente componente de entrada
     */
    private void agregarFila(JPanel panel, int fila, String etiqueta, JComponent componente) {
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(5, 5, 5, 5);
        g.anchor = GridBagConstraints.WEST;
        g.gridy = fila;
        g.gridx = 0;
        panel.add(new JLabel(etiqueta), g);
        g.gridx = 1;
        panel.add(componente, g);
    }

    /**
     * Carga en el formulario los datos del libro a modificar.
     *
     * @param l libro a modificar
     */
    private void cargarDatos(Libro l) {
        txtIsbn.setText(l.getIsbn());
        txtTitulo.setText(l.getTitulo());
        txtEditorial.setText(l.getEditorial());
        txtAnio.setText(String.valueOf(l.getAnioPublicacion()));
        cboGenero.setSelectedItem(l.getGenero());
        txtPaginas.setText(String.valueOf(l.getNumeroPaginas()));
        txtStock.setText(String.valueOf(l.getStock()));
    }

    /**
     * Devuelve el género elegido, o una cadena vacía si no se eligió.
     *
     * @return género seleccionado
     */
    private String generoSeleccionado() {
        Object item = cboGenero.getSelectedItem();
        return item == null || SELECCIONE.equals(item) ? "" : item.toString();
    }

    /**
     * Envía los datos al controlador para registrar o modificar el libro.
     */
    private void guardar() {
        Respuesta r;
        if (libro == null) {
            r = controller.registrarLibro(txtIsbn.getText(), txtTitulo.getText(),
                    txtEditorial.getText(), txtAnio.getText(), generoSeleccionado(),
                    txtPaginas.getText(), txtStock.getText());
        } else {
            r = controller.actualizarLibro(libro.getIdLibro(), txtIsbn.getText(), txtTitulo.getText(),
                    txtEditorial.getText(), txtAnio.getText(), generoSeleccionado(),
                    txtPaginas.getText(), txtStock.getText());
        }
        if (r.isExitosa()) {
            guardado = true;
            JOptionPane.showMessageDialog(this, r.getMensaje(), getTitle(), JOptionPane.INFORMATION_MESSAGE);
            dispose();
            return;
        }
        JOptionPane.showMessageDialog(this, r.getMensaje(), "Datos no válidos", JOptionPane.WARNING_MESSAGE);
        enfocarCampo(r.getCampo());
    }

    /**
     * Coloca el cursor en el campo que produjo el error.
     *
     * @param campo nombre del campo, o {@code null}
     */
    private void enfocarCampo(String campo) {
        JComponent componente = campos.get(campo);
        if (componente != null) {
            componente.requestFocusInWindow();
            if (componente instanceof JTextField) {
                ((JTextField) componente).selectAll();
            }
        }
    }

    /**
     * Indica si el libro se guardó antes de cerrar el diálogo.
     *
     * @return {@code true} si se registró o modificó correctamente
     */
    public boolean isGuardado() {
        return guardado;
    }
}
