-- =====================================================================
-- Sistema de Gestión de Biblioteca
-- Base de datos: bd_biblioteca (MySQL 8.x)
-- Proyecto Primer Parcial - Verificación y Validación de Software
--
-- Tablas:
--   usuario     -> solo lectura desde el sistema (login). Sin CRUD.
--   libro       -> módulo independiente
--   autor       -> módulo independiente
--   lector      -> módulo independiente
--   prestamo    -> depende de libro y lector
--   devolucion  -> depende de prestamo
--
-- Usuarios de prueba (contraseña en texto plano solo como referencia):
--   admin          / Admin123
--   bibliotecario  / Biblio123
--   invitado       / Invitado123   (INACTIVO: no debe poder ingresar)
--
-- Las fechas de préstamos y devoluciones se calculan respecto a la fecha
-- actual (CURDATE()), así los datos siempre tienen préstamos activos,
-- vencidos y devueltos sin importar el día en que se ejecute el script.
-- Volver a ejecutar este script borra y recrea toda la base.
-- =====================================================================

SET NAMES utf8mb4;

DROP DATABASE IF EXISTS bd_biblioteca;
CREATE DATABASE bd_biblioteca
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;
USE bd_biblioteca;

-- ---------------------------------------------------------------------
-- Tabla: usuario (autenticación)
-- La contraseña se guarda como hash SHA-256 en hexadecimal (64 caracteres).
-- ---------------------------------------------------------------------
CREATE TABLE usuario (
    id_usuario      INT AUTO_INCREMENT PRIMARY KEY,
    nombre_usuario  VARCHAR(30)  NOT NULL UNIQUE,
    contrasena      CHAR(64)     NOT NULL,
    nombres         VARCHAR(100) NOT NULL,
    rol             ENUM('ADMINISTRADOR', 'BIBLIOTECARIO') NOT NULL,
    activo          BOOLEAN      NOT NULL DEFAULT TRUE,
    fecha_registro  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE = InnoDB;

-- ---------------------------------------------------------------------
-- Tabla: libro
-- ---------------------------------------------------------------------
CREATE TABLE libro (
    id_libro          INT AUTO_INCREMENT PRIMARY KEY,
    isbn              VARCHAR(13)  NOT NULL UNIQUE,
    titulo            VARCHAR(150) NOT NULL,
    editorial         VARCHAR(100) NOT NULL,
    anio_publicacion  INT          NOT NULL,
    genero            VARCHAR(30)  NOT NULL,
    numero_paginas    INT          NOT NULL,
    stock             INT          NOT NULL DEFAULT 0,
    CONSTRAINT chk_libro_paginas CHECK (numero_paginas BETWEEN 1 AND 5000),
    CONSTRAINT chk_libro_stock   CHECK (stock BETWEEN 0 AND 500)
) ENGINE = InnoDB;

-- ---------------------------------------------------------------------
-- Tabla: autor
-- ---------------------------------------------------------------------
CREATE TABLE autor (
    id_autor          INT AUTO_INCREMENT PRIMARY KEY,
    nombres           VARCHAR(60)  NOT NULL,
    apellidos         VARCHAR(60)  NOT NULL,
    nacionalidad      VARCHAR(50)  NOT NULL,
    fecha_nacimiento  DATE         NULL,
    biografia         VARCHAR(255) NULL
) ENGINE = InnoDB;

-- ---------------------------------------------------------------------
-- Tabla: lector
-- ---------------------------------------------------------------------
CREATE TABLE lector (
    id_lector  INT AUTO_INCREMENT PRIMARY KEY,
    cedula     CHAR(10)     NOT NULL UNIQUE,
    nombres    VARCHAR(60)  NOT NULL,
    apellidos  VARCHAR(60)  NOT NULL,
    correo     VARCHAR(100) NOT NULL UNIQUE,
    telefono   VARCHAR(10)  NOT NULL,
    direccion  VARCHAR(150) NULL,
    estado     ENUM('ACTIVO', 'SUSPENDIDO') NOT NULL DEFAULT 'ACTIVO'
) ENGINE = InnoDB;

-- ---------------------------------------------------------------------
-- Tabla: prestamo (depende de libro y lector)
-- ---------------------------------------------------------------------
CREATE TABLE prestamo (
    id_prestamo     INT AUTO_INCREMENT PRIMARY KEY,
    id_libro        INT  NOT NULL,
    id_lector       INT  NOT NULL,
    fecha_prestamo  DATE NOT NULL,
    fecha_limite    DATE NOT NULL,
    estado          ENUM('ACTIVO', 'DEVUELTO', 'VENCIDO') NOT NULL DEFAULT 'ACTIVO',
    observacion     VARCHAR(255) NULL,
    CONSTRAINT fk_prestamo_libro  FOREIGN KEY (id_libro)  REFERENCES libro (id_libro),
    CONSTRAINT fk_prestamo_lector FOREIGN KEY (id_lector) REFERENCES lector (id_lector),
    CONSTRAINT chk_prestamo_fechas CHECK (fecha_limite >= fecha_prestamo)
) ENGINE = InnoDB;

-- ---------------------------------------------------------------------
-- Tabla: devolucion (depende de prestamo; un préstamo tiene una devolución)
-- ---------------------------------------------------------------------
CREATE TABLE devolucion (
    id_devolucion     INT AUTO_INCREMENT PRIMARY KEY,
    id_prestamo       INT           NOT NULL UNIQUE,
    fecha_devolucion  DATE          NOT NULL,
    estado_libro      ENUM('BUENO', 'DANADO', 'PERDIDO') NOT NULL DEFAULT 'BUENO',
    dias_retraso      INT           NOT NULL DEFAULT 0,
    multa             DECIMAL(8, 2) NOT NULL DEFAULT 0.00,
    observacion       VARCHAR(255)  NULL,
    CONSTRAINT fk_devolucion_prestamo FOREIGN KEY (id_prestamo) REFERENCES prestamo (id_prestamo)
) ENGINE = InnoDB;

-- =====================================================================
-- DATOS DE PRUEBA
-- =====================================================================

-- ---------------------------------------------------------------------
-- Usuarios (insertados directamente: el sistema no tiene CRUD de usuarios)
-- ---------------------------------------------------------------------
INSERT INTO usuario (nombre_usuario, contrasena, nombres, rol, activo) VALUES
('admin',         SHA2('Admin123', 256),    'Administrador del Sistema', 'ADMINISTRADOR', TRUE),
('bibliotecario', SHA2('Biblio123', 256),   'Bibliotecario de Turno',    'BIBLIOTECARIO', TRUE),
('invitado',      SHA2('Invitado123', 256), 'Usuario Inactivo',          'BIBLIOTECARIO', FALSE);

-- ---------------------------------------------------------------------
-- Libros (id 1 a 15). El stock es la cantidad de ejemplares disponibles
-- en este momento (ya descontados los préstamos activos y vencidos).
-- ---------------------------------------------------------------------
INSERT INTO libro (isbn, titulo, editorial, anio_publicacion, genero, numero_paginas, stock) VALUES
('9789978671234', 'Huasipungo',                      'Libresa',              1934, 'Novela',     240, 5),
('9780307474728', 'Cien años de soledad',            'Vintage Español',      1967, 'Novela',     496, 2),
('9788437604947', 'Pedro Páramo',                    'Cátedra',              1955, 'Novela',     136, 4),
('9788420412146', 'El túnel',                        'Alianza Editorial',    1948, 'Novela',     160, 1),
('9789974710245', 'Cuentos de la selva',             'Losada',               1918, 'Cuento',     128, 6),
('9788491050292', 'Veinte poemas de amor',           'Austral',              1924, 'Poesía',     104, 2),
('9780132350884', 'Clean Code',                      'Prentice Hall',        2008, 'Tecnología', 464, 1),
('9780201633610', 'Design Patterns',                 'Addison-Wesley',       1994, 'Tecnología', 395, 0),
('9788498382662', 'Breve historia del tiempo',       'Crítica',              1988, 'Ciencia',    256, 2),
('9789978043307', 'Historia del Ecuador',            'Corporación Editora',  2008, 'Historia',   320, 0),
('9788478884452', 'Harry Potter y la piedra filosofal', 'Salamandra',        1997, 'Infantil',   256, 3),
('9788497592208', 'El principito',                   'Salamandra',           1943, 'Infantil',    96, 4),
('9788420674209', 'La ciudad y los perros',          'Alfaguara',            1963, 'Novela',     480, 2),
('9780131103627', 'The C Programming Language',      'Prentice Hall',        1988, 'Tecnología', 272, 2),
('9788408163435', 'El origen de las especies',       'Planeta',              1859, 'Ciencia',    512, 1);

-- ---------------------------------------------------------------------
-- Autores (id 1 a 10)
-- ---------------------------------------------------------------------
INSERT INTO autor (nombres, apellidos, nacionalidad, fecha_nacimiento, biografia) VALUES
('Jorge',          'Icaza',            'Ecuatoriana',    '1906-07-10', 'Novelista ecuatoriano, autor de Huasipungo.'),
('Gabriel',        'García Márquez',   'Colombiana',     '1927-03-06', 'Premio Nobel de Literatura 1982.'),
('Juan',           'Rulfo',            'Mexicana',       '1917-05-16', 'Autor de Pedro Páramo y El llano en llamas.'),
('Ernesto',        'Sabato',           'Argentina',      '1911-06-24', 'Escritor y físico argentino.'),
('Robert',         'Martin',           'Estadounidense', '1952-12-05', 'Ingeniero de software, autor de Clean Code.'),
('Horacio',        'Quiroga',          'Uruguaya',       '1878-12-31', 'Maestro del cuento latinoamericano.'),
('Pablo',          'Neruda',           'Chilena',        '1904-07-12', 'Poeta, Premio Nobel de Literatura 1971.'),
('Mario',          'Vargas Llosa',     'Peruana',        '1936-03-28', 'Novelista, Premio Nobel de Literatura 2010.'),
('Antoine',        'de Saint-Exupéry', 'Francesa',       '1900-06-29', 'Aviador y escritor, autor de El principito.'),
('Joanne',         'Rowling',          'Británica',      NULL,         NULL);

-- ---------------------------------------------------------------------
-- Lectores (id 1 a 10). Todas las cédulas son válidas (dígito verificador).
-- ---------------------------------------------------------------------
INSERT INTO lector (cedula, nombres, apellidos, correo, telefono, direccion, estado) VALUES
('0922601810', 'María José',   'Pérez Loor',      'maria.perez@correo.com',     '0991234567', 'Guayaquil, Urdesa',        'ACTIVO'),
('0929083012', 'Carlos',       'Andrade Ruiz',    'carlos.andrade@correo.com',  '0982345678', 'Guayaquil, Alborada',      'ACTIVO'),
('0936131861', 'Lucía',        'Mendoza Vera',    'lucia.mendoza@correo.com',   '0973456789', 'Durán, Centro',            'ACTIVO'),
('1709139099', 'Andrés',       'Salazar Mora',    'andres.salazar@correo.com',  '0964567890', 'Quito, La Carolina',       'SUSPENDIDO'),
('0146030820', 'Valeria',      'Cordero Ríos',    'valeria.cordero@correo.com', '0995678901', 'Cuenca, El Vergel',        'ACTIVO'),
('0926281940', 'José Luis',    'Torres Bajaña',   'jose.torres@correo.com',     '0986789012', 'Guayaquil, Sauces 6',      'ACTIVO'),
('1342199351', 'Gabriela',     'Zambrano Cedeño', 'gabriela.zambrano@correo.com','0977890123','Manta, Barrio Córdova',    'ACTIVO'),
('0908190937', 'Pedro',        'Villacís León',   'pedro.villacis@correo.com',  '0968901234', 'Guayaquil, Centenario',    'ACTIVO'),
('0738657972', 'Daniela',      'Ortiz Pazmiño',   'daniela.ortiz@correo.com',   '0999012345', 'Machala, Centro',          'SUSPENDIDO'),
('0924323199', 'Kevin',        'Castro Suárez',   'kevin.castro@correo.com',    '0980123456', NULL,                       'ACTIVO');

-- ---------------------------------------------------------------------
-- Préstamos (id 1 a 10)
--   ACTIVO   : 2, 6, 8, 10        (fecha límite futura)
--   VENCIDO  : 3, 9               (fecha límite pasada, sin devolución)
--   DEVUELTO : 1, 4, 5, 7         (tienen devolución registrada)
-- ---------------------------------------------------------------------
INSERT INTO prestamo (id_libro, id_lector, fecha_prestamo, fecha_limite, estado, observacion) VALUES
(1,  1, CURDATE() - INTERVAL 20 DAY, CURDATE() - INTERVAL 13 DAY, 'DEVUELTO', 'Préstamo regular'),
(2,  2, CURDATE() - INTERVAL 5 DAY,  CURDATE() + INTERVAL 2 DAY,  'ACTIVO',   NULL),
(7,  3, CURDATE() - INTERVAL 25 DAY, CURDATE() - INTERVAL 18 DAY, 'VENCIDO',  'No se ha devuelto'),
(3,  4, CURDATE() - INTERVAL 30 DAY, CURDATE() - INTERVAL 23 DAY, 'DEVUELTO', NULL),
(5,  5, CURDATE() - INTERVAL 15 DAY, CURDATE() - INTERVAL 8 DAY,  'DEVUELTO', NULL),
(4,  1, CURDATE() - INTERVAL 2 DAY,  CURDATE() + INTERVAL 5 DAY,  'ACTIVO',   'Para trabajo de clase'),
(6,  6, CURDATE() - INTERVAL 40 DAY, CURDATE() - INTERVAL 33 DAY, 'DEVUELTO', NULL),
(11, 7, CURDATE() - INTERVAL 1 DAY,  CURDATE() + INTERVAL 6 DAY,  'ACTIVO',   NULL),
(12, 2, CURDATE() - INTERVAL 10 DAY, CURDATE() - INTERVAL 3 DAY,  'VENCIDO',  NULL),
(8,  8, CURDATE(),                   CURDATE() + INTERVAL 7 DAY,  'ACTIVO',   NULL);

-- ---------------------------------------------------------------------
-- Devoluciones (id 1 a 4). Multa: $0.50 por día de retraso,
-- + $5.00 si el libro vuelve dañado, + $20.00 si se perdió.
-- ---------------------------------------------------------------------
INSERT INTO devolucion (id_prestamo, fecha_devolucion, estado_libro, dias_retraso, multa, observacion) VALUES
(1, CURDATE() - INTERVAL 13 DAY, 'BUENO',   0, 0.00,  'Devuelto a tiempo'),
(4, CURDATE() - INTERVAL 20 DAY, 'BUENO',   3, 1.50,  'Devuelto con 3 días de retraso'),
(5, CURDATE() - INTERVAL 8 DAY,  'DANADO',  0, 5.00,  'Portada rota'),
(7, CURDATE() - INTERVAL 30 DAY, 'PERDIDO', 3, 21.50, 'El lector reportó la pérdida');
