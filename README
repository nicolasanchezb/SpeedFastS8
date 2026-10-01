# SpeedFast — Semana 8

## Gestión de pedidos, repartidores y entregas con CRUD y JDBC

**Autor:** Nicolás Sánchez Bustos  
**Carrera:** Analista Programador Computacional  
**Actividad:** Sumativa individual — Semana 8

## Descripción

SpeedFast es una aplicación de escritorio desarrollada en Java que permite gestionar pedidos, repartidores y entregas mediante una interfaz Swing y una base de datos MySQL.

Esta versión completa las operaciones CRUD (crear, leer, actualizar y eliminar) de las tres entidades. Los cambios se guardan en `speedfast_db` y permanecen disponibles después de cerrar y volver a abrir la aplicación.

## Funcionalidades

| Entidad | Funciones |
|---|---|
| Repartidores | Registrar nombre, listar en tabla, seleccionar, editar y eliminar |
| Pedidos | Registrar dirección, distancia, tipo y estado; listar, editar y eliminar |
| Entregas | Asociar pedido y repartidor con fecha y hora; listar, editar y eliminar |

### Pedidos

- Tipos permitidos: `COMIDA`, `ENCOMIENDA` y `EXPRESS`.
- Estados permitidos: `PENDIENTE`, `EN_REPARTO` y `ENTREGADO`.
- ID generado por MySQL al registrar desde la nueva pantalla.
- Distancia en kilómetros conservada del modelo de semanas anteriores.

### Entregas

- Selección de pedidos y repartidores mediante combos cargados desde MySQL.
- Texto legible en los combos, manteniendo internamente los objetos y sus IDs.
- Fecha con formato `AAAA-MM-DD` y hora con formato `HH:mm:ss`.
- Filtros por pedido y repartidor, utilizables por separado o combinados.
- Opción **Todos** para recuperar el listado completo.
- El registro de una entrega conserva el estado actual del pedido. El estado se modifica desde la gestión de pedidos.

### Interfaz

- Menú principal con acceso a las tres pantallas de gestión.
- Tablas de solo lectura; selección de filas para editar o eliminar.
- Actualización de las tablas después de cada operación exitosa.
- Actualización de los combos y del listado de entregas cuando cambian pedidos o repartidores.
- Mensajes de éxito, validación y error mediante `JOptionPane`.
- Confirmación antes de eliminar registros.
- Operaciones JDBC en segundo plano mediante `SwingWorker`.

## Tecnologías

- Java y programación orientada a objetos.
- Swing: `JFrame`, `JPanel`, `JTable`, `JTextField`, `JComboBox` y `JButton`.
- JDBC: `Connection`, `PreparedStatement` y `ResultSet`.
- MySQL y MySQL Connector/J.
- IntelliJ IDEA.
- `java.time` para fechas y horas.

## Organización del código

La aplicación utilizada en esta entrega se organiza en las siguientes capas:

| Paquete | Clases principales | Responsabilidad |
|---|---|---|
| `main` | `Main` | Iniciar la interfaz en el hilo de Swing |
| `model` | `Pedido`, `PedidoComida`, `PedidoEncomienda`, `PedidoExpress`, `EstadoPedido`, `Repartidor`, `Entrega` | Representar las entidades y sus datos |
| `dao` | `ConexionBD`, `PedidoDAO`, `RepartidorDAO`, `EntregaDAO` | Conectar con MySQL y ejecutar operaciones de persistencia |
| `ui` | `VentanaPrincipal`, `VentanaGestionPedidos`, `VentanaRepartidores`, `VentanaAsignarEntrega` | Formularios, tablas, eventos y validaciones |

Las clases de simulación de semanas anteriores pueden permanecer en el proyecto. El menú de la semana 8 utiliza las pantallas CRUD y no inicia la simulación automática de entregas.

### Programación orientada a objetos

`Pedido` mantiene su carácter abstracto. Al leer datos desde MySQL, `PedidoDAO` reconstruye la subclase correspondiente al tipo registrado. `Entrega` relaciona un objeto `Pedido` con un objeto `Repartidor` y almacena su ID y fecha/hora.

### Acceso a datos

Cada DAO implementa `create()`, `readAll()`, `update()` y `delete()`. También se conservan métodos anteriores como `guardar()` y `listarTodos()` para compatibilidad.

Las sentencias usan parámetros `?` y `PreparedStatement`, evitando concatenar las entradas del usuario en el SQL. Los recursos se cierran mediante `try-with-resources`. Las excepciones se propagan a la interfaz para informar el resultado de las operaciones.

## Base de datos

**Nombre:** `speedfast_db`.

La implementación utiliza las tablas existentes de la semana 7, cuyos nombres están en singular:

| Tabla | Columnas |
|---|---|
| `repartidor` | `id`, `nombre` |
| `pedido` | `id`, `direccion`, `distancia_km`, `tipo`, `estado` |
| `entrega` | `id`, `id_pedido`, `id_repartidor`, `fecha`, `hora` |

Respecto del esquema de referencia de la actividad, se conservó `distancia_km`, la dirección admite 150 caracteres y `tipo`/`estado` son campos `VARCHAR`. La aplicación restringe sus valores a las opciones exigidas.

### Creación en una instalación nueva

El siguiente SQL reproduce la estructura utilizada por los DAO. Ejecútalo en una instalación nueva; si ya tienes las tablas, verifica su estructura y conserva los datos existentes.

```sql
CREATE DATABASE IF NOT EXISTS speedfast_db
    CHARACTER SET utf8mb4;

USE speedfast_db;

CREATE TABLE repartidor (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL
) ENGINE = InnoDB;

CREATE TABLE pedido (
    id INT AUTO_INCREMENT PRIMARY KEY,
    direccion VARCHAR(150) NOT NULL,
    distancia_km DOUBLE NOT NULL,
    tipo VARCHAR(30) NOT NULL,
    estado VARCHAR(20) NOT NULL
) ENGINE = InnoDB;

CREATE TABLE entrega (
    id INT AUTO_INCREMENT PRIMARY KEY,
    id_pedido INT NOT NULL,
    id_repartidor INT NOT NULL,
    fecha DATE NOT NULL,
    hora TIME NOT NULL,
    CONSTRAINT fk_entrega_pedido
        FOREIGN KEY (id_pedido) REFERENCES pedido(id),
    CONSTRAINT fk_entrega_repartidor
        FOREIGN KEY (id_repartidor) REFERENCES repartidor(id)
) ENGINE = InnoDB;
```

Las claves foráneas impiden eliminar un pedido o repartidor con entregas asociadas. La aplicación informa esta restricción; para eliminar registros de prueba relacionados, elimina primero la entrega.

## Configuración y ejecución

### 1. Preparar MySQL

Inicia el servicio MySQL y confirma que exista `speedfast_db` con las tablas indicadas. Puedes comprobarlas desde MySQL Workbench:

```sql
USE speedfast_db;
SHOW TABLES;
DESCRIBE repartidor;
DESCRIBE pedido;
DESCRIBE entrega;
```

### 2. Agregar el conector JDBC

En IntelliJ IDEA, abre **File → Project Structure → Modules → Dependencies** y agrega el JAR de MySQL Connector/J mediante **+ → JARs or Directories**. Configura su alcance como **Compile**.

El entorno de desarrollo utiliza `mysql-connector-j-26.7.0.jar`. La configuración de ejecución debe utilizar el módulo que contiene esta dependencia.

### 3. Configurar la conexión

La clase `dao.ConexionBD` utiliza:

| Parámetro | Valor |
|---|---|
| URL | `jdbc:mysql://localhost:3306/speedfast_db` |
| Usuario | `root` |
| Contraseña | Variable de entorno `SPEEDFAST_DB_PASSWORD` |
| Método | `ConexionBD.conectar()` |

Configura `SPEEDFAST_DB_PASSWORD` en las variables de entorno de la configuración de ejecución de IntelliJ, con la contraseña del usuario MySQL. Si se configura como variable del sistema operativo, reinicia IntelliJ para que pueda leerla.

La contraseña no debe escribirse en el código ni publicarse en el repositorio.

### 4. Iniciar la aplicación

Ejecuta `main.Main` desde IntelliJ. La declaración de paquete de `Main.java` es `package main;` y el campo **Main class** de la configuración debe indicar `main.Main`.

El menú ofrece:

1. **Gestionar pedidos**.
2. **Gestionar repartidores**.
3. **Gestionar entregas**.

El inicio ya no vuelve a insertar los pedidos de ejemplo eliminados.

## Uso básico

1. Registra un repartidor y un pedido.
2. Abre la gestión de entregas y selecciona ambos desde los combos.
3. Ingresa la fecha y hora y pulsa **Registrar**.
4. Para editar, selecciona una fila, modifica sus datos y pulsa **Guardar cambios**.
5. Para eliminar, selecciona una fila y confirma la operación.
6. Utiliza **Nuevo** para limpiar el formulario y **Actualizar** para consultar nuevamente MySQL.
7. En entregas, utiliza los filtros para consultar un pedido o repartidor específico.

## Validaciones y manejo de errores

- Nombre del repartidor obligatorio, con un máximo de 100 caracteres.
- Dirección obligatoria, con un máximo de 150 caracteres.
- Distancia numérica, finita y mayor que cero.
- Tipo y estado seleccionados desde opciones válidas.
- Pedido y repartidor obligatorios para registrar una entrega.
- Fecha y hora válidas, con análisis estricto de sus formatos.
- Año de la entrega entre 1000 y 9999.
- Selección obligatoria de una fila antes de editar o eliminar.
- Mensajes claros ante errores de conexión, referencias inexistentes o eliminación impedida por relaciones.

## Verificación manual

Durante el desarrollo se reportaron satisfactorias las siguientes pruebas manuales:

| Prueba | Resultado esperado y reportado |
|---|---|
| Repartidores | Registro, edición, eliminación y validación de nombre vacío |
| Pedidos | Registro, edición de tipo/estado, eliminación y validación de entradas |
| Persistencia | Conservación de cambios después de reiniciar la aplicación |
| Entregas | Registro, edición, eliminación y validación de fecha imposible |
| Integridad referencial | Bloqueo al eliminar pedidos o repartidores con entregas asociadas |
| Filtros de entregas | Consulta por pedido, por repartidor y recuperación del listado completo |
| Selección filtrada | Carga del registro correcto al seleccionar una fila filtrada |

Estas comprobaciones son manuales; no corresponden a una suite automatizada de pruebas.

## Solución de problemas

| Mensaje o síntoma | Revisión |
|---|---|
| `Could not find or load main class` | Verificar `package main;` y ejecutar `main.Main` con el módulo correcto |
| `No suitable driver found` | Agregar el JAR de MySQL Connector/J a las dependencias y al classpath de ejecución |
| `Falta configurar SPEEDFAST_DB_PASSWORD` | Configurar la variable en la ejecución o reiniciar IntelliJ si se agregó al sistema |
| `Access denied` | Revisar usuario, contraseña y permisos de MySQL |
| Error de conexión con el servidor | Comprobar que MySQL esté iniciado y que host/puerto coincidan con la URL |
| Tabla inexistente | Verificar `speedfast_db` y los nombres en singular utilizados por los DAO |
| Eliminación impedida | Revisar las entregas asociadas al pedido o repartidor |

## Alcance de la entrega

La versión implementa CRUD de repartidores, pedidos y entregas, integración con Swing, consultas parametrizadas, cierre de recursos y filtros de entregas. Los filtros de pedidos por estado o tipo, opcionales en el enunciado, no se incorporaron.

Se utiliza `RepartidorDAO` porque el caso y el esquema describen repartidores; la mención de `ClienteDAO` en el paso 2 del enunciado no corresponde a una entidad del esquema proporcionado.
