#!/bin/sh
# Tests create and remove only their own schema with a validated QA prefix.
set -eu
for project in guiado-solucion final-solucion; do
  (
    cd "proyectos/hibernate/$project"
    sh mvnw -Dqa.mysql=true -Dfase=completo test --batch-mode
  )
done
