#!/bin/sh
# Integration tests create and remove only their own isolated QA schema.
set -eu
for project in guiado-solucion final-solucion; do
  (
    cd "proyectos/spring/$project"
    sh mvnw -Dqa.mysql=true -Daprendizaje.fase=todas -Daprendizaje.nivel=todos test --batch-mode
  )
done
