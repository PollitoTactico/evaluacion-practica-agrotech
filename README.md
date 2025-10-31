# evaluacion-practica-agrotech

Proyecto de ejemplo para la evaluación práctica (Apache Camel + Maven + SQLite).

Estructura creada:

```
/evaluacion-practica-agrotech/
├── sensores.csv
├── src/
├── database/
│   └── init.sql
├── logs/
│   └── .gitkeep
├── README.md
└── reflexion.pdf (placeholder)
```

Qué incluye el código:

- `src/main/java/com/agrotech/MainApp.java` - arranca Camel Main y crea la BD si no existe.
- `src/main/java/com/agrotech/Routes.java` - define las rutas Camel:
  - `file` (File Transfer): lee `sensores.csv` desde la raíz del proyecto, convierte cada fila en JSON y envía a `direct:agroAnalyzerIn`.
  - `direct:agroAnalyzerIn` (AgroAnalyzer): inserta lecturas en la base SQLite `database/lecturas.db`.
  - `direct:rpc.obtenerUltimo` (Servidor RPC): llama a `ServicioAnalitica.getUltimoValor`.
  - `direct:solicitarLectura` (Cliente RPC): solicita el último valor para un `id_sensor` y loguea la respuesta.
- `src/main/java/com/agrotech/DBHelper.java` - utilidades para inicializar la DB, insertar lecturas y consultar el último valor.
- `src/main/java/com/agrotech/ServicioAnalitica.java` - servicio solicitado por el enunciado.
- `sensores.csv` - CSV con al menos 3 registros de ejemplo.
- `database/init.sql` - script para crear la tabla `lecturas`.

Dependencias principales (en `pom.xml`): Apache Camel (camel-core, camel-main), Jackson (JSON), SQLite JDBC.

Cómo compilar y correr (cuando tengas Maven instalado):

- Desde PowerShell en la raíz del proyecto:

```powershell
mvn -q clean compile exec:java -Dexec.mainClass="com.agrotech.MainApp"
```

El `exec-maven-plugin` ya está configurado en el `pom.xml`. Al arrancar:

- Camel inicializará la DB (creará `database/lecturas.db` y la tabla si no existe).
- Si `sensores.csv` existe en la raíz, Camel lo leerá automáticamente, lo moverá a `processed/` y enviará registros a `AgroAnalyzer`.

Verificaciones / evidencias a revisar:

1. Movimientos y conversión a JSON:
   - Busca logs con el prefijo `[FILE]` y `[FILE->JSON]` (se muestran en consola).
2. Inserción en DB:
   - Comprueba que `database/lecturas.db` fue creado.
   - Puedes abrirlo con `sqlite3` o con un cliente SQLite y ejecutar: `SELECT * FROM lecturas;`.
3. RPC simulado:
   - Para probar la ruta RPC cliente manualmente puedes usar la consola de Camel o enviar un mensaje a la ruta `direct:solicitarLectura` usando un producer (o crear un pequeño test). Al ejecutarlo, verás logs con prefijo `[CLIENTE]` y `[SERVIDOR]`.

Pruebas sugeridas rápidas (manuales):

- 1) Ejecuta la aplicación con Maven (arriba).
- 2) Observa la consola: deberías ver logs de lectura del CSV y de inserción en DB.
- 3) Para simular una llamada RPC en caliente: puedes crear un `curl`-like call si expones una ruta HTTP (no implementada) o, de forma simple, modifica `MainApp` para enviar un mensaje de prueba a `direct:solicitarLectura` y terminar. Si quieres, lo agrego.
 - 3) Para simular una llamada RPC en caliente: puedes crear un `curl`-like call si expones una ruta HTTP (ahora implementada) o, de forma simple, modifica `MainApp` para enviar un mensaje de prueba a `direct:solicitarLectura` y terminar. Si quieres, lo agrego.

HTTP (endpoint) disponible
-------------------------

Al arrancar la aplicación también se expone un endpoint HTTP usando Undertow:

- GET http://0.0.0.0:8000/rpc/ultimo/{id}

Ejemplo (PowerShell):

```powershell
Invoke-WebRequest -Uri http://localhost:8000/rpc/ultimo/S001 -UseBasicParsing
```

El endpoint invoca internamente la ruta `direct:solicitarLectura` y devuelve el JSON del último valor.

Tests
-----

Para ejecutar los tests (JUnit 5) usa:

```powershell
mvn -q test
```

Los tests incluidos verifican la inserción en la base y el comportamiento de `ServicioAnalitica`.




