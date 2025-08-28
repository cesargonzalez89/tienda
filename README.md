# API REST - Tienda

## Descripción
API REST para gestión de productos, conectada a MySQL, con validaciones y respuestas estandarizadas.

## Tecnologías utilizadas
- **Java 17**
- **Spring Boot 3.2.5**
- **Maven**
- **MySQL 8.0.43**

## Inicialización de la base de datos
- El proyecto incluye el script SQL necesario para crear la base de datos, la tabla y los stored procedures requeridos.
- Archivo: `db/schema.sql`
- **¿Cómo usarlo?**
  1. Abre tu cliente de MySQL o consola.
  2. Ejecuta el script:
     ```sh
     mysql -u <usuario> -p < db/schema.sql
     ```
  3. Asegúrate de que la configuración de conexión en `application.properties` coincida con los datos de tu entorno.

## Requisitos previos y despliegue
- Tener instalado Java 17 y Maven.
- Contar con una base de datos MySQL accesible y configurada en `src/main/resources/application.properties`.
- Haber configurado la cadena de conexion a bd en application.properties, por defecto viene con variables de entorno, si pones tus datos de conexion en texto plano puedes ejecutar:
  ```sh
  mvn clean install
  mvn spring-boot:run
  ```
- La API estará disponible por defecto en: `http://localhost:8080`
- Si tienes problemas al ejecutar multiples veces y el puerto se mantiene ocupado usa start-app.sh ejecutando:
  ```
  sh start-app.sh 
  ```

## Seguridad y configuración de base de datos
- Para ejecutar desde consola si no pusiste las credenciales planas y dejaste las variables de entorno en el application.properties usa:
  ```properties
  export DB_URL="jdbc:mysql://localhost:3306/tienda?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true"
  export DB_USER="tu_usuario"
  export DB_PASS="tu_contraseña"
  mvn spring-boot:run
  ```

## Endpoints principales

### 1. Obtener todos los productos
- **GET** `/api/productos`
- **Respuesta:**
```json
{
  "codigo": 200,
  "mensaje": "informacion encontrada.",
  "informacion": [ { ...producto... } ]
}
```

### 2. Obtener producto por ID
- **GET** `/api/productos/{id}`
- **Respuesta exitosa:**
```json
{
  "codigo": 200,
  "mensaje": "informacion encontrada.",
  "informacion": { ...producto... }
}
```
- **No encontrado:**
```json
{
  "codigo": 404,
  "mensaje": "no encontrado.",
  "informacion": null
}
```
- **Validación fallida (400):**
```json
{
  "codigo": 400,
  "mensaje": "El id es obligatorio",
  "informacion": null
}
```

### 3. Crear producto
- **POST** `/api/productos`
- **Body ejemplo:**
```json
{
  "id": 123,
  "name": "Producto nuevo",
  "price": 99.99,
  "stock": 10,
  "status": true
}
```
- **Respuesta exitosa:**
```json
{
  "codigo": 200,
  "mensaje": "producto creado.",
  "informacion": { ...producto creado... }
}
```
- **Validación fallida (400):**
```json
{
  "codigo": 400,
  "mensaje": "El campo status es obligatorio y debe ser booleano (true/false)",
  "informacion": null
}
```
- **Validación fallida por otros campos (400):**
```json
{
  "codigo": 400,
  "mensaje": "El nombre no puede estar vacío, El precio es obligatorio, El stock debe ser mayor a 0",
  "informacion": null
}
```
- **ID duplicado (409):**
```json
{
  "codigo": 409,
  "mensaje": "id duplicado.",
  "informacion": null
}
```

### 4. Actualizar producto
- **PUT** `/api/productos/{id}`
- **Body ejemplo:**
```json
{
  "name": "Producto actualizado",
  "price": 150.0,
  "stock": 5,
  "status": false
}
```
- **Respuesta exitosa:**
```json
{
  "codigo": 200,
  "mensaje": "informacion actualizada.",
  "informacion": { ...producto actualizado... }
}
```
- **No encontrado:**
```json
{
  "codigo": 404,
  "mensaje": "no encontrado.",
  "informacion": null
}
```
- **Validación fallida (400):**
```json
{
  "codigo": 400,
  "mensaje": "El campo status es obligatorio y debe ser booleano (true/false)",
  "informacion": null
}
```
- **Validación fallida por otros campos (400):**
```json
{
  "codigo": 400,
  "mensaje": "El precio debe ser mayor a 0",
  "informacion": null
}
```

### 5. Eliminar producto
- **DELETE** `/api/productos/{id}`
- **Respuesta exitosa:**
```json
{
  "codigo": 200,
  "mensaje": "informacion borrada.",
  "informacion": null
}
```
- **No encontrado:**
```json
{
  "codigo": 404,
  "mensaje": "no encontrado.",
  "informacion": null
}
```
- **Validación fallida (400):**
```json
{
  "codigo": 400,
  "mensaje": "El id es obligatorio",
  "informacion": null
}
```

### 6. Top 10 productos por precio (de archivos JSON)
- **GET** `/api/productos/top-products`
- **Respuesta:**
```json
{
  "codigo": 200,
  "mensaje": "informacion encontrada.",
  "informacion": [ { ...top 10 productos... } ]
}
```

## Validaciones de entrada
- `id`: obligatorio en POST
- `name`: no puede estar vacío (`@NotBlank`)
- `price`: obligatorio y mayor a 0 (`@NotNull`, `@Positive`)
- `stock`: obligatorio y mayor a 0 (`@NotNull`, `@Positive`)
- `status`: obligatorio y debe ser booleano (`true` o `false`)

**Ejemplo de error de validación por status:**
```json
{
  "codigo": 400,
  "mensaje": "El campo status es obligatorio y debe ser booleano (true/false)",
  "informacion": null
}
```

**Ejemplo de error de validación por otros campos:**
```json
{
  "codigo": 400,
  "mensaje": "El nombre no puede estar vacío, El precio debe ser mayor a 0",
  "informacion": null
}
```

## Prueba rápida con curl
```sh
curl -X POST http://localhost:8080/api/productos \
  -H "Content-Type: application/json" \
  -d '{"id": 1, "name": "", "price": -5, "stock": 0, "status": null}'
```

## Notas
- Todos los endpoints devuelven un objeto con los campos: `codigo`, `mensaje`, `informacion`.
- Los mensajes y códigos cambian según el resultado de la operación y las reglas de negocio.
