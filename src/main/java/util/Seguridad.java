package util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Utilidades de seguridad para el inicio de sesión.
 *
 * <p>Las contraseñas de la tabla {@code usuario} se guardan como hash SHA-256
 * (generado en MySQL con {@code SHA2('clave', 256)}), por lo que la contraseña
 * escrita en el login se convierte con {@link #sha256(String)} antes de
 * compararla.</p>
 *
 * @author Guashpa Gualli Jefferson Nezareth
 * @version 1.0
 */
public final class Seguridad {

    /**
     * Constructor privado: la clase solo expone métodos estáticos.
     */
    private Seguridad() {
    }

    /**
     * Calcula el hash SHA-256 de un texto.
     *
     * @param texto texto original
     * @return hash en hexadecimal de 64 caracteres en minúsculas
     */
    public static String sha256(String texto) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(texto.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 no está disponible", e);
        }
    }
}
