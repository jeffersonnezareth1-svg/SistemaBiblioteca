package vista;

import controlador.PrestamoController;
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
import javax.swing.JList;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import modelo.Lector;
import modelo.Libro;
import modelo.Prestamo;
import util.Fechas;
import util.Respuesta;

/**
 * Formulario modal para registrar (C) o modificar (U) un préstamo.
 *
 * <p>El libro y el lector se eligen de listas desplegables cargadas desde la
 * base de datos. Al registrar, la fecha del préstamo es hoy y la fecha límite
 * se propone a 7 días.</p>
 *
 * @author Guashpa Gualli Jefferson Nezareth
 * @version 1.0
 */
public class PrestamoDialog extends JDialog {

    private static final String SELECCIONE = "-- Seleccione --";

    /** Días que se proponen por defecto para la fecha límite. */
    public static final int DIAS_POR_DEFECTO = 7;

    /** Controlador que valida y ejecuta las operaciones. */
    private final PrestamoController controller;
    /** Préstamo que se modifica, o null si se registra uno nuevo. */
    private final Prestamo prestamo;
    /** Indica si el préstamo se guardó correctamente. */
    private boolean guardado;

    /** Lista desplegable de libros. */
    private final JComboBox<Object> cboLibro = new JComboBox<>();
    /** Lista desplegable de lectores. */
    private final JComboBox<Object> cboLector = new JComboBox<>();
    /** Campo de texto de la fecha del préstamo. */
    private final JTextField txtFechaPrestamo = new JTextField(10);
    /** Campo de texto de la fecha límite. */
    private final JTextField txtFechaLimite = new JTextField(10);
    /** Campo de texto de la observación. */
    private final JTextField txtObservacion = new JTextField(30);
    /** Botón guardar. */
    private final JButton btnGuardar = Estilos.boton("Guardar", "Guardar el préstamo");
    /** Botón cancelar. */
    private final JButton btnCancelar = Estilos.boton("Cancelar", "Cerrar sin guardar");
    /** Relación entre el nombre de cada campo y su componente. */
    private final Map<String, JComponent> campos = new HashMap<>();

    /**
     * Crea el formulario.
     *
     * @param owner ventana dueña del diálogo
     * @param controller controlador del módulo Préstamo
     * @param prestamo préstamo a modificar, o {@code null} para registrar uno
     */
    public PrestamoDialog(Window owner, PrestamoController controller, Prestamo prestamo) {
        super(owner, prestamo == null ? "Registrar préstamo" : "Modificar préstamo",
                ModalityType.APPLICATION_MODAL);
        this.controller = controller;
        this.prestamo = prestamo;
        initComponents();
        cargarCombos();
        if (prestamo == null) {
            txtFechaPrestamo.setText(Fechas.hoy());
            txtFechaLimite.setText(Fechas.formatear(LocalDate.now().plusDays(DIAS_POR_DEFECTO)));
        } else {
            cargarDatos(prestamo);
        }
        pack();
        setResizable(false);
        setLocationRelativeTo(owner);
    }

    /**
     * Crea y organiza los componentes del formulario.
     */
    private void initComponents() {
        cboLibro.setRenderer(new OpcionRenderer());
        cboLector.setRenderer(new OpcionRenderer());
        registrarCampo(PrestamoController.CAMPO_LIBRO, cboLibro);
        registrarCampo(PrestamoController.CAMPO_LECTOR, cboLector);
        registrarCampo(PrestamoController.CAMPO_FECHA_PRESTAMO, txtFechaPrestamo);
        registrarCampo(PrestamoController.CAMPO_FECHA_LIMITE, txtFechaLimite);
        registrarCampo(PrestamoController.CAMPO_OBSERVACION, txtObservacion);

        JPanel formulario = new JPanel(new GridBagLayout());
        formulario.setBorder(BorderFactory.createEmptyBorder(15, 20, 5, 20));
        agregarFila(formulario, 0, "Libro *:", cboLibro);
        agregarFila(formulario, 1, "Lector *:", cboLector);
        agregarFila(formulario, 2, "Fecha del préstamo * (" + Fechas.PATRON + "):", txtFechaPrestamo);
        agregarFila(formulario, 3, "Fecha límite * (" + Fechas.PATRON + "):", txtFechaLimite);
        agregarFila(formulario, 4, "Observación:", txtObservacion);

        JPanel botones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 10));
        botones.add(btnGuardar);
        botones.add(btnCancelar);

        JPanel norte = new JPanel(new BorderLayout());
        norte.setBorder(BorderFactory.createEmptyBorder(12, 20, 0, 20));
        norte.add(Estilos.titulo(getTitle()), BorderLayout.NORTH);
        norte.add(new JLabel("Los campos con * son obligatorios. Plazo máximo: "
                + PrestamoController.DIAS_MAXIMOS + " días."), BorderLayout.SOUTH);

        getContentPane().setLayout(new BorderLayout());
        add(norte, BorderLayout.NORTH);
        add(formulario, BorderLayout.CENTER);
        add(botones, BorderLayout.SOUTH);

        getRootPane().setDefaultButton(btnGuardar);
        btnGuardar.addActionListener(e -> guardar());
        btnCancelar.addActionListener(e -> dispose());
    }

    /**
     * Llena las listas de libros (con su stock) y de lectores (con su
     * estado).
     */
    private void cargarCombos() {
        cboLibro.addItem(SELECCIONE);
        cboLector.addItem(SELECCIONE);
        try {
            for (Libro l : controller.listarLibros()) {
                cboLibro.addItem(l);
            }
            for (Lector l : controller.listarLectores()) {
                cboLector.addItem(l);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "No se pudo cargar libros y lectores: " + e.getMessage(),
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
        g.insets = new Insets(5, 5, 5, 5);
        g.anchor = GridBagConstraints.WEST;
        g.gridy = fila;
        g.gridx = 0;
        panel.add(new JLabel(etiqueta), g);
        g.gridx = 1;
        panel.add(componente, g);
    }

    /**
     * Carga en el formulario los datos del préstamo a modificar.
     *
     * @param p préstamo a modificar
     */
    private void cargarDatos(Prestamo p) {
        seleccionarPorId(cboLibro, p.getIdLibro());
        seleccionarPorId(cboLector, p.getIdLector());
        txtFechaPrestamo.setText(Fechas.formatear(p.getFechaPrestamo()));
        txtFechaLimite.setText(Fechas.formatear(p.getFechaLimite()));
        txtObservacion.setText(p.getObservacion() == null ? "" : p.getObservacion());
    }

    /**
     * Selecciona en un combo el libro o lector con el id indicado.
     *
     * @param combo combo de libros o de lectores
     * @param id identificador a seleccionar
     */
    private void seleccionarPorId(JComboBox<Object> combo, int id) {
        for (int i = 0; i < combo.getItemCount(); i++) {
            if (idDe(combo.getItemAt(i)) == id) {
                combo.setSelectedIndex(i);
                return;
            }
        }
    }

    /**
     * Devuelve el id del libro o lector de un elemento del combo.
     *
     * @param item elemento del combo
     * @return id del libro o lector, o 0 si es la opción "Seleccione"
     */
    private int idDe(Object item) {
        if (item instanceof Libro) {
            return ((Libro) item).getIdLibro();
        }
        if (item instanceof Lector) {
            return ((Lector) item).getIdLector();
        }
        return 0;
    }

    /**
     * Envía los datos al controlador para registrar o modificar el préstamo.
     */
    private void guardar() {
        int idLibro = idDe(cboLibro.getSelectedItem());
        int idLector = idDe(cboLector.getSelectedItem());
        Respuesta r;
        if (prestamo == null) {
            r = controller.registrarPrestamo(idLibro, idLector, txtFechaPrestamo.getText(),
                    txtFechaLimite.getText(), txtObservacion.getText());
        } else {
            r = controller.actualizarPrestamo(prestamo.getIdPrestamo(), idLibro, idLector,
                    txtFechaPrestamo.getText(), txtFechaLimite.getText(), txtObservacion.getText());
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
     * Muestra cada libro con su stock y cada lector con su estado dentro de
     * las listas desplegables.
     */
    private static class OpcionRenderer extends DefaultListCellRenderer {

        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                boolean isSelected, boolean cellHasFocus) {
            Object texto = value;
            if (value instanceof Libro) {
                Libro l = (Libro) value;
                texto = l.getTitulo() + "  (stock: " + l.getStock() + ")";
            } else if (value instanceof Lector) {
                Lector l = (Lector) value;
                texto = l.getNombres() + " " + l.getApellidos() + "  -  " + l.getCedula()
                        + (Lector.ACTIVO.equals(l.getEstado()) ? "" : "  (" + l.getEstado() + ")");
            }
            return super.getListCellRendererComponent(list, texto, index, isSelected, cellHasFocus);
        }
    }

    /**
     * Indica si el préstamo se guardó antes de cerrar el diálogo.
     *
     * @return {@code true} si se registró o modificó correctamente
     */
    public boolean isGuardado() {
        return guardado;
    }
}
