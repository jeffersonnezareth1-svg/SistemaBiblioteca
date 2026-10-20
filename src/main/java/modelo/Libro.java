package modelo;

/**
 * Libro registrado en la biblioteca (tabla {@code libro}).
 *
 * <p>Es una entidad independiente: no depende de otras tablas. Los módulos de
 * Préstamo y Devolución sí dependen de ella.</p>
 *
 * @author Guashpa Gualli Jefferson Nezareth
 * @version 1.0
 */
public class Libro {

    /** Identificador único (clave primaria). */
    private int idLibro;

    /** Código ISBN de 10 o 13 dígitos. */
    private String isbn;

    /** Título del libro. */
    private String titulo;

    /** Editorial que publicó el libro. */
    private String editorial;

    /** Año de publicación. */
    private int anioPublicacion;

    /** Género literario. */
    private String genero;

    /** Número de páginas. */
    private int numeroPaginas;

    /** Ejemplares disponibles. */
    private int stock;

    /**
     * Crea un libro vacío.
     */
    public Libro() {
    }

    /**
     * Crea un libro con todos sus datos.
     *
     * @param idLibro identificador (0 si aún no se registra)
     * @param isbn código ISBN de 10 o 13 dígitos
     * @param titulo título del libro
     * @param editorial editorial que lo publicó
     * @param anioPublicacion año de publicación
     * @param genero género literario
     * @param numeroPaginas número de páginas
     * @param stock ejemplares disponibles
     */
    public Libro(int idLibro, String isbn, String titulo, String editorial,
            int anioPublicacion, String genero, int numeroPaginas, int stock) {
        this.idLibro = idLibro;
        this.isbn = isbn;
        this.titulo = titulo;
        this.editorial = editorial;
        this.anioPublicacion = anioPublicacion;
        this.genero = genero;
        this.numeroPaginas = numeroPaginas;
        this.stock = stock;
    }

    /**
     * Devuelve el dato: identificador del libro.
     *
     * @return identificador del libro
     */
    public int getIdLibro() {
        return idLibro;
    }

    /**
     * Asigna el dato: identificador del libro.
     *
     * @param idLibro identificador del libro
     */
    public void setIdLibro(int idLibro) {
        this.idLibro = idLibro;
    }

    /**
     * Devuelve el dato: código ISBN.
     *
     * @return código ISBN
     */
    public String getIsbn() {
        return isbn;
    }

    /**
     * Asigna el dato: código ISBN.
     *
     * @param isbn código ISBN
     */
    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    /**
     * Devuelve el dato: título del libro.
     *
     * @return título del libro
     */
    public String getTitulo() {
        return titulo;
    }

    /**
     * Asigna el dato: título del libro.
     *
     * @param titulo título del libro
     */
    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    /**
     * Devuelve el dato: editorial.
     *
     * @return editorial
     */
    public String getEditorial() {
        return editorial;
    }

    /**
     * Asigna el dato: editorial.
     *
     * @param editorial editorial
     */
    public void setEditorial(String editorial) {
        this.editorial = editorial;
    }

    /**
     * Devuelve el dato: año de publicación.
     *
     * @return año de publicación
     */
    public int getAnioPublicacion() {
        return anioPublicacion;
    }

    /**
     * Asigna el dato: año de publicación.
     *
     * @param anioPublicacion año de publicación
     */
    public void setAnioPublicacion(int anioPublicacion) {
        this.anioPublicacion = anioPublicacion;
    }

    /**
     * Devuelve el dato: género literario.
     *
     * @return género literario
     */
    public String getGenero() {
        return genero;
    }

    /**
     * Asigna el dato: género literario.
     *
     * @param genero género literario
     */
    public void setGenero(String genero) {
        this.genero = genero;
    }

    /**
     * Devuelve el dato: número de páginas.
     *
     * @return número de páginas
     */
    public int getNumeroPaginas() {
        return numeroPaginas;
    }

    /**
     * Asigna el dato: número de páginas.
     *
     * @param numeroPaginas número de páginas
     */
    public void setNumeroPaginas(int numeroPaginas) {
        this.numeroPaginas = numeroPaginas;
    }

    /**
     * Devuelve el dato: ejemplares disponibles.
     *
     * @return ejemplares disponibles
     */
    public int getStock() {
        return stock;
    }

    /**
     * Asigna el dato: ejemplares disponibles.
     *
     * @param stock ejemplares disponibles
     */
    public void setStock(int stock) {
        this.stock = stock;
    }

    /**
     * Texto usado en listas y combos (por ejemplo, en el módulo Préstamo).
     *
     * @return ISBN y título del libro
     */
    @Override
    public String toString() {
        return isbn + " - " + titulo;
    }
}
