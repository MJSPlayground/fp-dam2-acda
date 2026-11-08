#!/bin/sh
# Ejecuta los oráculos de aprendizaje completos sobre las soluciones locales.
set -eu
if [ "$#" -eq 0 ]; then
  set -- ficheros jdbc
fi
for topic in "$@"; do
  case "$topic" in ficheros|jdbc) ;; *) echo "Tema local desconocido: $topic" >&2; exit 2 ;; esac
  for project in guiado-solucion final-solucion; do
    (
      cd "proyectos/$topic/$project"
      case "$topic" in
        ficheros) sh mvnw -Dfase=completo -Dtest=FasesGuiadoTest,FasesFinalTest test --batch-mode ;;
        jdbc) sh mvnw -Daprendizaje.fase=todas -Daprendizaje.nivel=todos -Dtest=AprendizajeGuiadoTest,AprendizajeFinalTest test --batch-mode ;;
      esac
    )
  done
done
