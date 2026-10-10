# Leer y generar XML con DOM

XML representa un árbol de elementos y atributos. Puede expresar estructuras jerárquicas, incluir espacios de nombres y tener un esquema de validación. DOM carga el documento completo en memoria y permite recorrer o modificar nodos; es cómodo para archivos pequeños y medianos. Para archivos muy grandes conviene un parser incremental. La [API DOM de Java](https://docs.oracle.com/en/java/javase/25/docs/api/java.xml/org/w3c/dom/package-summary.html) describe `Document`, `Element` y `NodeList`.

Considera `<cliente id="C01"><nombre>Ana</nombre></cliente>`. `cliente` es un elemento; `id` es un atributo; `nombre` es un elemento hijo cuyo texto contiene el valor. A diferencia de JSON, el texto `12` no tiene tipo numérico implícito: la aplicación debe interpretarlo y validarlo.

## Analizar sin resolver entidades externas

Un archivo recibido puede contener declaraciones `DOCTYPE` que intenten leer archivos locales o recursos de red. Antes de llamar a `parse`, configura [`DocumentBuilderFactory`](https://docs.oracle.com/en/java/javase/25/docs/api/java.xml/javax/xml/parsers/DocumentBuilderFactory.html) para rechazar DTD y desactivar el acceso externo. Estas opciones deben fijarse juntas; no basta con desactivar una sola característica.

```java
DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
factory.setXIncludeAware(false);
factory.setExpandEntityReferences(false);
factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
Document documento = factory.newDocumentBuilder().parse(entrada.toFile());
```

El fragmento es ilustrativo y se usa dentro de un método que declara o maneja `ParserConfigurationException`, `SAXException` e `IOException`. Un proveedor que no soporte alguna opción debe causar un fallo de configuración: no continúes con un parser inseguro. Después de `parse`, `getDocumentElement()` devuelve la raíz. `getElementsByTagName("cliente")` busca **descendientes**, no solo hijos directos; si el árbol admite `cliente` anidados, recorre `getChildNodes()` y comprueba `getNodeType()` para evitar coincidencias inesperadas.

## Crear y transformar

Para generar XML, se crea un `Document`, luego elementos con `createElement`, texto con `setTextContent` y relaciones con `appendChild`. `setTextContent` escapa caracteres especiales al serializar; concatenar XML manualmente puede producir un documento inválido. [`Transformer`](https://docs.oracle.com/en/java/javase/25/docs/api/java.xml/javax/xml/transform/Transformer.html) escribe el DOM a un `StreamResult`. Configura `OutputKeys.ENCODING` como `UTF-8` y un `OutputStream` externo; en transformaciones de fuentes no confiables restringe también [`ACCESS_EXTERNAL_DTD` y `ACCESS_EXTERNAL_STYLESHEET`](https://docs.oracle.com/en/java/javase/25/docs/api/java.xml/javax/xml/XMLConstants.html).

```java
Document documento = factory.newDocumentBuilder().newDocument();
Element raiz = documento.createElement("clientes");
documento.appendChild(raiz);
Element cliente = documento.createElement("cliente");
cliente.setAttribute("id", "C01");
raiz.appendChild(cliente);
Element nombre = documento.createElement("nombre");
nombre.setTextContent("Ana & Luis");
cliente.appendChild(nombre);
```

El texto se serializa como `Ana &amp; Luis`; cuando DOM vuelve a leerlo, `getTextContent()` devuelve `Ana & Luis`. En la [conversión guiada](guiada-xml-csv.md) comprobarás estructura y valores antes de exportar. La [práctica autónoma](practica-csv-xml.md) recorre el camino inverso.
