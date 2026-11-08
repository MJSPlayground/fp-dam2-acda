#!/bin/sh
# The caller supplies an isolated MongoDB server and URI; tests use their own database.
set -eu
for project in guiado-solucion final-solucion; do
  (
    cd "proyectos/mongodb/$project"
    sh mvnw -Dqa.mongo=true -Daprendizaje.fase=todas -Daprendizaje.nivel=todos test --batch-mode
  )
done
