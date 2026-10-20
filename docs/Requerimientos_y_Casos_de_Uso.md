# Especificación de Requerimientos y Casos de Uso
## Sistema de Gestión de Biblioteca

**Asignatura:** Verificación y Validación de Software · Primer Parcial
**Responsable del módulo Libro, conexión, login y menú:** Guashpa Gualli Jefferson Nezareth
**Tecnología:** Java 17 · Swing · MySQL 8 · Maven · Arquitectura MVC (modelo, dao, controlador, vista, util)
**Versión del documento:** 1.0 · 20/10/2026

---

## 1. Introducción

### 1.1 Propósito
Este documento define **qué debe hacer** el Sistema de Gestión de Biblioteca. Es la **especificación** contra la que
se verifican las partes del sistema y el producto final: cada prueba (análisis estático, JUnit, DU-cadena y pruebas
dinámicas) debe poder rastrearse a un requerimiento numerado de este documento (ver sección 6, Matriz de trazabilidad).

> *Verificación:* ¿construimos el producto correctamente? (el código cumple estos requerimientos).
> *Validación:* ¿construimos el producto correcto? (los requerimientos responden a lo que necesita la biblioteca).

### 1.2 Alcance
Aplicación de escritorio para que el personal de una biblioteca administre libros, autores, lectores, préstamos y
devoluciones, con inicio de sesión de usuarios. No incluye gestión de usuarios del sistema, reservas en línea ni
acceso de lectores.

### 1.3 Actores

| Actor | Descripción |
|-------|-------------|
| **Administrador** | Usuario con rol ADMINISTRADOR (`admin`). Usa todos los módulos. |
| **Bibliotecario** | Usuario con rol BIBLIOTECARIO (`bibliotecario`). Usa todos los módulos. |
| **Sistema / Base de datos MySQL** | Actor secundario: guarda y consulta la información (`bd_biblioteca`). |

En esta versión ambos roles tienen los mismos permisos; en los casos de uso se los llama **Usuario**.

### 1.4 Convenciones
* `RF-XXX-NN`: requerimiento funcional. `RNF-NN`: requerimiento no funcional. `RN-XXX-NN`: regla de negocio.
* `CU-NN`: caso de uso.
* Fechas en formato `dd/MM/yyyy`. Todos los campos de texto se evalúan quitando espacios al inicio y al final.
* Cuando hay varios errores, el sistema muestra **el primero** según el orden de los campos y enfoca ese campo.

---

## 2. Requerimientos funcionales

### 2.1 Conexión, inicio de sesión y menú (GEN / LOG / MEN)

| ID | Requerimiento | Implementado en |
|----|---------------|-----------------|
| RF-GEN-01 | El sistema debe conectarse a la base de datos MySQL `bd_biblioteca` y mostrar un mensaje si la conexión falla. | `util.Conexion` |
| RF-LOG-01 | El sistema debe solicitar usuario y contraseña al iniciar. | `vista.LoginFrame` |
| RF-LOG-02 | El usuario es obligatorio, sin espacios y de máximo 30 caracteres. | `LoginController.validarUsuario` |
| RF-LOG-03 | La contraseña es obligatoria y de mínimo 6 caracteres. | `LoginController.validarContrasena` |
| RF-LOG-04 | La contraseña se compara cifrada con SHA-256, nunca en texto plano. | `util.Seguridad.sha256`, `UsuarioDAO.autenticar` |
| RF-LOG-05 | Si las credenciales son incorrectas, mostrar "Usuario o contraseña incorrectos". | `LoginController.iniciarSesion` |
| RF-LOG-06 | Un usuario inactivo no puede ingresar ("El usuario está inactivo. Contacte al administrador"). | `LoginController.iniciarSesion` |
| RF-LOG-07 | Tras **3 intentos fallidos** el sistema se cierra. | `LoginFrame.MAX_INTENTOS` |
| RF-LOG-08 | Con credenciales válidas se abre el menú principal con un mensaje de bienvenida y se guarda la sesión. | `util.Sesion`, `MenuPrincipalFrame` |
| RF-MEN-01 | El menú principal debe dar acceso a los módulos Libros, Autores, Lectores, Préstamos y Devoluciones (barra de menú y botones laterales). | `MenuPrincipalFrame` |
| RF-MEN-02 | El usuario puede **cerrar sesión** (vuelve al login) o **salir** (Ctrl+Q), ambos con confirmación. | `MenuPrincipalFrame` |

### 2.2 Módulo Libro (LIB) — detalle

| ID | Requerimiento | Implementado en |
|----|---------------|-----------------|
| RF-LIB-01 | **Registrar** un libro con ISBN, título, editorial, año de publicación, género, número de páginas y stock. | `LibroController.registrarLibro` |
| RF-LIB-02 | **Listar** todos los libros en una tabla. | `listarLibros` |
| RF-LIB-03 | **Buscar** libros por ISBN, título, editorial o género (coincidencia parcial). Si el texto está vacío, mostrar todos. | `buscarLibros`, `LibroDAO.buscar` |
| RF-LIB-04 | **Modificar** un libro seleccionado (botón Editar o doble clic). | `actualizarLibro` |
| RF-LIB-05 | **Eliminar** un libro seleccionado, previa confirmación. | `eliminarLibro` |
| RF-LIB-06 | **ISBN:** obligatorio, solo dígitos (sin guiones ni espacios), exactamente 10 o 13 dígitos. | `validarIsbn` |
| RF-LIB-07 | **ISBN único:** no se registra ni se modifica un libro con un ISBN que ya tenga otro libro. | `LibroDAO.existeIsbn` |
| RF-LIB-08 | **Título:** obligatorio, de 2 a 150 caracteres. | `validarTitulo` |
| RF-LIB-09 | **Editorial:** obligatoria, de 2 a 100 caracteres; solo letras, números, espacios y `. , & ' -`. | `validarEditorial` |
| RF-LIB-10 | **Año de publicación:** obligatorio, numérico de hasta 4 dígitos, entre 1450 y el año actual. | `validarAnioPublicacion` |
| RF-LIB-11 | **Género:** obligatorio, uno de: Novela, Cuento, Poesía, Ciencia, Historia, Tecnología, Infantil, Otro. | `validarGenero` |
| RF-LIB-12 | **Páginas:** obligatorio, entero entre 1 y 5000. | `validarNumeroPaginas` |
| RF-LIB-13 | **Stock:** obligatorio, entero entre 0 y 500. | `validarStock` |
| RF-LIB-14 | No se elimina un libro que tenga préstamos registrados. | `LibroDAO.tienePrestamos` |
| RF-LIB-15 | Para modificar o eliminar debe haber un libro seleccionado y debe existir en la base. | `actualizarLibro`, `eliminarLibro` |
| RF-LIB-16 | Mostrar un mensaje de éxito o de error tras cada operación y enfocar el campo con error. | `util.Respuesta`, `LibroDialog` |

**Mensajes de error del módulo Libro (resultado esperado en las pruebas):**

| Campo | Condición | Mensaje |
|-------|-----------|---------|
| ISBN | vacío | El ISBN es obligatorio |
| ISBN | contiene letras, guiones o espacios | El ISBN solo debe contener números (sin guiones ni espacios) |
| ISBN | longitud distinta de 10 y 13 | El ISBN debe tener 10 o 13 dígitos |
| ISBN | repetido | Ya existe un libro registrado con ese ISBN |
| Título | vacío / <2 / >150 | El título es obligatorio / debe tener al menos 2 caracteres / no puede superar 150 caracteres |
| Editorial | vacía / caracteres inválidos / longitud | La editorial es obligatoria / contiene caracteres no permitidos / debe tener entre 2 y 100 caracteres |
| Año | vacío / no numérico / <1450 / > año actual | El año de publicación es obligatorio / debe ser un número de hasta 4 dígitos / no puede ser menor a 1450 / no puede ser mayor al año actual |
| Género | vacío / fuera de lista | Debe seleccionar un género / El género seleccionado no es válido |
| Páginas | vacío / no entero / fuera de rango | El número de páginas es obligatorio / debe ser un número entero / debe estar entre 1 y 5000 |
| Stock | vacío / no entero o negativo / >500 | El stock es obligatorio / debe ser un número entero no negativo / no puede superar 500 ejemplares |
| — | libro con préstamos | No se puede eliminar: el libro tiene préstamos registrados |

### 2.3 Módulo Autor (AUT)

| ID | Requerimiento |
|----|---------------|
| RF-AUT-01 | Registrar, listar, buscar (nombres, apellidos o nacionalidad), modificar y eliminar autores. |
| RF-AUT-02 | Nombres y apellidos: obligatorios, solo letras (y `. ' -`), 2 a 60 caracteres. |
| RF-AUT-03 | Nacionalidad: obligatoria, solo letras y espacios, 3 a 50 caracteres. |
| RF-AUT-04 | Fecha de nacimiento: opcional; si se escribe debe ser una fecha real, no futura y no anterior al año 1000. |
| RF-AUT-05 | Biografía: opcional, máximo 255 caracteres. |
| RF-AUT-06 | No se registra dos veces el mismo autor (mismos nombres y apellidos). |

### 2.4 Módulo Lector (LEC)

| ID | Requerimiento |
|----|---------------|
| RF-LEC-01 | Registrar, listar, buscar (cédula, nombres, apellidos o correo), modificar y eliminar lectores. |
| RF-LEC-02 | Cédula ecuatoriana: obligatoria, 10 dígitos, provincia 01–24 o 30, tercer dígito menor a 6, dígito verificador correcto, única. |
| RF-LEC-03 | Nombres y apellidos: obligatorios, solo letras y espacios, 2 a 60 caracteres. |
| RF-LEC-04 | Correo: obligatorio, formato `usuario@dominio.ext`, único. |
| RF-LEC-05 | Teléfono: obligatorio, celular de 10 dígitos que empieza con 09. |
| RF-LEC-06 | Dirección: opcional, máximo 150 caracteres. |
| RF-LEC-07 | Estado: ACTIVO o SUSPENDIDO. |
| RF-LEC-08 | No se elimina un lector que tenga préstamos. |

### 2.5 Módulo Préstamo (PRE) — depende de Libro y Lector

| ID | Requerimiento |
|----|---------------|
| RF-PRE-01 | Registrar, listar, buscar (título, lector, cédula o estado), modificar y eliminar préstamos. |
| RF-PRE-02 | Libro y lector obligatorios, elegidos de una lista. |
| RF-PRE-03 | Fecha de préstamo: obligatoria, fecha real, no futura. |
| RF-PRE-04 | Fecha límite: obligatoria, no anterior a la fecha de préstamo y como máximo 30 días después. |
| RF-PRE-05 | Observación: opcional, máximo 255 caracteres. |
| RF-PRE-06 | El libro debe tener stock disponible (> 0). |
| RF-PRE-07 | El lector debe estar ACTIVO y tener como máximo 3 préstamos sin devolver. |
| RF-PRE-08 | Al registrar se descuenta 1 ejemplar del stock; al eliminar un préstamo pendiente se devuelve. |
| RF-PRE-09 | El estado pasa automáticamente a VENCIDO cuando la fecha límite ya pasó. |
| RF-PRE-10 | No se modifica un préstamo DEVUELTO y no se elimina uno que tenga devolución. |

### 2.6 Módulo Devolución (DEV) — depende de Préstamo

| ID | Requerimiento |
|----|---------------|
| RF-DEV-01 | Registrar, listar, buscar (título, lector o estado del libro), modificar y eliminar devoluciones. |
| RF-DEV-02 | Solo se devuelven préstamos ACTIVO o VENCIDO que no tengan devolución. |
| RF-DEV-03 | Fecha de devolución: obligatoria, fecha real, no futura, no anterior a la fecha del préstamo. |
| RF-DEV-04 | Estado del libro: BUENO, DANADO o PERDIDO. Observación opcional, máximo 255 caracteres. |
| RF-DEV-05 | Días de retraso = días después de la fecha límite (mínimo 0). |
| RF-DEV-06 | Multa = $0.50 × días de retraso, + $5.00 si DANADO, + $20.00 si PERDIDO. |
| RF-DEV-07 | Al registrar, el préstamo pasa a DEVUELTO y el ejemplar vuelve al stock (salvo si se perdió). |
| RF-DEV-08 | Al modificar se recalculan retraso, multa y stock según el nuevo estado del libro. |
| RF-DEV-09 | Al eliminar, el préstamo vuelve a ACTIVO o VENCIDO y se ajusta el stock. |

---

## 3. Requerimientos no funcionales

| ID | Tipo | Requerimiento |
|----|------|---------------|
| RNF-01 | Plataforma | Aplicación de escritorio en Java 17 o superior con interfaz Swing; se construye con Maven. |
| RNF-02 | Persistencia | Datos en MySQL 8 (`bd_biblioteca`, 6 tablas). El script `database/bd_biblioteca.sql` crea la base y datos de prueba. |
| RNF-03 | Arquitectura | Capas MVC: `modelo`, `dao`, `controlador`, `vista`, `util`. La vista no accede directamente a la base. |
| RNF-04 | Seguridad | Contraseñas guardadas como hash SHA-256; consultas con `PreparedStatement` (evita inyección SQL); máximo 3 intentos de login. |
| RNF-05 | Integridad | Operaciones que cambian préstamo y stock a la vez se ejecutan en una transacción. Claves foráneas impiden borrar datos relacionados. |
| RNF-06 | Usabilidad | Mensajes claros en español; el foco va al campo con error; confirmación antes de eliminar, cerrar sesión o salir; listas desplegables para valores cerrados (género, estado). |
| RNF-07 | Mantenibilidad | Un método `validarXxx` pequeño por campo; código documentado con Javadoc (`docs/javadoc`). |
| RNF-08 | Testabilidad | Los controladores reciben el DAO por constructor para poder probarlos con JUnit usando un DAO simulado, sin base de datos. |
| RNF-09 | Formato | Fechas en `dd/MM/yyyy`; montos con 2 decimales. |
| RNF-10 | Rendimiento | Las búsquedas y listados deben responder en menos de 2 segundos con los datos de prueba. |

---

## 4. Diagrama general de casos de uso

```
                          +-------------------------------------------------+
                          |          Sistema de Gestión de Biblioteca       |
                          |                                                 |
                          |  (CU-01 Iniciar sesión)                         |
                          |  (CU-02 Navegar menú / Cerrar sesión)           |
   +-----------------+    |                                                 |
   |   Usuario       |----|  (CU-03 Registrar libro) ---<<include>>--+      |
   | (Administrador  |    |  (CU-04 Buscar/listar libros)            |      |
   |  o              |    |  (CU-05 Modificar libro) ---<<include>>--+--> (Validar datos del libro)
   |  Bibliotecario) |    |  (CU-06 Eliminar libro)                         |
   +-----------------+    |  (CU-07 Gestionar autores)                      |
                          |  (CU-08 Gestionar lectores)                     |
                          |  (CU-09 Registrar préstamo)                     |
                          |  (CU-10 Gestionar préstamos)                    |
                          |  (CU-11 Registrar devolución)                   |
                          |  (CU-12 Gestionar devoluciones)                 |
                          +-------------------------------------------------+
   Todos los CU-03..CU-12 requieren <<include>> CU-01 (sesión iniciada).
```

---

## 5. Casos de uso

### CU-01 Iniciar sesión
| Elemento | Descripción |
|----------|-------------|
| Actor | Usuario |
| Requerimientos | RF-GEN-01, RF-LOG-01 a RF-LOG-08 |
| Precondición | La base de datos está disponible y el usuario existe. |
| Flujo básico | 1. El sistema muestra la ventana de login. 2. El usuario escribe usuario y contraseña y pulsa **Ingresar**. 3. El sistema valida los campos. 4. El sistema cifra la contraseña con SHA-256 y busca el usuario. 5. El sistema verifica que el usuario esté activo. 6. El sistema guarda la sesión, muestra "Bienvenido, <nombre>" y abre el menú principal. |
| Flujos alternos | 3a. Usuario vacío, con espacios o >30 caracteres, o contraseña vacía o <6 caracteres: muestra el error y enfoca el campo. 4a. Credenciales incorrectas: muestra "Usuario o contraseña incorrectos" y suma un intento; al tercer intento muestra un aviso y cierra el sistema. 5a. Usuario inactivo (`invitado`): muestra el mensaje de inactivo. 4b. Falla la conexión: "No se pudo conectar con la base de datos". El usuario pulsa **Salir**: termina la aplicación. |
| Postcondición | Sesión iniciada y menú principal visible. |

### CU-02 Navegar el menú y cerrar sesión
| Elemento | Descripción |
|----------|-------------|
| Actor | Usuario |
| Requerimientos | RF-MEN-01, RF-MEN-02 |
| Precondición | Sesión iniciada (CU-01). |
| Flujo básico | 1. El usuario elige un módulo en la barra de menú o en los botones laterales. 2. El sistema muestra el panel del módulo con sus datos. |
| Flujos alternos | 1a. **Cerrar sesión**: el sistema pide confirmación, cierra la sesión y vuelve al login. 1b. **Salir** (Ctrl+Q): pide confirmación y cierra la aplicación. 1c. El usuario responde "No": se queda en el menú. |
| Postcondición | Módulo visible, o sesión cerrada. |

### CU-03 Registrar libro *(módulo de JEFF)*
| Elemento | Descripción |
|----------|-------------|
| Actor | Usuario |
| Requerimientos | RF-LIB-01, RF-LIB-06 a RF-LIB-13, RF-LIB-16 |
| Precondición | Sesión iniciada; el usuario está en el módulo Libros. |
| Flujo básico | 1. El usuario pulsa **Nuevo**. 2. El sistema abre el formulario vacío. 3. El usuario ingresa ISBN, título, editorial, año, género, páginas y stock y pulsa **Guardar**. 4. El sistema valida los campos en orden (ISBN → título → editorial → año → género → páginas → stock). 5. El sistema verifica que el ISBN no exista. 6. El sistema guarda el libro. 7. El sistema muestra "Libro registrado correctamente", cierra el formulario y actualiza la tabla. |
| Flujos alternos | 4a. Un campo no cumple su regla: muestra el mensaje del campo (tabla de la sección 2.2), enfoca ese campo y vuelve al paso 3. 5a. ISBN repetido: "Ya existe un libro registrado con ese ISBN" y vuelve al paso 3. 6a. Error de base de datos: "Error de base de datos: …". 3a. El usuario pulsa **Cancelar**: se cierra el formulario sin guardar. |
| Postcondición | El libro queda registrado en la tabla `libro`. |

### CU-04 Buscar y listar libros
| Elemento | Descripción |
|----------|-------------|
| Actor | Usuario |
| Requerimientos | RF-LIB-02, RF-LIB-03 |
| Precondición | Sesión iniciada; módulo Libros abierto. |
| Flujo básico | 1. El sistema muestra todos los libros al abrir el módulo. 2. El usuario escribe un texto y pulsa **Buscar**. 3. El sistema muestra los libros cuyo ISBN, título, editorial o género contienen el texto. |
| Flujos alternos | 2a. Texto vacío o **Mostrar todos**: muestra todos los libros. 3a. No hay coincidencias: la tabla queda vacía. |
| Postcondición | La tabla muestra el resultado. |

### CU-05 Modificar libro
| Elemento | Descripción |
|----------|-------------|
| Actor | Usuario |
| Requerimientos | RF-LIB-04, RF-LIB-06 a RF-LIB-13, RF-LIB-15, RF-LIB-16 |
| Precondición | Sesión iniciada; hay al menos un libro. |
| Flujo básico | 1. El usuario selecciona un libro y pulsa **Editar** (o doble clic). 2. El sistema abre el formulario con los datos actuales. 3. El usuario cambia los datos y pulsa **Guardar**. 4. El sistema valida los campos (igual que CU-03). 5. El sistema verifica que el ISBN no pertenezca a **otro** libro. 6. El sistema actualiza y muestra "Libro actualizado correctamente". |
| Flujos alternos | 1a. No hay libro seleccionado: "Debe seleccionar un libro para modificar". 4a. Dato inválido: muestra el error del campo. 5a. ISBN de otro libro: "Ya existe otro libro registrado con ese ISBN". 6a. El libro fue borrado: "El libro ya no existe en la base de datos". |
| Postcondición | Los datos del libro quedan actualizados. |

### CU-06 Eliminar libro
| Elemento | Descripción |
|----------|-------------|
| Actor | Usuario |
| Requerimientos | RF-LIB-05, RF-LIB-14, RF-LIB-15 |
| Precondición | Sesión iniciada; hay al menos un libro. |
| Flujo básico | 1. El usuario selecciona un libro y pulsa **Eliminar**. 2. El sistema pide confirmación. 3. El usuario confirma. 4. El sistema verifica que el libro exista y no tenga préstamos. 5. El sistema lo elimina y muestra "Libro eliminado correctamente". |
| Flujos alternos | 1a. Sin selección: "Debe seleccionar un libro para eliminar". 3a. El usuario cancela: no se elimina. 4a. No existe: "El libro no existe". 4b. Tiene préstamos: "No se puede eliminar: el libro tiene préstamos registrados" (p. ej. libros 1–8, 11, 12 del script). |
| Postcondición | El libro ya no aparece en la tabla. |

### CU-07 Gestionar autores
| Elemento | Descripción |
|----------|-------------|
| Actor | Usuario |
| Requerimientos | RF-AUT-01 a RF-AUT-06 |
| Precondición | Sesión iniciada; módulo Autores abierto. |
| Flujo básico | 1. El usuario elige Nuevo, Editar, Eliminar o Buscar. 2. Para Nuevo/Editar ingresa nombres, apellidos, nacionalidad, fecha de nacimiento y biografía y pulsa Guardar. 3. El sistema valida los campos y que el autor no esté repetido. 4. El sistema guarda y muestra el mensaje de éxito. |
| Flujos alternos | 3a. Dato inválido: mensaje del campo. 3b. Autor repetido: se rechaza. Eliminar sin selección o autor inexistente: mensaje de error. |
| Postcondición | Autor registrado, actualizado o eliminado. |

### CU-08 Gestionar lectores
| Elemento | Descripción |
|----------|-------------|
| Actor | Usuario |
| Requerimientos | RF-LEC-01 a RF-LEC-08 |
| Precondición | Sesión iniciada; módulo Lectores abierto. |
| Flujo básico | 1. El usuario elige Nuevo o Editar. 2. Ingresa cédula, nombres, apellidos, correo, teléfono, dirección y estado y pulsa Guardar. 3. El sistema valida la cédula (incluido dígito verificador), los demás campos y que cédula y correo sean únicos. 4. El sistema guarda y muestra el mensaje de éxito. |
| Flujos alternos | 3a. Dato inválido o duplicado: mensaje del campo. Eliminar un lector con préstamos: se rechaza. |
| Postcondición | Lector registrado, actualizado o eliminado. |

### CU-09 Registrar préstamo
| Elemento | Descripción |
|----------|-------------|
| Actor | Usuario |
| Requerimientos | RF-PRE-02 a RF-PRE-08 |
| Precondición | Sesión iniciada; existen libros y lectores. |
| Flujo básico | 1. El usuario pulsa Nuevo en Préstamos. 2. Elige libro y lector, escribe fecha de préstamo, fecha límite y observación y pulsa Guardar. 3. El sistema valida los campos. 4. El sistema verifica que el libro tenga stock. 5. El sistema verifica que el lector esté ACTIVO y tenga menos de 3 préstamos pendientes. 6. El sistema registra el préstamo como ACTIVO y descuenta 1 del stock. |
| Flujos alternos | 3a. Fecha inválida, futura o límite mayor a 30 días: mensaje del campo. 4a. Sin stock (*Design Patterns*, *Historia del Ecuador*): se rechaza. 5a. Lector SUSPENDIDO o con 3 préstamos pendientes: se rechaza. |
| Postcondición | Préstamo registrado y stock del libro reducido en 1. |

### CU-10 Gestionar préstamos (buscar, modificar, eliminar)
| Elemento | Descripción |
|----------|-------------|
| Actor | Usuario |
| Requerimientos | RF-PRE-01, RF-PRE-08 a RF-PRE-10 |
| Flujo básico | 1. El usuario busca o selecciona un préstamo. 2. Edita los datos o lo elimina. 3. El sistema revisa las reglas (stock si cambia el libro, estado y límite si cambia el lector) y guarda. |
| Flujos alternos | Préstamo DEVUELTO: no se modifica. Préstamo con devolución: no se elimina. |
| Postcondición | Préstamo actualizado o eliminado; stock ajustado. |

### CU-11 Registrar devolución
| Elemento | Descripción |
|----------|-------------|
| Actor | Usuario |
| Requerimientos | RF-DEV-02 a RF-DEV-07 |
| Precondición | Existe un préstamo ACTIVO o VENCIDO sin devolución. |
| Flujo básico | 1. El usuario pulsa Nuevo en Devoluciones. 2. Elige el préstamo pendiente, escribe la fecha de devolución, el estado del libro y la observación. 3. El sistema valida los datos. 4. El sistema calcula días de retraso y multa. 5. El sistema registra la devolución, marca el préstamo como DEVUELTO, devuelve el ejemplar al stock (si no se perdió) y muestra los días de retraso y la multa. |
| Flujos alternos | 2a. Préstamo ya devuelto o inexistente: se rechaza. 3a. Fecha futura o anterior al préstamo: mensaje del campo. 5a. Estado PERDIDO: no se suma al stock y la multa incluye $20.00. |
| Postcondición | Devolución registrada; préstamo DEVUELTO. |

### CU-12 Gestionar devoluciones (buscar, modificar, eliminar)
| Elemento | Descripción |
|----------|-------------|
| Actor | Usuario |
| Requerimientos | RF-DEV-01, RF-DEV-08, RF-DEV-09 |
| Flujo básico | 1. El usuario busca o selecciona una devolución. 2. Modifica fecha, estado u observación (el sistema recalcula multa y stock) o la elimina (el préstamo vuelve a ACTIVO o VENCIDO). |
| Flujos alternos | Al eliminar, si el ejemplar ya se volvió a prestar (stock 0) no se puede descontar y se rechaza. |
| Postcondición | Devolución actualizada o eliminada. |

---

## 6. Matriz de trazabilidad (módulo Libro)

Sirve para el documento de pruebas: cada prueba debe citar el RF que verifica.

| Requerimiento | Caso de uso | Método a probar | Pruebas sugeridas |
|---------------|-------------|-----------------|-------------------|
| RF-LIB-06 ISBN válido | CU-03, CU-05 | `validarIsbn` (V(G) = 6) | Análisis estático (grafo, CC, caminos), JUnit: null, "", "12a4567890", "123456789", "1234567890", "9781234567897" |
| RF-LIB-07 ISBN único | CU-03, CU-05 | `registrarLibro`, `actualizarLibro` | JUnit con DAO simulado; prueba dinámica con ISBN del script |
| RF-LIB-01 Registrar | CU-03 | `registrarLibro` | Análisis estático, DU-cadena (variables `validacion`, `libro`), JUnit |
| RF-LIB-08 a RF-LIB-13 | CU-03, CU-05 | `validarTitulo` … `validarStock` | Valores límite: título 1/2/150/151; año 1449/1450/actual/actual+1; páginas 0/1/5000/5001; stock -1/0/500/501 |
| RF-LIB-14 | CU-06 | `eliminarLibro` | Libro con préstamos (id 1) vs. sin préstamos (id 9) |
| RF-LIB-15 | CU-05, CU-06 | `actualizarLibro`, `eliminarLibro` | id ≤ 0, id inexistente |
| RF-LIB-02, RF-LIB-03 | CU-04 | `buscarLibros` | Texto vacío, coincidencia parcial, sin resultados |
| RF-LOG-02 a RF-LOG-07 | CU-01 | `LoginController.iniciarSesion` | admin/Admin123 (éxito), clave errónea ×3, `invitado` inactivo |
