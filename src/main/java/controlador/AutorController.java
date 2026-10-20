package controlador;

import dao.AutorDAO;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.regex.Pattern;
import modelo.Autor;
import util.Fechas;
import util.Respuesta;

/**
 * Controlador del módulo Autor.
 *
 * <p>Recibe los datos del formulario como texto, los valida con un método
 * pequeño por campo, aplica las reglas de negocio (no repetir el mismo autor)
 * y delega el acceso a datos a {@link AutorDAO}.</p>
 *
 * <ul>
 * <li>C: {@link #registrarAutor}</li>
 * <li>R: {@link #listarAutores()} y {@link #buscarAutores(String)}</li>
 * <li>U: {@link #actualizarAutor}</li>
 * <li>D: {@link #eliminarAutor(int)}</li>
 * </ul>
 *
 * @author Guashpa Gualli Jefferson Nezareth
 * @version 1.0
 */
public class AutorController {

    /** Nombre del campo nombres en el formulario. */
    public static final String CAMPO_NOMBRES = "nombres";
    /** Nombre del campo apellidos en el formulario. */
    public static final String CAMPO_APELLIDOS = "apellidos";
    /** Nombre del campo nacionalidad en el formulario. */
    public static final String CAMPO_NACIONALIDAD = "nacionalidad";
    /** Nombre del campo fecha de nacimiento en el formulario. */
    public static final String CAMPO_FECHA = "fechaNacimiento";
    /** Nombre del campo biografía en el formulario. */
    public static final String CAMPO_BIOGRAFIA = "biografia";

    /** Fecha de nacimiento más antigua aceptada. */
    public static final LocalDate FECHA_MINIMA = LocalDate.of(1000, 1, 1);

    private static final Pattern NOMBRE_VALIDO = Pattern.compile("[\\p{L} .'-]+");
    private static final Pattern SOLO_LETRAS = Pattern.compile("[\\p{L} ]+");

    private final AutorDAO dao;

    /**
     * Crea el controlador con el DAO real.
     */
    public AutorController() {
        this(new AutorDAO());
    }

    /**
     * Crea el controlador con un DAO dado (útil en pruebas unitarias).
     *
     * @param dao DAO de autores
     */
    public AutorController(AutorDAO dao) {
        this.dao = dao;
    }

    /**
     * Lista todos los autores.
     *
     * @return lista de autores
     * @throws SQLException si ocurre un error con la base de datos
     */
    public List<Autor> listarAutores() throws SQLException {
        return dao.listar();
    }

    /**
     * Busca autores por nombre, apellido o nacionalidad. Si el texto está
     * vacío devuelve todos.
     *
     * @param texto texto a buscar
     * @return autores que coinciden
     * @throws SQLException si ocurre un error con la base de datos
     */
    public List<Autor> buscarAutores(String texto) throws SQLException {
        if (texto == null || texto.isBlank()) {
            return dao.listar();
        }
        return dao.buscar(texto.trim());
    }

    /**
     * Registra un autor nuevo.
     *
     * @param nombres nombres
     * @param apellidos apellidos
     * @param nacionalidad nacionalidad
     * @param fechaNacimiento fecha {@code dd/MM/yyyy} (opcional)
     * @param biografia biografía (opcional)
     * @return respuesta exitosa o el error encontrado
     */
    public Respuesta registrarAutor(String nombres, String apellidos, String nacionalidad,
            String fechaNacimiento, String biografia) {
        Respuesta validacion = validarCampos(nombres, apellidos, nacionalidad, fechaNacimiento, biografia);
        if (!validacion.isExitosa()) {
            return validacion;
        }
        Autor autor = crearAutor(0, nombres, apellidos, nacionalidad, fechaNacimiento, biografia);
        try {
            if (dao.existeNombre(autor.getNombres(), autor.getApellidos(), 0)) {
                return Respuesta.error("Ya existe un autor registrado con ese nombre", CAMPO_NOMBRES);
            }
            if (dao.insertar(autor)) {
                return Respuesta.ok("Autor registrado correctamente");
            }
            return Respuesta.error("No se pudo registrar el autor");
        } catch (SQLException e) {
            return Respuesta.error("Error de base de datos: " + e.getMessage());
        }
    }

    /**
     * Actualiza un autor existente.
     *
     * @param idAutor identificador del autor
     * @param nombres nombres
     * @param apellidos apellidos
     * @param nacionalidad nacionalidad
     * @param fechaNacimiento fecha {@code dd/MM/yyyy} (opcional)
     * @param biografia biografía (opcional)
     * @return respuesta exitosa o el error encontrado
     */
    public Respuesta actualizarAutor(int idAutor, String nombres, String apellidos, String nacionalidad,
            String fechaNacimiento, String biografia) {
        if (idAutor <= 0) {
            return Respuesta.error("Debe seleccionar un autor para modificar");
        }
        Respuesta validacion = validarCampos(nombres, apellidos, nacionalidad, fechaNacimiento, biografia);
        if (!validacion.isExitosa()) {
            return validacion;
        }
        Autor autor = crearAutor(idAutor, nombres, apellidos, nacionalidad, fechaNacimiento, biografia);
        try {
            if (dao.existeNombre(autor.getNombres(), autor.getApellidos(), idAutor)) {
                return Respuesta.error("Ya existe otro autor registrado con ese nombre", CAMPO_NOMBRES);
            }
            if (dao.actualizar(autor)) {
                return Respuesta.ok("Autor actualizado correctamente");
            }
            return Respuesta.error("El autor ya no existe en la base de datos");
        } catch (SQLException e) {
            return Respuesta.error("Error de base de datos: " + e.getMessage());
        }
    }

    /**
     * Elimina un autor si existe.
     *
     * @param idAutor identificador del autor
     * @return respuesta exitosa o el motivo por el que no se eliminó
     */
    public Respuesta eliminarAutor(int idAutor) {
        if (idAutor <= 0) {
            return Respuesta.error("Debe seleccionar un autor para eliminar");
        }
        try {
            if (dao.obtenerPorId(idAutor) == null) {
                return Respuesta.error("El autor no existe");
            }
            if (dao.eliminar(idAutor)) {
                return Respuesta.ok("Autor eliminado correctamente");
            }
            return Respuesta.error("No se pudo eliminar el autor");
        } catch (SQLException e) {
            return Respuesta.error("Error de base de datos: " + e.getMessage());
        }
    }

    /**
     * Valida todos los campos en orden y devuelve el primer error.
     *
     * @param nombres nombres
     * @param apellidos apellidos
     * @param nacionalidad nacionalidad
     * @param fechaNacimiento fecha de nacimiento
     * @param biografia biografía
     * @return respuesta exitosa si todo es válido, o el error con su campo
     */
    public Respuesta validarCampos(String nombres, String apellidos, String nacionalidad,
            String fechaNacimiento, String biografia) {
        String error = validarNombres(nombres);
        if (error != null) {
            return Respuesta.error(error, CAMPO_NOMBRES);
        }
        error = validarApellidos(apellidos);
        if (error != null) {
            return Respuesta.error(error, CAMPO_APELLIDOS);
        }
        error = validarNacionalidad(nacionalidad);
        if (error != null) {
            return Respuesta.error(error, CAMPO_NACIONALIDAD);
        }
        error = validarFechaNacimiento(fechaNacimiento);
        if (error != null) {
            return Respuesta.error(error, CAMPO_FECHA);
        }
        error = validarBiografia(biografia);
        if (error != null) {
            return Respuesta.error(error, CAMPO_BIOGRAFIA);
        }
        return Respuesta.ok("Datos válidos");
    }

    /**
     * Valida los nombres: obligatorios, solo letras (y . ' -), entre 2 y 60
     * caracteres.
     *
     * @param nombres nombres escritos
     * @return mensaje de error o {@code null} si son válidos
     */
    public String validarNombres(String nombres) {
        if (nombres == null || nombres.isBlank()) {
            return "Los nombres son obligatorios";
        }
        String valor = nombres.trim();
        if (!NOMBRE_VALIDO.matcher(valor).matches()) {
            return "Los nombres solo deben contener letras";
        }
        if (valor.length() < 2 || valor.length() > 60) {
            return "Los nombres deben tener entre 2 y 60 caracteres";
        }
        return null;
    }

    /**
     * Valida los apellidos: obligatorios, solo letras (y . ' -), entre 2 y 60
     * caracteres.
     *
     * @param apellidos apellidos escritos
     * @return mensaje de error o {@code null} si son válidos
     */
    public String validarApellidos(String apellidos) {
        if (apellidos == null || apellidos.isBlank()) {
            return "Los apellidos son obligatorios";
        }
        String valor = apellidos.trim();
        if (!NOMBRE_VALIDO.matcher(valor).matches()) {
            return "Los apellidos solo deben contener letras";
        }
        if (valor.length() < 2 || valor.length() > 60) {
            return "Los apellidos deben tener entre 2 y 60 caracteres";
        }
        return null;
    }

    /**
     * Valida la nacionalidad: obligatoria, solo letras y espacios, entre 3 y
     * 50 caracteres.
     *
     * @param nacionalidad nacionalidad escrita
     * @return mensaje de error o {@code null} si es válida
     */
    public String validarNacionalidad(String nacionalidad) {
        if (nacionalidad == null || nacionalidad.isBlank()) {
            return "La nacionalidad es obligatoria";
        }
        String valor = nacionalidad.trim();
        if (!SOLO_LETRAS.matcher(valor).matches()) {
            return "La nacionalidad solo debe contener letras";
        }
        if (valor.length() < 3 || valor.length() > 50) {
            return "La nacionalidad debe tener entre 3 y 50 caracteres";
        }
        return null;
    }

    /**
     * Valida la fecha de nacimiento: es opcional, pero si se escribe debe
     * tener formato {@code dd/MM/yyyy}, existir, no ser futura ni anterior a
     * {@link #FECHA_MINIMA}.
     *
     * @param fecha fecha escrita
     * @return mensaje de error o {@code null} si es válida o está vacía
     */
    public String validarFechaNacimiento(String fecha) {
        if (fecha == null || fecha.isBlank()) {
            return null;
        }
        LocalDate valor = Fechas.convertir(fecha);
        if (valor == null) {
            return "La fecha de nacimiento debe tener el formato " + Fechas.PATRON + " y ser una fecha real";
        }
        if (valor.isAfter(LocalDate.now())) {
            return "La fecha de nacimiento no puede ser futura";
        }
        if (valor.isBefore(FECHA_MINIMA)) {
            return "La fecha de nacimiento no puede ser anterior al año 1000";
        }
        return null;
    }

    /**
     * Valida la biografía: opcional, máximo 255 caracteres.
     *
     * @param biografia biografía escrita
     * @return mensaje de error o {@code null} si es válida
     */
    public String validarBiografia(String biografia) {
        if (biografia != null && biografia.trim().length() > 255) {
            return "La biografía no puede superar 255 caracteres";
        }
        return null;
    }

    /**
     * Construye un {@link Autor} con los datos ya validados.
     *
     * @param idAutor identificador (0 para uno nuevo)
     * @param nombres nombres
     * @param apellidos apellidos
     * @param nacionalidad nacionalidad
     * @param fechaNacimiento fecha de nacimiento (puede estar vacía)
     * @param biografia biografía (puede estar vacía)
     * @return autor listo para guardar
     */
    private Autor crearAutor(int idAutor, String nombres, String apellidos, String nacionalidad,
            String fechaNacimiento, String biografia) {
        String bio = biografia == null || biografia.isBlank() ? null : biografia.trim();
        return new Autor(idAutor, nombres.trim(), apellidos.trim(), nacionalidad.trim(),
                Fechas.convertir(fechaNacimiento), bio);
    }
}
