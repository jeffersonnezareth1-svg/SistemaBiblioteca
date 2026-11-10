# Diagramas y generación del documento

* `*.puml`: fuentes PlantUML de los diagramas (estilo común en `estilo.iuml`); `*.png`: imágenes exportadas.
* Regenerar las imágenes: `java -jar plantuml.jar -tpng -charset UTF-8 0*.puml`
* Regenerar el Word (desde `docs/`):

```bash
pandoc Requerimientos_y_Casos_de_Uso.md -o Requerimientos_y_Casos_de_Uso.docx \
  --reference-doc=diagramas/fuente_docx/ref.docx --resource-path=.
python3 diagramas/fuente_docx/build.py main Requerimientos_y_Casos_de_Uso.docx
```
