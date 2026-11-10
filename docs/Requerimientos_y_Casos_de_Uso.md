# Sistema de Gestión de Biblioteca

## 1. Descripción

Sistema de escritorio para una biblioteca, donde el personal (administrador y bibliotecarios) registra y controla
los **libros**, **autores** y **lectores**, y lleva el control de los **préstamos** de libros a los lectores y de sus
**devoluciones**, con cálculo de días de retraso y multa. El ingreso al sistema es con usuario y contraseña. Está
desarrollado en Java 17 con interfaz Swing, base de datos MySQL 8 y arquitectura en capas MVC (modelo, dao,
controlador, vista, util).

Este documento es la **especificación** contra la que se verifican las partes del sistema y el producto final: cada
prueba (análisis estático, JUnit, DU-cadena y pruebas dinámicas) se rastrea a un requerimiento numerado.

**Actores**

| Actor | Descripción |
|-------|-------------|
| **Administrador** | Usuario con rol ADMINISTRADOR (`admin`). Usa todos los módulos. |
| **Bibliotecario** | Usuario con rol BIBLIOTECARIO (`bibliotecario`). Usa todos los módulos. |
| **Sistema / Base de datos MySQL** | Actor secundario: guarda y consulta la información (`bd_biblioteca`). |

En esta versión ambos roles tienen los mismos permisos; en los casos de uso se los llama **Usuario**.

**Integrantes y módulos**

| Módulo | Tipo |
|--------|------|
| Módulo Libro - (                            ) | Independiente |
| Módulo Autor - (                            ) | Independiente |
| Módulo Lector - (                            ) | Independiente |
| Módulo Préstamo - (                            ) | Depende de Libro y Lector |
| Módulo Devolución - (                            ) | Depende de Préstamo |

Cada integrante escribe su nombre en el paréntesis del título de su módulo (sección 3).

---

## 2. Grupo (trabajo grupal)

Esta sección la elabora todo el grupo.

### 2.1 Modelo Entidad-Relación (Grupal)

![Figura 1. Modelo Entidad-Relación de bd_biblioteca](diagramas/09_modelo_datos.png){width=16cm}

Un **libro** y un **lector** pueden tener muchos **préstamos**; cada préstamo es de un solo libro y un solo lector.
Cada préstamo se cierra con **una** **devolución** como máximo. **Usuario** guarda las cuentas de acceso y **autor** es
un catálogo independiente.

### 2.2 Diagrama de Casos de Uso General (Grupal)

![Figura 2. Diagrama de casos de uso general](diagramas/01_cu_general.png){width=9cm}

El usuario del sistema puede ser Administrador o Bibliotecario (generalización); ambos tienen acceso a todos los casos de uso.

### 2.3 Arquitectura del sistema (Grupal)

![Figura 3. Arquitectura en capas (MVC)](diagramas/08_arquitectura.png){width=11cm}

La vista envía los datos del formulario como texto al controlador; el controlador valida, aplica las reglas de
negocio y usa el DAO, que accede a MySQL con `PreparedStatement` mediante `util.Conexion`. El controlador responde con
un `util.Respuesta` (éxito o error y el campo con error).

### 2.4 Formato de los requerimientos funcionales (Grupal)

Todos los requerimientos funcionales se escriben con el **formato de requerimientos funcionales** de la asignatura
(una ficha por requerimiento), para que cada caso de prueba pueda citar el RF y comparar el resultado obtenido con el
esperado.

| Campo de la ficha | Qué contiene |
|-------------------|--------------|
| **Número** | Identificador único `RF-<módulo>-<número>` (GEN, LOG, MEN, LIB, AUT, LEC, PRE, DEV). |
| **Título** | Qué hace el sistema, en pocas palabras. |
| **Texto (DEFINICIÓN)** | "El sistema permitirá / validará…", una sola capacidad por requerimiento. |
| **Tipo** | Funcional. |
| **Detalles de requisitos y restricciones (ESPECIFICACIÓN)** | Entradas, proceso, salidas, reglas de cada campo, mensajes que muestra el sistema, criterio de aceptación y clase/método del código donde se cumple. |
| **Fecha de revisión y versión, AUTOR** | Fecha, versión del documento e integrante responsable del módulo. |
| **Prioridad** | **Alta** (sin esto el sistema no sirve), **Media** (necesario pero hay alternativa), **Baja** (comodidad). |

Reglas comunes a todos los módulos (aplican a cada RF de registro y modificación):

* Los textos se evalúan sin los espacios del inicio y del final (`trim`).
* Los campos se validan **en el orden del formulario** y se informa solo **el primer error**, enfocando ese campo.
* Toda operación termina con un mensaje de éxito o de error en una ventana emergente.
* Si la base de datos falla, el mensaje es "Error de base de datos: \<detalle\>" y no se pierde lo escrito en el formulario.

### 2.5 Acceso al sistema: conexión, inicio de sesión y menú (Grupal)

#### Requerimientos funcionales

##### Resumen

| ID | Nombre | Prioridad |
|----|--------|-----------|
| RF-GEN-01 | Conectar con la base de datos | Alta |
| RF-LOG-01 | Iniciar sesión | Alta |
| RF-LOG-02 | Validar el nombre de usuario | Alta |
| RF-LOG-03 | Validar la contraseña | Alta |
| RF-LOG-04 | Rechazar credenciales incorrectas | Alta |
| RF-LOG-05 | Rechazar usuarios inactivos | Alta |
| RF-LOG-06 | Bloquear tras 3 intentos fallidos | Media |
| RF-MEN-01 | Acceder a los módulos desde el menú | Alta |
| RF-MEN-02 | Cerrar sesión y salir | Media |

+--------------------------+------------------------------------------------------------------------+
| Número:                  | **RF-GEN-01**                                                          |
+--------------------------+------------------------------------------------------------------------+
| Título:                  | Conectar con la base de datos                                          |
+--------------------------+------------------------------------------------------------------------+
| Texto:                   | El sistema debe abrir la conexión con la base MySQL `bd_biblioteca`    |
|                          | cada vez que necesita leer o guardar datos.                            |
| DEFINICIÓN               |                                                                        |
+--------------------------+------------------------------------------------------------------------+
| Tipo:                    | Funcional                                                              |
+--------------------------+------------------------------------------------------------------------+
| Detalles de requisitos y | - Entradas: URL, usuario y contraseña definidos en `util.Conexion`.    |
| restricciones:           |                                                                        |
|                          | - Proceso: Carga el driver JDBC y abre la conexión.                    |
| ESPECIFICACIÓN           |                                                                        |
|                          | - Salidas: Conexión abierta, o mensaje de error si el servidor no      |
|                          |   responde o las credenciales son erróneas.                            |
|                          |                                                                        |
|                          | Criterio de aceptación: Con MySQL detenido, al iniciar sesión aparece  |
|                          | "No se pudo conectar con la base de datos: …" y la aplicación no se    |
|                          | cae.                                                                   |
|                          |                                                                        |
|                          | Implementado en: `util.Conexion.getConexion`                           |
+--------------------------+------------------------------------------------------------------------+
| Fecha de revisión y      | 10/11/2026                                                             |
| versión, AUTOR:          |                                                                        |
|                          | Versión 1.3                                                            |
|                          |                                                                        |
|                          | Grupo                                                                  |
+--------------------------+------------------------------------------------------------------------+
| Prioridad:               | Alta                                                                   |
+--------------------------+------------------------------------------------------------------------+

+--------------------------+------------------------------------------------------------------------+
| Número:                  | **RF-LOG-01**                                                          |
+--------------------------+------------------------------------------------------------------------+
| Título:                  | Iniciar sesión                                                         |
+--------------------------+------------------------------------------------------------------------+
| Texto:                   | El sistema debe permitir el ingreso solo a usuarios registrados y      |
|                          | activos.                                                               |
| DEFINICIÓN               |                                                                        |
+--------------------------+------------------------------------------------------------------------+
| Tipo:                    | Funcional                                                              |
+--------------------------+------------------------------------------------------------------------+
| Detalles de requisitos y | - Entradas: Usuario, contraseña.                                       |
| restricciones:           |                                                                        |
|                          | - Proceso: Valida los campos (RF-LOG-02, RF-LOG-03), cifra la          |
| ESPECIFICACIÓN           |   contraseña con SHA-256 y busca el par usuario/hash en la tabla       |
|                          |   `usuario`.                                                           |
|                          |                                                                        |
|                          | - Salidas: "Bienvenido, \<nombres\>", se guarda la sesión y se abre el |
|                          |   menú principal.                                                      |
|                          |                                                                        |
|                          | Criterio de aceptación: `admin` / `Admin123` y `bibliotecario` /       |
|                          | `Biblio123` abren el menú principal.                                   |
|                          |                                                                        |
|                          | Implementado en: `LoginController.iniciarSesion`,                      |
|                          | `UsuarioDAO.autenticar`, `util.Sesion`                                 |
+--------------------------+------------------------------------------------------------------------+
| Fecha de revisión y      | 10/11/2026                                                             |
| versión, AUTOR:          |                                                                        |
|                          | Versión 1.3                                                            |
|                          |                                                                        |
|                          | Grupo                                                                  |
+--------------------------+------------------------------------------------------------------------+
| Prioridad:               | Alta                                                                   |
+--------------------------+------------------------------------------------------------------------+

+--------------------------+------------------------------------------------------------------------+
| Número:                  | **RF-LOG-02**                                                          |
+--------------------------+------------------------------------------------------------------------+
| Título:                  | Validar el nombre de usuario                                           |
+--------------------------+------------------------------------------------------------------------+
| Texto:                   | El usuario debe ser obligatorio, sin espacios internos y de máximo 30  |
|                          | caracteres.                                                            |
| DEFINICIÓN               |                                                                        |
+--------------------------+------------------------------------------------------------------------+
| Tipo:                    | Funcional                                                              |
+--------------------------+------------------------------------------------------------------------+
| Detalles de requisitos y | Criterio de aceptación: Vacío → "El usuario es obligatorio"; `ad min`  |
| restricciones:           | → "El usuario no debe contener espacios"; 31 caracteres → "El usuario  |
|                          | no puede superar 30 caracteres".                                       |
| ESPECIFICACIÓN           |                                                                        |
|                          | Implementado en: `LoginController.validarUsuario`                      |
+--------------------------+------------------------------------------------------------------------+
| Fecha de revisión y      | 10/11/2026                                                             |
| versión, AUTOR:          |                                                                        |
|                          | Versión 1.3                                                            |
|                          |                                                                        |
|                          | Grupo                                                                  |
+--------------------------+------------------------------------------------------------------------+
| Prioridad:               | Alta                                                                   |
+--------------------------+------------------------------------------------------------------------+

+--------------------------+------------------------------------------------------------------------+
| Número:                  | **RF-LOG-03**                                                          |
+--------------------------+------------------------------------------------------------------------+
| Título:                  | Validar la contraseña                                                  |
+--------------------------+------------------------------------------------------------------------+
| Texto:                   | La contraseña debe ser obligatoria y tener al menos 6 caracteres.      |
|                          |                                                                        |
| DEFINICIÓN               |                                                                        |
+--------------------------+------------------------------------------------------------------------+
| Tipo:                    | Funcional                                                              |
+--------------------------+------------------------------------------------------------------------+
| Detalles de requisitos y | Criterio de aceptación: Vacía → "La contraseña es obligatoria";        |
| restricciones:           | `12345` → "La contraseña debe tener al menos 6 caracteres".            |
|                          |                                                                        |
| ESPECIFICACIÓN           | Implementado en: `LoginController.validarContrasena`                   |
+--------------------------+------------------------------------------------------------------------+
| Fecha de revisión y      | 10/11/2026                                                             |
| versión, AUTOR:          |                                                                        |
|                          | Versión 1.3                                                            |
|                          |                                                                        |
|                          | Grupo                                                                  |
+--------------------------+------------------------------------------------------------------------+
| Prioridad:               | Alta                                                                   |
+--------------------------+------------------------------------------------------------------------+

+--------------------------+------------------------------------------------------------------------+
| Número:                  | **RF-LOG-04**                                                          |
+--------------------------+------------------------------------------------------------------------+
| Título:                  | Rechazar credenciales incorrectas                                      |
+--------------------------+------------------------------------------------------------------------+
| Texto:                   | Si el usuario no existe o la contraseña no coincide, el sistema debe   |
|                          | negar el ingreso sin indicar cuál de los dos falló.                    |
| DEFINICIÓN               |                                                                        |
+--------------------------+------------------------------------------------------------------------+
| Tipo:                    | Funcional                                                              |
+--------------------------+------------------------------------------------------------------------+
| Detalles de requisitos y | Criterio de aceptación: `admin` / `Admin999` → "Usuario o contraseña   |
| restricciones:           | incorrectos" y el foco va a la contraseña.                             |
|                          |                                                                        |
| ESPECIFICACIÓN           | Implementado en: `LoginController.iniciarSesion`                       |
+--------------------------+------------------------------------------------------------------------+
| Fecha de revisión y      | 10/11/2026                                                             |
| versión, AUTOR:          |                                                                        |
|                          | Versión 1.3                                                            |
|                          |                                                                        |
|                          | Grupo                                                                  |
+--------------------------+------------------------------------------------------------------------+
| Prioridad:               | Alta                                                                   |
+--------------------------+------------------------------------------------------------------------+

+--------------------------+------------------------------------------------------------------------+
| Número:                  | **RF-LOG-05**                                                          |
+--------------------------+------------------------------------------------------------------------+
| Título:                  | Rechazar usuarios inactivos                                            |
+--------------------------+------------------------------------------------------------------------+
| Texto:                   | Un usuario con estado inactivo no debe poder ingresar aunque su        |
|                          | contraseña sea correcta.                                               |
| DEFINICIÓN               |                                                                        |
+--------------------------+------------------------------------------------------------------------+
| Tipo:                    | Funcional                                                              |
+--------------------------+------------------------------------------------------------------------+
| Detalles de requisitos y | Criterio de aceptación: `invitado` / `Invitado123` → "El usuario está  |
| restricciones:           | inactivo. Contacte al administrador".                                  |
|                          |                                                                        |
| ESPECIFICACIÓN           | Implementado en: `LoginController.iniciarSesion`                       |
+--------------------------+------------------------------------------------------------------------+
| Fecha de revisión y      | 10/11/2026                                                             |
| versión, AUTOR:          |                                                                        |
|                          | Versión 1.3                                                            |
|                          |                                                                        |
|                          | Grupo                                                                  |
+--------------------------+------------------------------------------------------------------------+
| Prioridad:               | Alta                                                                   |
+--------------------------+------------------------------------------------------------------------+

+--------------------------+------------------------------------------------------------------------+
| Número:                  | **RF-LOG-06**                                                          |
+--------------------------+------------------------------------------------------------------------+
| Título:                  | Bloquear tras 3 intentos fallidos                                      |
+--------------------------+------------------------------------------------------------------------+
| Texto:                   | Después de 3 intentos de ingreso fallidos el sistema debe avisar y     |
|                          | cerrarse.                                                              |
| DEFINICIÓN               |                                                                        |
+--------------------------+------------------------------------------------------------------------+
| Tipo:                    | Funcional                                                              |
+--------------------------+------------------------------------------------------------------------+
| Detalles de requisitos y | Criterio de aceptación: Al tercer error aparece "Superó el número      |
| restricciones:           | máximo de intentos. El sistema se cerrará." y la aplicación termina.   |
|                          |                                                                        |
| ESPECIFICACIÓN           | Implementado en: `LoginFrame.MAX_INTENTOS`, `LoginFrame` (contador     |
|                          | `intentosFallidos`)                                                    |
+--------------------------+------------------------------------------------------------------------+
| Fecha de revisión y      | 10/11/2026                                                             |
| versión, AUTOR:          |                                                                        |
|                          | Versión 1.3                                                            |
|                          |                                                                        |
|                          | Grupo                                                                  |
+--------------------------+------------------------------------------------------------------------+
| Prioridad:               | Media                                                                  |
+--------------------------+------------------------------------------------------------------------+

+--------------------------+------------------------------------------------------------------------+
| Número:                  | **RF-MEN-01**                                                          |
+--------------------------+------------------------------------------------------------------------+
| Título:                  | Acceder a los módulos desde el menú                                    |
+--------------------------+------------------------------------------------------------------------+
| Texto:                   | El menú principal debe mostrar los módulos Libros, Autores, Lectores,  |
|                          | Préstamos y Devoluciones, tanto en la barra de menú como en botones    |
| DEFINICIÓN               | laterales.                                                             |
+--------------------------+------------------------------------------------------------------------+
| Tipo:                    | Funcional                                                              |
+--------------------------+------------------------------------------------------------------------+
| Detalles de requisitos y | Criterio de aceptación: Cada opción abre el panel de su módulo con la  |
| restricciones:           | tabla cargada.                                                         |
|                          |                                                                        |
| ESPECIFICACIÓN           | Implementado en: `MenuPrincipalFrame`                                  |
+--------------------------+------------------------------------------------------------------------+
| Fecha de revisión y      | 10/11/2026                                                             |
| versión, AUTOR:          |                                                                        |
|                          | Versión 1.3                                                            |
|                          |                                                                        |
|                          | Grupo                                                                  |
+--------------------------+------------------------------------------------------------------------+
| Prioridad:               | Alta                                                                   |
+--------------------------+------------------------------------------------------------------------+

+--------------------------+------------------------------------------------------------------------+
| Número:                  | **RF-MEN-02**                                                          |
+--------------------------+------------------------------------------------------------------------+
| Título:                  | Cerrar sesión y salir                                                  |
+--------------------------+------------------------------------------------------------------------+
| Texto:                   | El usuario debe poder cerrar sesión (volver al login) o salir de la    |
|                          | aplicación (Ctrl+Q), en ambos casos con confirmación.                  |
| DEFINICIÓN               |                                                                        |
+--------------------------+------------------------------------------------------------------------+
| Tipo:                    | Funcional                                                              |
+--------------------------+------------------------------------------------------------------------+
| Detalles de requisitos y | Criterio de aceptación: Al responder "Sí" se vuelve al login o se      |
| restricciones:           | cierra la aplicación; al responder "No" se queda en el menú.           |
|                          |                                                                        |
| ESPECIFICACIÓN           | Implementado en: `MenuPrincipalFrame`, `LoginController.cerrarSesion`  |
+--------------------------+------------------------------------------------------------------------+
| Fecha de revisión y      | 10/11/2026                                                             |
| versión, AUTOR:          |                                                                        |
|                          | Versión 1.3                                                            |
|                          |                                                                        |
|                          | Grupo                                                                  |
+--------------------------+------------------------------------------------------------------------+
| Prioridad:               | Media                                                                  |
+--------------------------+------------------------------------------------------------------------+

#### Casos de uso

![Figura 4. Casos de uso de acceso al sistema](diagramas/02_cu_login.png){width=15cm}

##### CU-01 Iniciar sesión
| Elemento | Descripción |
|----------|-------------|
| Actor | Usuario |
| Requerimientos | RF-GEN-01, RF-LOG-01 a RF-LOG-06 |
| Precondición | La base de datos está disponible y el usuario existe. |
| Flujo básico | 1. El sistema muestra la ventana de login. 2. El usuario escribe usuario y contraseña y pulsa **Ingresar**. 3. El sistema valida los campos. 4. El sistema cifra la contraseña con SHA-256 y busca el usuario. 5. El sistema verifica que el usuario esté activo. 6. El sistema guarda la sesión, muestra "Bienvenido, \<nombre\>" y abre el menú principal. |
| Flujos alternos | 3a. Usuario vacío, con espacios o >30 caracteres, o contraseña vacía o <6 caracteres: muestra el error y enfoca el campo. 4a. Credenciales incorrectas: muestra "Usuario o contraseña incorrectos" y suma un intento; al tercer intento muestra un aviso y cierra el sistema. 5a. Usuario inactivo (`invitado`): muestra el mensaje de inactivo. 4b. Falla la conexión: "No se pudo conectar con la base de datos". El usuario pulsa **Salir**: termina la aplicación. |
| Postcondición | Sesión iniciada y menú principal visible. |

##### CU-02 Navegar el menú y cerrar sesión
| Elemento | Descripción |
|----------|-------------|
| Actor | Usuario |
| Requerimientos | RF-MEN-01, RF-MEN-02 |
| Precondición | Sesión iniciada (CU-01). |
| Flujo básico | 1. El usuario elige un módulo en la barra de menú o en los botones laterales. 2. El sistema muestra el panel del módulo con sus datos. |
| Flujos alternos | 1a. **Cerrar sesión**: el sistema pide confirmación, cierra la sesión y vuelve al login. 1b. **Salir** (Ctrl+Q): pide confirmación y cierra la aplicación. 1c. El usuario responde "No": se queda en el menú. |
| Postcondición | Módulo visible, o sesión cerrada. |

#### Interfaces (Pantallas)

[[CAPTURA: Ventana de inicio de sesión]]

[[CAPTURA: Mensaje de usuario o contraseña incorrectos]]

[[CAPTURA: Menú principal]]

### 2.6 Requerimientos no funcionales (Grupal)

Clasificados según las características de calidad de la norma **ISO/IEC 25010**. Cada uno indica un criterio
**medible** y **cómo se verifica**, para que también puedan probarse y no queden como frases generales.

| ID | Característica (ISO 25010) | Requerimiento | Criterio medible | Cómo se verifica | Prioridad |
|----|----------------------------|---------------|------------------|------------------|-----------|
| RNF-01 | Seguridad · Confidencialidad | Las contraseñas no se guardan en texto plano. | 100 % de las contraseñas en la tabla `usuario` son hash SHA-256 (64 caracteres hexadecimales). | Consulta `SELECT contrasena FROM usuario`; revisión de `util.Seguridad`. | Alta |
| RNF-02 | Seguridad · Integridad | Protección contra inyección SQL. | 100 % de las consultas usan `PreparedStatement` con parámetros `?`; ninguna concatena datos del usuario. | Revisión de código de los 6 DAO; login con `' OR '1'='1` debe fallar. | Alta |
| RNF-03 | Seguridad · Autenticidad | Acceso solo con sesión iniciada y límite de intentos. | Ningún módulo se abre sin login; máximo 3 intentos fallidos. | Prueba dinámica del login (CU-01). | Alta |
| RNF-04 | Fiabilidad · Tolerancia a fallos | Los errores no cierran la aplicación. | Ante una falla de base de datos se muestra un mensaje y la aplicación sigue abierta (0 excepciones sin capturar). | Detener MySQL y realizar una operación de cada módulo. | Alta |
| RNF-05 | Fiabilidad · Integridad de datos | Las operaciones que cambian préstamo y stock son atómicas. | Si falla un paso, no queda ningún cambio a medias (transacción con `commit`/`rollback`); el stock nunca es negativo. | Revisión de `PrestamoDAO` y `DevolucionDAO`; claves foráneas y `CHECK` en `bd_biblioteca.sql`. | Alta |
| RNF-06 | Eficiencia de desempeño · Tiempo de respuesta | Las consultas responden rápido. | Listar, buscar y guardar tardan menos de 2 segundos con los datos de prueba. | Medición con cronómetro o `System.currentTimeMillis()` en pruebas dinámicas. | Media |
| RNF-07 | Usabilidad · Protección contra errores del usuario | El sistema guía al usuario cuando se equivoca. | 100 % de los errores de validación muestran un mensaje en español que indica el campo y la regla; el foco va al campo; eliminar, cerrar sesión y salir piden confirmación; valores cerrados (género, estado) se eligen de una lista. | Pruebas dinámicas de cada formulario. | Alta |
| RNF-08 | Usabilidad · Aprendizaje | Interfaz uniforme. | Los 5 módulos tienen los mismos botones (Nuevo, Editar, Eliminar, Buscar, Mostrar todos) en la misma posición; doble clic edita. | Inspección de las capturas en `docs/capturas`. | Media |
| RNF-09 | Mantenibilidad · Modularidad | Arquitectura en capas MVC. | La vista no contiene SQL; cada módulo tiene sus 5 clases (modelo, DAO, controlador, panel, diálogo). | Revisión de paquetes: `grep` de `SELECT` solo en `dao`. | Alta |
| RNF-10 | Mantenibilidad · Analizabilidad | Código fácil de analizar. | Un método `validarXxx` por campo con complejidad ciclomática ≤ 10; 100 % de las clases públicas con Javadoc. | Análisis estático (grafo de flujo y V(G)); `docs/javadoc`. | Media |
| RNF-11 | Mantenibilidad · Capacidad de ser probado | Los controladores se prueban sin base de datos. | Todos los controladores aceptan el DAO por constructor; las validaciones son métodos públicos que devuelven el mensaje o `null`. | Pruebas JUnit con DAO simulado. | Alta |
| RNF-12 | Portabilidad · Instalabilidad | Instalación sencilla. | Java 17+, MySQL 8 y Maven; la base se crea ejecutando un solo script (`database/bd_biblioteca.sql`); solo se configura la contraseña en `util.Conexion`. | Instalar en un equipo nuevo siguiendo el README. | Media |
| RNF-13 | Adecuación funcional · Corrección | Formatos consistentes. | Fechas siempre `dd/MM/yyyy`; montos con 2 decimales (`BigDecimal`). | Pruebas JUnit de `util.Fechas` y `calcularMulta`. | Media |
| RNF-14 | Compatibilidad · Coexistencia | Funciona en Windows con NetBeans o IntelliJ. | La aplicación compila con `mvn package` y se ejecuta desde `vista.SistemaBiblioteca`. | Ejecución en el equipo de cada integrante. | Baja |

---

## 3. Individual (trabajo de cada integrante)

Cada integrante completa su módulo: nombre en el título, requerimientos funcionales (crear, consultar, actualizar y
eliminar), casos de uso y las capturas de sus pantallas.

### 3.1 Módulo Libro - (                            )

#### 3.1.1 Requerimientos funcionales (CRU)

##### Resumen

| ID | Nombre | Tipo | Prioridad |
|----|--------|------|-----------|
| RF-LIB-01 | Registrar libro | Operación (C) | Alta |
| RF-LIB-02 | Listar libros | Operación (R) | Alta |
| RF-LIB-03 | Buscar libros | Operación (R) | Media |
| RF-LIB-04 | Modificar libro | Operación (U) | Alta |
| RF-LIB-05 | Eliminar libro | Operación (D) | Alta |
| RF-LIB-06 | Validar ISBN | Validación | Alta |
| RF-LIB-07 | Controlar ISBN único | Regla de negocio | Alta |
| RF-LIB-08 | Validar título | Validación | Alta |
| RF-LIB-09 | Validar editorial | Validación | Alta |
| RF-LIB-10 | Validar año de publicación | Validación | Alta |
| RF-LIB-11 | Validar género | Validación | Alta |
| RF-LIB-12 | Validar número de páginas | Validación | Media |
| RF-LIB-13 | Validar stock | Validación | Alta |
| RF-LIB-14 | Impedir eliminar libros con préstamos | Regla de negocio | Alta |
| RF-LIB-15 | Exigir un libro seleccionado y existente | Regla de negocio | Media |
| RF-LIB-16 | Informar el resultado de cada operación | Interfaz | Media |

##### Especificación de operaciones

+--------------------------+------------------------------------------------------------------------+
| Número:                  | **RF-LIB-01**                                                          |
+--------------------------+------------------------------------------------------------------------+
| Título:                  | Registrar libro                                                        |
+--------------------------+------------------------------------------------------------------------+
| Texto:                   | El sistema debe permitir registrar un libro nuevo.                     |
|                          |                                                                        |
| DEFINICIÓN               |                                                                        |
+--------------------------+------------------------------------------------------------------------+
| Tipo:                    | Funcional                                                              |
+--------------------------+------------------------------------------------------------------------+
| Detalles de requisitos y | - Entradas: ISBN, título, editorial, año de publicación, género,       |
| restricciones:           |   número de páginas, stock (todos como texto del formulario).          |
|                          |                                                                        |
| ESPECIFICACIÓN           | - Proceso: 1) Valida los campos en orden: ISBN → título → editorial →  |
|                          |   año → género → páginas → stock (RF-LIB-06 a RF-LIB-13). 2) Verifica  |
|                          |   que el ISBN no exista (RF-LIB-07). 3) Inserta el registro en la      |
|                          |   tabla `libro`.                                                       |
|                          |                                                                        |
|                          | - Salidas: "Libro registrado correctamente"; el formulario se cierra y |
|                          |   la tabla se actualiza.                                               |
|                          |                                                                        |
|                          | Criterio de aceptación: Con `9789942000001`, "La vorágine", "Editorial |
|                          | Planeta", `1924`, Novela, `320`, `3` el libro aparece en la tabla con  |
|                          | esos datos.                                                            |
|                          |                                                                        |
|                          | Implementado en: `LibroController.registrarLibro`, `validarCampos`,    |
|                          | `LibroDAO.insertar`                                                    |
+--------------------------+------------------------------------------------------------------------+
| Fecha de revisión y      | 10/11/2026                                                             |
| versión, AUTOR:          |                                                                        |
|                          | Versión 1.3                                                            |
|                          |                                                                        |
|                          | (                    )                                                 |
+--------------------------+------------------------------------------------------------------------+
| Prioridad:               | Alta                                                                   |
+--------------------------+------------------------------------------------------------------------+

+--------------------------+------------------------------------------------------------------------+
| Número:                  | **RF-LIB-02**                                                          |
+--------------------------+------------------------------------------------------------------------+
| Título:                  | Listar libros                                                          |
+--------------------------+------------------------------------------------------------------------+
| Texto:                   | El sistema debe mostrar todos los libros registrados al abrir el       |
|                          | módulo y al pulsar "Mostrar todos".                                    |
| DEFINICIÓN               |                                                                        |
+--------------------------+------------------------------------------------------------------------+
| Tipo:                    | Funcional                                                              |
+--------------------------+------------------------------------------------------------------------+
| Detalles de requisitos y | - Salidas: Tabla con id, ISBN, título, editorial, año, género, páginas |
| restricciones:           |   y stock.                                                             |
|                          |                                                                        |
| ESPECIFICACIÓN           | Criterio de aceptación: Con el script de datos de prueba recién        |
|                          | ejecutado se muestran los 15 libros.                                   |
|                          |                                                                        |
|                          | Implementado en: `LibroController.listarLibros`, `LibroDAO.listar`     |
+--------------------------+------------------------------------------------------------------------+
| Fecha de revisión y      | 10/11/2026                                                             |
| versión, AUTOR:          |                                                                        |
|                          | Versión 1.3                                                            |
|                          |                                                                        |
|                          | (                    )                                                 |
+--------------------------+------------------------------------------------------------------------+
| Prioridad:               | Alta                                                                   |
+--------------------------+------------------------------------------------------------------------+

+--------------------------+------------------------------------------------------------------------+
| Número:                  | **RF-LIB-03**                                                          |
+--------------------------+------------------------------------------------------------------------+
| Título:                  | Buscar libros                                                          |
+--------------------------+------------------------------------------------------------------------+
| Texto:                   | El sistema debe filtrar los libros cuyo ISBN, título, editorial o      |
|                          | género contengan el texto escrito (coincidencia parcial).              |
| DEFINICIÓN               |                                                                        |
+--------------------------+------------------------------------------------------------------------+
| Tipo:                    | Funcional                                                              |
+--------------------------+------------------------------------------------------------------------+
| Detalles de requisitos y | - Entradas: Texto de búsqueda.                                         |
| restricciones:           |                                                                        |
|                          | - Proceso: Si el texto está vacío lista todos (RF-LIB-02); si no,      |
| ESPECIFICACIÓN           |   consulta con `LIKE %texto%`.                                         |
|                          |                                                                        |
|                          | - Salidas: Tabla con las coincidencias; vacía si no hay ninguna.       |
|                          |                                                                        |
|                          | Criterio de aceptación: Buscar "Novela" muestra solo libros de ese     |
|                          | género; buscar "zzz" deja la tabla vacía; texto vacío muestra los 15.  |
|                          |                                                                        |
|                          | Implementado en: `LibroController.buscarLibros`, `LibroDAO.buscar`     |
+--------------------------+------------------------------------------------------------------------+
| Fecha de revisión y      | 10/11/2026                                                             |
| versión, AUTOR:          |                                                                        |
|                          | Versión 1.3                                                            |
|                          |                                                                        |
|                          | (                    )                                                 |
+--------------------------+------------------------------------------------------------------------+
| Prioridad:               | Media                                                                  |
+--------------------------+------------------------------------------------------------------------+

+--------------------------+------------------------------------------------------------------------+
| Número:                  | **RF-LIB-04**                                                          |
+--------------------------+------------------------------------------------------------------------+
| Título:                  | Modificar libro                                                        |
+--------------------------+------------------------------------------------------------------------+
| Texto:                   | El sistema debe permitir cambiar los datos de un libro seleccionado    |
|                          | (botón Editar o doble clic).                                           |
| DEFINICIÓN               |                                                                        |
+--------------------------+------------------------------------------------------------------------+
| Tipo:                    | Funcional                                                              |
+--------------------------+------------------------------------------------------------------------+
| Detalles de requisitos y | - Entradas: Id del libro seleccionado y los mismos campos de           |
| restricciones:           |   RF-LIB-01.                                                           |
|                          |                                                                        |
| ESPECIFICACIÓN           | - Proceso: 1) Verifica que haya un libro seleccionado (RF-LIB-15). 2)  |
|                          |   Valida los campos igual que al registrar. 3) Verifica que el ISBN no |
|                          |   pertenezca a **otro** libro. 4) Actualiza el registro.               |
|                          |                                                                        |
|                          | - Salidas: "Libro actualizado correctamente"; la tabla muestra los     |
|                          |   nuevos datos.                                                        |
|                          |                                                                        |
|                          | Criterio de aceptación: Cambiar solo el stock de un libro conserva su  |
|                          | ISBN y no da error de ISBN repetido.                                   |
|                          |                                                                        |
|                          | Implementado en: `LibroController.actualizarLibro`,                    |
|                          | `LibroDAO.actualizar`                                                  |
+--------------------------+------------------------------------------------------------------------+
| Fecha de revisión y      | 10/11/2026                                                             |
| versión, AUTOR:          |                                                                        |
|                          | Versión 1.3                                                            |
|                          |                                                                        |
|                          | (                    )                                                 |
+--------------------------+------------------------------------------------------------------------+
| Prioridad:               | Alta                                                                   |
+--------------------------+------------------------------------------------------------------------+

+--------------------------+------------------------------------------------------------------------+
| Número:                  | **RF-LIB-05**                                                          |
+--------------------------+------------------------------------------------------------------------+
| Título:                  | Eliminar libro                                                         |
+--------------------------+------------------------------------------------------------------------+
| Texto:                   | El sistema debe permitir eliminar un libro seleccionado después de     |
|                          | pedir confirmación.                                                    |
| DEFINICIÓN               |                                                                        |
+--------------------------+------------------------------------------------------------------------+
| Tipo:                    | Funcional                                                              |
+--------------------------+------------------------------------------------------------------------+
| Detalles de requisitos y | - Proceso: 1) Verifica selección y existencia (RF-LIB-15). 2) Verifica |
| restricciones:           |   que no tenga préstamos (RF-LIB-14). 3) Borra el registro.            |
|                          |                                                                        |
| ESPECIFICACIÓN           | - Salidas: "Libro eliminado correctamente"; el libro desaparece de la  |
|                          |   tabla. Si el usuario responde "No" en la confirmación, no pasa nada. |
|                          |                                                                        |
|                          | Criterio de aceptación: El libro id 9 (sin préstamos) se elimina.      |
|                          |                                                                        |
|                          | Implementado en: `LibroController.eliminarLibro`, `LibroDAO.eliminar`  |
+--------------------------+------------------------------------------------------------------------+
| Fecha de revisión y      | 10/11/2026                                                             |
| versión, AUTOR:          |                                                                        |
|                          | Versión 1.3                                                            |
|                          |                                                                        |
|                          | (                    )                                                 |
+--------------------------+------------------------------------------------------------------------+
| Prioridad:               | Alta                                                                   |
+--------------------------+------------------------------------------------------------------------+

##### Especificación de validaciones y reglas

En cada ficha, los mensajes entre comillas son el resultado esperado exacto para las pruebas.

+--------------------------+------------------------------------------------------------------------+
| Número:                  | **RF-LIB-06**                                                          |
+--------------------------+------------------------------------------------------------------------+
| Título:                  | Validar ISBN                                                           |
+--------------------------+------------------------------------------------------------------------+
| Texto:                   | El sistema validará que el ISBN ingresado sea obligatorio, numérico y  |
|                          | de 10 o 13 dígitos antes de registrar o modificar un libro.            |
| DEFINICIÓN               |                                                                        |
+--------------------------+------------------------------------------------------------------------+
| Tipo:                    | Funcional                                                              |
+--------------------------+------------------------------------------------------------------------+
| Detalles de requisitos y | - Regla: Obligatorio; solo dígitos (sin guiones ni espacios);          |
| restricciones:           |   exactamente 10 o 13 dígitos.                                         |
|                          |                                                                        |
| ESPECIFICACIÓN           | Condiciones y mensaje que muestra el sistema:                          |
|                          |                                                                        |
|                          | - Vacío o solo espacios (ej. `""`, `"   "`): El ISBN es obligatorio    |
|                          |                                                                        |
|                          | - Contiene letras, guiones o espacios internos (ej. `978-9942`,        |
|                          |   `12345abcde`): El ISBN solo debe contener números (sin guiones ni    |
|                          |   espacios)                                                            |
|                          |                                                                        |
|                          | - Longitud distinta de 10 y 13 (ej. `123456789` (9), `12345678901`     |
|                          |   (11)): El ISBN debe tener 10 o 13 dígitos                            |
|                          |                                                                        |
|                          | - Válido (ej. `1234567890`, `9789942000001`): se acepta                |
|                          |                                                                        |
|                          | Implementado en: `LibroController.validarIsbn`                         |
+--------------------------+------------------------------------------------------------------------+
| Fecha de revisión y      | 10/11/2026                                                             |
| versión, AUTOR:          |                                                                        |
|                          | Versión 1.3                                                            |
|                          |                                                                        |
|                          | (                    )                                                 |
+--------------------------+------------------------------------------------------------------------+
| Prioridad:               | Alta                                                                   |
+--------------------------+------------------------------------------------------------------------+

+--------------------------+------------------------------------------------------------------------+
| Número:                  | **RF-LIB-07**                                                          |
+--------------------------+------------------------------------------------------------------------+
| Título:                  | Controlar ISBN único                                                   |
+--------------------------+------------------------------------------------------------------------+
| Texto:                   | El sistema no permitirá registrar ni modificar un libro con un ISBN    |
|                          | que ya pertenezca a otro libro.                                        |
| DEFINICIÓN               |                                                                        |
+--------------------------+------------------------------------------------------------------------+
| Tipo:                    | Funcional                                                              |
+--------------------------+------------------------------------------------------------------------+
| Detalles de requisitos y | - Regla: No puede haber dos libros con el mismo ISBN. Al modificar se  |
| restricciones:           |   excluye el propio libro.                                             |
|                          |                                                                        |
| ESPECIFICACIÓN           | - Mensaje: Al registrar "Ya existe un libro registrado con ese ISBN";  |
|                          |   al modificar "Ya existe otro libro registrado con ese ISBN".         |
|                          |                                                                        |
|                          | Implementado en: `LibroDAO.existeIsbn`                                 |
+--------------------------+------------------------------------------------------------------------+
| Fecha de revisión y      | 10/11/2026                                                             |
| versión, AUTOR:          |                                                                        |
|                          | Versión 1.3                                                            |
|                          |                                                                        |
|                          | (                    )                                                 |
+--------------------------+------------------------------------------------------------------------+
| Prioridad:               | Alta                                                                   |
+--------------------------+------------------------------------------------------------------------+

+--------------------------+------------------------------------------------------------------------+
| Número:                  | **RF-LIB-08**                                                          |
+--------------------------+------------------------------------------------------------------------+
| Título:                  | Validar título                                                         |
+--------------------------+------------------------------------------------------------------------+
| Texto:                   | El sistema validará que el título del libro sea obligatorio y tenga    |
|                          | entre 2 y 150 caracteres.                                              |
| DEFINICIÓN               |                                                                        |
+--------------------------+------------------------------------------------------------------------+
| Tipo:                    | Funcional                                                              |
+--------------------------+------------------------------------------------------------------------+
| Detalles de requisitos y | Condiciones y mensaje que muestra el sistema:                          |
| restricciones:           |                                                                        |
|                          | - Vacío: "El título es obligatorio"                                    |
| ESPECIFICACIÓN           |                                                                        |
|                          | - Menos de 2 caracteres: "El título debe tener al menos 2 caracteres"  |
|                          |                                                                        |
|                          | - Más de 150 caracteres: "El título no puede superar 150 caracteres"   |
|                          |                                                                        |
|                          | Valores límite: 1 ✗, 2 ✓, 150 ✓, 151 ✗.                                |
|                          |                                                                        |
|                          | Implementado en: `validarTitulo`                                       |
+--------------------------+------------------------------------------------------------------------+
| Fecha de revisión y      | 10/11/2026                                                             |
| versión, AUTOR:          |                                                                        |
|                          | Versión 1.3                                                            |
|                          |                                                                        |
|                          | (                    )                                                 |
+--------------------------+------------------------------------------------------------------------+
| Prioridad:               | Alta                                                                   |
+--------------------------+------------------------------------------------------------------------+

+--------------------------+------------------------------------------------------------------------+
| Número:                  | **RF-LIB-09**                                                          |
+--------------------------+------------------------------------------------------------------------+
| Título:                  | Validar editorial                                                      |
+--------------------------+------------------------------------------------------------------------+
| Texto:                   | El sistema validará que la editorial sea obligatoria, tenga entre 2 y  |
|                          | 100 caracteres y solo contenga caracteres permitidos.                  |
| DEFINICIÓN               |                                                                        |
+--------------------------+------------------------------------------------------------------------+
| Tipo:                    | Funcional                                                              |
+--------------------------+------------------------------------------------------------------------+
| Detalles de requisitos y | Condiciones y mensaje que muestra el sistema:                          |
| restricciones:           |                                                                        |
|                          | - Vacía: "La editorial es obligatoria"                                 |
| ESPECIFICACIÓN           |                                                                        |
|                          | - Caracteres distintos de letras, números, espacios y `. , & ' -`: "La |
|                          |   editorial contiene caracteres no permitidos"                         |
|                          |                                                                        |
|                          | - Menos de 2 o más de 100 caracteres: "La editorial debe tener entre 2 |
|                          |   y 100 caracteres"                                                    |
|                          |                                                                        |
|                          | Valores límite: 1 ✗, 2 ✓, 100 ✓, 101 ✗. Ejemplo inválido:              |
|                          | `Editorial#1`.                                                         |
|                          |                                                                        |
|                          | Implementado en: `validarEditorial`                                    |
+--------------------------+------------------------------------------------------------------------+
| Fecha de revisión y      | 10/11/2026                                                             |
| versión, AUTOR:          |                                                                        |
|                          | Versión 1.3                                                            |
|                          |                                                                        |
|                          | (                    )                                                 |
+--------------------------+------------------------------------------------------------------------+
| Prioridad:               | Alta                                                                   |
+--------------------------+------------------------------------------------------------------------+

+--------------------------+------------------------------------------------------------------------+
| Número:                  | **RF-LIB-10**                                                          |
+--------------------------+------------------------------------------------------------------------+
| Título:                  | Validar año de publicación                                             |
+--------------------------+------------------------------------------------------------------------+
| Texto:                   | El sistema validará que el año de publicación sea obligatorio,         |
|                          | numérico y esté entre 1450 y el año actual.                            |
| DEFINICIÓN               |                                                                        |
+--------------------------+------------------------------------------------------------------------+
| Tipo:                    | Funcional                                                              |
+--------------------------+------------------------------------------------------------------------+
| Detalles de requisitos y | Condiciones y mensaje que muestra el sistema:                          |
| restricciones:           |                                                                        |
|                          | - Vacío: "El año de publicación es obligatorio"                        |
| ESPECIFICACIÓN           |                                                                        |
|                          | - No numérico o más de 4 dígitos: "El año de publicación debe ser un   |
|                          |   número de hasta 4 dígitos"                                           |
|                          |                                                                        |
|                          | - Menor a 1450: "El año de publicación no puede ser menor a 1450"      |
|                          |                                                                        |
|                          | - Mayor al año actual: "El año de publicación no puede ser mayor al    |
|                          |   año actual"                                                          |
|                          |                                                                        |
|                          | Valores límite: 1449 ✗, 1450 ✓, año actual ✓, año actual + 1 ✗.        |
|                          |                                                                        |
|                          | Implementado en: `validarAnioPublicacion`                              |
+--------------------------+------------------------------------------------------------------------+
| Fecha de revisión y      | 10/11/2026                                                             |
| versión, AUTOR:          |                                                                        |
|                          | Versión 1.3                                                            |
|                          |                                                                        |
|                          | (                    )                                                 |
+--------------------------+------------------------------------------------------------------------+
| Prioridad:               | Alta                                                                   |
+--------------------------+------------------------------------------------------------------------+

+--------------------------+------------------------------------------------------------------------+
| Número:                  | **RF-LIB-11**                                                          |
+--------------------------+------------------------------------------------------------------------+
| Título:                  | Validar género                                                         |
+--------------------------+------------------------------------------------------------------------+
| Texto:                   | El sistema permitirá elegir el género del libro solo de una lista de   |
|                          | valores permitidos.                                                    |
| DEFINICIÓN               |                                                                        |
+--------------------------+------------------------------------------------------------------------+
| Tipo:                    | Funcional                                                              |
+--------------------------+------------------------------------------------------------------------+
| Detalles de requisitos y | - Regla: Uno de Novela, Cuento, Poesía, Ciencia, Historia, Tecnología, |
| restricciones:           |   Infantil, Otro (lista desplegable).                                  |
|                          |                                                                        |
| ESPECIFICACIÓN           | Condiciones y mensaje que muestra el sistema:                          |
|                          |                                                                        |
|                          | - Sin seleccionar: "Debe seleccionar un género"                        |
|                          |                                                                        |
|                          | - Valor fuera de la lista: "El género seleccionado no es válido"       |
|                          |                                                                        |
|                          | Implementado en: `validarGenero`                                       |
+--------------------------+------------------------------------------------------------------------+
| Fecha de revisión y      | 10/11/2026                                                             |
| versión, AUTOR:          |                                                                        |
|                          | Versión 1.3                                                            |
|                          |                                                                        |
|                          | (                    )                                                 |
+--------------------------+------------------------------------------------------------------------+
| Prioridad:               | Alta                                                                   |
+--------------------------+------------------------------------------------------------------------+

+--------------------------+------------------------------------------------------------------------+
| Número:                  | **RF-LIB-12**                                                          |
+--------------------------+------------------------------------------------------------------------+
| Título:                  | Validar número de páginas                                              |
+--------------------------+------------------------------------------------------------------------+
| Texto:                   | El sistema validará que el número de páginas sea obligatorio y un      |
|                          | entero entre 1 y 5000.                                                 |
| DEFINICIÓN               |                                                                        |
+--------------------------+------------------------------------------------------------------------+
| Tipo:                    | Funcional                                                              |
+--------------------------+------------------------------------------------------------------------+
| Detalles de requisitos y | Condiciones y mensaje que muestra el sistema:                          |
| restricciones:           |                                                                        |
|                          | - Vacío: "El número de páginas es obligatorio"                         |
| ESPECIFICACIÓN           |                                                                        |
|                          | - No entero, negativo o más de 4 dígitos: "El número de páginas debe   |
|                          |   ser un número entero"                                                |
|                          |                                                                        |
|                          | - Menor a 1 o mayor a 5000: "El número de páginas debe estar entre 1 y |
|                          |   5000"                                                                |
|                          |                                                                        |
|                          | Valores límite: 0 ✗, 1 ✓, 5000 ✓, 5001 ✗.                              |
|                          |                                                                        |
|                          | Implementado en: `validarNumeroPaginas`                                |
+--------------------------+------------------------------------------------------------------------+
| Fecha de revisión y      | 10/11/2026                                                             |
| versión, AUTOR:          |                                                                        |
|                          | Versión 1.3                                                            |
|                          |                                                                        |
|                          | (                    )                                                 |
+--------------------------+------------------------------------------------------------------------+
| Prioridad:               | Media                                                                  |
+--------------------------+------------------------------------------------------------------------+

+--------------------------+------------------------------------------------------------------------+
| Número:                  | **RF-LIB-13**                                                          |
+--------------------------+------------------------------------------------------------------------+
| Título:                  | Validar stock                                                          |
+--------------------------+------------------------------------------------------------------------+
| Texto:                   | El sistema validará que el stock sea obligatorio y un entero entre 0 y |
|                          | 500 ejemplares.                                                        |
| DEFINICIÓN               |                                                                        |
+--------------------------+------------------------------------------------------------------------+
| Tipo:                    | Funcional                                                              |
+--------------------------+------------------------------------------------------------------------+
| Detalles de requisitos y | Condiciones y mensaje que muestra el sistema:                          |
| restricciones:           |                                                                        |
|                          | - Vacío: "El stock es obligatorio"                                     |
| ESPECIFICACIÓN           |                                                                        |
|                          | - No entero, negativo o más de 3 dígitos: "El stock debe ser un número |
|                          |   entero no negativo"                                                  |
|                          |                                                                        |
|                          | - Mayor a 500: "El stock no puede superar 500 ejemplares"              |
|                          |                                                                        |
|                          | Valores límite: -1 ✗, 0 ✓, 500 ✓, 501 ✗.                               |
|                          |                                                                        |
|                          | Implementado en: `validarStock`                                        |
+--------------------------+------------------------------------------------------------------------+
| Fecha de revisión y      | 10/11/2026                                                             |
| versión, AUTOR:          |                                                                        |
|                          | Versión 1.3                                                            |
|                          |                                                                        |
|                          | (                    )                                                 |
+--------------------------+------------------------------------------------------------------------+
| Prioridad:               | Alta                                                                   |
+--------------------------+------------------------------------------------------------------------+

+--------------------------+------------------------------------------------------------------------+
| Número:                  | **RF-LIB-14**                                                          |
+--------------------------+------------------------------------------------------------------------+
| Título:                  | Impedir eliminar libros con préstamos                                  |
+--------------------------+------------------------------------------------------------------------+
| Texto:                   | El sistema no permitirá eliminar un libro que tenga préstamos          |
|                          | registrados.                                                           |
| DEFINICIÓN               |                                                                        |
+--------------------------+------------------------------------------------------------------------+
| Tipo:                    | Funcional                                                              |
+--------------------------+------------------------------------------------------------------------+
| Detalles de requisitos y | - Regla: Un libro con al menos un préstamo (de cualquier estado) no se |
| restricciones:           |   elimina.                                                             |
|                          |                                                                        |
| ESPECIFICACIÓN           | - Mensaje: "No se puede eliminar: el libro tiene préstamos             |
|                          |   registrados".                                                        |
|                          |                                                                        |
|                          | Criterio de aceptación: El libro id 1 no se elimina; el id 9 sí.       |
|                          |                                                                        |
|                          | Implementado en: `LibroDAO.tienePrestamos`                             |
+--------------------------+------------------------------------------------------------------------+
| Fecha de revisión y      | 10/11/2026                                                             |
| versión, AUTOR:          |                                                                        |
|                          | Versión 1.3                                                            |
|                          |                                                                        |
|                          | (                    )                                                 |
+--------------------------+------------------------------------------------------------------------+
| Prioridad:               | Alta                                                                   |
+--------------------------+------------------------------------------------------------------------+

+--------------------------+------------------------------------------------------------------------+
| Número:                  | **RF-LIB-15**                                                          |
+--------------------------+------------------------------------------------------------------------+
| Título:                  | Exigir un libro seleccionado y existente                               |
+--------------------------+------------------------------------------------------------------------+
| Texto:                   | El sistema exigirá que haya un libro seleccionado y que exista en la   |
|                          | base de datos para modificarlo o eliminarlo.                           |
| DEFINICIÓN               |                                                                        |
+--------------------------+------------------------------------------------------------------------+
| Tipo:                    | Funcional                                                              |
+--------------------------+------------------------------------------------------------------------+
| Detalles de requisitos y | Condiciones y mensaje que muestra el sistema:                          |
| restricciones:           |                                                                        |
|                          | - Ningún libro seleccionado (id ≤ 0) al modificar: "Debe seleccionar   |
| ESPECIFICACIÓN           |   un libro para modificar"                                             |
|                          |                                                                        |
|                          | - Ningún libro seleccionado (id ≤ 0) al eliminar: "Debe seleccionar un |
|                          |   libro para eliminar"                                                 |
|                          |                                                                        |
|                          | - El libro fue borrado antes de modificarlo: "El libro ya no existe en |
|                          |   la base de datos"                                                    |
|                          |                                                                        |
|                          | - El libro no existe al eliminar: "El libro no existe"                 |
|                          |                                                                        |
|                          | Implementado en: `actualizarLibro`, `eliminarLibro`                    |
+--------------------------+------------------------------------------------------------------------+
| Fecha de revisión y      | 10/11/2026                                                             |
| versión, AUTOR:          |                                                                        |
|                          | Versión 1.3                                                            |
|                          |                                                                        |
|                          | (                    )                                                 |
+--------------------------+------------------------------------------------------------------------+
| Prioridad:               | Media                                                                  |
+--------------------------+------------------------------------------------------------------------+

+--------------------------+------------------------------------------------------------------------+
| Número:                  | **RF-LIB-16**                                                          |
+--------------------------+------------------------------------------------------------------------+
| Título:                  | Informar el resultado de cada operación                                |
+--------------------------+------------------------------------------------------------------------+
| Texto:                   | Después de guardar o eliminar, el sistema debe mostrar un mensaje de   |
|                          | éxito o de error; si el error es de un campo, el cursor se coloca en   |
| DEFINICIÓN               | ese campo y el formulario no se cierra.                                |
+--------------------------+------------------------------------------------------------------------+
| Tipo:                    | Funcional                                                              |
+--------------------------+------------------------------------------------------------------------+
| Detalles de requisitos y | Implementado en: `util.Respuesta`, `LibroDialog`                       |
| restricciones:           |                                                                        |
|                          |                                                                        |
| ESPECIFICACIÓN           |                                                                        |
+--------------------------+------------------------------------------------------------------------+
| Fecha de revisión y      | 10/11/2026                                                             |
| versión, AUTOR:          |                                                                        |
|                          | Versión 1.3                                                            |
|                          |                                                                        |
|                          | (                    )                                                 |
+--------------------------+------------------------------------------------------------------------+
| Prioridad:               | Media                                                                  |
+--------------------------+------------------------------------------------------------------------+

#### 3.1.2 Casos de uso

![Figura 5. Casos de uso del módulo Libro](diagramas/03_cu_libro.png){width=16cm}

##### CU-03 Registrar libro
| Elemento | Descripción |
|----------|-------------|
| Actor | Usuario |
| Requerimientos | RF-LIB-01, RF-LIB-06 a RF-LIB-13, RF-LIB-16 |
| Precondición | Sesión iniciada; el usuario está en el módulo Libros. |
| Flujo básico | 1. El usuario pulsa **Nuevo**. 2. El sistema abre el formulario vacío. 3. El usuario ingresa ISBN, título, editorial, año, género, páginas y stock y pulsa **Guardar**. 4. El sistema valida los campos en orden (ISBN → título → editorial → año → género → páginas → stock). 5. El sistema verifica que el ISBN no exista. 6. El sistema guarda el libro. 7. El sistema muestra "Libro registrado correctamente", cierra el formulario y actualiza la tabla. |
| Flujos alternos | 4a. Un campo no cumple su regla: muestra el mensaje del campo (RF-LIB-06 a RF-LIB-13), enfoca ese campo y vuelve al paso 3. 5a. ISBN repetido: "Ya existe un libro registrado con ese ISBN" y vuelve al paso 3. 6a. Error de base de datos: "Error de base de datos: …". 3a. El usuario pulsa **Cancelar**: se cierra el formulario sin guardar. |
| Postcondición | El libro queda registrado en la tabla `libro`. |

##### CU-04 Buscar y listar libros
| Elemento | Descripción |
|----------|-------------|
| Actor | Usuario |
| Requerimientos | RF-LIB-02, RF-LIB-03 |
| Precondición | Sesión iniciada; módulo Libros abierto. |
| Flujo básico | 1. El sistema muestra todos los libros al abrir el módulo. 2. El usuario escribe un texto y pulsa **Buscar**. 3. El sistema muestra los libros cuyo ISBN, título, editorial o género contienen el texto. |
| Flujos alternos | 2a. Texto vacío o **Mostrar todos**: muestra todos los libros. 3a. No hay coincidencias: la tabla queda vacía. |
| Postcondición | La tabla muestra el resultado. |

##### CU-05 Modificar libro
| Elemento | Descripción |
|----------|-------------|
| Actor | Usuario |
| Requerimientos | RF-LIB-04, RF-LIB-06 a RF-LIB-13, RF-LIB-15, RF-LIB-16 |
| Precondición | Sesión iniciada; hay al menos un libro. |
| Flujo básico | 1. El usuario selecciona un libro y pulsa **Editar** (o doble clic). 2. El sistema abre el formulario con los datos actuales. 3. El usuario cambia los datos y pulsa **Guardar**. 4. El sistema valida los campos (igual que CU-03). 5. El sistema verifica que el ISBN no pertenezca a **otro** libro. 6. El sistema actualiza y muestra "Libro actualizado correctamente". |
| Flujos alternos | 1a. No hay libro seleccionado: "Debe seleccionar un libro para modificar". 4a. Dato inválido: muestra el error del campo. 5a. ISBN de otro libro: "Ya existe otro libro registrado con ese ISBN". 6a. El libro fue borrado: "El libro ya no existe en la base de datos". |
| Postcondición | Los datos del libro quedan actualizados. |

##### CU-06 Eliminar libro
| Elemento | Descripción |
|----------|-------------|
| Actor | Usuario |
| Requerimientos | RF-LIB-05, RF-LIB-14, RF-LIB-15 |
| Precondición | Sesión iniciada; hay al menos un libro. |
| Flujo básico | 1. El usuario selecciona un libro y pulsa **Eliminar**. 2. El sistema pide confirmación. 3. El usuario confirma. 4. El sistema verifica que el libro exista y no tenga préstamos. 5. El sistema lo elimina y muestra "Libro eliminado correctamente". |
| Flujos alternos | 1a. Sin selección: "Debe seleccionar un libro para eliminar". 3a. El usuario cancela: no se elimina. 4a. No existe: "El libro no existe". 4b. Tiene préstamos: "No se puede eliminar: el libro tiene préstamos registrados" (p. ej. libros 1–8, 11, 12 del script). |
| Postcondición | El libro ya no aparece en la tabla. |

#### 3.1.3 Interfaces (Pantallas)

Capturas de las pantallas del módulo funcionando. Pegue cada captura en el cuadro correspondiente.

[[CAPTURA: Pantalla principal del módulo Libro (tabla con los libros y búsqueda)]]

[[CAPTURA: Formulario Nuevo libro con datos válidos]]

[[CAPTURA: Mensaje de error de validación (campo enfocado)]]

[[CAPTURA: Mensaje de éxito al guardar / confirmación al eliminar]]

#### 3.1.4 Matriz de trazabilidad

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
| RF-LOG-01 a RF-LOG-06 | CU-01 | `LoginController.iniciarSesion` | admin/Admin123 (éxito), clave errónea ×3, `invitado` inactivo |

---

### 3.2 Módulo Autor - (                            )

#### 3.2.1 Requerimientos funcionales (CRU)

##### Resumen

| ID | Nombre | Prioridad |
|----|--------|-----------|
| RF-AUT-01 | Registrar autor | Alta |
| RF-AUT-02 | Listar y buscar autores | Media |
| RF-AUT-03 | Modificar autor | Alta |
| RF-AUT-04 | Eliminar autor | Alta |
| RF-AUT-05 | Validar nombres y apellidos | Alta |
| RF-AUT-06 | Validar nacionalidad | Alta |
| RF-AUT-07 | Validar fecha de nacimiento | Media |
| RF-AUT-08 | Validar biografía | Baja |
| RF-AUT-09 | Controlar autor duplicado | Alta |

##### Especificación

+--------------------------+------------------------------------------------------------------------+
| Número:                  | **RF-AUT-01**                                                          |
+--------------------------+------------------------------------------------------------------------+
| Título:                  | Registrar autor                                                        |
+--------------------------+------------------------------------------------------------------------+
| Texto:                   | El sistema permitirá el ingreso de los datos de un autor nuevo         |
|                          | (nombres, apellidos, nacionalidad, fecha de nacimiento y biografía) y  |
| DEFINICIÓN               | lo guardará en la base de datos.                                       |
+--------------------------+------------------------------------------------------------------------+
| Tipo:                    | Funcional                                                              |
+--------------------------+------------------------------------------------------------------------+
| Detalles de requisitos y | - Regla: Guarda un autor nuevo con nombres, apellidos, nacionalidad,   |
| restricciones:           |   fecha de nacimiento y biografía.                                     |
|                          |                                                                        |
| ESPECIFICACIÓN           | - Mensaje de éxito: "Autor registrado correctamente"                   |
|                          |                                                                        |
|                          | Implementado en: `AutorController.registrarAutor`, `AutorDAO.insertar` |
+--------------------------+------------------------------------------------------------------------+
| Fecha de revisión y      | 10/11/2026                                                             |
| versión, AUTOR:          |                                                                        |
|                          | Versión 1.3                                                            |
|                          |                                                                        |
|                          | (                    )                                                 |
+--------------------------+------------------------------------------------------------------------+
| Prioridad:               | Alta                                                                   |
+--------------------------+------------------------------------------------------------------------+

+--------------------------+------------------------------------------------------------------------+
| Número:                  | **RF-AUT-02**                                                          |
+--------------------------+------------------------------------------------------------------------+
| Título:                  | Listar y buscar autores                                                |
+--------------------------+------------------------------------------------------------------------+
| Texto:                   | El sistema mostrará todos los autores registrados y permitirá          |
|                          | buscarlos por nombres, apellidos o nacionalidad.                       |
| DEFINICIÓN               |                                                                        |
+--------------------------+------------------------------------------------------------------------+
| Tipo:                    | Funcional                                                              |
+--------------------------+------------------------------------------------------------------------+
| Detalles de requisitos y | - Regla: Muestra todos o filtra por nombres, apellidos o nacionalidad. |
| restricciones:           |                                                                        |
|                          | Implementado en: `AutorController.listarAutores`, `buscarAutores`,     |
| ESPECIFICACIÓN           | `AutorDAO.buscar`                                                      |
+--------------------------+------------------------------------------------------------------------+
| Fecha de revisión y      | 10/11/2026                                                             |
| versión, AUTOR:          |                                                                        |
|                          | Versión 1.3                                                            |
|                          |                                                                        |
|                          | (                    )                                                 |
+--------------------------+------------------------------------------------------------------------+
| Prioridad:               | Media                                                                  |
+--------------------------+------------------------------------------------------------------------+

+--------------------------+------------------------------------------------------------------------+
| Número:                  | **RF-AUT-03**                                                          |
+--------------------------+------------------------------------------------------------------------+
| Título:                  | Modificar autor                                                        |
+--------------------------+------------------------------------------------------------------------+
| Texto:                   | El sistema permitirá modificar los datos del autor seleccionado.       |
|                          |                                                                        |
| DEFINICIÓN               |                                                                        |
+--------------------------+------------------------------------------------------------------------+
| Tipo:                    | Funcional                                                              |
+--------------------------+------------------------------------------------------------------------+
| Detalles de requisitos y | - Regla: Cambia los datos del autor seleccionado.                      |
| restricciones:           |                                                                        |
|                          | Mensajes de error que muestra el sistema:                              |
| ESPECIFICACIÓN           |                                                                        |
|                          | - "Debe seleccionar un autor para modificar"                           |
|                          |                                                                        |
|                          | Implementado en: `AutorController.actualizarAutor`,                    |
|                          | `AutorDAO.actualizar`                                                  |
+--------------------------+------------------------------------------------------------------------+
| Fecha de revisión y      | 10/11/2026                                                             |
| versión, AUTOR:          |                                                                        |
|                          | Versión 1.3                                                            |
|                          |                                                                        |
|                          | (                    )                                                 |
+--------------------------+------------------------------------------------------------------------+
| Prioridad:               | Alta                                                                   |
+--------------------------+------------------------------------------------------------------------+

+--------------------------+------------------------------------------------------------------------+
| Número:                  | **RF-AUT-04**                                                          |
+--------------------------+------------------------------------------------------------------------+
| Título:                  | Eliminar autor                                                         |
+--------------------------+------------------------------------------------------------------------+
| Texto:                   | El sistema permitirá eliminar el autor seleccionado, previa            |
|                          | confirmación.                                                          |
| DEFINICIÓN               |                                                                        |
+--------------------------+------------------------------------------------------------------------+
| Tipo:                    | Funcional                                                              |
+--------------------------+------------------------------------------------------------------------+
| Detalles de requisitos y | - Regla: Borra el autor seleccionado con confirmación.                 |
| restricciones:           |                                                                        |
|                          | Mensajes de error que muestra el sistema:                              |
| ESPECIFICACIÓN           |                                                                        |
|                          | - "Debe seleccionar un autor para eliminar"                            |
|                          |                                                                        |
|                          | - "El autor no existe"                                                 |
|                          |                                                                        |
|                          | Implementado en: `AutorController.eliminarAutor`, `AutorDAO.eliminar`  |
+--------------------------+------------------------------------------------------------------------+
| Fecha de revisión y      | 10/11/2026                                                             |
| versión, AUTOR:          |                                                                        |
|                          | Versión 1.3                                                            |
|                          |                                                                        |
|                          | (                    )                                                 |
+--------------------------+------------------------------------------------------------------------+
| Prioridad:               | Alta                                                                   |
+--------------------------+------------------------------------------------------------------------+

+--------------------------+------------------------------------------------------------------------+
| Número:                  | **RF-AUT-05**                                                          |
+--------------------------+------------------------------------------------------------------------+
| Título:                  | Validar nombres y apellidos                                            |
+--------------------------+------------------------------------------------------------------------+
| Texto:                   | El sistema validará los nombres y apellidos del autor.                 |
|                          |                                                                        |
| DEFINICIÓN               |                                                                        |
+--------------------------+------------------------------------------------------------------------+
| Tipo:                    | Funcional                                                              |
+--------------------------+------------------------------------------------------------------------+
| Detalles de requisitos y | - Regla: Obligatorios, solo letras (y `. ' -`), 2 a 60 caracteres.     |
| restricciones:           |                                                                        |
|                          | Mensajes de error que muestra el sistema:                              |
| ESPECIFICACIÓN           |                                                                        |
|                          | - "Los nombres son obligatorios"                                       |
|                          |                                                                        |
|                          | - "Los nombres solo deben contener letras"                             |
|                          |                                                                        |
|                          | - "Los nombres deben tener entre 2 y 60 caracteres"                    |
|                          |                                                                        |
|                          | - "Los apellidos son obligatorios"                                     |
|                          |                                                                        |
|                          | - "Los apellidos solo deben contener letras"                           |
|                          |                                                                        |
|                          | - "Los apellidos deben tener entre 2 y 60 caracteres"                  |
|                          |                                                                        |
|                          | Implementado en: `AutorController.validarNombres`, `validarApellidos`  |
+--------------------------+------------------------------------------------------------------------+
| Fecha de revisión y      | 10/11/2026                                                             |
| versión, AUTOR:          |                                                                        |
|                          | Versión 1.3                                                            |
|                          |                                                                        |
|                          | (                    )                                                 |
+--------------------------+------------------------------------------------------------------------+
| Prioridad:               | Alta                                                                   |
+--------------------------+------------------------------------------------------------------------+

+--------------------------+------------------------------------------------------------------------+
| Número:                  | **RF-AUT-06**                                                          |
+--------------------------+------------------------------------------------------------------------+
| Título:                  | Validar nacionalidad                                                   |
+--------------------------+------------------------------------------------------------------------+
| Texto:                   | El sistema validará la nacionalidad del autor.                         |
|                          |                                                                        |
| DEFINICIÓN               |                                                                        |
+--------------------------+------------------------------------------------------------------------+
| Tipo:                    | Funcional                                                              |
+--------------------------+------------------------------------------------------------------------+
| Detalles de requisitos y | - Regla: Obligatoria, solo letras y espacios, 3 a 50 caracteres.       |
| restricciones:           |                                                                        |
|                          | Mensajes de error que muestra el sistema:                              |
| ESPECIFICACIÓN           |                                                                        |
|                          | - "La nacionalidad es obligatoria"                                     |
|                          |                                                                        |
|                          | - "La nacionalidad solo debe contener letras"                          |
|                          |                                                                        |
|                          | - "La nacionalidad debe tener entre 3 y 50 caracteres"                 |
|                          |                                                                        |
|                          | Implementado en: `AutorController.validarNacionalidad`                 |
+--------------------------+------------------------------------------------------------------------+
| Fecha de revisión y      | 10/11/2026                                                             |
| versión, AUTOR:          |                                                                        |
|                          | Versión 1.3                                                            |
|                          |                                                                        |
|                          | (                    )                                                 |
+--------------------------+------------------------------------------------------------------------+
| Prioridad:               | Alta                                                                   |
+--------------------------+------------------------------------------------------------------------+

+--------------------------+------------------------------------------------------------------------+
| Número:                  | **RF-AUT-07**                                                          |
+--------------------------+------------------------------------------------------------------------+
| Título:                  | Validar fecha de nacimiento                                            |
+--------------------------+------------------------------------------------------------------------+
| Texto:                   | El sistema validará la fecha de nacimiento del autor cuando se         |
|                          | escriba.                                                               |
| DEFINICIÓN               |                                                                        |
+--------------------------+------------------------------------------------------------------------+
| Tipo:                    | Funcional                                                              |
+--------------------------+------------------------------------------------------------------------+
| Detalles de requisitos y | - Regla: Opcional; si se escribe: `dd/MM/yyyy`, fecha real, no futura, |
| restricciones:           |   no anterior al año 1000.                                             |
|                          |                                                                        |
| ESPECIFICACIÓN           | Mensajes de error que muestra el sistema:                              |
|                          |                                                                        |
|                          | - "La fecha de nacimiento debe tener el formato dd/MM/yyyy y ser una   |
|                          |   fecha real"                                                          |
|                          |                                                                        |
|                          | - "La fecha de nacimiento no puede ser futura"                         |
|                          |                                                                        |
|                          | - "La fecha de nacimiento no puede ser anterior al año 1000"           |
|                          |                                                                        |
|                          | Implementado en: `AutorController.validarFechaNacimiento`              |
+--------------------------+------------------------------------------------------------------------+
| Fecha de revisión y      | 10/11/2026                                                             |
| versión, AUTOR:          |                                                                        |
|                          | Versión 1.3                                                            |
|                          |                                                                        |
|                          | (                    )                                                 |
+--------------------------+------------------------------------------------------------------------+
| Prioridad:               | Media                                                                  |
+--------------------------+------------------------------------------------------------------------+

+--------------------------+------------------------------------------------------------------------+
| Número:                  | **RF-AUT-08**                                                          |
+--------------------------+------------------------------------------------------------------------+
| Título:                  | Validar biografía                                                      |
+--------------------------+------------------------------------------------------------------------+
| Texto:                   | El sistema validará la longitud de la biografía del autor.             |
|                          |                                                                        |
| DEFINICIÓN               |                                                                        |
+--------------------------+------------------------------------------------------------------------+
| Tipo:                    | Funcional                                                              |
+--------------------------+------------------------------------------------------------------------+
| Detalles de requisitos y | - Regla: Opcional, máximo 255 caracteres.                              |
| restricciones:           |                                                                        |
|                          | Mensajes de error que muestra el sistema:                              |
| ESPECIFICACIÓN           |                                                                        |
|                          | - "La biografía no puede superar 255 caracteres"                       |
|                          |                                                                        |
|                          | Implementado en: `AutorController.validarBiografia`                    |
+--------------------------+------------------------------------------------------------------------+
| Fecha de revisión y      | 10/11/2026                                                             |
| versión, AUTOR:          |                                                                        |
|                          | Versión 1.3                                                            |
|                          |                                                                        |
|                          | (                    )                                                 |
+--------------------------+------------------------------------------------------------------------+
| Prioridad:               | Baja                                                                   |
+--------------------------+------------------------------------------------------------------------+

+--------------------------+------------------------------------------------------------------------+
| Número:                  | **RF-AUT-09**                                                          |
+--------------------------+------------------------------------------------------------------------+
| Título:                  | Controlar autor duplicado                                              |
+--------------------------+------------------------------------------------------------------------+
| Texto:                   | El sistema no permitirá registrar dos veces un autor con los mismos    |
|                          | nombres y apellidos.                                                   |
| DEFINICIÓN               |                                                                        |
+--------------------------+------------------------------------------------------------------------+
| Tipo:                    | Funcional                                                              |
+--------------------------+------------------------------------------------------------------------+
| Detalles de requisitos y | - Regla: No se repiten los mismos nombres y apellidos.                 |
| restricciones:           |                                                                        |
|                          | Mensajes de error que muestra el sistema:                              |
| ESPECIFICACIÓN           |                                                                        |
|                          | - "Ya existe un autor registrado con ese nombre"                       |
|                          |                                                                        |
|                          | Implementado en: `AutorDAO.existeNombre`                               |
+--------------------------+------------------------------------------------------------------------+
| Fecha de revisión y      | 10/11/2026                                                             |
| versión, AUTOR:          |                                                                        |
|                          | Versión 1.3                                                            |
|                          |                                                                        |
|                          | (                    )                                                 |
+--------------------------+------------------------------------------------------------------------+
| Prioridad:               | Alta                                                                   |
+--------------------------+------------------------------------------------------------------------+

#### 3.2.2 Casos de uso

![Figura 6. Casos de uso del módulo Autor](diagramas/04_cu_autor.png){width=14cm}

##### CU-07 Gestionar autores
| Elemento | Descripción |
|----------|-------------|
| Actor | Usuario |
| Requerimientos | RF-AUT-01 a RF-AUT-09 |
| Precondición | Sesión iniciada; módulo Autores abierto. |
| Flujo básico | 1. El usuario elige Nuevo, Editar, Eliminar o Buscar. 2. Para Nuevo/Editar ingresa nombres, apellidos, nacionalidad, fecha de nacimiento y biografía y pulsa Guardar. 3. El sistema valida los campos y que el autor no esté repetido. 4. El sistema guarda y muestra el mensaje de éxito. |
| Flujos alternos | 3a. Dato inválido: mensaje del campo. 3b. Autor repetido: se rechaza. Eliminar sin selección o autor inexistente: mensaje de error. |
| Postcondición | Autor registrado, actualizado o eliminado. |

#### 3.2.3 Interfaces (Pantallas)

Capturas de las pantallas del módulo funcionando. Pegue cada captura en el cuadro correspondiente.

[[CAPTURA: Pantalla principal del módulo Autor (tabla con los autores y búsqueda)]]

[[CAPTURA: Formulario Nuevo autor con datos válidos]]

[[CAPTURA: Mensaje de error de validación (campo enfocado)]]

[[CAPTURA: Mensaje de éxito al guardar / confirmación al eliminar]]

---

### 3.3 Módulo Lector - (                            )

#### 3.3.1 Requerimientos funcionales (CRU)

##### Resumen

| ID | Nombre | Prioridad |
|----|--------|-----------|
| RF-LEC-01 | Registrar lector | Alta |
| RF-LEC-02 | Listar y buscar lectores | Media |
| RF-LEC-03 | Modificar lector | Alta |
| RF-LEC-04 | Eliminar lector | Alta |
| RF-LEC-05 | Validar cédula ecuatoriana | Alta |
| RF-LEC-06 | Validar nombres y apellidos | Alta |
| RF-LEC-07 | Validar correo | Alta |
| RF-LEC-08 | Validar teléfono | Media |
| RF-LEC-09 | Validar dirección y estado | Baja |
| RF-LEC-10 | Controlar cédula y correo únicos | Alta |
| RF-LEC-11 | Impedir eliminar lectores con préstamos | Alta |

##### Especificación

+--------------------------+------------------------------------------------------------------------+
| Número:                  | **RF-LEC-01**                                                          |
+--------------------------+------------------------------------------------------------------------+
| Título:                  | Registrar lector                                                       |
+--------------------------+------------------------------------------------------------------------+
| Texto:                   | El sistema permitirá el ingreso de los datos de un lector nuevo        |
|                          | (cédula, nombres, apellidos, correo, teléfono, dirección y estado) y   |
| DEFINICIÓN               | lo guardará en la base de datos.                                       |
+--------------------------+------------------------------------------------------------------------+
| Tipo:                    | Funcional                                                              |
+--------------------------+------------------------------------------------------------------------+
| Detalles de requisitos y | - Regla: Guarda un lector con cédula, nombres, apellidos, correo,      |
| restricciones:           |   teléfono, dirección y estado.                                        |
|                          |                                                                        |
| ESPECIFICACIÓN           | Implementado en: `LectorController.registrarLector`,                   |
|                          | `LectorDAO.insertar`                                                   |
+--------------------------+------------------------------------------------------------------------+
| Fecha de revisión y      | 10/11/2026                                                             |
| versión, AUTOR:          |                                                                        |
|                          | Versión 1.3                                                            |
|                          |                                                                        |
|                          | (                    )                                                 |
+--------------------------+------------------------------------------------------------------------+
| Prioridad:               | Alta                                                                   |
+--------------------------+------------------------------------------------------------------------+

+--------------------------+------------------------------------------------------------------------+
| Número:                  | **RF-LEC-02**                                                          |
+--------------------------+------------------------------------------------------------------------+
| Título:                  | Listar y buscar lectores                                               |
+--------------------------+------------------------------------------------------------------------+
| Texto:                   | El sistema mostrará todos los lectores registrados y permitirá         |
|                          | buscarlos por cédula, nombres, apellidos o correo.                     |
| DEFINICIÓN               |                                                                        |
+--------------------------+------------------------------------------------------------------------+
| Tipo:                    | Funcional                                                              |
+--------------------------+------------------------------------------------------------------------+
| Detalles de requisitos y | - Regla: Muestra todos o filtra por cédula, nombres, apellidos o       |
| restricciones:           |   correo.                                                              |
|                          |                                                                        |
| ESPECIFICACIÓN           | Implementado en: `LectorController.listarLectores`, `buscarLectores`,  |
|                          | `LectorDAO.buscar`                                                     |
+--------------------------+------------------------------------------------------------------------+
| Fecha de revisión y      | 10/11/2026                                                             |
| versión, AUTOR:          |                                                                        |
|                          | Versión 1.3                                                            |
|                          |                                                                        |
|                          | (                    )                                                 |
+--------------------------+------------------------------------------------------------------------+
| Prioridad:               | Media                                                                  |
+--------------------------+------------------------------------------------------------------------+

+--------------------------+------------------------------------------------------------------------+
| Número:                  | **RF-LEC-03**                                                          |
+--------------------------+------------------------------------------------------------------------+
| Título:                  | Modificar lector                                                       |
+--------------------------+------------------------------------------------------------------------+
| Texto:                   | El sistema permitirá modificar los datos del lector seleccionado.      |
|                          |                                                                        |
| DEFINICIÓN               |                                                                        |
+--------------------------+------------------------------------------------------------------------+
| Tipo:                    | Funcional                                                              |
+--------------------------+------------------------------------------------------------------------+
| Detalles de requisitos y | - Regla: Cambia los datos del lector seleccionado.                     |
| restricciones:           |                                                                        |
|                          | Mensajes de error que muestra el sistema:                              |
| ESPECIFICACIÓN           |                                                                        |
|                          | - "Debe seleccionar un lector para modificar"                          |
|                          |                                                                        |
|                          | Implementado en: `LectorController.actualizarLector`,                  |
|                          | `LectorDAO.actualizar`                                                 |
+--------------------------+------------------------------------------------------------------------+
| Fecha de revisión y      | 10/11/2026                                                             |
| versión, AUTOR:          |                                                                        |
|                          | Versión 1.3                                                            |
|                          |                                                                        |
|                          | (                    )                                                 |
+--------------------------+------------------------------------------------------------------------+
| Prioridad:               | Alta                                                                   |
+--------------------------+------------------------------------------------------------------------+

+--------------------------+------------------------------------------------------------------------+
| Número:                  | **RF-LEC-04**                                                          |
+--------------------------+------------------------------------------------------------------------+
| Título:                  | Eliminar lector                                                        |
+--------------------------+------------------------------------------------------------------------+
| Texto:                   | El sistema permitirá eliminar el lector seleccionado, previa           |
|                          | confirmación.                                                          |
| DEFINICIÓN               |                                                                        |
+--------------------------+------------------------------------------------------------------------+
| Tipo:                    | Funcional                                                              |
+--------------------------+------------------------------------------------------------------------+
| Detalles de requisitos y | - Regla: Borra el lector seleccionado con confirmación.                |
| restricciones:           |                                                                        |
|                          | Mensajes de error que muestra el sistema:                              |
| ESPECIFICACIÓN           |                                                                        |
|                          | - "Debe seleccionar un lector para eliminar"                           |
|                          |                                                                        |
|                          | - "El lector no existe"                                                |
|                          |                                                                        |
|                          | Implementado en: `LectorController.eliminarLector`,                    |
|                          | `LectorDAO.eliminar`                                                   |
+--------------------------+------------------------------------------------------------------------+
| Fecha de revisión y      | 10/11/2026                                                             |
| versión, AUTOR:          |                                                                        |
|                          | Versión 1.3                                                            |
|                          |                                                                        |
|                          | (                    )                                                 |
+--------------------------+------------------------------------------------------------------------+
| Prioridad:               | Alta                                                                   |
+--------------------------+------------------------------------------------------------------------+

+--------------------------+------------------------------------------------------------------------+
| Número:                  | **RF-LEC-05**                                                          |
+--------------------------+------------------------------------------------------------------------+
| Título:                  | Validar cédula ecuatoriana                                             |
+--------------------------+------------------------------------------------------------------------+
| Texto:                   | El sistema validará que la cédula sea una cédula ecuatoriana válida.   |
|                          |                                                                        |
| DEFINICIÓN               |                                                                        |
+--------------------------+------------------------------------------------------------------------+
| Tipo:                    | Funcional                                                              |
+--------------------------+------------------------------------------------------------------------+
| Detalles de requisitos y | - Regla: Obligatoria, 10 dígitos, provincia 01–24 o 30, tercer dígito  |
| restricciones:           |   < 6, dígito verificador (módulo 10) correcto.                        |
|                          |                                                                        |
| ESPECIFICACIÓN           | Mensajes de error que muestra el sistema:                              |
|                          |                                                                        |
|                          | - "La cédula es obligatoria"                                           |
|                          |                                                                        |
|                          | - "La cédula debe tener exactamente 10 dígitos numéricos"              |
|                          |                                                                        |
|                          | - "Los dos primeros dígitos de la cédula no son un código de provincia |
|                          |   válido"                                                              |
|                          |                                                                        |
|                          | - "El tercer dígito de la cédula debe ser menor a 6"                   |
|                          |                                                                        |
|                          | - "La cédula no es válida (dígito verificador incorrecto)"             |
|                          |                                                                        |
|                          | Implementado en: `LectorController.validarCedula`,                     |
|                          | `digitoVerificadorValido`                                              |
+--------------------------+------------------------------------------------------------------------+
| Fecha de revisión y      | 10/11/2026                                                             |
| versión, AUTOR:          |                                                                        |
|                          | Versión 1.3                                                            |
|                          |                                                                        |
|                          | (                    )                                                 |
+--------------------------+------------------------------------------------------------------------+
| Prioridad:               | Alta                                                                   |
+--------------------------+------------------------------------------------------------------------+

+--------------------------+------------------------------------------------------------------------+
| Número:                  | **RF-LEC-06**                                                          |
+--------------------------+------------------------------------------------------------------------+
| Título:                  | Validar nombres y apellidos                                            |
+--------------------------+------------------------------------------------------------------------+
| Texto:                   | El sistema validará los nombres y apellidos del lector.                |
|                          |                                                                        |
| DEFINICIÓN               |                                                                        |
+--------------------------+------------------------------------------------------------------------+
| Tipo:                    | Funcional                                                              |
+--------------------------+------------------------------------------------------------------------+
| Detalles de requisitos y | - Regla: Obligatorios, solo letras y espacios, 2 a 60 caracteres.      |
| restricciones:           |                                                                        |
|                          | Mensajes de error que muestra el sistema:                              |
| ESPECIFICACIÓN           |                                                                        |
|                          | - "Los nombres son obligatorios"                                       |
|                          |                                                                        |
|                          | - "Los nombres solo deben contener letras y espacios"                  |
|                          |                                                                        |
|                          | - "Los nombres deben tener entre 2 y 60 caracteres (y los mismos       |
|                          |   mensajes para los apellidos)"                                        |
|                          |                                                                        |
|                          | Implementado en: `LectorController.validarNombres`, `validarApellidos` |
+--------------------------+------------------------------------------------------------------------+
| Fecha de revisión y      | 10/11/2026                                                             |
| versión, AUTOR:          |                                                                        |
|                          | Versión 1.3                                                            |
|                          |                                                                        |
|                          | (                    )                                                 |
+--------------------------+------------------------------------------------------------------------+
| Prioridad:               | Alta                                                                   |
+--------------------------+------------------------------------------------------------------------+

+--------------------------+------------------------------------------------------------------------+
| Número:                  | **RF-LEC-07**                                                          |
+--------------------------+------------------------------------------------------------------------+
| Título:                  | Validar correo                                                         |
+--------------------------+------------------------------------------------------------------------+
| Texto:                   | El sistema validará el correo electrónico del lector.                  |
|                          |                                                                        |
| DEFINICIÓN               |                                                                        |
+--------------------------+------------------------------------------------------------------------+
| Tipo:                    | Funcional                                                              |
+--------------------------+------------------------------------------------------------------------+
| Detalles de requisitos y | - Regla: Obligatorio, formato `usuario@dominio.ext`, máximo 100        |
| restricciones:           |   caracteres.                                                          |
|                          |                                                                        |
| ESPECIFICACIÓN           | Mensajes de error que muestra el sistema:                              |
|                          |                                                                        |
|                          | - "El correo es obligatorio"                                           |
|                          |                                                                        |
|                          | - "El correo no puede superar 100 caracteres"                          |
|                          |                                                                        |
|                          | - "El correo no tiene un formato válido (ejemplo: nombre@correo.com)"  |
|                          |                                                                        |
|                          | Implementado en: `LectorController.validarCorreo`                      |
+--------------------------+------------------------------------------------------------------------+
| Fecha de revisión y      | 10/11/2026                                                             |
| versión, AUTOR:          |                                                                        |
|                          | Versión 1.3                                                            |
|                          |                                                                        |
|                          | (                    )                                                 |
+--------------------------+------------------------------------------------------------------------+
| Prioridad:               | Alta                                                                   |
+--------------------------+------------------------------------------------------------------------+

+--------------------------+------------------------------------------------------------------------+
| Número:                  | **RF-LEC-08**                                                          |
+--------------------------+------------------------------------------------------------------------+
| Título:                  | Validar teléfono                                                       |
+--------------------------+------------------------------------------------------------------------+
| Texto:                   | El sistema validará el número de celular del lector.                   |
|                          |                                                                        |
| DEFINICIÓN               |                                                                        |
+--------------------------+------------------------------------------------------------------------+
| Tipo:                    | Funcional                                                              |
+--------------------------+------------------------------------------------------------------------+
| Detalles de requisitos y | - Regla: Obligatorio, celular de 10 dígitos que empieza con 09.        |
| restricciones:           |                                                                        |
|                          | Mensajes de error que muestra el sistema:                              |
| ESPECIFICACIÓN           |                                                                        |
|                          | - "El teléfono es obligatorio"                                         |
|                          |                                                                        |
|                          | - "El teléfono debe tener 10 dígitos y empezar con 09"                 |
|                          |                                                                        |
|                          | Implementado en: `LectorController.validarTelefono`                    |
+--------------------------+------------------------------------------------------------------------+
| Fecha de revisión y      | 10/11/2026                                                             |
| versión, AUTOR:          |                                                                        |
|                          | Versión 1.3                                                            |
|                          |                                                                        |
|                          | (                    )                                                 |
+--------------------------+------------------------------------------------------------------------+
| Prioridad:               | Media                                                                  |
+--------------------------+------------------------------------------------------------------------+

+--------------------------+------------------------------------------------------------------------+
| Número:                  | **RF-LEC-09**                                                          |
+--------------------------+------------------------------------------------------------------------+
| Título:                  | Validar dirección y estado                                             |
+--------------------------+------------------------------------------------------------------------+
| Texto:                   | El sistema validará la dirección y el estado del lector.               |
|                          |                                                                        |
| DEFINICIÓN               |                                                                        |
+--------------------------+------------------------------------------------------------------------+
| Tipo:                    | Funcional                                                              |
+--------------------------+------------------------------------------------------------------------+
| Detalles de requisitos y | - Regla: Dirección opcional, máx. 150; estado ACTIVO o SUSPENDIDO.     |
| restricciones:           |                                                                        |
|                          | Mensajes de error que muestra el sistema:                              |
| ESPECIFICACIÓN           |                                                                        |
|                          | - "La dirección no puede superar 150 caracteres"                       |
|                          |                                                                        |
|                          | - "Debe seleccionar un estado"                                         |
|                          |                                                                        |
|                          | - "El estado seleccionado no es válido"                                |
|                          |                                                                        |
|                          | Implementado en: `LectorController.validarDireccion`, `validarEstado`  |
+--------------------------+------------------------------------------------------------------------+
| Fecha de revisión y      | 10/11/2026                                                             |
| versión, AUTOR:          |                                                                        |
|                          | Versión 1.3                                                            |
|                          |                                                                        |
|                          | (                    )                                                 |
+--------------------------+------------------------------------------------------------------------+
| Prioridad:               | Baja                                                                   |
+--------------------------+------------------------------------------------------------------------+

+--------------------------+------------------------------------------------------------------------+
| Número:                  | **RF-LEC-10**                                                          |
+--------------------------+------------------------------------------------------------------------+
| Título:                  | Controlar cédula y correo únicos                                       |
+--------------------------+------------------------------------------------------------------------+
| Texto:                   | El sistema no permitirá que dos lectores tengan la misma cédula o el   |
|                          | mismo correo.                                                          |
| DEFINICIÓN               |                                                                        |
+--------------------------+------------------------------------------------------------------------+
| Tipo:                    | Funcional                                                              |
+--------------------------+------------------------------------------------------------------------+
| Detalles de requisitos y | - Regla: No se repite la cédula ni el correo de otro lector.           |
| restricciones:           |                                                                        |
|                          | Mensajes de error que muestra el sistema:                              |
| ESPECIFICACIÓN           |                                                                        |
|                          | - "Ya existe un lector registrado con esa cédula"                      |
|                          |                                                                        |
|                          | - "Ya existe un lector registrado con ese correo"                      |
|                          |                                                                        |
|                          | Implementado en: `LectorDAO.existeCedula`, `existeCorreo`              |
+--------------------------+------------------------------------------------------------------------+
| Fecha de revisión y      | 10/11/2026                                                             |
| versión, AUTOR:          |                                                                        |
|                          | Versión 1.3                                                            |
|                          |                                                                        |
|                          | (                    )                                                 |
+--------------------------+------------------------------------------------------------------------+
| Prioridad:               | Alta                                                                   |
+--------------------------+------------------------------------------------------------------------+

+--------------------------+------------------------------------------------------------------------+
| Número:                  | **RF-LEC-11**                                                          |
+--------------------------+------------------------------------------------------------------------+
| Título:                  | Impedir eliminar lectores con préstamos                                |
+--------------------------+------------------------------------------------------------------------+
| Texto:                   | El sistema no permitirá eliminar un lector que tenga préstamos         |
|                          | registrados.                                                           |
| DEFINICIÓN               |                                                                        |
+--------------------------+------------------------------------------------------------------------+
| Tipo:                    | Funcional                                                              |
+--------------------------+------------------------------------------------------------------------+
| Detalles de requisitos y | - Regla: Un lector con préstamos no se elimina.                        |
| restricciones:           |                                                                        |
|                          | Mensajes de error que muestra el sistema:                              |
| ESPECIFICACIÓN           |                                                                        |
|                          | - "No se puede eliminar: el lector tiene préstamos registrados"        |
|                          |                                                                        |
|                          | Implementado en: `LectorDAO.tienePrestamos`                            |
+--------------------------+------------------------------------------------------------------------+
| Fecha de revisión y      | 10/11/2026                                                             |
| versión, AUTOR:          |                                                                        |
|                          | Versión 1.3                                                            |
|                          |                                                                        |
|                          | (                    )                                                 |
+--------------------------+------------------------------------------------------------------------+
| Prioridad:               | Alta                                                                   |
+--------------------------+------------------------------------------------------------------------+

#### 3.3.2 Casos de uso

![Figura 7. Casos de uso del módulo Lector](diagramas/05_cu_lector.png){width=14cm}

##### CU-08 Gestionar lectores
| Elemento | Descripción |
|----------|-------------|
| Actor | Usuario |
| Requerimientos | RF-LEC-01 a RF-LEC-11 |
| Precondición | Sesión iniciada; módulo Lectores abierto. |
| Flujo básico | 1. El usuario elige Nuevo o Editar. 2. Ingresa cédula, nombres, apellidos, correo, teléfono, dirección y estado y pulsa Guardar. 3. El sistema valida la cédula (incluido dígito verificador), los demás campos y que cédula y correo sean únicos. 4. El sistema guarda y muestra el mensaje de éxito. |
| Flujos alternos | 3a. Dato inválido o duplicado: mensaje del campo. Eliminar un lector con préstamos: se rechaza. |
| Postcondición | Lector registrado, actualizado o eliminado. |

#### 3.3.3 Interfaces (Pantallas)

Capturas de las pantallas del módulo funcionando. Pegue cada captura en el cuadro correspondiente.

[[CAPTURA: Pantalla principal del módulo Lector (tabla con los lectores y búsqueda)]]

[[CAPTURA: Formulario Nuevo lector con datos válidos]]

[[CAPTURA: Mensaje de error de validación (campo enfocado)]]

[[CAPTURA: Mensaje de éxito al guardar / confirmación al eliminar]]

---

### 3.4 Módulo Préstamo - (                            )

#### 3.4.1 Requerimientos funcionales (CRU)

##### Resumen

| ID | Nombre | Prioridad |
|----|--------|-----------|
| RF-PRE-01 | Registrar préstamo | Alta |
| RF-PRE-02 | Listar y buscar préstamos | Media |
| RF-PRE-03 | Modificar préstamo | Alta |
| RF-PRE-04 | Eliminar préstamo | Alta |
| RF-PRE-05 | Validar libro y lector | Alta |
| RF-PRE-06 | Validar fecha de préstamo | Alta |
| RF-PRE-07 | Validar fecha límite | Alta |
| RF-PRE-08 | Verificar stock | Alta |
| RF-PRE-09 | Verificar lector habilitado | Alta |
| RF-PRE-10 | Actualizar stock | Alta |
| RF-PRE-11 | Marcar vencidos | Media |
| RF-PRE-12 | Validar observación | Baja |

##### Especificación

+--------------------------+------------------------------------------------------------------------+
| Número:                  | **RF-PRE-01**                                                          |
+--------------------------+------------------------------------------------------------------------+
| Título:                  | Registrar préstamo                                                     |
+--------------------------+------------------------------------------------------------------------+
| Texto:                   | El sistema permitirá registrar el préstamo de un libro a un lector,    |
|                          | con fecha de préstamo, fecha límite y observación; el préstamo queda   |
| DEFINICIÓN               | en estado ACTIVO.                                                      |
+--------------------------+------------------------------------------------------------------------+
| Tipo:                    | Funcional                                                              |
+--------------------------+------------------------------------------------------------------------+
| Detalles de requisitos y | - Regla: Registra el préstamo de un libro a un lector con estado       |
| restricciones:           |   ACTIVO.                                                              |
|                          |                                                                        |
| ESPECIFICACIÓN           | - Mensaje de éxito: "Préstamo registrado correctamente"                |
|                          |                                                                        |
|                          | Implementado en: `PrestamoController.registrarPrestamo`,               |
|                          | `PrestamoDAO.insertar`                                                 |
+--------------------------+------------------------------------------------------------------------+
| Fecha de revisión y      | 10/11/2026                                                             |
| versión, AUTOR:          |                                                                        |
|                          | Versión 1.3                                                            |
|                          |                                                                        |
|                          | (                    )                                                 |
+--------------------------+------------------------------------------------------------------------+
| Prioridad:               | Alta                                                                   |
+--------------------------+------------------------------------------------------------------------+

+--------------------------+------------------------------------------------------------------------+
| Número:                  | **RF-PRE-02**                                                          |
+--------------------------+------------------------------------------------------------------------+
| Título:                  | Listar y buscar préstamos                                              |
+--------------------------+------------------------------------------------------------------------+
| Texto:                   | El sistema mostrará todos los préstamos y permitirá buscarlos por      |
|                          | título del libro, lector, cédula o estado.                             |
| DEFINICIÓN               |                                                                        |
+--------------------------+------------------------------------------------------------------------+
| Tipo:                    | Funcional                                                              |
+--------------------------+------------------------------------------------------------------------+
| Detalles de requisitos y | - Regla: Muestra todos o filtra por título, lector, cédula o estado.   |
| restricciones:           |                                                                        |
|                          | Implementado en: `PrestamoController.listarPrestamos`,                 |
| ESPECIFICACIÓN           | `buscarPrestamos`, `PrestamoDAO.buscar`                                |
+--------------------------+------------------------------------------------------------------------+
| Fecha de revisión y      | 10/11/2026                                                             |
| versión, AUTOR:          |                                                                        |
|                          | Versión 1.3                                                            |
|                          |                                                                        |
|                          | (                    )                                                 |
+--------------------------+------------------------------------------------------------------------+
| Prioridad:               | Media                                                                  |
+--------------------------+------------------------------------------------------------------------+

+--------------------------+------------------------------------------------------------------------+
| Número:                  | **RF-PRE-03**                                                          |
+--------------------------+------------------------------------------------------------------------+
| Título:                  | Modificar préstamo                                                     |
+--------------------------+------------------------------------------------------------------------+
| Texto:                   | El sistema permitirá modificar un préstamo que todavía no ha sido      |
|                          | devuelto.                                                              |
| DEFINICIÓN               |                                                                        |
+--------------------------+------------------------------------------------------------------------+
| Tipo:                    | Funcional                                                              |
+--------------------------+------------------------------------------------------------------------+
| Detalles de requisitos y | - Regla: Cambia los datos de un préstamo no devuelto.                  |
| restricciones:           |                                                                        |
|                          | Mensajes de error que muestra el sistema:                              |
| ESPECIFICACIÓN           |                                                                        |
|                          | - "No se puede modificar un préstamo que ya fue devuelto"              |
|                          |                                                                        |
|                          | Implementado en: `PrestamoController.actualizarPrestamo`,              |
|                          | `PrestamoDAO.actualizar`                                               |
+--------------------------+------------------------------------------------------------------------+
| Fecha de revisión y      | 10/11/2026                                                             |
| versión, AUTOR:          |                                                                        |
|                          | Versión 1.3                                                            |
|                          |                                                                        |
|                          | (                    )                                                 |
+--------------------------+------------------------------------------------------------------------+
| Prioridad:               | Alta                                                                   |
+--------------------------+------------------------------------------------------------------------+

+--------------------------+------------------------------------------------------------------------+
| Número:                  | **RF-PRE-04**                                                          |
+--------------------------+------------------------------------------------------------------------+
| Título:                  | Eliminar préstamo                                                      |
+--------------------------+------------------------------------------------------------------------+
| Texto:                   | El sistema permitirá eliminar un préstamo que no tenga devolución,     |
|                          | devolviendo el ejemplar al stock.                                      |
| DEFINICIÓN               |                                                                        |
+--------------------------+------------------------------------------------------------------------+
| Tipo:                    | Funcional                                                              |
+--------------------------+------------------------------------------------------------------------+
| Detalles de requisitos y | - Regla: Borra un préstamo sin devolución y devuelve el ejemplar al    |
| restricciones:           |   stock.                                                               |
|                          |                                                                        |
| ESPECIFICACIÓN           | Mensajes de error que muestra el sistema:                              |
|                          |                                                                        |
|                          | - "No se puede eliminar: el préstamo tiene una devolución registrada"  |
|                          |                                                                        |
|                          | Implementado en: `PrestamoController.eliminarPrestamo`,                |
|                          | `PrestamoDAO.eliminar`, `tieneDevolucion`                              |
+--------------------------+------------------------------------------------------------------------+
| Fecha de revisión y      | 10/11/2026                                                             |
| versión, AUTOR:          |                                                                        |
|                          | Versión 1.3                                                            |
|                          |                                                                        |
|                          | (                    )                                                 |
+--------------------------+------------------------------------------------------------------------+
| Prioridad:               | Alta                                                                   |
+--------------------------+------------------------------------------------------------------------+

+--------------------------+------------------------------------------------------------------------+
| Número:                  | **RF-PRE-05**                                                          |
+--------------------------+------------------------------------------------------------------------+
| Título:                  | Validar libro y lector                                                 |
+--------------------------+------------------------------------------------------------------------+
| Texto:                   | El sistema exigirá elegir de una lista el libro y el lector del        |
|                          | préstamo.                                                              |
| DEFINICIÓN               |                                                                        |
+--------------------------+------------------------------------------------------------------------+
| Tipo:                    | Funcional                                                              |
+--------------------------+------------------------------------------------------------------------+
| Detalles de requisitos y | - Regla: Ambos obligatorios, elegidos de listas.                       |
| restricciones:           |                                                                        |
|                          | Mensajes de error que muestra el sistema:                              |
| ESPECIFICACIÓN           |                                                                        |
|                          | - "El libro seleccionado no existe"                                    |
|                          |                                                                        |
|                          | - "El lector seleccionado no existe"                                   |
|                          |                                                                        |
|                          | Implementado en: `PrestamoController.validarLibro`, `validarLector`    |
+--------------------------+------------------------------------------------------------------------+
| Fecha de revisión y      | 10/11/2026                                                             |
| versión, AUTOR:          |                                                                        |
|                          | Versión 1.3                                                            |
|                          |                                                                        |
|                          | (                    )                                                 |
+--------------------------+------------------------------------------------------------------------+
| Prioridad:               | Alta                                                                   |
+--------------------------+------------------------------------------------------------------------+

+--------------------------+------------------------------------------------------------------------+
| Número:                  | **RF-PRE-06**                                                          |
+--------------------------+------------------------------------------------------------------------+
| Título:                  | Validar fecha de préstamo                                              |
+--------------------------+------------------------------------------------------------------------+
| Texto:                   | El sistema validará la fecha del préstamo.                             |
|                          |                                                                        |
| DEFINICIÓN               |                                                                        |
+--------------------------+------------------------------------------------------------------------+
| Tipo:                    | Funcional                                                              |
+--------------------------+------------------------------------------------------------------------+
| Detalles de requisitos y | - Regla: Obligatoria, `dd/MM/yyyy`, fecha real, no futura.             |
| restricciones:           |                                                                        |
|                          | Mensajes de error que muestra el sistema:                              |
| ESPECIFICACIÓN           |                                                                        |
|                          | - "La fecha del préstamo es obligatoria"                               |
|                          |                                                                        |
|                          | - "La fecha del préstamo debe tener el formato dd/MM/yyyy y ser una    |
|                          |   fecha real"                                                          |
|                          |                                                                        |
|                          | - "La fecha del préstamo no puede ser futura"                          |
|                          |                                                                        |
|                          | Implementado en: `PrestamoController.validarFechaPrestamo`             |
+--------------------------+------------------------------------------------------------------------+
| Fecha de revisión y      | 10/11/2026                                                             |
| versión, AUTOR:          |                                                                        |
|                          | Versión 1.3                                                            |
|                          |                                                                        |
|                          | (                    )                                                 |
+--------------------------+------------------------------------------------------------------------+
| Prioridad:               | Alta                                                                   |
+--------------------------+------------------------------------------------------------------------+

+--------------------------+------------------------------------------------------------------------+
| Número:                  | **RF-PRE-07**                                                          |
+--------------------------+------------------------------------------------------------------------+
| Título:                  | Validar fecha límite                                                   |
+--------------------------+------------------------------------------------------------------------+
| Texto:                   | El sistema validará la fecha límite de devolución del préstamo.        |
|                          |                                                                        |
| DEFINICIÓN               |                                                                        |
+--------------------------+------------------------------------------------------------------------+
| Tipo:                    | Funcional                                                              |
+--------------------------+------------------------------------------------------------------------+
| Detalles de requisitos y | - Regla: Obligatoria, no anterior al préstamo, máximo 30 días después. |
| restricciones:           |                                                                        |
|                          | Mensajes de error que muestra el sistema:                              |
| ESPECIFICACIÓN           |                                                                        |
|                          | - "La fecha límite es obligatoria"                                     |
|                          |                                                                        |
|                          | - "La fecha límite no puede ser anterior a la fecha del préstamo"      |
|                          |                                                                        |
|                          | - "La fecha límite no puede superar 30 días desde el préstamo"         |
|                          |                                                                        |
|                          | Implementado en: `PrestamoController.validarFechaLimite`               |
+--------------------------+------------------------------------------------------------------------+
| Fecha de revisión y      | 10/11/2026                                                             |
| versión, AUTOR:          |                                                                        |
|                          | Versión 1.3                                                            |
|                          |                                                                        |
|                          | (                    )                                                 |
+--------------------------+------------------------------------------------------------------------+
| Prioridad:               | Alta                                                                   |
+--------------------------+------------------------------------------------------------------------+

+--------------------------+------------------------------------------------------------------------+
| Número:                  | **RF-PRE-08**                                                          |
+--------------------------+------------------------------------------------------------------------+
| Título:                  | Verificar stock                                                        |
+--------------------------+------------------------------------------------------------------------+
| Texto:                   | El sistema no permitirá prestar un libro que no tenga ejemplares       |
|                          | disponibles.                                                           |
| DEFINICIÓN               |                                                                        |
+--------------------------+------------------------------------------------------------------------+
| Tipo:                    | Funcional                                                              |
+--------------------------+------------------------------------------------------------------------+
| Detalles de requisitos y | - Regla: El libro debe tener al menos 1 ejemplar.                      |
| restricciones:           |                                                                        |
|                          | Mensajes de error que muestra el sistema:                              |
| ESPECIFICACIÓN           |                                                                        |
|                          | - "El libro no tiene ejemplares disponibles (stock 0)"                 |
|                          |                                                                        |
|                          | Implementado en: `PrestamoController.verificarStock`                   |
+--------------------------+------------------------------------------------------------------------+
| Fecha de revisión y      | 10/11/2026                                                             |
| versión, AUTOR:          |                                                                        |
|                          | Versión 1.3                                                            |
|                          |                                                                        |
|                          | (                    )                                                 |
+--------------------------+------------------------------------------------------------------------+
| Prioridad:               | Alta                                                                   |
+--------------------------+------------------------------------------------------------------------+

+--------------------------+------------------------------------------------------------------------+
| Número:                  | **RF-PRE-09**                                                          |
+--------------------------+------------------------------------------------------------------------+
| Título:                  | Verificar lector habilitado                                            |
+--------------------------+------------------------------------------------------------------------+
| Texto:                   | El sistema solo permitirá prestar a lectores ACTIVOS con menos de 3    |
|                          | préstamos sin devolver.                                                |
| DEFINICIÓN               |                                                                        |
+--------------------------+------------------------------------------------------------------------+
| Tipo:                    | Funcional                                                              |
+--------------------------+------------------------------------------------------------------------+
| Detalles de requisitos y | - Regla: El lector debe estar ACTIVO y tener menos de 3 préstamos sin  |
| restricciones:           |   devolver.                                                            |
|                          |                                                                        |
| ESPECIFICACIÓN           | Mensajes de error que muestra el sistema:                              |
|                          |                                                                        |
|                          | - "El lector está SUSPENDIDO y no puede recibir préstamos"             |
|                          |                                                                        |
|                          | - "El lector ya tiene 3 préstamos sin devolver"                        |
|                          |                                                                        |
|                          | Implementado en: `PrestamoController.verificarLector`,                 |
|                          | `PrestamoDAO.contarPendientesLector`                                   |
+--------------------------+------------------------------------------------------------------------+
| Fecha de revisión y      | 10/11/2026                                                             |
| versión, AUTOR:          |                                                                        |
|                          | Versión 1.3                                                            |
|                          |                                                                        |
|                          | (                    )                                                 |
+--------------------------+------------------------------------------------------------------------+
| Prioridad:               | Alta                                                                   |
+--------------------------+------------------------------------------------------------------------+

+--------------------------+------------------------------------------------------------------------+
| Número:                  | **RF-PRE-10**                                                          |
+--------------------------+------------------------------------------------------------------------+
| Título:                  | Actualizar stock                                                       |
+--------------------------+------------------------------------------------------------------------+
| Texto:                   | El sistema actualizará el stock del libro al registrar o eliminar un   |
|                          | préstamo.                                                              |
| DEFINICIÓN               |                                                                        |
+--------------------------+------------------------------------------------------------------------+
| Tipo:                    | Funcional                                                              |
+--------------------------+------------------------------------------------------------------------+
| Detalles de requisitos y | - Regla: Al registrar resta 1 al stock; al eliminar un préstamo        |
| restricciones:           |   pendiente lo suma (en una transacción).                              |
|                          |                                                                        |
| ESPECIFICACIÓN           | Implementado en: `PrestamoDAO.insertar`, `actualizar`, `eliminar`      |
|                          | (transacción)                                                          |
+--------------------------+------------------------------------------------------------------------+
| Fecha de revisión y      | 10/11/2026                                                             |
| versión, AUTOR:          |                                                                        |
|                          | Versión 1.3                                                            |
|                          |                                                                        |
|                          | (                    )                                                 |
+--------------------------+------------------------------------------------------------------------+
| Prioridad:               | Alta                                                                   |
+--------------------------+------------------------------------------------------------------------+

+--------------------------+------------------------------------------------------------------------+
| Número:                  | **RF-PRE-11**                                                          |
+--------------------------+------------------------------------------------------------------------+
| Título:                  | Marcar vencidos                                                        |
+--------------------------+------------------------------------------------------------------------+
| Texto:                   | El sistema mostrará como VENCIDO un préstamo ACTIVO cuya fecha límite  |
|                          | ya pasó.                                                               |
| DEFINICIÓN               |                                                                        |
+--------------------------+------------------------------------------------------------------------+
| Tipo:                    | Funcional                                                              |
+--------------------------+------------------------------------------------------------------------+
| Detalles de requisitos y | - Regla: Un préstamo ACTIVO cuya fecha límite ya pasó se muestra como  |
| restricciones:           |   VENCIDO.                                                             |
|                          |                                                                        |
| ESPECIFICACIÓN           | Implementado en: `PrestamoController.calcularEstado`                   |
+--------------------------+------------------------------------------------------------------------+
| Fecha de revisión y      | 10/11/2026                                                             |
| versión, AUTOR:          |                                                                        |
|                          | Versión 1.3                                                            |
|                          |                                                                        |
|                          | (                    )                                                 |
+--------------------------+------------------------------------------------------------------------+
| Prioridad:               | Media                                                                  |
+--------------------------+------------------------------------------------------------------------+

+--------------------------+------------------------------------------------------------------------+
| Número:                  | **RF-PRE-12**                                                          |
+--------------------------+------------------------------------------------------------------------+
| Título:                  | Validar observación                                                    |
+--------------------------+------------------------------------------------------------------------+
| Texto:                   | El sistema validará la longitud de la observación del préstamo.        |
|                          |                                                                        |
| DEFINICIÓN               |                                                                        |
+--------------------------+------------------------------------------------------------------------+
| Tipo:                    | Funcional                                                              |
+--------------------------+------------------------------------------------------------------------+
| Detalles de requisitos y | - Regla: Opcional, máximo 255 caracteres.                              |
| restricciones:           |                                                                        |
|                          | Mensajes de error que muestra el sistema:                              |
| ESPECIFICACIÓN           |                                                                        |
|                          | - "La observación no puede superar 255 caracteres"                     |
|                          |                                                                        |
|                          | Implementado en: `PrestamoController.validarObservacion`               |
+--------------------------+------------------------------------------------------------------------+
| Fecha de revisión y      | 10/11/2026                                                             |
| versión, AUTOR:          |                                                                        |
|                          | Versión 1.3                                                            |
|                          |                                                                        |
|                          | (                    )                                                 |
+--------------------------+------------------------------------------------------------------------+
| Prioridad:               | Baja                                                                   |
+--------------------------+------------------------------------------------------------------------+

#### 3.4.2 Casos de uso

![Figura 8. Casos de uso del módulo Préstamo](diagramas/06_cu_prestamo.png){width=16cm}

##### CU-09 Registrar préstamo
| Elemento | Descripción |
|----------|-------------|
| Actor | Usuario |
| Requerimientos | RF-PRE-01, RF-PRE-05 a RF-PRE-10, RF-PRE-12 |
| Precondición | Sesión iniciada; existen libros y lectores. |
| Flujo básico | 1. El usuario pulsa Nuevo en Préstamos. 2. Elige libro y lector, escribe fecha de préstamo, fecha límite y observación y pulsa Guardar. 3. El sistema valida los campos. 4. El sistema verifica que el libro tenga stock. 5. El sistema verifica que el lector esté ACTIVO y tenga menos de 3 préstamos pendientes. 6. El sistema registra el préstamo como ACTIVO y descuenta 1 del stock. |
| Flujos alternos | 3a. Fecha inválida, futura o límite mayor a 30 días: mensaje del campo. 4a. Sin stock (*Design Patterns*, *Historia del Ecuador*): se rechaza. 5a. Lector SUSPENDIDO o con 3 préstamos pendientes: se rechaza. |
| Postcondición | Préstamo registrado y stock del libro reducido en 1. |

##### CU-10 Gestionar préstamos (buscar, modificar, eliminar)
| Elemento | Descripción |
|----------|-------------|
| Actor | Usuario |
| Requerimientos | RF-PRE-02 a RF-PRE-04, RF-PRE-10, RF-PRE-11 |
| Flujo básico | 1. El usuario busca o selecciona un préstamo. 2. Edita los datos o lo elimina. 3. El sistema revisa las reglas (stock si cambia el libro, estado y límite si cambia el lector) y guarda. |
| Flujos alternos | Préstamo DEVUELTO: no se modifica. Préstamo con devolución: no se elimina. |
| Postcondición | Préstamo actualizado o eliminado; stock ajustado. |

#### 3.4.3 Interfaces (Pantallas)

Capturas de las pantallas del módulo funcionando. Pegue cada captura en el cuadro correspondiente.

[[CAPTURA: Pantalla principal del módulo Préstamo (tabla con los préstamos y su estado)]]

[[CAPTURA: Formulario Nuevo préstamo con datos válidos]]

[[CAPTURA: Mensaje de regla de negocio (libro sin stock o lector suspendido)]]

[[CAPTURA: Mensaje de éxito al guardar / confirmación al eliminar]]

---

### 3.5 Módulo Devolución - (                            )

#### 3.5.1 Requerimientos funcionales (CRU)

##### Resumen

| ID | Nombre | Prioridad |
|----|--------|-----------|
| RF-DEV-01 | Registrar devolución | Alta |
| RF-DEV-02 | Listar y buscar devoluciones | Media |
| RF-DEV-03 | Modificar devolución | Alta |
| RF-DEV-04 | Eliminar devolución | Alta |
| RF-DEV-05 | Validar préstamo pendiente | Alta |
| RF-DEV-06 | Validar fecha de devolución | Alta |
| RF-DEV-07 | Validar estado del libro | Alta |
| RF-DEV-08 | Calcular días de retraso | Alta |
| RF-DEV-09 | Calcular multa | Alta |
| RF-DEV-10 | Actualizar préstamo y stock | Alta |

##### Especificación

+--------------------------+------------------------------------------------------------------------+
| Número:                  | **RF-DEV-01**                                                          |
+--------------------------+------------------------------------------------------------------------+
| Título:                  | Registrar devolución                                                   |
+--------------------------+------------------------------------------------------------------------+
| Texto:                   | El sistema permitirá registrar la devolución de un préstamo pendiente, |
|                          | con fecha de devolución, estado del libro y observación.               |
| DEFINICIÓN               |                                                                        |
+--------------------------+------------------------------------------------------------------------+
| Tipo:                    | Funcional                                                              |
+--------------------------+------------------------------------------------------------------------+
| Detalles de requisitos y | - Regla: Registra la devolución de un préstamo pendiente.              |
| restricciones:           |                                                                        |
|                          | - Mensaje de éxito: "Devolución registrada. Días de retraso: N. Multa: |
| ESPECIFICACIÓN           |   $X"                                                                  |
|                          |                                                                        |
|                          | Implementado en: `DevolucionController.registrarDevolucion`,           |
|                          | `DevolucionDAO.insertar`                                               |
+--------------------------+------------------------------------------------------------------------+
| Fecha de revisión y      | 10/11/2026                                                             |
| versión, AUTOR:          |                                                                        |
|                          | Versión 1.3                                                            |
|                          |                                                                        |
|                          | (                    )                                                 |
+--------------------------+------------------------------------------------------------------------+
| Prioridad:               | Alta                                                                   |
+--------------------------+------------------------------------------------------------------------+

+--------------------------+------------------------------------------------------------------------+
| Número:                  | **RF-DEV-02**                                                          |
+--------------------------+------------------------------------------------------------------------+
| Título:                  | Listar y buscar devoluciones                                           |
+--------------------------+------------------------------------------------------------------------+
| Texto:                   | El sistema mostrará todas las devoluciones y permitirá buscarlas por   |
|                          | título, lector o estado del libro.                                     |
| DEFINICIÓN               |                                                                        |
+--------------------------+------------------------------------------------------------------------+
| Tipo:                    | Funcional                                                              |
+--------------------------+------------------------------------------------------------------------+
| Detalles de requisitos y | - Regla: Muestra todas o filtra por título, lector o estado del libro. |
| restricciones:           |                                                                        |
|                          | Implementado en: `DevolucionController.listarDevoluciones`,            |
| ESPECIFICACIÓN           | `buscarDevoluciones`, `DevolucionDAO.buscar`                           |
+--------------------------+------------------------------------------------------------------------+
| Fecha de revisión y      | 10/11/2026                                                             |
| versión, AUTOR:          |                                                                        |
|                          | Versión 1.3                                                            |
|                          |                                                                        |
|                          | (                    )                                                 |
+--------------------------+------------------------------------------------------------------------+
| Prioridad:               | Media                                                                  |
+--------------------------+------------------------------------------------------------------------+

+--------------------------+------------------------------------------------------------------------+
| Número:                  | **RF-DEV-03**                                                          |
+--------------------------+------------------------------------------------------------------------+
| Título:                  | Modificar devolución                                                   |
+--------------------------+------------------------------------------------------------------------+
| Texto:                   | El sistema permitirá modificar la fecha, el estado del libro o la      |
|                          | observación de una devolución, recalculando la multa y el stock.       |
| DEFINICIÓN               |                                                                        |
+--------------------------+------------------------------------------------------------------------+
| Tipo:                    | Funcional                                                              |
+--------------------------+------------------------------------------------------------------------+
| Detalles de requisitos y | - Regla: Cambia fecha, estado del libro u observación y recalcula      |
| restricciones:           |   multa y stock.                                                       |
|                          |                                                                        |
| ESPECIFICACIÓN           | Mensajes de error que muestra el sistema:                              |
|                          |                                                                        |
|                          | - "Debe seleccionar una devolución para modificar"                     |
|                          |                                                                        |
|                          | Implementado en: `DevolucionController.actualizarDevolucion`,          |
|                          | `calcularAjusteStock`, `DevolucionDAO.actualizar`                      |
+--------------------------+------------------------------------------------------------------------+
| Fecha de revisión y      | 10/11/2026                                                             |
| versión, AUTOR:          |                                                                        |
|                          | Versión 1.3                                                            |
|                          |                                                                        |
|                          | (                    )                                                 |
+--------------------------+------------------------------------------------------------------------+
| Prioridad:               | Alta                                                                   |
+--------------------------+------------------------------------------------------------------------+

+--------------------------+------------------------------------------------------------------------+
| Número:                  | **RF-DEV-04**                                                          |
+--------------------------+------------------------------------------------------------------------+
| Título:                  | Eliminar devolución                                                    |
+--------------------------+------------------------------------------------------------------------+
| Texto:                   | El sistema permitirá eliminar una devolución; el préstamo vuelve a     |
|                          | quedar pendiente y se ajusta el stock.                                 |
| DEFINICIÓN               |                                                                        |
+--------------------------+------------------------------------------------------------------------+
| Tipo:                    | Funcional                                                              |
+--------------------------+------------------------------------------------------------------------+
| Detalles de requisitos y | - Regla: Borra la devolución; el préstamo vuelve a ACTIVO o VENCIDO y  |
| restricciones:           |   se ajusta el stock.                                                  |
|                          |                                                                        |
| ESPECIFICACIÓN           | Mensajes de error que muestra el sistema:                              |
|                          |                                                                        |
|                          | - "No se puede eliminar: el ejemplar ya se volvió a prestar (stock 0)" |
|                          |                                                                        |
|                          | Implementado en: `DevolucionController.eliminarDevolucion`,            |
|                          | `DevolucionDAO.eliminar`                                               |
+--------------------------+------------------------------------------------------------------------+
| Fecha de revisión y      | 10/11/2026                                                             |
| versión, AUTOR:          |                                                                        |
|                          | Versión 1.3                                                            |
|                          |                                                                        |
|                          | (                    )                                                 |
+--------------------------+------------------------------------------------------------------------+
| Prioridad:               | Alta                                                                   |
+--------------------------+------------------------------------------------------------------------+

+--------------------------+------------------------------------------------------------------------+
| Número:                  | **RF-DEV-05**                                                          |
+--------------------------+------------------------------------------------------------------------+
| Título:                  | Validar préstamo pendiente                                             |
+--------------------------+------------------------------------------------------------------------+
| Texto:                   | El sistema solo permitirá devolver préstamos ACTIVO o VENCIDO que no   |
|                          | tengan devolución.                                                     |
| DEFINICIÓN               |                                                                        |
+--------------------------+------------------------------------------------------------------------+
| Tipo:                    | Funcional                                                              |
+--------------------------+------------------------------------------------------------------------+
| Detalles de requisitos y | - Regla: Solo préstamos ACTIVO o VENCIDO sin devolución.               |
| restricciones:           |                                                                        |
|                          | Mensajes de error que muestra el sistema:                              |
| ESPECIFICACIÓN           |                                                                        |
|                          | - "El préstamo no existe"                                              |
|                          |                                                                        |
|                          | - "El préstamo ya tiene una devolución registrada"                     |
|                          |                                                                        |
|                          | Implementado en: `DevolucionController.verificarPrestamoPendiente`,    |
|                          | `listarPrestamosPendientes`                                            |
+--------------------------+------------------------------------------------------------------------+
| Fecha de revisión y      | 10/11/2026                                                             |
| versión, AUTOR:          |                                                                        |
|                          | Versión 1.3                                                            |
|                          |                                                                        |
|                          | (                    )                                                 |
+--------------------------+------------------------------------------------------------------------+
| Prioridad:               | Alta                                                                   |
+--------------------------+------------------------------------------------------------------------+

+--------------------------+------------------------------------------------------------------------+
| Número:                  | **RF-DEV-06**                                                          |
+--------------------------+------------------------------------------------------------------------+
| Título:                  | Validar fecha de devolución                                            |
+--------------------------+------------------------------------------------------------------------+
| Texto:                   | El sistema validará la fecha de devolución.                            |
|                          |                                                                        |
| DEFINICIÓN               |                                                                        |
+--------------------------+------------------------------------------------------------------------+
| Tipo:                    | Funcional                                                              |
+--------------------------+------------------------------------------------------------------------+
| Detalles de requisitos y | - Regla: Obligatoria, fecha real, no futura, no anterior a la fecha    |
| restricciones:           |   del préstamo.                                                        |
|                          |                                                                        |
| ESPECIFICACIÓN           | Mensajes de error que muestra el sistema:                              |
|                          |                                                                        |
|                          | - "La fecha de devolución es obligatoria"                              |
|                          |                                                                        |
|                          | - "La fecha de devolución no puede ser futura"                         |
|                          |                                                                        |
|                          | - "La fecha de devolución no puede ser anterior a la fecha del         |
|                          |   préstamo"                                                            |
|                          |                                                                        |
|                          | Implementado en: `DevolucionController.validarFechaDevolucion`         |
+--------------------------+------------------------------------------------------------------------+
| Fecha de revisión y      | 10/11/2026                                                             |
| versión, AUTOR:          |                                                                        |
|                          | Versión 1.3                                                            |
|                          |                                                                        |
|                          | (                    )                                                 |
+--------------------------+------------------------------------------------------------------------+
| Prioridad:               | Alta                                                                   |
+--------------------------+------------------------------------------------------------------------+

+--------------------------+------------------------------------------------------------------------+
| Número:                  | **RF-DEV-07**                                                          |
+--------------------------+------------------------------------------------------------------------+
| Título:                  | Validar estado del libro                                               |
+--------------------------+------------------------------------------------------------------------+
| Texto:                   | El sistema permitirá elegir el estado del libro devuelto de una lista  |
|                          | y validará la observación.                                             |
| DEFINICIÓN               |                                                                        |
+--------------------------+------------------------------------------------------------------------+
| Tipo:                    | Funcional                                                              |
+--------------------------+------------------------------------------------------------------------+
| Detalles de requisitos y | - Regla: BUENO, DANADO o PERDIDO. Observación opcional, máx. 255.      |
| restricciones:           |                                                                        |
|                          | Mensajes de error que muestra el sistema:                              |
| ESPECIFICACIÓN           |                                                                        |
|                          | - "Debe seleccionar el estado del libro"                               |
|                          |                                                                        |
|                          | - "El estado del libro no es válido"                                   |
|                          |                                                                        |
|                          | - "La observación no puede superar 255 caracteres"                     |
|                          |                                                                        |
|                          | Implementado en: `DevolucionController.validarEstadoLibro`,            |
|                          | `validarObservacion`                                                   |
+--------------------------+------------------------------------------------------------------------+
| Fecha de revisión y      | 10/11/2026                                                             |
| versión, AUTOR:          |                                                                        |
|                          | Versión 1.3                                                            |
|                          |                                                                        |
|                          | (                    )                                                 |
+--------------------------+------------------------------------------------------------------------+
| Prioridad:               | Alta                                                                   |
+--------------------------+------------------------------------------------------------------------+

+--------------------------+------------------------------------------------------------------------+
| Número:                  | **RF-DEV-08**                                                          |
+--------------------------+------------------------------------------------------------------------+
| Título:                  | Calcular días de retraso                                               |
+--------------------------+------------------------------------------------------------------------+
| Texto:                   | El sistema calculará automáticamente los días de retraso de la         |
|                          | devolución.                                                            |
| DEFINICIÓN               |                                                                        |
+--------------------------+------------------------------------------------------------------------+
| Tipo:                    | Funcional                                                              |
+--------------------------+------------------------------------------------------------------------+
| Detalles de requisitos y | - Regla: Días entre la fecha límite y la de devolución; mínimo 0.      |
| restricciones:           |                                                                        |
|                          | Implementado en: `DevolucionController.calcularDiasRetraso`            |
| ESPECIFICACIÓN           |                                                                        |
+--------------------------+------------------------------------------------------------------------+
| Fecha de revisión y      | 10/11/2026                                                             |
| versión, AUTOR:          |                                                                        |
|                          | Versión 1.3                                                            |
|                          |                                                                        |
|                          | (                    )                                                 |
+--------------------------+------------------------------------------------------------------------+
| Prioridad:               | Alta                                                                   |
+--------------------------+------------------------------------------------------------------------+

+--------------------------+------------------------------------------------------------------------+
| Número:                  | **RF-DEV-09**                                                          |
+--------------------------+------------------------------------------------------------------------+
| Título:                  | Calcular multa                                                         |
+--------------------------+------------------------------------------------------------------------+
| Texto:                   | El sistema calculará automáticamente la multa de la devolución.        |
|                          |                                                                        |
| DEFINICIÓN               |                                                                        |
+--------------------------+------------------------------------------------------------------------+
| Tipo:                    | Funcional                                                              |
+--------------------------+------------------------------------------------------------------------+
| Detalles de requisitos y | - Regla: $0.50 × días de retraso, + $5.00 si DANADO, + $20.00 si       |
| restricciones:           |   PERDIDO.                                                             |
|                          |                                                                        |
| ESPECIFICACIÓN           | Implementado en: `DevolucionController.calcularMulta`                  |
+--------------------------+------------------------------------------------------------------------+
| Fecha de revisión y      | 10/11/2026                                                             |
| versión, AUTOR:          |                                                                        |
|                          | Versión 1.3                                                            |
|                          |                                                                        |
|                          | (                    )                                                 |
+--------------------------+------------------------------------------------------------------------+
| Prioridad:               | Alta                                                                   |
+--------------------------+------------------------------------------------------------------------+

+--------------------------+------------------------------------------------------------------------+
| Número:                  | **RF-DEV-10**                                                          |
+--------------------------+------------------------------------------------------------------------+
| Título:                  | Actualizar préstamo y stock                                            |
+--------------------------+------------------------------------------------------------------------+
| Texto:                   | El sistema actualizará el estado del préstamo y el stock del libro al  |
|                          | registrar una devolución.                                              |
| DEFINICIÓN               |                                                                        |
+--------------------------+------------------------------------------------------------------------+
| Tipo:                    | Funcional                                                              |
+--------------------------+------------------------------------------------------------------------+
| Detalles de requisitos y | - Regla: El préstamo pasa a DEVUELTO; el ejemplar vuelve al stock      |
| restricciones:           |   salvo si se perdió.                                                  |
|                          |                                                                        |
| ESPECIFICACIÓN           | Implementado en: `DevolucionDAO.insertar` (transacción)                |
+--------------------------+------------------------------------------------------------------------+
| Fecha de revisión y      | 10/11/2026                                                             |
| versión, AUTOR:          |                                                                        |
|                          | Versión 1.3                                                            |
|                          |                                                                        |
|                          | (                    )                                                 |
+--------------------------+------------------------------------------------------------------------+
| Prioridad:               | Alta                                                                   |
+--------------------------+------------------------------------------------------------------------+

#### 3.5.2 Casos de uso

![Figura 9. Casos de uso del módulo Devolución](diagramas/07_cu_devolucion.png){width=15cm}

##### CU-11 Registrar devolución
| Elemento | Descripción |
|----------|-------------|
| Actor | Usuario |
| Requerimientos | RF-DEV-01, RF-DEV-05 a RF-DEV-10 |
| Precondición | Existe un préstamo ACTIVO o VENCIDO sin devolución. |
| Flujo básico | 1. El usuario pulsa Nuevo en Devoluciones. 2. Elige el préstamo pendiente, escribe la fecha de devolución, el estado del libro y la observación. 3. El sistema valida los datos. 4. El sistema calcula días de retraso y multa. 5. El sistema registra la devolución, marca el préstamo como DEVUELTO, devuelve el ejemplar al stock (si no se perdió) y muestra los días de retraso y la multa. |
| Flujos alternos | 2a. Préstamo ya devuelto o inexistente: se rechaza. 3a. Fecha futura o anterior al préstamo: mensaje del campo. 5a. Estado PERDIDO: no se suma al stock y la multa incluye $20.00. |
| Postcondición | Devolución registrada; préstamo DEVUELTO. |

##### CU-12 Gestionar devoluciones (buscar, modificar, eliminar)
| Elemento | Descripción |
|----------|-------------|
| Actor | Usuario |
| Requerimientos | RF-DEV-02 a RF-DEV-04, RF-DEV-08 a RF-DEV-10 |
| Flujo básico | 1. El usuario busca o selecciona una devolución. 2. Modifica fecha, estado u observación (el sistema recalcula multa y stock) o la elimina (el préstamo vuelve a ACTIVO o VENCIDO). |
| Flujos alternos | Al eliminar, si el ejemplar ya se volvió a prestar (stock 0) no se puede descontar y se rechaza. |
| Postcondición | Devolución actualizada o eliminada. |

#### 3.5.3 Interfaces (Pantallas)

Capturas de las pantallas del módulo funcionando. Pegue cada captura en el cuadro correspondiente.

[[CAPTURA: Pantalla principal del módulo Devolución (tabla con las devoluciones)]]

[[CAPTURA: Formulario Nueva devolución con datos válidos]]

[[CAPTURA: Mensaje de éxito con días de retraso y multa calculada]]

[[CAPTURA: Mensaje de error de validación / confirmación al eliminar]]

