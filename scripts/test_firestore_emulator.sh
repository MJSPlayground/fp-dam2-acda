#!/bin/sh
# Firebase CLI starts/stops the official emulator around this command in CI.
set -eu
for project in guiado-solucion final-solucion; do
  (
    cd "proyectos/firestore/$project"
    sh mvnw -Dqa.emulador=true -Daprendizaje.fase=todas -Daprendizaje.nivel=todos test --batch-mode
  )
done
