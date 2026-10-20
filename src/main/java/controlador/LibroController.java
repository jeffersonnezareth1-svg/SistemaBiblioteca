package controlador;

import dao.LibroDAO;
import java.sql.SQLException;
import java.time.Year;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import modelo.Libro;
import util.Respuesta;

/**
 * Controlador del módulo Libro.
 *
 * <p>Recibe los datos del formulario tal como los escribe el usuario (texto),
 * los valida mediante métodos pequeños (uno por campo), aplica las reglas de
 * negocio (ISBN único, no eliminar libros con préstamos) y delega el acceso a
 * datos a {@link LibroDAO}.</p>
 *
 * <p>Operaciones CRUD:</p>
 * <ul>
 * <li>C: {@link #registrarLibro}</li>
 * <li>R: {@link #listarLibros()} y {@link #buscarLibros(String)}</li>
 * <li>U: {@link #actualizarLibro}</li>
 * <li>D: {@link #eliminarLibro(int)}</li>
 * </ul>
 *
 * @author Guashpa Gualli Jefferson Nezareth
 * @version 1.0
 */
public class LibroController {

    /** Géneros permitidos para un libro. */
    public static final List<String> GENEROS = Arrays.asList(
            "Novela", "Cuento", "Poesía", "Ciencia", "Historia", "Tecnología", "Infantil", "Otro");

    /** Año mínimo de publicación aceptado (imprenta de Gutenberg). */
    public static final int ANIO_MINIMO = 1450;
    /** Número máximo de páginas aceptado. */
    public static final int PAGINAS_MAXIMO = 5000;
    /** Cantidad máxima de ejemplares en stock. */
    public static final int STOCK_MAXIMO = 500;

    /** Nombre del campo ISBN en el formulario. */
    public static final String CAMPO_ISBN = "isbn";
    /** Nombre del campo título en el formulario. */
    public static final String CAMPO_TITULO = "titulo";
    /** Nombre del campo editorial en el formulario. */
    public static final String CAMPO_EDITORIAL = "editorial";
    /** Nombre del campo año de publicación en el formulario. */
    public static final String CAMPO_ANIO = "anio";
    /** Nombre del campo género en el formulario. */
    public static final String CAMPO_GENERO = "genero";
    /** Nombre del campo número de páginas en el formulario. */
    public static final String CAMPO_PAGINAS = "paginas";
    /** Nombre del campo stock en el formulario. */
    public static final String CAMPO_STOCK = "stock";

    private static final Pattern SOLO_DIGITOS = Pattern.compile("\\d+");
    private static final Pattern EDITORIAL_VALIDA = Pattern.compile("[\\p{L}0-9 .,&'-]+");

    private final LibroDAO dao;

    /**
     * Crea el controlador con el DAO real.
     */
    public LibroController() {
        this(new LibroDAO());
    }

    /**
     * Crea el controlador con un DAO dado (útil en pruebas unitarias, donde
     * se puede pasar un DAO simulado que no use la base de datos).
     *
     * @param dao DAO de libros
     */
    public LibroController(LibroDAO dao) {
        this.dao = dao;
    }

    // ------------------------------------------------------------------
    // R: listar y buscar
    // ------------------------------------------------------------------

    /**
     * Lista todos los libros registrados.
     *
     * @return lista de libros
     * @throws SQLException si ocurre un error con la base de datos
     */
    public List<Libro> listarLibros() throws SQLException {
        return dao.listar();
    }

    /**
     * Busca libros por ISBN, título, editorial o género. Si el texto está
     * vacío devuelve todos los libros.
     *
     * @param texto texto a buscar
     * @return libros que coinciden
     * @throws SQLException si ocurre un error con la base de datos
     */
    public List<Libro> buscarLibros(String texto) throws SQLException {
        if (texto == null || texto.isBlank()) {
            return dao.listar();
        }
        return dao.buscar(texto.trim());
    }

    /**
     * Obtiene un libro por su identificador.
     *
     * @param idLibro identificador del libro
     * @return el libro o {@code null} si no existe
     * @throws SQLException si ocurre un error con la base de datos
     */
    public Libro obtenerLibro(int idLibro) throws SQLException {
        return dao.obtenerPorId(idLibro);
    }

    // ------------------------------------------------------------------
    // C: registrar
    // ------------------------------------------------------------------

    /**
     * Registra un libro nuevo a partir de los datos del formulario.
     *
     * @param isbn ISBN de 10 o 13 dígitos
     * @param titulo título del libro
     * @param editorial editorial
     * @param anio año de publicación
     * @param genero género (uno de {@link #GENEROS})
     * @param paginas número de páginas
     * @param stock ejemplares disponibles
     * @return respuesta exitosa o el error encontrado
     */
    public Respuesta registrarLibro(String isbn, String titulo, String editorial, String anio,
            String genero, String paginas, String stock) {
        Respuesta validacion = validarCampos(isbn, titulo, editorial, anio, genero, paginas, stock);
        if (!validacion.isExitosa()) {
            return validacion;
        }
        Libro libro = crearLibro(0, isbn, titulo, editorial, anio, genero, paginas, stock);
        try {
            if (dao.existeIsbn(libro.getIsbn(), 0)) {
                return Respuesta.error("Ya existe un libro registrado con ese ISBN", CAMPO_ISBN);
            }
            if (dao.insertar(libro)) {
                return Respuesta.ok("Libro registrado correctamente");
            }
            return Respuesta.error("No se pudo registrar el libro");
        } catch (SQLException e) {
            return Respuesta.error("Error de base de datos: " + e.getMessage());
        }
    }

    // ------------------------------------------------------------------
    // U: actualizar
    // ------------------------------------------------------------------

    /**
     * Actualiza un libro existente con los datos del formulario.
     *
     * @param idLibro identificador del libro a modificar
     * @param isbn ISBN de 10 o 13 dígitos
     * @param titulo título del libro
     * @param editorial editorial
     * @param anio año de publicación
     * @param genero género (uno de {@link #GENEROS})
     * @param paginas número de páginas
     * @param stock ejemplares disponibles
     * @return respuesta exitosa o el error encontrado
     */
    public Respuesta actualizarLibro(int idLibro, String isbn, String titulo, String editorial,
            String anio, String genero, String paginas, String stock) {
        if (idLibro <= 0) {
            return Respuesta.error("Debe seleccionar un libro para modificar");
        }
        Respuesta validacion = validarCampos(isbn, titulo, editorial, anio, genero, paginas, stock);
        if (!validacion.isExitosa()) {
            return validacion;
        }
        Libro libro = crearLibro(idLibro, isbn, titulo, editorial, anio, genero, paginas, stock);
        try {
            if (dao.existeIsbn(libro.getIsbn(), idLibro)) {
                return Respuesta.error("Ya existe otro libro registrado con ese ISBN", CAMPO_ISBN);
            }
            if (dao.actualizar(libro)) {
                return Respuesta.ok("Libro actualizado correctamente");
            }
            return Respuesta.error("El libro ya no existe en la base de datos");
        } catch (SQLException e) {
            return Respuesta.error("Error de base de datos: " + e.getMessage());
        }
    }

    // ------------------------------------------------------------------
    // D: eliminar
    // ------------------------------------------------------------------

    /**
     * Elimina un libro si existe y no tiene préstamos registrados.
     *
     * @param idLibro identificador del libro
     * @return respuesta exitosa o el motivo por el que no se eliminó
     */
    public Respuesta eliminarLibro(int idLibro) {
        if (idLibro <= 0) {
            return Respuesta.error("Debe seleccionar un libro para eliminar");
        }
        try {
            if (dao.obtenerPorId(idLibro) == null) {
                return Respuesta.error("El libro no existe");
            }
            if (dao.tienePrestamos(idLibro)) {
                return Respuesta.error("No se puede eliminar: el libro tiene préstamos registrados");
            }
            if (dao.eliminar(idLibro)) {
                return Respuesta.ok("Libro eliminado correctamente");
            }
            return Respuesta.error("No se pudo eliminar el libro");
        } catch (SQLException e) {
            return Respuesta.error("Error de base de datos: " + e.getMessage());
        }
    }

    // ------------------------------------------------------------------
    // Validaciones (un método pequeño por campo)
    // ------------------------------------------------------------------

    /**
     * Valida todos los campos del formulario en orden y devuelve el primer
     * error encontrado.
     *
     * @param isbn ISBN
     * @param titulo título
     * @param editorial editorial
     * @param anio año de publicación
     * @param genero género
     * @param paginas número de páginas
     * @param stock ejemplares disponibles
     * @return respuesta exitosa si todo es válido, o el error con su campo
     */
    public Respuesta validarCampos(String isbn, String titulo, String editorial, String anio,
            String genero, String paginas, String stock) {
        String error = validarIsbn(isbn);
        if (error != null) {
            return Respuesta.error(error, CAMPO_ISBN);
        }
        error = validarTitulo(titulo);
        if (error != null) {
            return Respuesta.error(error, CAMPO_TITULO);
        }
        error = validarEditorial(editorial);
        if (error != null) {
            return Respuesta.error(error, CAMPO_EDITORIAL);
        }
        error = validarAnioPublicacion(anio);
        if (error != null) {
            return Respuesta.error(error, CAMPO_ANIO);
        }
        error = validarGenero(genero);
        if (error != null) {
            return Respuesta.error(error, CAMPO_GENERO);
        }
        error = validarNumeroPaginas(paginas);
        if (error != null) {
            return Respuesta.error(error, CAMPO_PAGINAS);
        }
        error = validarStock(stock);
        if (error != null) {
            return Respuesta.error(error, CAMPO_STOCK);
        }
        return Respuesta.ok("Datos válidos");
    }

    /**
     * Valida el ISBN: obligatorio, solo dígitos y con 10 o 13 dígitos.
     *
     * @param isbn ISBN escrito
     * @return mensaje de error o {@code null} si es válido
     */
    public String validarIsbn(String isbn) {
        if (isbn == null || isbn.isBlank()) {
            return "El ISBN es obligatorio";
        }
        String valor = isbn.trim();
        if (!SOLO_DIGITOS.matcher(valor).matches()) {
            return "El ISBN solo debe contener números (sin guiones ni espacios)";
        }
        if (valor.length() != 10 && valor.length() != 13) {
            return "El ISBN debe tener 10 o 13 dígitos";
        }
        return null;
    }

    /**
     * Valida el título: obligatorio, entre 2 y 150 caracteres.
     *
     * @param titulo título escrito
     * @return mensaje de error o {@code null} si es válido
     */
    public String validarTitulo(String titulo) {
        if (titulo == null || titulo.isBlank()) {
            return "El título es obligatorio";
        }
        int largo = titulo.trim().length();
        if (largo < 2) {
            return "El título debe tener al menos 2 caracteres";
        }
        if (largo > 150) {
            return "El título no puede superar 150 caracteres";
        }
        return null;
    }

    /**
     * Valida la editorial: obligatoria, entre 2 y 100 caracteres, solo
     * letras, números, espacios y los signos . , &amp; ' -
     *
     * @param editorial editorial escrita
     * @return mensaje de error o {@code null} si es válida
     */
    public String validarEditorial(String editorial) {
        if (editorial == null || editorial.isBlank()) {
            return "La editorial es obligatoria";
        }
        String valor = editorial.trim();
        if (!EDITORIAL_VALIDA.matcher(valor).matches()) {
            return "La editorial contiene caracteres no permitidos";
        }
        if (valor.length() < 2 || valor.length() > 100) {
            return "La editorial debe tener entre 2 y 100 caracteres";
        }
        return null;
    }

    /**
     * Valida el año de publicación: obligatorio, numérico, entre
     * {@link #ANIO_MINIMO} y el año actual.
     *
     * @param anio año escrito
     * @return mensaje de error o {@code null} si es válido
     */
    public String validarAnioPublicacion(String anio) {
        if (anio == null || anio.isBlank()) {
            return "El año de publicación es obligatorio";
        }
        String valor = anio.trim();
        if (!SOLO_DIGITOS.matcher(valor).matches() || valor.length() > 4) {
            return "El año de publicación debe ser un número de hasta 4 dígitos";
        }
        int numero = Integer.parseInt(valor);
        if (numero < ANIO_MINIMO) {
            return "El año de publicación no puede ser menor a " + ANIO_MINIMO;
        }
        if (numero > Year.now().getValue()) {
            return "El año de publicación no puede ser mayor al año actual";
        }
        return null;
    }

    /**
     * Valida el género: obligatorio y debe ser uno de {@link #GENEROS}.
     *
     * @param genero género seleccionado
     * @return mensaje de error o {@code null} si es válido
     */
    public String validarGenero(String genero) {
        if (genero == null || genero.isBlank()) {
            return "Debe seleccionar un género";
        }
        if (!GENEROS.contains(genero.trim())) {
            return "El género seleccionado no es válido";
        }
        return null;
    }

    /**
     * Valida el número de páginas: obligatorio, entero entre 1 y
     * {@link #PAGINAS_MAXIMO}.
     *
     * @param paginas número escrito
     * @return mensaje de error o {@code null} si es válido
     */
    public String validarNumeroPaginas(String paginas) {
        if (paginas == null || paginas.isBlank()) {
            return "El número de páginas es obligatorio";
        }
        String valor = paginas.trim();
        if (!SOLO_DIGITOS.matcher(valor).matches() || valor.length() > 4) {
            return "El número de páginas debe ser un número entero";
        }
        int numero = Integer.parseInt(valor);
        if (numero < 1 || numero > PAGINAS_MAXIMO) {
            return "El número de páginas debe estar entre 1 y " + PAGINAS_MAXIMO;
        }
        return null;
    }

    /**
     * Valida el stock: obligatorio, entero entre 0 y {@link #STOCK_MAXIMO}.
     *
     * @param stock cantidad escrita
     * @return mensaje de error o {@code null} si es válido
     */
    public String validarStock(String stock) {
        if (stock == null || stock.isBlank()) {
            return "El stock es obligatorio";
        }
        String valor = stock.trim();
        if (!SOLO_DIGITOS.matcher(valor).matches() || valor.length() > 3) {
            return "El stock debe ser un número entero no negativo";
        }
        if (Integer.parseInt(valor) > STOCK_MAXIMO) {
            return "El stock no puede superar " + STOCK_MAXIMO + " ejemplares";
        }
        return null;
    }

    /**
     * Construye un {@link Libro} con los datos ya validados del formulario.
     *
     * @param idLibro identificador (0 para un libro nuevo)
     * @param isbn ISBN
     * @param titulo título
     * @param editorial editorial
     * @param anio año de publicación
     * @param genero género
     * @param paginas número de páginas
     * @param stock ejemplares disponibles
     * @return libro listo para guardar
     */
    private Libro crearLibro(int idLibro, String isbn, String titulo, String editorial,
            String anio, String genero, String paginas, String stock) {
        return new Libro(idLibro, isbn.trim(), titulo.trim(), editorial.trim(),
                Integer.parseInt(anio.trim()), genero.trim(),
                Integer.parseInt(paginas.trim()), Integer.parseInt(stock.trim()));
    }
}
