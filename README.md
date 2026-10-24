# Desafío Técnico – API de Tipo de Cambio

## Descripción del proyecto

Microservicio REST para convertir montos entre monedas. Consulta los tipos de cambio de ExchangeRate-API mediante Spring Cloud OpenFeign y persiste las operaciones en H2 usando Spring Data JPA.

## Tecnologías utilizadas

| Tecnología | Versión / uso |
|---|---|
| Java | 17 |
| Spring Boot | 4.0.6 |
| Spring Cloud OpenFeign | Gestionado por Spring Cloud BOM 2025.1.1 |
| Spring Data JPA | Versión gestionada por Spring Boot |
| H2 | Base de datos en memoria |
| Lombok | Versión gestionada por Spring Boot |
| Maven | Maven Wrapper incluido |

## Requisitos previos

- JDK 17.
- Git para clonar el repositorio.

Verifica la versión de Java con:

```bash
java -version
```

La salida debe indicar una versión 17.

## Clonar el repositorio

La URL configurada para el remoto `origin` es:

```bash
git clone https://github.com/HenrryPortillaP/test-exchange-service.git
cd test-exchange-service
```

Si ya descargaste el proyecto, abre una terminal en la carpeta raíz, donde se encuentran `pom.xml`, `mvnw` y `mvnw.cmd`.

## Configuración

La propiedad `exchange-rate.api.key` se resuelve desde la variable de entorno `EXCHANGE_API_KEY`. Configúrala antes de iniciar la aplicación y no publiques ni incluyas la clave en archivos versionados.

**Windows PowerShell:**

```powershell
$env:EXCHANGE_API_KEY = "TU_API_KEY"
```

**Linux/macOS:**

```bash
export EXCHANGE_API_KEY="TU_API_KEY"
```

La URL base configurada para ExchangeRate-API es `https://v6.exchangerate-api.com/v6`.

## Ejecución

Ejecuta los comandos desde la raíz del proyecto.

**Windows:**

```powershell
.\mvnw.cmd spring-boot:run
```

**Linux/macOS:**

```bash
./mvnw spring-boot:run
```

La aplicación inicia en `http://localhost:8080`.

## Pruebas

Ejecuta toda la suite desde la raíz del proyecto:

**Windows:**

```powershell
.\mvnw.cmd clean test
```

**Linux/macOS:**

```bash
./mvnw clean test
```

La ejecución es satisfactoria si Maven termina con `BUILD SUCCESS` y el resumen muestra cero fallos y errores. Los reportes individuales quedan en `target/surefire-reports`.

## API de conversiones

### Crear una conversión

- **Método:** `POST`
- **Ruta:** `/api/v1/conversiones`
- **Éxito:** `201 Created`
- **Content-Type:** `application/json`

El cuerpo requiere:

| Campo | Tipo | Validación |
|---|---|---|
| `monto` | Número decimal | Obligatorio y mayor que cero |
| `monedaOrigen` | Cadena | Obligatoria; exactamente tres letras |
| `monedaDestino` | Cadena | Obligatoria; exactamente tres letras |

Ejemplo con `curl`:

```bash
curl -X POST "http://localhost:8080/api/v1/conversiones" \
  -H "Content-Type: application/json" \
  -d "{\"monto\":100,\"monedaOrigen\":\"USD\",\"monedaDestino\":\"PEN\"}"
```

Ejemplo de respuesta:

```json
{
  "id": "e7f3b512-57ab-4f3c-a20b-5dc79b065129",
  "monto": 100.000000,
  "monedaOrigen": "USD",
  "monedaDestino": "PEN",
  "tipoCambio": 3.7000000000,
  "montoConvertido": 370.000000,
  "fecha": "2026-01-15T12:30:00"
}
```

Los números de este ejemplo son ilustrativos. El tipo de cambio y el monto convertido reales dependen de la respuesta de ExchangeRate-API al momento de la solicitud.

### Consultar el historial

- **Método:** `GET`
- **Ruta:** `/api/v1/conversiones`

Devuelve las conversiones almacenadas como una lista de objetos con los mismos campos de la respuesta de creación. No se implementa paginación en este endpoint.

## Persistencia y consola H2

La configuración actual usa H2 en memoria:

| Parámetro | Valor configurado |
|---|---|
| URL JDBC | `jdbc:h2:mem:testdb` |
| Usuario | `sa` |
| Contraseña | `password` |
| Consola | `http://localhost:8080/h2-console` |

Para conectarte:

1. Inicia la aplicación.
2. Abre `http://localhost:8080/h2-console`.
3. Introduce la URL JDBC `jdbc:h2:mem:testdb`, el usuario `sa` y la contraseña `password`.
4. Conecta con la consola.

Al ser una base en memoria, los datos no persisten después de detener la aplicación. La consola está habilitada por la configuración actual; esta configuración y las credenciales indicadas corresponden al entorno incluido en el proyecto y deben revisarse antes de exponer el servicio fuera de un entorno local.

## Errores HTTP

La aplicación gestiona explícitamente estos casos:

| HTTP | Caso |
|---|---|
| `400 Bad Request` | Campos de entrada inválidos, monto no válido, moneda no soportada, cuerpo JSON vacío o ilegible. Los errores de validación incluyen un mensaje general y una lista `detalles`; los errores de cuerpo ilegible devuelven un mensaje en `error`. |
| `502 Bad Gateway` | Error Feign al consultar el proveedor o respuesta de tasas que la aplicación considera inválida. |

Otras excepciones no tienen un handler general propio en `ApiExceptionHandler` y quedan sujetas al manejo predeterminado de Spring.

## Arquitectura

- **Dominio (`domain`):** modelos y reglas del negocio (`Moneda`, `Conversion`), cálculo del tipo de cambio e interfaz de repositorio.
- **Aplicación (`application`):** controladores REST, DTO de entrada/salida, validación HTTP, servicio de aplicación y mapeo de excepciones a respuestas HTTP.
- **Infraestructura (`infrastructure`):** cliente OpenFeign y DTO del proveedor, entidad JPA, repositorio Spring Data, adaptador de persistencia y configuración de servicios de dominio.
