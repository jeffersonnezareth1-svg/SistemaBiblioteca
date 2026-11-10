"""Convierte todos los RF del documento al formato del docente (tabla de 7 filas)."""
import re, sys, textwrap

P = sys.argv[1]
s = open(P).read()
FECHA = '10/11/2026'
VERSION = 'Versión 1.3'
NB = ' ' * 20

TEXTO = {
 'RF-LIB-06': 'El sistema validará que el ISBN ingresado sea obligatorio, numérico y de 10 o 13 dígitos antes de registrar o modificar un libro.',
 'RF-LIB-07': 'El sistema no permitirá registrar ni modificar un libro con un ISBN que ya pertenezca a otro libro.',
 'RF-LIB-08': 'El sistema validará que el título del libro sea obligatorio y tenga entre 2 y 150 caracteres.',
 'RF-LIB-09': 'El sistema validará que la editorial sea obligatoria, tenga entre 2 y 100 caracteres y solo contenga caracteres permitidos.',
 'RF-LIB-10': 'El sistema validará que el año de publicación sea obligatorio, numérico y esté entre 1450 y el año actual.',
 'RF-LIB-11': 'El sistema permitirá elegir el género del libro solo de una lista de valores permitidos.',
 'RF-LIB-12': 'El sistema validará que el número de páginas sea obligatorio y un entero entre 1 y 5000.',
 'RF-LIB-13': 'El sistema validará que el stock sea obligatorio y un entero entre 0 y 500 ejemplares.',
 'RF-LIB-14': 'El sistema no permitirá eliminar un libro que tenga préstamos registrados.',
 'RF-LIB-15': 'El sistema exigirá que haya un libro seleccionado y que exista en la base de datos para modificarlo o eliminarlo.',
 'RF-LOG-02': 'El sistema validará el nombre de usuario escrito en la ventana de inicio de sesión.',
 'RF-LOG-03': 'El sistema validará la contraseña escrita en la ventana de inicio de sesión.',
 'RF-LOG-04': 'El sistema negará el ingreso cuando el usuario no exista o la contraseña no coincida, sin indicar cuál de los dos falló.',
 'RF-LOG-05': 'El sistema no permitirá ingresar a un usuario inactivo aunque su contraseña sea correcta.',
 'RF-LOG-06': 'El sistema se cerrará después de 3 intentos fallidos de inicio de sesión.',
 'RF-AUT-01': 'El sistema permitirá el ingreso de los datos de un autor nuevo (nombres, apellidos, nacionalidad, fecha de nacimiento y biografía) y lo guardará en la base de datos.',
 'RF-AUT-02': 'El sistema mostrará todos los autores registrados y permitirá buscarlos por nombres, apellidos o nacionalidad.',
 'RF-AUT-03': 'El sistema permitirá modificar los datos del autor seleccionado.',
 'RF-AUT-04': 'El sistema permitirá eliminar el autor seleccionado, previa confirmación.',
 'RF-AUT-05': 'El sistema validará los nombres y apellidos del autor.',
 'RF-AUT-06': 'El sistema validará la nacionalidad del autor.',
 'RF-AUT-07': 'El sistema validará la fecha de nacimiento del autor cuando se escriba.',
 'RF-AUT-08': 'El sistema validará la longitud de la biografía del autor.',
 'RF-AUT-09': 'El sistema no permitirá registrar dos veces un autor con los mismos nombres y apellidos.',
 'RF-LEC-01': 'El sistema permitirá el ingreso de los datos de un lector nuevo (cédula, nombres, apellidos, correo, teléfono, dirección y estado) y lo guardará en la base de datos.',
 'RF-LEC-02': 'El sistema mostrará todos los lectores registrados y permitirá buscarlos por cédula, nombres, apellidos o correo.',
 'RF-LEC-03': 'El sistema permitirá modificar los datos del lector seleccionado.',
 'RF-LEC-04': 'El sistema permitirá eliminar el lector seleccionado, previa confirmación.',
 'RF-LEC-05': 'El sistema validará que la cédula sea una cédula ecuatoriana válida.',
 'RF-LEC-06': 'El sistema validará los nombres y apellidos del lector.',
 'RF-LEC-07': 'El sistema validará el correo electrónico del lector.',
 'RF-LEC-08': 'El sistema validará el número de celular del lector.',
 'RF-LEC-09': 'El sistema validará la dirección y el estado del lector.',
 'RF-LEC-10': 'El sistema no permitirá que dos lectores tengan la misma cédula o el mismo correo.',
 'RF-LEC-11': 'El sistema no permitirá eliminar un lector que tenga préstamos registrados.',
 'RF-PRE-01': 'El sistema permitirá registrar el préstamo de un libro a un lector, con fecha de préstamo, fecha límite y observación; el préstamo queda en estado ACTIVO.',
 'RF-PRE-02': 'El sistema mostrará todos los préstamos y permitirá buscarlos por título del libro, lector, cédula o estado.',
 'RF-PRE-03': 'El sistema permitirá modificar un préstamo que todavía no ha sido devuelto.',
 'RF-PRE-04': 'El sistema permitirá eliminar un préstamo que no tenga devolución, devolviendo el ejemplar al stock.',
 'RF-PRE-05': 'El sistema exigirá elegir de una lista el libro y el lector del préstamo.',
 'RF-PRE-06': 'El sistema validará la fecha del préstamo.',
 'RF-PRE-07': 'El sistema validará la fecha límite de devolución del préstamo.',
 'RF-PRE-08': 'El sistema no permitirá prestar un libro que no tenga ejemplares disponibles.',
 'RF-PRE-09': 'El sistema solo permitirá prestar a lectores ACTIVOS con menos de 3 préstamos sin devolver.',
 'RF-PRE-10': 'El sistema actualizará el stock del libro al registrar o eliminar un préstamo.',
 'RF-PRE-11': 'El sistema mostrará como VENCIDO un préstamo ACTIVO cuya fecha límite ya pasó.',
 'RF-PRE-12': 'El sistema validará la longitud de la observación del préstamo.',
 'RF-DEV-01': 'El sistema permitirá registrar la devolución de un préstamo pendiente, con fecha de devolución, estado del libro y observación.',
 'RF-DEV-02': 'El sistema mostrará todas las devoluciones y permitirá buscarlas por título, lector o estado del libro.',
 'RF-DEV-03': 'El sistema permitirá modificar la fecha, el estado del libro o la observación de una devolución, recalculando la multa y el stock.',
 'RF-DEV-04': 'El sistema permitirá eliminar una devolución; el préstamo vuelve a quedar pendiente y se ajusta el stock.',
 'RF-DEV-05': 'El sistema solo permitirá devolver préstamos ACTIVO o VENCIDO que no tengan devolución.',
 'RF-DEV-06': 'El sistema validará la fecha de devolución.',
 'RF-DEV-07': 'El sistema permitirá elegir el estado del libro devuelto de una lista y validará la observación.',
 'RF-DEV-08': 'El sistema calculará automáticamente los días de retraso de la devolución.',
 'RF-DEV-09': 'El sistema calculará automáticamente la multa de la devolución.',
 'RF-DEV-10': 'El sistema actualizará el estado del préstamo y el stock del libro al registrar una devolución.',
}

IMPL = {
 'RF-AUT-01': '`AutorController.registrarAutor`, `AutorDAO.insertar`',
 'RF-AUT-02': '`AutorController.listarAutores`, `buscarAutores`, `AutorDAO.buscar`',
 'RF-AUT-03': '`AutorController.actualizarAutor`, `AutorDAO.actualizar`',
 'RF-AUT-04': '`AutorController.eliminarAutor`, `AutorDAO.eliminar`',
 'RF-AUT-05': '`AutorController.validarNombres`, `validarApellidos`',
 'RF-AUT-06': '`AutorController.validarNacionalidad`',
 'RF-AUT-07': '`AutorController.validarFechaNacimiento`',
 'RF-AUT-08': '`AutorController.validarBiografia`',
 'RF-AUT-09': '`AutorDAO.existeNombre`',
 'RF-LEC-01': '`LectorController.registrarLector`, `LectorDAO.insertar`',
 'RF-LEC-02': '`LectorController.listarLectores`, `buscarLectores`, `LectorDAO.buscar`',
 'RF-LEC-03': '`LectorController.actualizarLector`, `LectorDAO.actualizar`',
 'RF-LEC-04': '`LectorController.eliminarLector`, `LectorDAO.eliminar`',
 'RF-LEC-05': '`LectorController.validarCedula`, `digitoVerificadorValido`',
 'RF-LEC-06': '`LectorController.validarNombres`, `validarApellidos`',
 'RF-LEC-07': '`LectorController.validarCorreo`',
 'RF-LEC-08': '`LectorController.validarTelefono`',
 'RF-LEC-09': '`LectorController.validarDireccion`, `validarEstado`',
 'RF-LEC-10': '`LectorDAO.existeCedula`, `existeCorreo`',
 'RF-LEC-11': '`LectorDAO.tienePrestamos`',
 'RF-PRE-01': '`PrestamoController.registrarPrestamo`, `PrestamoDAO.insertar`',
 'RF-PRE-02': '`PrestamoController.listarPrestamos`, `buscarPrestamos`, `PrestamoDAO.buscar`',
 'RF-PRE-03': '`PrestamoController.actualizarPrestamo`, `PrestamoDAO.actualizar`',
 'RF-PRE-04': '`PrestamoController.eliminarPrestamo`, `PrestamoDAO.eliminar`, `tieneDevolucion`',
 'RF-PRE-05': '`PrestamoController.validarLibro`, `validarLector`',
 'RF-PRE-06': '`PrestamoController.validarFechaPrestamo`',
 'RF-PRE-07': '`PrestamoController.validarFechaLimite`',
 'RF-PRE-08': '`PrestamoController.verificarStock`',
 'RF-PRE-09': '`PrestamoController.verificarLector`, `PrestamoDAO.contarPendientesLector`',
 'RF-PRE-10': '`PrestamoDAO.insertar`, `actualizar`, `eliminar` (transacción)',
 'RF-PRE-11': '`PrestamoController.calcularEstado`',
 'RF-PRE-12': '`PrestamoController.validarObservacion`',
 'RF-DEV-01': '`DevolucionController.registrarDevolucion`, `DevolucionDAO.insertar`',
 'RF-DEV-02': '`DevolucionController.listarDevoluciones`, `buscarDevoluciones`, `DevolucionDAO.buscar`',
 'RF-DEV-03': '`DevolucionController.actualizarDevolucion`, `calcularAjusteStock`, `DevolucionDAO.actualizar`',
 'RF-DEV-04': '`DevolucionController.eliminarDevolucion`, `DevolucionDAO.eliminar`',
 'RF-DEV-05': '`DevolucionController.verificarPrestamoPendiente`, `listarPrestamosPendientes`',
 'RF-DEV-06': '`DevolucionController.validarFechaDevolucion`',
 'RF-DEV-07': '`DevolucionController.validarEstadoLibro`, `validarObservacion`',
 'RF-DEV-08': '`DevolucionController.calcularDiasRetraso`',
 'RF-DEV-09': '`DevolucionController.calcularMulta`',
 'RF-DEV-10': '`DevolucionDAO.insertar` (transacción)',
}

def autor(rid):
    return 'Grupo' if rid.split('-')[1] in ('GEN', 'LOG', 'MEN') else f'({NB})'

def grid(rows, w1=24, w2=70):
    """rows: list of (label_paragraphs, value_paragraphs); each paragraph a str ('- ' = viñeta)."""
    sep = '+' + '-' * (w1 + 2) + '+' + '-' * (w2 + 2) + '+'
    out = [sep]
    for k, (left, right) in enumerate(rows):
        def wrap(pars, w):
            lines = []
            for i, p in enumerate(pars):
                if i:
                    lines.append('')
                if p.startswith('- '):
                    ls = textwrap.wrap(p[2:], w - 2, break_long_words=False, break_on_hyphens=False) or ['']
                    lines += ['- ' + ls[0]] + ['  ' + x for x in ls[1:]]
                else:
                    lines += textwrap.wrap(p, w, break_long_words=False, break_on_hyphens=False) or ['']
            return lines
        L, R = wrap(left, w1), wrap(right, w2)
        if any(len(x) > w1 for x in L) or any(len(x) > w2 for x in R):
            raise SystemExit(f'celda demasiado ancha: {L} {R}')
        n = max(len(L), len(R))
        L += [''] * (n - len(L)); R += [''] * (n - len(R))
        for a, b in zip(L, R):
            out.append(f'| {a.ljust(w1)} | {b.ljust(w2)} |')
        out.append(sep)
    # bullet lists inside cells need a blank line before them
    return '\n'.join(out)

def card(rid, title, texto, detalles, prioridad):
    rows = [
        (['Número:'], [f'**{rid}**']),
        (['Título:'], [title]),
        (['Texto:', 'DEFINICIÓN'], [texto]),
        (['Tipo:'], ['Funcional']),
        (['Detalles de requisitos y restricciones:', 'ESPECIFICACIÓN'], detalles),
        (['Fecha de revisión y versión, AUTOR:'], [FECHA, VERSION, autor(rid)]),
        (['Prioridad:'], [prioridad]),
    ]
    return grid(rows)

def fix_cap(t):
    t = t.strip()
    return t[0].upper() + t[1:] if t else t

# ---- 1) fichas existentes (RF-GEN/LOG/MEN/LIB) ----
lines = s.split('\n')
out = []
i = 0
head = re.compile(r'^\| \*\*(RF-[A-Z]+-\d+)\*\* \| \*\*(.+)\*\* \|$')
while i < len(lines):
    m = head.match(lines[i])
    if not m:
        out.append(lines[i]); i += 1; continue
    rid, title = m.groups()
    i += 2
    fields = {}
    while i < len(lines) and lines[i].startswith('|'):
        k, v = [x.strip() for x in lines[i].strip('|').split('|', 1)]
        fields[k] = v.replace('\\|', '|'); i += 1
    # tablas de condición y viñetas que siguen a la ficha
    extra = []
    j = i
    while j < len(lines):
        if lines[j] == '':
            j += 1; continue
        if lines[j].startswith('| Condición'):
            hdr = [x.strip() for x in lines[j].strip('|').split('|')]
            j += 2
            while j < len(lines) and lines[j].startswith('|'):
                c = [x.strip() for x in lines[j].strip('|').split('|')]
                if len(c) == 3:
                    extra.append(f'- {c[0]} (ej. {c[1]}): {c[2]}' if c[2] != '(sin error)' else f'- {c[0]} (ej. {c[1]}): se acepta')
                else:
                    extra.append(f'- {c[0]}: "{c[1]}"')
                j += 1
            continue
        if lines[j].startswith('* **Valores límite:**'):
            extra.append('Valores límite: ' + lines[j].split(':**', 1)[1].strip()); j += 1; continue
        break
    i = j
    texto = fields.get('Descripción')
    texto = fix_cap(texto) if texto else TEXTO[rid]
    det = []
    for k in ('Entradas', 'Proceso', 'Salidas', 'Regla', 'Mensaje'):
        if k in fields:
            det.append(f'- {k}: {fix_cap(fields[k])}')
    if extra:
        cond = [e for e in extra if e.startswith('- ')]
        if cond:
            det.append('Condiciones y mensaje que muestra el sistema:')
            det += cond
        det += [e for e in extra if not e.startswith('- ')]
    if 'Criterio de aceptación' in fields:
        det.append('Criterio de aceptación: ' + fix_cap(fields['Criterio de aceptación']))
    if 'Implementado en' in fields:
        det.append('Implementado en: ' + fields['Implementado en'])
    if not det:
        det = ['-']
    out.append(card(rid, title, texto, det, fields.get('Prioridad', 'Media')))
    out.append('')
s = '\n'.join(out)

# ---- 2) tablas de 5 columnas (Autor, Lector, Préstamo, Devolución) ----
def repl(m):
    rows = [r for r in m.group(0).strip().split('\n')[2:]]
    summary = ['| ID | Nombre | Prioridad |', '|----|--------|-----------|']
    cards = []
    for r in rows:
        c = [x.strip() for x in r.strip('|').split('|')]
        rid, nombre, regla, msg, prio = c
        summary.append(f'| {rid} | {nombre} | {prio} |')
        det = [f'- Regla: {fix_cap(regla)}']
        msgs = [x.strip() for x in msg.split(' / ') if x.strip() and not x.strip().startswith('—')]
        ok = re.search(r'\("(.+)"\)', msg)
        if ok:
            det.append(f'- Mensaje de éxito: "{ok.group(1)}"')
        if msgs and not ok:
            det.append('Mensajes de error que muestra el sistema:')
            det += [f'- "{fix_cap(x)}"' for x in msgs]
        det.append('Implementado en: ' + IMPL[rid])
        cards.append(card(rid, nombre, TEXTO[rid], det, prio))
    return '##### Resumen\n\n' + '\n'.join(summary) + '\n\n##### Especificación\n\n' + '\n\n'.join(cards) + '\n'

s = re.sub(r'\| ID \| Nombre \| Descripción / regla \| Mensaje de error esperado \| Prioridad \|\n\|[-| ]+\|\n(\|.*\|\n)+', repl, s)
s = re.sub(r'\n{3,}', '\n\n', s)
open(P, 'w').write(s)
print('fichas:', s.count('| Número:'))
