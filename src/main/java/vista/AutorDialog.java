package vista;

import controlador.AutorController;
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
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.text.JTextComponent;
import modelo.Autor;
import util.Fechas;
import util.Respuesta;

/**
 * Formulario modal para registrar (C) o modificar (U) un autor.
 *
 * @author Guashpa Gualli Jefferson Nezareth
 * @version 1.0
 */
public class AutorDialog extends JDialog {

    /** Controlador que valida y ejecuta las operaciones. */
    private final AutorController controller;
    /** Autor que se modifica, o null si se registra uno nuevo. */
    private final Autor autor;
    /** Indica si el autor se guardó correctamente. */
    private boolean guardado;

    /** Campo de texto de los nombres. */
    private final JTextField txtNombres = new JTextField(25);
    /** Campo de texto de los apellidos. */
    private final JTextField txtApellidos = new JTextField(25);
    /** Campo de texto de la nacionalidad. */
    private final JTextField txtNacionalidad = new JTextField(18);
    /** Campo de texto de la fecha de nacimiento. */
    private final JTextField txtFechaNacimiento = new JTextField(10);
    /** Área de texto de la biografía. */
    private final JTextArea txtBiografia = new JTextArea(4, 25);
    /** Botón guardar. */
    private final JButton btnGuardar = Estilos.boton("Guardar", "Guardar los datos del autor");
    /** Botón cancelar. */
    private final JButton btnCancelar = Estilos.boton("Cancelar", "Cerrar sin guardar");
    /** Relación entre el nombre de cada campo y su componente. */
    private final Map<String, JComponent> campos = new HashMap<>();

    /**
     * Crea el formulario.
     *
     * @param owner ventana dueña del diálogo
     * @param controller controlador del módulo Autor
     * @param autor autor a modificar, o {@code null} para registrar uno nuevo
     */
    public AutorDialog(Window owner, AutorController controller, Autor autor) {
        super(owner, autor == null ? "Registrar autor" : "Modificar autor", ModalityType.APPLICATION_MODAL);
        this.controller = controller;
        this.autor = autor;
        initComponents();
        if (autor != null) {
            cargarDatos(autor);
        }
        pack();
        setResizable(false);
        setLocationRelativeTo(owner);
    }

    /**
     * Crea y organiza los componentes del formulario.
     */
    private void initComponents() {
        txtBiografia.setLineWrap(true);
        txtBiografia.setWrapStyleWord(true);
        registrarCampo(AutorController.CAMPO_NOMBRES, txtNombres);
        registrarCampo(AutorController.CAMPO_APELLIDOS, txtApellidos);
        registrarCampo(AutorController.CAMPO_NACIONALIDAD, txtNacionalidad);
        registrarCampo(AutorController.CAMPO_FECHA, txtFechaNacimiento);
        registrarCampo(AutorController.CAMPO_BIOGRAFIA, txtBiografia);

        JPanel formulario = new JPanel(new GridBagLayout());
        formulario.setBorder(BorderFactory.createEmptyBorder(15, 20, 5, 20));
        agregarFila(formulario, 0, "Nombres *:", txtNombres);
        agregarFila(formulario, 1, "Apellidos *:", txtApellidos);
        agregarFila(formulario, 2, "Nacionalidad *:", txtNacionalidad);
        agregarFila(formulario, 3, "Fecha de nacimiento (" + Fechas.PATRON + "):", txtFechaNacimiento);
        agregarFila(formulario, 4, "Biografía:", new JScrollPane(txtBiografia));

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
        g.anchor = GridBagConstraints.NORTHWEST;
        g.gridy = fila;
        g.gridx = 0;
        panel.add(new JLabel(etiqueta), g);
        g.gridx = 1;
        panel.add(componente, g);
    }

    /**
     * Carga en el formulario los datos del autor a modificar.
     *
     * @param a autor a modificar
     */
    private void cargarDatos(Autor a) {
        txtNombres.setText(a.getNombres());
        txtApellidos.setText(a.getApellidos());
        txtNacionalidad.setText(a.getNacionalidad());
        txtFechaNacimiento.setText(Fechas.formatear(a.getFechaNacimiento()));
        txtBiografia.setText(a.getBiografia() == null ? "" : a.getBiografia());
    }

    /**
     * Envía los datos al controlador para registrar o modificar el autor.
     */
    private void guardar() {
        Respuesta r;
        if (autor == null) {
            r = controller.registrarAutor(txtNombres.getText(), txtApellidos.getText(),
                    txtNacionalidad.getText(), txtFechaNacimiento.getText(), txtBiografia.getText());
        } else {
            r = controller.actualizarAutor(autor.getIdAutor(), txtNombres.getText(), txtApellidos.getText(),
                    txtNacionalidad.getText(), txtFechaNacimiento.getText(), txtBiografia.getText());
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
            if (componente instanceof JTextComponent) {
                ((JTextComponent) componente).selectAll();
            }
        }
    }

    /**
     * Indica si el autor se guardó antes de cerrar el diálogo.
     *
     * @return {@code true} si se registró o modificó correctamente
     */
    public boolean isGuardado() {
        return guardado;
    }
}
