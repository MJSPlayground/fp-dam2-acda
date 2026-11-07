"""Ejecuta los ejemplos reales de Ampliación en una base QA nueva de MongoDB.

No modifica bases existentes. La base aislada vive en el contenedor QA temporal.
"""
import argparse
import json
from pathlib import Path
import re
import subprocess
import uuid


def check(container):
    root = Path(__file__).resolve().parent.parent
    source = (root / "docs/mongodb/ampliacion.md").read_text(encoding="utf-8")
    blocks = re.findall(r"```javascript\s*\n(.*?)```", source, re.S)
    assert len(blocks) == 5, "Revisar la correspondencia entre ejemplos y comprobaciones"
    database = "acda_qa_ampliacion_" + uuid.uuid4().hex[:12]
    script = f"db = db.getSiblingDB({json.dumps(database)});\n" + """
function qaEq(expected,actual){if(expected!==actual)throw new Error("Expected "+expected+" got "+actual);}
function qaAssert(value){if(!value)throw new Error("Assertion failed");}
db.sedes.insertOne({_id:"CENTRO",nombre:"Pabellón Centro"});
db.equipos.insertMany([
 {nombre:"A",categoria:"SENIOR",sedeId:"CENTRO",jugadores:[{estadisticas:{goles:2,asistencias:1}},{estadisticas:{goles:3,asistencias:2}}]},
 {nombre:"B",categoria:"SENIOR",sedeId:"CENTRO",jugadores:[{estadisticas:{goles:4,asistencias:0}}]},
 {nombre:"C",categoria:"JUNIOR",sedeId:"CENTRO",jugadores:[{estadisticas:{goles:100,asistencias:0}}]}
]);
"""
    script += "const agrupado = " + blocks[0].strip().rstrip(";") + ".toArray();\n"
    script += 'assert.eq(1,agrupado.length);assert.eq("SENIOR",agrupado[0]._id);assert.eq(2,agrupado[0].equipos);assert.eq(9,agrupado[0].goles);\n'
    script += "const calculado = db.equipos.aggregate([" + blocks[1].strip() + "]).toArray();\n"
    script += 'assert.eq(8,calculado.find(e=>e.nombre==="A").intervenciones);\n'
    script += "const relacionado = " + blocks[2].strip().rstrip(";") + ".toArray();\n"
    script += 'assert.eq(3,relacionado.length);assert(relacionado.every(e=>e.sede.length===1&&e.sede[0].nombre==="Pabellón Centro"));\n'
    script += "const antes = " + blocks[4].strip().rstrip(";") + ";\n"
    script += blocks[3].strip() + "\n"
    script += "const despues = " + blocks[4].strip().rstrip(";") + ";\n"
    script += 'assert.eq(2,antes.executionStats.nReturned);assert.eq(2,despues.executionStats.nReturned);print(JSON.stringify({database:db.getName(),version:db.version(),goles:agrupado[0].goles,intervenciones:8,lookup:relacionado.length,returned:despues.executionStats.nReturned,index:db.equipos.getIndexes().some(i=>i.name==="demo_categoria_nombre_id")}));\n'
    script = script.replace("assert.eq(", "qaEq(").replace("assert(", "qaAssert(")
    result = subprocess.run(["docker", "exec", container, "mongosh", "--quiet", "--eval", script], text=True, encoding="utf-8", capture_output=True, check=True)
    assert '"goles":9' in result.stdout and '"index":true' in result.stdout, result.stdout
    print(f"OK Ampliación MongoDB: cinco ejemplos documentados ejecutados; base aislada {database}; goles9, intervenciones8, lookup3, índice y explain2 resultados")


if __name__ == "__main__":
    parser = argparse.ArgumentParser()
    parser.add_argument("--container", default="acda-mongodb-qa")
    check(parser.parse_args().container)
