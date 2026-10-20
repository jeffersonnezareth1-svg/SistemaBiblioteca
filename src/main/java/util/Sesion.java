package util;

import modelo.Usuario;

/**
 * Guarda el usuario que inició sesión mientras el sistema está abierto.
 *
 * @author Guashpa Gualli Jefferson Nezareth
 * @version 1.0
 */
public final class Sesion {

    private static Usuario usuarioActual;

    /**
     * Constructor privado: la clase solo expone métodos estáticos.
     */
    private Sesion() {
    }

    /**
     * Registra el usuario autenticado.
     *
     * @param usuario usuario que inició sesión
     */
    public static void iniciar(Usuario usuario) {
        usuarioActual = usuario;
    }

    /**
     * Devuelve el usuario autenticado.
     *
     * @return usuario actual o {@code null} si no hay sesión
     */
    public static Usuario getUsuarioActual() {
        return usuarioActual;
    }

    /**
     * Indica si hay una sesión activa.
     *
     * @return {@code true} si hay un usuario autenticado
     */
    public static boolean haySesion() {
        return usuarioActual != null;
    }

    /**
     * Cierra la sesión actual.
     */
    public static void cerrar() {
        usuarioActual = null;
    }
}
