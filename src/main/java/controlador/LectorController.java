package controlador;

import dao.LectorDAO;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import modelo.Lector;
import util.Respuesta;

/**
 * Controlador del módulo Lector.
 *
 * <p>Valida los datos del formulario con un método pequeño por campo
 * (incluido el dígito verificador de la cédula ecuatoriana), controla que la
 * cédula y el correo no se repitan y que no se elimine un lector con
 * préstamos.</p>
 *
 * <ul>
 * <li>C: {@link #registrarLector}</li>
 * <li>R: {@link #listarLectores()} y {@link #buscarLectores(String)}</li>
 * <li>U: {@link #actualizarLector}</li>
 * <li>D: {@link #eliminarLector(int)}</li>
 * </ul>
 *
 * @author Guashpa Gualli Jefferson Nezareth
 * @version 1.0
 */
public class LectorController {

    /** Estados permitidos para un lector. */
    public static final List<String> ESTADOS = Arrays.asList(Lector.ACTIVO, Lector.SUSPENDIDO);

    /** Nombre del campo cédula en el formulario. */
    public static final String CAMPO_CEDULA = "cedula";
    /** Nombre del campo nombres en el formulario. */
    public static final String CAMPO_NOMBRES = "nombres";
    /** Nombre del campo apellidos en el formulario. */
    public static final String CAMPO_APELLIDOS = "apellidos";
    /** Nombre del campo correo en el formulario. */
    public static final String CAMPO_CORREO = "correo";
    /** Nombre del campo teléfono en el formulario. */
    public static final String CAMPO_TELEFONO = "telefono";
    /** Nombre del campo dirección en el formulario. */
    public static final String CAMPO_DIRECCION = "direccion";
    /** Nombre del campo estado en el formulario. */
    public static final String CAMPO_ESTADO = "estado";

    private static final Pattern DIEZ_DIGITOS = Pattern.compile("\\d{10}");
    private static final Pattern SOLO_LETRAS = Pattern.compile("[\\p{L} ]+");
    private static final Pattern CORREO = Pattern.compile("[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}");
    private static final Pattern CELULAR = Pattern.compile("09\\d{8}");

    private final LectorDAO dao;

    /**
     * Crea el controlador con el DAO real.
     */
    public LectorController() {
        this(new LectorDAO());
    }

    /**
     * Crea el controlador con un DAO dado (útil en pruebas unitarias).
     *
     * @param dao DAO de lectores
     */
    public LectorController(LectorDAO dao) {
        this.dao = dao;
    }

    /**
     * Lista todos los lectores.
     *
     * @return lista de lectores
     * @throws SQLException si ocurre un error con la base de datos
     */
    public List<Lector> listarLectores() throws SQLException {
        return dao.listar();
    }

    /**
     * Busca lectores por cédula, nombres, apellidos o correo. Si el texto
     * está vacío devuelve todos.
     *
     * @param texto texto a buscar
     * @return lectores que coinciden
     * @throws SQLException si ocurre un error con la base de datos
     */
    public List<Lector> buscarLectores(String texto) throws SQLException {
        if (texto == null || texto.isBlank()) {
            return dao.listar();
        }
        return dao.buscar(texto.trim());
    }

    /**
     * Registra un lector nuevo.
     *
     * @param cedula cédula ecuatoriana
     * @param nombres nombres
     * @param apellidos apellidos
     * @param correo correo electrónico
     * @param telefono celular (09xxxxxxxx)
     * @param direccion dirección (opcional)
     * @param estado ACTIVO o SUSPENDIDO
     * @return respuesta exitosa o el error encontrado
     */
    public Respuesta registrarLector(String cedula, String nombres, String apellidos, String correo,
            String telefono, String direccion, String estado) {
        Respuesta validacion = validarCampos(cedula, nombres, apellidos, correo, telefono, direccion, estado);
        if (!validacion.isExitosa()) {
            return validacion;
        }
        Lector lector = crearLector(0, cedula, nombres, apellidos, correo, telefono, direccion, estado);
        try {
            Respuesta duplicado = verificarDuplicados(lector);
            if (duplicado != null) {
                return duplicado;
            }
            if (dao.insertar(lector)) {
                return Respuesta.ok("Lector registrado correctamente");
            }
            return Respuesta.error("No se pudo registrar el lector");
        } catch (SQLException e) {
            return Respuesta.error("Error de base de datos: " + e.getMessage());
        }
    }

    /**
     * Actualiza un lector existente.
     *
     * @param idLector identificador del lector
     * @param cedula cédula ecuatoriana
     * @param nombres nombres
     * @param apellidos apellidos
     * @param correo correo electrónico
     * @param telefono celular (09xxxxxxxx)
     * @param direccion dirección (opcional)
     * @param estado ACTIVO o SUSPENDIDO
     * @return respuesta exitosa o el error encontrado
     */
    public Respuesta actualizarLector(int idLector, String cedula, String nombres, String apellidos,
            String correo, String telefono, String direccion, String estado) {
        if (idLector <= 0) {
            return Respuesta.error("Debe seleccionar un lector para modificar");
        }
        Respuesta validacion = validarCampos(cedula, nombres, apellidos, correo, telefono, direccion, estado);
        if (!validacion.isExitosa()) {
            return validacion;
        }
        Lector lector = crearLector(idLector, cedula, nombres, apellidos, correo, telefono, direccion, estado);
        try {
            Respuesta duplicado = verificarDuplicados(lector);
            if (duplicado != null) {
                return duplicado;
            }
            if (dao.actualizar(lector)) {
                return Respuesta.ok("Lector actualizado correctamente");
            }
            return Respuesta.error("El lector ya no existe en la base de datos");
        } catch (SQLException e) {
            return Respuesta.error("Error de base de datos: " + e.getMessage());
        }
    }

    /**
     * Elimina un lector si existe y no tiene préstamos registrados.
     *
     * @param idLector identificador del lector
     * @return respuesta exitosa o el motivo por el que no se eliminó
     */
    public Respuesta eliminarLector(int idLector) {
        if (idLector <= 0) {
            return Respuesta.error("Debe seleccionar un lector para eliminar");
        }
        try {
            if (dao.obtenerPorId(idLector) == null) {
                return Respuesta.error("El lector no existe");
            }
            if (dao.tienePrestamos(idLector)) {
                return Respuesta.error("No se puede eliminar: el lector tiene préstamos registrados");
            }
            if (dao.eliminar(idLector)) {
                return Respuesta.ok("Lector eliminado correctamente");
            }
            return Respuesta.error("No se pudo eliminar el lector");
        } catch (SQLException e) {
            return Respuesta.error("Error de base de datos: " + e.getMessage());
        }
    }

    /**
     * Comprueba que la cédula y el correo no pertenezcan a otro lector.
     *
     * @param lector lector a guardar
     * @return respuesta de error, o {@code null} si no hay duplicados
     * @throws SQLException si ocurre un error con la base de datos
     */
    private Respuesta verificarDuplicados(Lector lector) throws SQLException {
        if (dao.existeCedula(lector.getCedula(), lector.getIdLector())) {
            return Respuesta.error("Ya existe un lector registrado con esa cédula", CAMPO_CEDULA);
        }
        if (dao.existeCorreo(lector.getCorreo(), lector.getIdLector())) {
            return Respuesta.error("Ya existe un lector registrado con ese correo", CAMPO_CORREO);
        }
        return null;
    }

    /**
     * Valida todos los campos en orden y devuelve el primer error.
     *
     * @param cedula cédula
     * @param nombres nombres
     * @param apellidos apellidos
     * @param correo correo
     * @param telefono teléfono
     * @param direccion dirección
     * @param estado estado
     * @return respuesta exitosa si todo es válido, o el error con su campo
     */
    public Respuesta validarCampos(String cedula, String nombres, String apellidos, String correo,
            String telefono, String direccion, String estado) {
        String error = validarCedula(cedula);
        if (error != null) {
            return Respuesta.error(error, CAMPO_CEDULA);
        }
        error = validarNombres(nombres);
        if (error != null) {
            return Respuesta.error(error, CAMPO_NOMBRES);
        }
        error = validarApellidos(apellidos);
        if (error != null) {
            return Respuesta.error(error, CAMPO_APELLIDOS);
        }
        error = validarCorreo(correo);
        if (error != null) {
            return Respuesta.error(error, CAMPO_CORREO);
        }
        error = validarTelefono(telefono);
        if (error != null) {
            return Respuesta.error(error, CAMPO_TELEFONO);
        }
        error = validarDireccion(direccion);
        if (error != null) {
            return Respuesta.error(error, CAMPO_DIRECCION);
        }
        error = validarEstado(estado);
        if (error != null) {
            return Respuesta.error(error, CAMPO_ESTADO);
        }
        return Respuesta.ok("Datos válidos");
    }

    /**
     * Valida la cédula ecuatoriana: obligatoria, 10 dígitos, código de
     * provincia entre 01 y 24 (o 30), tercer dígito menor a 6 y dígito
     * verificador correcto.
     *
     * @param cedula cédula escrita
     * @return mensaje de error o {@code null} si es válida
     */
    public String validarCedula(String cedula) {
        if (cedula == null || cedula.isBlank()) {
            return "La cédula es obligatoria";
        }
        String valor = cedula.trim();
        if (!DIEZ_DIGITOS.matcher(valor).matches()) {
            return "La cédula debe tener exactamente 10 dígitos numéricos";
        }
        int provincia = Integer.parseInt(valor.substring(0, 2));
        if ((provincia < 1 || provincia > 24) && provincia != 30) {
            return "Los dos primeros dígitos de la cédula no son un código de provincia válido";
        }
        if (valor.charAt(2) >= '6') {
            return "El tercer dígito de la cédula debe ser menor a 6";
        }
        if (!digitoVerificadorValido(valor)) {
            return "La cédula no es válida (dígito verificador incorrecto)";
        }
        return null;
    }

    /**
     * Comprueba el dígito verificador de una cédula de 10 dígitos con el
     * algoritmo módulo 10 del Registro Civil del Ecuador.
     *
     * @param cedula cédula de 10 dígitos
     * @return {@code true} si el décimo dígito es correcto
     */
    public boolean digitoVerificadorValido(String cedula) {
        int suma = 0;
        for (int i = 0; i < 9; i++) {
            int producto = (cedula.charAt(i) - '0') * (i % 2 == 0 ? 2 : 1);
            suma += producto > 9 ? producto - 9 : producto;
        }
        int verificador = (10 - suma % 10) % 10;
        return verificador == cedula.charAt(9) - '0';
    }

    /**
     * Valida los nombres: obligatorios, solo letras y espacios, entre 2 y 60
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
        if (!SOLO_LETRAS.matcher(valor).matches()) {
            return "Los nombres solo deben contener letras y espacios";
        }
        if (valor.length() < 2 || valor.length() > 60) {
            return "Los nombres deben tener entre 2 y 60 caracteres";
        }
        return null;
    }

    /**
     * Valida los apellidos: obligatorios, solo letras y espacios, entre 2 y
     * 60 caracteres.
     *
     * @param apellidos apellidos escritos
     * @return mensaje de error o {@code null} si son válidos
     */
    public String validarApellidos(String apellidos) {
        if (apellidos == null || apellidos.isBlank()) {
            return "Los apellidos son obligatorios";
        }
        String valor = apellidos.trim();
        if (!SOLO_LETRAS.matcher(valor).matches()) {
            return "Los apellidos solo deben contener letras y espacios";
        }
        if (valor.length() < 2 || valor.length() > 60) {
            return "Los apellidos deben tener entre 2 y 60 caracteres";
        }
        return null;
    }

    /**
     * Valida el correo: obligatorio, formato usuario@dominio.ext y máximo
     * 100 caracteres.
     *
     * @param correo correo escrito
     * @return mensaje de error o {@code null} si es válido
     */
    public String validarCorreo(String correo) {
        if (correo == null || correo.isBlank()) {
            return "El correo es obligatorio";
        }
        String valor = correo.trim();
        if (valor.length() > 100) {
            return "El correo no puede superar 100 caracteres";
        }
        if (!CORREO.matcher(valor).matches()) {
            return "El correo no tiene un formato válido (ejemplo: nombre@correo.com)";
        }
        return null;
    }

    /**
     * Valida el teléfono: obligatorio, celular de 10 dígitos que empieza con
     * 09.
     *
     * @param telefono teléfono escrito
     * @return mensaje de error o {@code null} si es válido
     */
    public String validarTelefono(String telefono) {
        if (telefono == null || telefono.isBlank()) {
            return "El teléfono es obligatorio";
        }
        if (!CELULAR.matcher(telefono.trim()).matches()) {
            return "El teléfono debe tener 10 dígitos y empezar con 09";
        }
        return null;
    }

    /**
     * Valida la dirección: opcional, máximo 150 caracteres.
     *
     * @param direccion dirección escrita
     * @return mensaje de error o {@code null} si es válida
     */
    public String validarDireccion(String direccion) {
        if (direccion != null && direccion.trim().length() > 150) {
            return "La dirección no puede superar 150 caracteres";
        }
        return null;
    }

    /**
     * Valida el estado: obligatorio y debe ser ACTIVO o SUSPENDIDO.
     *
     * @param estado estado seleccionado
     * @return mensaje de error o {@code null} si es válido
     */
    public String validarEstado(String estado) {
        if (estado == null || estado.isBlank()) {
            return "Debe seleccionar un estado";
        }
        if (!ESTADOS.contains(estado.trim())) {
            return "El estado seleccionado no es válido";
        }
        return null;
    }

    /**
     * Construye un {@link Lector} con los datos ya validados.
     *
     * @param idLector identificador (0 para uno nuevo)
     * @param cedula cédula
     * @param nombres nombres
     * @param apellidos apellidos
     * @param correo correo
     * @param telefono teléfono
     * @param direccion dirección (puede estar vacía)
     * @param estado estado
     * @return lector listo para guardar
     */
    private Lector crearLector(int idLector, String cedula, String nombres, String apellidos,
            String correo, String telefono, String direccion, String estado) {
        String dir = direccion == null || direccion.isBlank() ? null : direccion.trim();
        return new Lector(idLector, cedula.trim(), nombres.trim(), apellidos.trim(),
                correo.trim().toLowerCase(), telefono.trim(), dir, estado.trim());
    }
}
