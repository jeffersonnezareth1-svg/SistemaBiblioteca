package util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;

/**
 * Conversión de fechas entre el formato que escribe el usuario
 * ({@code dd/MM/yyyy}) y {@link LocalDate}.
 *
 * <p>La conversión es estricta: fechas inexistentes como {@code 31/02/2026}
 * se rechazan.</p>
 *
 * @author Guashpa Gualli Jefferson Nezareth
 * @version 1.0
 */
public final class Fechas {

    /** Formato de fecha usado en todos los formularios. */
    public static final String PATRON = "dd/MM/yyyy";

    private static final DateTimeFormatter FORMATO =
            DateTimeFormatter.ofPattern("dd/MM/uuuu").withResolverStyle(ResolverStyle.STRICT);

    /**
     * Constructor privado: la clase solo expone métodos estáticos.
     */
    private Fechas() {
    }

    /**
     * Convierte un texto {@code dd/MM/yyyy} en fecha.
     *
     * @param texto fecha escrita por el usuario
     * @return la fecha, o {@code null} si el texto está vacío o no es una
     * fecha válida
     */
    public static LocalDate convertir(String texto) {
        if (texto == null || texto.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(texto.trim(), FORMATO);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    /**
     * Convierte una fecha en texto {@code dd/MM/yyyy}.
     *
     * @param fecha fecha a mostrar
     * @return texto con la fecha, o cadena vacía si la fecha es {@code null}
     */
    public static String formatear(LocalDate fecha) {
        return fecha == null ? "" : fecha.format(FORMATO);
    }

    /**
     * Devuelve la fecha actual en formato {@code dd/MM/yyyy}.
     *
     * @return fecha de hoy como texto
     */
    public static String hoy() {
        return formatear(LocalDate.now());
    }
}
