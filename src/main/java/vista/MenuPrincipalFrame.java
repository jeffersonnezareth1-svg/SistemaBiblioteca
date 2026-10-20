package vista;

import controlador.LoginController;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import modelo.Usuario;
import util.Sesion;

/**
 * Menú principal del sistema.
 *
 * <p>Muestra un encabezado con el usuario en sesión, un menú lateral con los
 * cinco módulos y un área central donde se carga el panel CRUD de cada
 * módulo mediante {@link CardLayout}. Cada vez que se abre un módulo, sus
 * datos se vuelven a leer de la base (por ejemplo, el stock de libros después
 * de un préstamo).</p>
 *
 * @author Guashpa Gualli Jefferson Nezareth
 * @version 1.0
 */
public class MenuPrincipalFrame extends JFrame {

    /** Identificador de la tarjeta de inicio. */
    public static final String INICIO = "Inicio";
    /** Identificador de la tarjeta del módulo Libro. */
    public static final String LIBROS = "Libros";
    /** Identificador de la tarjeta del módulo Autor. */
    public static final String AUTORES = "Autores";
    /** Identificador de la tarjeta del módulo Lector. */
    public static final String LECTORES = "Lectores";
    /** Identificador de la tarjeta del módulo Préstamo. */
    public static final String PRESTAMOS = "Préstamos";
    /** Identificador de la tarjeta del módulo Devolución. */
    public static final String DEVOLUCIONES = "Devoluciones";

    private static final String[] MODULOS = {INICIO, LIBROS, AUTORES, LECTORES, PRESTAMOS, DEVOLUCIONES};

    /** Distribución que muestra un módulo a la vez. */
    private final CardLayout tarjetas = new CardLayout();
    /** Área central donde se cargan los módulos. */
    private final JPanel contenido = new JPanel(tarjetas);
    /** Paneles de módulo que se recargan al abrirse. */
    private final Map<String, Recargable> recargables = new HashMap<>();

    /**
     * Construye el menú principal para el usuario en sesión.
     */
    public MenuPrincipalFrame() {
        super("Sistema de Gestión de Biblioteca");
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                salir();
            }
        });
        setMinimumSize(new Dimension(1000, 620));
        initComponents();
        setLocationRelativeTo(null);
    }

    /**
     * Crea el encabezado, el menú lateral, la barra de menú y el contenido.
     */
    private void initComponents() {
        setJMenuBar(crearBarraMenu());
        getContentPane().setLayout(new BorderLayout());
        add(crearEncabezado(), BorderLayout.NORTH);
        add(crearMenuLateral(), BorderLayout.WEST);
        registrarModulos();
        add(contenido, BorderLayout.CENTER);
        mostrar(INICIO);
    }

    /**
     * Agrega al área central el panel de cada uno de los cinco módulos.
     */
    private void registrarModulos() {
        contenido.add(crearPanelInicio(), INICIO);
        registrar(new LibroCRUDPanel(), LIBROS);
        registrar(new AutorCRUDPanel(), AUTORES);
        registrar(new LectorCRUDPanel(), LECTORES);
        registrar(new PrestamoCRUDPanel(), PRESTAMOS);
        registrar(new DevolucionCRUDPanel(), DEVOLUCIONES);
    }

    /**
     * Agrega un panel de módulo al área central y lo recuerda para recargarlo
     * cada vez que se abra.
     *
     * @param panel panel del módulo
     * @param modulo identificador del módulo
     */
    private void registrar(JPanel panel, String modulo) {
        contenido.add(panel, modulo);
        if (panel instanceof Recargable) {
            recargables.put(modulo, (Recargable) panel);
        }
    }

    /**
     * Crea la barra de menú superior.
     *
     * @return barra de menú
     */
    private JMenuBar crearBarraMenu() {
        JMenuBar barra = new JMenuBar();

        JMenu mnuArchivo = new JMenu("Archivo");
        JMenuItem itmCerrarSesion = new JMenuItem("Cerrar sesión");
        itmCerrarSesion.addActionListener(e -> cerrarSesion());
        JMenuItem itmSalir = new JMenuItem("Salir");
        itmSalir.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, KeyEvent.CTRL_DOWN_MASK));
        itmSalir.addActionListener(e -> salir());
        mnuArchivo.add(itmCerrarSesion);
        mnuArchivo.addSeparator();
        mnuArchivo.add(itmSalir);

        JMenu mnuModulos = new JMenu("Módulos");
        for (String modulo : MODULOS) {
            JMenuItem item = new JMenuItem(modulo);
            item.addActionListener(e -> mostrar(modulo));
            mnuModulos.add(item);
        }

        JMenu mnuAyuda = new JMenu("Ayuda");
        JMenuItem itmAcerca = new JMenuItem("Acerca de");
        itmAcerca.addActionListener(e -> JOptionPane.showMessageDialog(this,
                "Sistema de Gestión de Biblioteca v1.0\n"
                + "Proyecto Primer Parcial - Verificación y Validación de Software",
                "Acerca de", JOptionPane.INFORMATION_MESSAGE));
        mnuAyuda.add(itmAcerca);

        barra.add(mnuArchivo);
        barra.add(mnuModulos);
        barra.add(mnuAyuda);
        return barra;
    }

    /**
     * Crea el encabezado con el nombre del sistema y el usuario en sesión.
     *
     * @return panel de encabezado
     */
    private JPanel crearEncabezado() {
        JPanel encabezado = new JPanel(new BorderLayout());
        encabezado.setBackground(Estilos.PRIMARIO);
        encabezado.setBorder(BorderFactory.createEmptyBorder(10, 16, 10, 16));

        JLabel lblSistema = new JLabel("Sistema de Gestión de Biblioteca");
        lblSistema.setFont(Estilos.TITULO);
        lblSistema.setForeground(Color.WHITE);

        Usuario u = Sesion.getUsuarioActual();
        String texto = u == null ? "" : u.getNombres() + "  (" + u.getRol() + ")";
        JLabel lblUsuario = new JLabel(texto);
        lblUsuario.setForeground(Color.WHITE);

        JButton btnCerrar = Estilos.boton("Cerrar sesión", "Volver a la ventana de inicio de sesión");
        btnCerrar.addActionListener(e -> cerrarSesion());

        JPanel derecha = new JPanel();
        derecha.setOpaque(false);
        derecha.add(lblUsuario);
        derecha.add(btnCerrar);

        encabezado.add(lblSistema, BorderLayout.WEST);
        encabezado.add(derecha, BorderLayout.EAST);
        return encabezado;
    }

    /**
     * Crea el menú lateral con un botón por módulo.
     *
     * @return panel del menú lateral
     */
    private JPanel crearMenuLateral() {
        JPanel lateral = new JPanel();
        lateral.setLayout(new BoxLayout(lateral, BoxLayout.Y_AXIS));
        lateral.setBackground(Estilos.PRIMARIO);
        lateral.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        lateral.setPreferredSize(new Dimension(180, 0));
        for (String modulo : MODULOS) {
            lateral.add(crearBotonLateral(modulo));
        }
        return lateral;
    }

    /**
     * Crea un botón del menú lateral.
     *
     * @param modulo módulo que abre el botón
     * @return botón configurado
     */
    private JButton crearBotonLateral(String modulo) {
        JButton btn = new JButton(modulo);
        btn.setName("btn" + modulo);
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setForeground(Color.WHITE);
        btn.setBackground(Estilos.PRIMARIO);
        btn.setFont(Estilos.NORMAL.deriveFont(14f));
        btn.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 10));
        btn.setContentAreaFilled(false);
        btn.setOpaque(true);
        btn.setFocusPainted(false);
        btn.addActionListener(e -> mostrar(modulo));
        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(Estilos.PRIMARIO_CLARO);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                btn.setBackground(Estilos.PRIMARIO);
            }
        });
        return btn;
    }

    /**
     * Crea el panel de bienvenida.
     *
     * @return panel de inicio
     */
    private JPanel crearPanelInicio() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Estilos.FONDO);
        JPanel caja = new JPanel(new GridLayout(0, 1, 0, 8));
        caja.setOpaque(false);
        Usuario u = Sesion.getUsuarioActual();
        JLabel lblBienvenida = Estilos.titulo("Bienvenido" + (u == null ? "" : ", " + u.getNombres()));
        lblBienvenida.setHorizontalAlignment(SwingConstants.CENTER);
        caja.add(lblBienvenida);
        caja.add(new JLabel("Seleccione un módulo en el menú de la izquierda.", SwingConstants.CENTER));
        panel.add(caja);
        return panel;
    }

    /**
     * Muestra el módulo indicado, recargando antes sus datos.
     *
     * @param modulo identificador del módulo
     */
    public void mostrar(String modulo) {
        Recargable panel = recargables.get(modulo);
        if (panel != null) {
            panel.recargar();
        }
        tarjetas.show(contenido, modulo);
    }

    /**
     * Cierra la sesión y vuelve al login, previa confirmación.
     */
    private void cerrarSesion() {
        int op = JOptionPane.showConfirmDialog(this, "¿Desea cerrar la sesión?",
                "Cerrar sesión", JOptionPane.YES_NO_OPTION);
        if (op == JOptionPane.YES_OPTION) {
            new LoginController().cerrarSesion();
            new LoginFrame().setVisible(true);
            dispose();
        }
    }

    /**
     * Cierra la aplicación, previa confirmación.
     */
    private void salir() {
        int op = JOptionPane.showConfirmDialog(this, "¿Desea salir del sistema?",
                "Salir", JOptionPane.YES_NO_OPTION);
        if (op == JOptionPane.YES_OPTION) {
            System.exit(0);
        }
    }
}
