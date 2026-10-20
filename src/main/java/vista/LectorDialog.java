package vista;

import controlador.LectorController;
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
import modelo.Lector;
import util.Respuesta;

/**
 * Formulario modal para registrar (C) o modificar (U) un lector.
 *
 * @author Guashpa Gualli Jefferson Nezareth
 * @version 1.0
 */
public class LectorDialog extends JDialog {

    /** Controlador que valida y ejecuta las operaciones. */
    private final LectorController controller;
    /** Lector que se modifica, o null si se registra uno nuevo. */
    private final Lector lector;
    /** Indica si el lector se guardó correctamente. */
    private boolean guardado;

    /** Campo de texto de la cédula. */
    private final JTextField txtCedula = new JTextField(12);
    /** Campo de texto de los nombres. */
    private final JTextField txtNombres = new JTextField(25);
    /** Campo de texto de los apellidos. */
    private final JTextField txtApellidos = new JTextField(25);
    /** Campo de texto del correo. */
    private final JTextField txtCorreo = new JTextField(25);
    /** Campo de texto del teléfono. */
    private final JTextField txtTelefono = new JTextField(12);
    /** Campo de texto de la dirección. */
    private final JTextField txtDireccion = new JTextField(25);
    /** Lista desplegable de estados. */
    private final JComboBox<String> cboEstado = new JComboBox<>();
    /** Botón guardar. */
    private final JButton btnGuardar = Estilos.boton("Guardar", "Guardar los datos del lector");
    /** Botón cancelar. */
    private final JButton btnCancelar = Estilos.boton("Cancelar", "Cerrar sin guardar");
    /** Relación entre el nombre de cada campo y su componente. */
    private final Map<String, JComponent> campos = new HashMap<>();

    /**
     * Crea el formulario.
     *
     * @param owner ventana dueña del diálogo
     * @param controller controlador del módulo Lector
     * @param lector lector a modificar, o {@code null} para registrar uno nuevo
     */
    public LectorDialog(Window owner, LectorController controller, Lector lector) {
        super(owner, lector == null ? "Registrar lector" : "Modificar lector", ModalityType.APPLICATION_MODAL);
        this.controller = controller;
        this.lector = lector;
        initComponents();
        if (lector != null) {
            cargarDatos(lector);
        }
        pack();
        setResizable(false);
        setLocationRelativeTo(owner);
    }

    /**
     * Crea y organiza los componentes del formulario.
     */
    private void initComponents() {
        for (String estado : LectorController.ESTADOS) {
            cboEstado.addItem(estado);
        }
        registrarCampo(LectorController.CAMPO_CEDULA, txtCedula);
        registrarCampo(LectorController.CAMPO_NOMBRES, txtNombres);
        registrarCampo(LectorController.CAMPO_APELLIDOS, txtApellidos);
        registrarCampo(LectorController.CAMPO_CORREO, txtCorreo);
        registrarCampo(LectorController.CAMPO_TELEFONO, txtTelefono);
        registrarCampo(LectorController.CAMPO_DIRECCION, txtDireccion);
        registrarCampo(LectorController.CAMPO_ESTADO, cboEstado);

        JPanel formulario = new JPanel(new GridBagLayout());
        formulario.setBorder(BorderFactory.createEmptyBorder(15, 20, 5, 20));
        agregarFila(formulario, 0, "Cédula *:", txtCedula);
        agregarFila(formulario, 1, "Nombres *:", txtNombres);
        agregarFila(formulario, 2, "Apellidos *:", txtApellidos);
        agregarFila(formulario, 3, "Correo *:", txtCorreo);
        agregarFila(formulario, 4, "Teléfono celular *:", txtTelefono);
        agregarFila(formulario, 5, "Dirección:", txtDireccion);
        agregarFila(formulario, 6, "Estado *:", cboEstado);

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
     * Carga en el formulario los datos del lector a modificar.
     *
     * @param l lector a modificar
     */
    private void cargarDatos(Lector l) {
        txtCedula.setText(l.getCedula());
        txtNombres.setText(l.getNombres());
        txtApellidos.setText(l.getApellidos());
        txtCorreo.setText(l.getCorreo());
        txtTelefono.setText(l.getTelefono());
        txtDireccion.setText(l.getDireccion() == null ? "" : l.getDireccion());
        cboEstado.setSelectedItem(l.getEstado());
    }

    /**
     * Envía los datos al controlador para registrar o modificar el lector.
     */
    private void guardar() {
        String estado = cboEstado.getSelectedItem() == null ? "" : cboEstado.getSelectedItem().toString();
        Respuesta r;
        if (lector == null) {
            r = controller.registrarLector(txtCedula.getText(), txtNombres.getText(), txtApellidos.getText(),
                    txtCorreo.getText(), txtTelefono.getText(), txtDireccion.getText(), estado);
        } else {
            r = controller.actualizarLector(lector.getIdLector(), txtCedula.getText(), txtNombres.getText(),
                    txtApellidos.getText(), txtCorreo.getText(), txtTelefono.getText(),
                    txtDireccion.getText(), estado);
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
     * Indica si el lector se guardó antes de cerrar el diálogo.
     *
     * @return {@code true} si se registró o modificó correctamente
     */
    public boolean isGuardado() {
        return guardado;
    }
}
