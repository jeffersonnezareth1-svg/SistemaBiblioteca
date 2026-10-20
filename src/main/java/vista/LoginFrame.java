package vista;

import controlador.LoginController;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import util.Respuesta;

/**
 * Ventana de inicio de sesión.
 *
 * <p>Solicita usuario y contraseña, los envía a {@link LoginController} y, si
 * son correctos, abre el {@link MenuPrincipalFrame}. Después de
 * {@value #MAX_INTENTOS} intentos fallidos la aplicación se cierra.</p>
 *
 * @author Guashpa Gualli Jefferson Nezareth
 * @version 1.0
 */
public class LoginFrame extends JFrame {

    /** Número máximo de intentos fallidos permitidos. */
    public static final int MAX_INTENTOS = 3;

    /** Controlador que valida y ejecuta las operaciones. */
    private final LoginController controller = new LoginController();
    /** Campo de texto del usuario. */
    private final JTextField txtUsuario = new JTextField(18);
    /** Campo de la contraseña. */
    private final JPasswordField txtContrasena = new JPasswordField(18);
    /** Botón ingresar. */
    private final JButton btnIngresar = Estilos.boton("Ingresar", "Iniciar sesión");
    /** Botón salir. */
    private final JButton btnSalir = Estilos.boton("Salir", "Cerrar la aplicación");
    /** Número de intentos fallidos de inicio de sesión. */
    private int intentosFallidos;

    /**
     * Construye la ventana de inicio de sesión.
     */
    public LoginFrame() {
        super("Sistema de Biblioteca - Iniciar sesión");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        initComponents();
        pack();
        setLocationRelativeTo(null);
    }

    /**
     * Crea y organiza los componentes de la ventana.
     */
    private void initComponents() {
        JLabel lblEncabezado = new JLabel("Biblioteca", SwingConstants.CENTER);
        lblEncabezado.setFont(Estilos.TITULO.deriveFont(26f));
        lblEncabezado.setForeground(Color.WHITE);
        JLabel lblSub = new JLabel("Sistema de Gestión de Biblioteca", SwingConstants.CENTER);
        lblSub.setForeground(new Color(210, 220, 235));

        JPanel encabezado = new JPanel(new BorderLayout());
        encabezado.setBackground(Estilos.PRIMARIO);
        encabezado.setBorder(BorderFactory.createEmptyBorder(20, 20, 16, 20));
        encabezado.add(lblEncabezado, BorderLayout.CENTER);
        encabezado.add(lblSub, BorderLayout.SOUTH);

        txtUsuario.setName(LoginController.CAMPO_USUARIO);
        txtContrasena.setName(LoginController.CAMPO_CONTRASENA);

        JPanel formulario = new JPanel(new GridBagLayout());
        formulario.setBorder(BorderFactory.createEmptyBorder(20, 30, 10, 30));
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(6, 6, 6, 6);
        g.anchor = GridBagConstraints.WEST;
        g.gridx = 0;
        g.gridy = 0;
        formulario.add(new JLabel("Usuario:"), g);
        g.gridx = 1;
        formulario.add(txtUsuario, g);
        g.gridx = 0;
        g.gridy = 1;
        formulario.add(new JLabel("Contraseña:"), g);
        g.gridx = 1;
        formulario.add(txtContrasena, g);

        JPanel botones = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        botones.add(btnIngresar);
        botones.add(btnSalir);

        getContentPane().setLayout(new BorderLayout());
        add(encabezado, BorderLayout.NORTH);
        add(formulario, BorderLayout.CENTER);
        add(botones, BorderLayout.SOUTH);

        getRootPane().setDefaultButton(btnIngresar);
        btnIngresar.addActionListener(e -> ingresar());
        btnSalir.addActionListener(e -> System.exit(0));
    }

    /**
     * Valida las credenciales y abre el menú principal si son correctas.
     */
    private void ingresar() {
        Respuesta r = controller.iniciarSesion(txtUsuario.getText(),
                new String(txtContrasena.getPassword()));
        if (r.isExitosa()) {
            new MenuPrincipalFrame().setVisible(true);
            dispose();
            return;
        }
        JOptionPane.showMessageDialog(this, r.getMensaje(), "Inicio de sesión",
                JOptionPane.WARNING_MESSAGE);
        if ("Usuario o contraseña incorrectos".equals(r.getMensaje())) {
            registrarIntentoFallido();
        }
        enfocarCampo(r.getCampo());
    }

    /**
     * Suma un intento fallido y cierra la aplicación al llegar al máximo.
     */
    private void registrarIntentoFallido() {
        intentosFallidos++;
        if (intentosFallidos >= MAX_INTENTOS) {
            JOptionPane.showMessageDialog(this,
                    "Superó el número máximo de intentos. El sistema se cerrará.",
                    "Acceso bloqueado", JOptionPane.ERROR_MESSAGE);
            System.exit(0);
        }
        txtContrasena.setText("");
    }

    /**
     * Coloca el cursor en el campo que produjo el error.
     *
     * @param campo nombre del campo, o {@code null}
     */
    private void enfocarCampo(String campo) {
        if (LoginController.CAMPO_USUARIO.equals(campo)) {
            txtUsuario.requestFocusInWindow();
        } else {
            txtContrasena.requestFocusInWindow();
        }
    }
}
