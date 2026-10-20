# Guía rápida de GitHub para el equipo

Repositorio: https://github.com/jeffersonnezareth1-svg/SistemaBiblioteca

## 1. JEFF: invitar a los compañeros

1. Abre el repositorio en GitHub y entra a **Settings** (pestaña de arriba).
2. Menú izquierdo: **Collaborators** (puede pedirte tu contraseña).
3. Clic en **Add people**, escribe el usuario o correo de GitHub del compañero y clic en **Add ... to this repository**.
4. El compañero recibe un correo y debe dar clic en **Accept invitation**.

Repite con cada integrante.

## 2. Cada compañero: descargar el proyecto (una sola vez)

Necesitan tener instalado **Git** (https://git-scm.com/download/win).

Abrir **Git Bash** (o la terminal) en la carpeta donde quieran el proyecto y escribir:

```bash
git clone https://github.com/jeffersonnezareth1-svg/SistemaBiblioteca.git
cd SistemaBiblioteca
git config user.name "Tu Nombre"
git config user.email "tu-correo@ejemplo.com"
```

Luego abrir la carpeta en NetBeans (File > Open Project). En `src/main/java/util/Conexion.java` cada uno pone **su propia** contraseña de MySQL, y ejecuta `database/bd_biblioteca.sql` en su MySQL.

> Importante: no suban su contraseña real. Antes de hacer commit, dejen `CONTRASENA = ""` o no agreguen ese archivo.

## 3. Cada compañero: crear su rama

Una rama por persona/módulo, por ejemplo:

```bash
git checkout -b pruebas-autor
```

Nombres sugeridos: `pruebas-libro`, `pruebas-autor`, `pruebas-lector`, `pruebas-prestamo`, `pruebas-devolucion`.

## 4. Guardar y subir cambios

Cada vez que terminen algo:

```bash
git add .
git commit -m "Agrego pruebas JUnit del módulo Autor"
git push -u origin pruebas-autor
```

(Las siguientes veces basta con `git push`.)

## 5. Juntar el trabajo en main

1. En GitHub aparecerá el botón **Compare & pull request**. Clic ahí.
2. Escribir qué se hizo y clic en **Create pull request**.
3. JEFF lo revisa y da clic en **Merge pull request**.

## 6. Traer los cambios de los demás

```bash
git checkout main
git pull
git checkout pruebas-autor
git merge main
```

## Reglas del equipo

- Nadie trabaja directo en `main`.
- Cada uno toca solo los archivos de su módulo (y sus pruebas).
- Hacer `git pull` en main antes de empezar a trabajar cada día.
