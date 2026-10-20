package vista;

import controlador.DevolucionController;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import modelo.Devolucion;
import modelo.Prestamo;
import util.Fechas;
import util.Respuesta;

/**
 * Formulario modal para registrar (C) o modificar (U) una devolución.
 *
 * <p>Al registrar se elige un préstamo pendiente (ACTIVO o VENCIDO). Al
 * modificar, el préstamo no se puede cambiar. Los días de retraso y la multa
 * se calculan y muestran mientras se escribe la fecha o se cambia el estado
 * del libro.</p>
 *
 * @author Guashpa Gualli Jefferson Nezareth
 * @version 1.0
 */
public class DevolucionDialog extends JDialog {

    private static final String SELECCIONE = "-- Seleccione --";

    /** Controlador que valida y ejecuta las operaciones. */
    private final DevolucionController controller;
    /** Devolución que se modifica, o null si se registra una nueva. */
    private final Devolucion devolucion;
    /** Indica si la devolución se guardó correctamente. */
    private boolean guardado;

    /** Lista desplegable de préstamos pendientes. */
    private final JComboBox<Object> cboPrestamo = new JComboBox<>();
    /** Etiqueta con las fechas del préstamo seleccionado. */
    private final JLabel lblFechasPrestamo = new JLabel(" ");
    /** Campo de texto de la fecha de devolución. */
    private final JTextField txtFechaDevolucion = new JTextField(10);
    /** Lista desplegable del estado del libro. */
    private final JComboBox<String> cboEstadoLibro = new JComboBox<>();
    /** Campo de texto de la observación. */
    private final JTextField txtObservacion = new JTextField(30);
    /** Etiqueta con los días de retraso calculados. */
    private final JLabel lblDiasRetraso = new JLabel("-");
    /** Etiqueta con la multa calculada. */
    private final JLabel lblMulta = new JLabel("-");
    /** Botón guardar. */
    private final JButton btnGuardar = Estilos.boton("Guardar", "Guardar la devolución");
    /** Botón cancelar. */
    private final JButton btnCancelar = Estilos.boton("Cancelar", "Cerrar sin guardar");
    /** Relación entre el nombre de cada campo y su componente. */
    private final Map<String, JComponent> campos = new HashMap<>();

    /**
     * Crea el formulario.
     *
     * @param owner ventana dueña del diálogo
     * @param controller controlador del módulo Devolución
     * @param devolucion devolución a modificar, o {@code null} para registrar
     */
    public DevolucionDialog(Window owner, DevolucionController controller, Devolucion devolucion) {
        super(owner, devolucion == null ? "Registrar devolución" : "Modificar devolución",
                ModalityType.APPLICATION_MODAL);
        this.controller = controller;
        this.devolucion = devolucion;
        initComponents();
        cargarPrestamos();
        if (devolucion == null) {
            txtFechaDevolucion.setText(Fechas.hoy());
        } else {
            cargarDatos(devolucion);
        }
        actualizarCalculo();
        pack();
        setResizable(false);
        setLocationRelativeTo(owner);
    }

    /**
     * Crea y organiza los componentes del formulario.
     */
    private void initComponents() {
        for (String estado : DevolucionController.ESTADOS_LIBRO) {
            cboEstadoLibro.addItem(estado);
        }
        cboPrestamo.setRenderer(new PrestamoRenderer());
        registrarCampo(DevolucionController.CAMPO_PRESTAMO, cboPrestamo);
        registrarCampo(DevolucionController.CAMPO_FECHA, txtFechaDevolucion);
        registrarCampo(DevolucionController.CAMPO_ESTADO, cboEstadoLibro);
        registrarCampo(DevolucionController.CAMPO_OBSERVACION, txtObservacion);
        lblDiasRetraso.setName("lblDiasRetraso");
        lblMulta.setName("lblMulta");
        lblMulta.setFont(lblMulta.getFont().deriveFont(java.awt.Font.BOLD));

        JPanel formulario = new JPanel(new GridBagLayout());
        formulario.setBorder(BorderFactory.createEmptyBorder(15, 20, 5, 20));
        agregarFila(formulario, 0, "Préstamo *:", cboPrestamo);
        agregarFila(formulario, 1, "", lblFechasPrestamo);
        agregarFila(formulario, 2, "Fecha de devolución * (" + Fechas.PATRON + "):", txtFechaDevolucion);
        agregarFila(formulario, 3, "Estado del libro *:", cboEstadoLibro);
        agregarFila(formulario, 4, "Observación:", txtObservacion);
        agregarFila(formulario, 5, "Días de retraso:", lblDiasRetraso);
        agregarFila(formulario, 6, "Multa ($):", lblMulta);

        JPanel botones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 10));
        botones.add(btnGuardar);
        botones.add(btnCancelar);

        JPanel norte = new JPanel(new BorderLayout());
        norte.setBorder(BorderFactory.createEmptyBorder(12, 20, 0, 20));
        norte.add(Estilos.titulo(getTitle()), BorderLayout.NORTH);
        norte.add(new JLabel("Multa: $" + DevolucionController.MULTA_POR_DIA + " por día de retraso, + $"
                + DevolucionController.RECARGO_DANADO + " si está dañado, + $"
                + DevolucionController.RECARGO_PERDIDO + " si se perdió."), BorderLayout.SOUTH);

        getContentPane().setLayout(new BorderLayout());
        add(norte, BorderLayout.NORTH);
        add(formulario, BorderLayout.CENTER);
        add(botones, BorderLayout.SOUTH);

        cboPrestamo.addActionListener(e -> actualizarCalculo());
        cboEstadoLibro.addActionListener(e -> actualizarCalculo());
        txtFechaDevolucion.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                actualizarCalculo();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                actualizarCalculo();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                actualizarCalculo();
            }
        });

        getRootPane().setDefaultButton(btnGuardar);
        btnGuardar.addActionListener(e -> guardar());
        btnCancelar.addActionListener(e -> dispose());
    }

    /**
     * Llena el combo de préstamos: los pendientes al registrar, o solo el
     * préstamo de la devolución al modificar.
     */
    private void cargarPrestamos() {
        try {
            if (devolucion == null) {
                cboPrestamo.addItem(SELECCIONE);
                for (Prestamo p : controller.listarPrestamosPendientes()) {
                    cboPrestamo.addItem(p);
                }
            } else {
                cboPrestamo.addItem(controller.obtenerPrestamo(devolucion.getIdPrestamo()));
                cboPrestamo.setEnabled(false);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "No se pudo cargar los préstamos: " + e.getMessage(),
                    "Error de base de datos", JOptionPane.ERROR_MESSAGE);
        }
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
        g.insets = new Insets(4, 5, 4, 5);
        g.anchor = GridBagConstraints.WEST;
        g.gridy = fila;
        g.gridx = 0;
        panel.add(new JLabel(etiqueta), g);
        g.gridx = 1;
        panel.add(componente, g);
    }

    /**
     * Carga en el formulario los datos de la devolución a modificar.
     *
     * @param d devolución a modificar
     */
    private void cargarDatos(Devolucion d) {
        txtFechaDevolucion.setText(Fechas.formatear(d.getFechaDevolucion()));
        cboEstadoLibro.setSelectedItem(d.getEstadoLibro());
        txtObservacion.setText(d.getObservacion() == null ? "" : d.getObservacion());
    }

    /**
     * Devuelve el préstamo seleccionado en el combo.
     *
     * @return préstamo seleccionado o {@code null}
     */
    private Prestamo prestamoSeleccionado() {
        Object item = cboPrestamo.getSelectedItem();
        return item instanceof Prestamo ? (Prestamo) item : null;
    }

    /**
     * Muestra las fechas del préstamo y recalcula días de retraso y multa con
     * los datos actuales del formulario.
     */
    private void actualizarCalculo() {
        Prestamo p = prestamoSeleccionado();
        LocalDate fecha = Fechas.convertir(txtFechaDevolucion.getText());
        if (p == null) {
            lblFechasPrestamo.setText(" ");
        } else {
            lblFechasPrestamo.setText("Prestado el " + Fechas.formatear(p.getFechaPrestamo())
                    + " - fecha límite " + Fechas.formatear(p.getFechaLimite()));
        }
        if (p == null || fecha == null) {
            lblDiasRetraso.setText("-");
            lblMulta.setText("-");
            return;
        }
        int dias = controller.calcularDiasRetraso(p.getFechaLimite(), fecha);
        String estado = String.valueOf(cboEstadoLibro.getSelectedItem());
        lblDiasRetraso.setText(String.valueOf(dias));
        lblMulta.setText(controller.calcularMulta(dias, estado).toPlainString());
    }

    /**
     * Envía los datos al controlador para registrar o modificar la
     * devolución.
     */
    private void guardar() {
        String estado = cboEstadoLibro.getSelectedItem() == null ? "" : cboEstadoLibro.getSelectedItem().toString();
        Respuesta r;
        if (devolucion == null) {
            Prestamo p = prestamoSeleccionado();
            r = controller.registrarDevolucion(p == null ? 0 : p.getIdPrestamo(),
                    txtFechaDevolucion.getText(), estado, txtObservacion.getText());
        } else {
            r = controller.actualizarDevolucion(devolucion.getIdDevolucion(),
                    txtFechaDevolucion.getText(), estado, txtObservacion.getText());
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
     * Muestra cada préstamo del combo con su número, libro, lector y estado.
     */
    private static class PrestamoRenderer extends DefaultListCellRenderer {

        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                boolean isSelected, boolean cellHasFocus) {
            Object texto = value;
            if (value instanceof Prestamo) {
                Prestamo p = (Prestamo) value;
                texto = p + "  (" + p.getEstado() + ")";
            }
            return super.getListCellRendererComponent(list, texto, index, isSelected, cellHasFocus);
        }
    }

    /**
     * Indica si la devolución se guardó antes de cerrar el diálogo.
     *
     * @return {@code true} si se registró o modificó correctamente
     */
    public boolean isGuardado() {
        return guardado;
    }
}
