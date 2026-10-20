# Sistema de Gestión de Biblioteca

Proyecto Primer Parcial · Verificación y Validación de Software.
Java 17+ · Swing · MySQL 8 · Maven · Arquitectura en capas MVC.

## Cómo ejecutarlo

1. Ejecutar `database/bd_biblioteca.sql` en MySQL (Workbench: *File > Open SQL Script* y ejecutar todo).
   Crea la base `bd_biblioteca`, las 6 tablas y los datos de prueba. **Volver a ejecutarlo borra y
   recrea todo**, así que sirve para dejar los datos como al inicio antes de cada prueba.
2. Abrir la carpeta del proyecto en NetBeans (o IntelliJ) como proyecto Maven.
3. Editar la contraseña de MySQL en `src/main/java/util/Conexion.java` (constante `CONTRASENA`).
4. Ejecutar la clase `vista.SistemaBiblioteca` (Run Project).

### Usuarios de prueba

| Usuario         | Contraseña    | Estado                      |
|-----------------|---------------|-----------------------------|
| `admin`         | `Admin123`    | Activo (ADMINISTRADOR)      |
| `bibliotecario` | `Biblio123`   | Activo (BIBLIOTECARIO)      |
| `invitado`      | `Invitado123` | Inactivo: no puede ingresar |

Las contraseñas se guardan como SHA-256 (`SHA2('clave', 256)`). No existe CRUD de usuarios.
Después de 3 contraseñas incorrectas el sistema se cierra.

## Estructura

```
src/main/java/
├── modelo/       Libro, Autor, Lector, Prestamo, Devolucion, Usuario
├── dao/          LibroDAO, AutorDAO, LectorDAO, PrestamoDAO, DevolucionDAO, UsuarioDAO
├── controlador/  LibroController, AutorController, LectorController,
│                 PrestamoController, DevolucionController, LoginController
├── vista/        SistemaBiblioteca (main), LoginFrame, MenuPrincipalFrame,
│                 XxxCRUDPanel + XxxDialog de cada módulo, Estilos, EstadoRenderer, Recargable
└── util/         Conexion, Respuesta, Seguridad, Sesion, Fechas
database/         bd_biblioteca.sql
docs/javadoc/     Javadoc generado (abrir index.html)
docs/capturas/    Capturas de cada pantalla
```

Cada módulo tiene sus 5 archivos: `Xxx` (modelo), `XxxDAO`, `XxxController`, `XxxCRUDPanel` y `XxxDialog`.
Todos los controladores siguen el mismo patrón:

* Reciben los datos del formulario como texto, igual que los escribe el usuario.
* Validan con **un método pequeño por campo** (`validarXxx`) que devuelve el mensaje de error o `null`.
* Devuelven un `util.Respuesta` (éxito/error, mensaje y campo con error).
* Aceptan el DAO por constructor, así las pruebas JUnit pueden usar un DAO simulado sin base de datos.

Todas las fechas se escriben con el formato `dd/MM/yyyy`.

## Módulos

| Módulo     | Tipo                      | Responsable                       |
|------------|---------------------------|-----------------------------------|
| Libro      | Independiente             | Guashpa Gualli Jefferson Nezareth |
| Autor      | Independiente             |                                   |
| Lector     | Independiente             |                                   |
| Préstamo   | Depende de Libro y Lector |                                   |
| Devolución | Depende de Préstamo       |                                   |

En todos: **Nuevo** (C), **Buscar / Mostrar todos** (R), **Editar** o doble clic (U), **Eliminar** con confirmación (D).

### Libro

| Campo     | Regla |
|-----------|-------|
| ISBN      | Obligatorio, solo dígitos, 10 o 13 dígitos, único |
| Título    | Obligatorio, 2 a 150 caracteres |
| Editorial | Obligatoria, 2 a 100 caracteres, letras/números/espacios y `. , & ' -` |
| Año       | Obligatorio, numérico, entre 1450 y el año actual |
| Género    | Obligatorio: Novela, Cuento, Poesía, Ciencia, Historia, Tecnología, Infantil u Otro |
| Páginas   | Obligatorio, entero entre 1 y 5000 |
| Stock     | Obligatorio, entero entre 0 y 500 |

No se elimina un libro que tenga préstamos.

### Autor

| Campo               | Regla |
|---------------------|-------|
| Nombres / Apellidos | Obligatorios, solo letras (y `. ' -`), 2 a 60 caracteres |
| Nacionalidad        | Obligatoria, solo letras y espacios, 3 a 50 caracteres |
| Fecha de nacimiento | Opcional; si se escribe: fecha real, no futura, no anterior al año 1000 |
| Biografía           | Opcional, máximo 255 caracteres |

No se registra dos veces el mismo autor (mismos nombres y apellidos).

### Lector

| Campo               | Regla |
|---------------------|-------|
| Cédula              | Obligatoria, 10 dígitos, provincia 01–24 o 30, tercer dígito < 6, dígito verificador correcto, única |
| Nombres / Apellidos | Obligatorios, solo letras y espacios, 2 a 60 caracteres |
| Correo              | Obligatorio, formato `usuario@dominio.ext`, único |
| Teléfono            | Obligatorio, celular de 10 dígitos que empieza con 09 |
| Dirección           | Opcional, máximo 150 caracteres |
| Estado              | ACTIVO o SUSPENDIDO |

No se elimina un lector que tenga préstamos.

### Préstamo

| Campo              | Regla |
|--------------------|-------|
| Libro / Lector     | Obligatorios (se eligen de una lista) |
| Fecha del préstamo | Obligatoria, fecha real, no futura |
| Fecha límite       | Obligatoria, no anterior al préstamo y como máximo 30 días después |
| Observación        | Opcional, máximo 255 caracteres |

Reglas: el libro debe tener stock; el lector debe estar ACTIVO; máximo 3 préstamos sin devolver por lector.
Al registrar se descuenta 1 del stock. El estado pasa solo a VENCIDO cuando la fecha límite ya pasó.
No se modifica un préstamo DEVUELTO y no se elimina uno que tenga devolución.

### Devolución

| Campo               | Regla |
|---------------------|-------|
| Préstamo            | Obligatorio; solo préstamos ACTIVO o VENCIDO sin devolución |
| Fecha de devolución | Obligatoria, fecha real, no futura, no anterior a la fecha del préstamo |
| Estado del libro    | BUENO, DANADO o PERDIDO |
| Observación         | Opcional, máximo 255 caracteres |

Cálculos: días de retraso = días después de la fecha límite (mínimo 0).
Multa = $0.50 × días de retraso, + $5.00 si DANADO, + $20.00 si PERDIDO.
Al registrar, el préstamo pasa a DEVUELTO y el ejemplar vuelve al stock (salvo si se perdió).
Al eliminar, el préstamo vuelve a ACTIVO o VENCIDO.

## Datos de prueba

El script carga 15 libros, 10 autores, 10 lectores (2 suspendidos), 10 préstamos (4 activos, 2 vencidos,
4 devueltos) y 4 devoluciones (buena, con retraso, dañada y perdida). Las fechas de préstamos y
devoluciones se calculan a partir del día en que se ejecuta el script, así siempre hay casos vigentes.

Casos útiles para las pruebas:

* Libro sin stock: *Design Patterns* e *Historia del Ecuador* (stock 0).
* Libros sin préstamos (se pueden eliminar): ids 9, 10, 13, 14 y 15.
* Lectores suspendidos: Andrés Salazar Mora y Daniela Ortiz Pazmiño.
* Lector con 2 préstamos pendientes (al tercero llega al límite): Carlos Andrade Ruiz.
* Lectores sin préstamos (se pueden eliminar): Daniela Ortiz Pazmiño y Kevin Castro Suárez.
* Préstamos pendientes para registrar una devolución: N° 2, 3, 6, 8, 9 y 10.
