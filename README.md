# Sistema de Encuestas (Backend)

API REST para gestionar encuestas y exámenes dirigidos a egresados: registro de usuarios, aplicación de encuestas, almacenamiento de respuestas, reportes por carrera/facultad y un dashboard de indicadores de satisfacción.

## Sistema en producción

La aplicación está publicada en: https://encuestauseil.unsm.edu.pe/auth/selector-rol

## Tecnologías

- Java 21
- Spring Boot (Web, Data JPA, Security)
- MySQL
- Lombok
- Maven (wrapper incluido)
- Docker

## Estructura

```
src/main/java/com/gaslac/sistema_encuestas/
├── SistemaEncuestasApplication.java
└── modules/
    ├── config/        Configuración de seguridad
    ├── controller/    Endpoints REST
    ├── dto/           Objetos de transferencia (y dto/dashboard)
    ├── entity/        Entidades JPA (Encuesta, Dimension, Item, Respuesta, Usuario, TipoItem)
    ├── repository/    Repositorios Spring Data
    └── service/       Lógica de negocio (EncuestaService, RespuestaService, DashboardService)
```

### Modelo de datos

`Encuesta` → `Dimension` → `Item` (pregunta) → `Respuesta` (de un `Usuario`).
Los ítems tienen un tipo (`TipoItem`): `ESCALA` (1-5), `BOOLEANO`, `FECHA` y `TEXTO`.

## Requisitos

- JDK 21
- MySQL 8+ con una base de datos `sistema_encuestas`

## Configuración

Se define en [application.properties](src/main/resources/application.properties) y se puede sobrescribir con variables de entorno:

| Variable | Descripción | Valor por defecto |
|---|---|---|
| `SPRING_DATASOURCE_URL` | URL JDBC de MySQL | `jdbc:mysql://localhost:3306/sistema_encuestas?...` |
| `SPRING_DATASOURCE_USERNAME` | Usuario de la base de datos | `encuestas_user` |
| `SPRING_DATASOURCE_PASSWORD` | Contraseña de la base de datos | ver `application.properties` |
| `SERVER_PORT` | Puerto del servidor | `8080` |

Hibernate usa `ddl-auto=update`, por lo que las tablas se crean/actualizan automáticamente.

## Ejecución

```bash
./mvnw spring-boot:run
```

Generar el JAR:

```bash
./mvnw clean package
java -jar target/*.jar
```

### Docker

```bash
docker build -t sistema-encuestas .
docker run -p 8080:8080 \
  -e SPRING_DATASOURCE_URL=jdbc:mysql://host:3306/sistema_encuestas \
  -e SPRING_DATASOURCE_USERNAME=usuario \
  -e SPRING_DATASOURCE_PASSWORD=clave \
  sistema-encuestas
```

## Endpoints

Todas las rutas están bajo `/api`. Actualmente la seguridad permite el acceso a todas las peticiones (`permitAll`).

### Usuarios — `/api/usuarios` (alias `/api/usuario`)

| Método | Ruta | Descripción |
|---|---|---|
| POST | `/` | Registrar un usuario |
| GET | `/{dni}` | Obtener un usuario por DNI |

### Exámenes / Encuestas — `/api/examen`

| Método | Ruta | Descripción |
|---|---|---|
| POST | `/` | Crear una encuesta |
| GET | `/` | Listar encuestas (filtros opcionales `nombre`, `cargo`) |
| GET | `/nombres` | Listar nombres de encuestas |
| GET | `/{id}` | Obtener la encuesta completa |
| GET | `/usuario/{idUsuario}` | Encuesta completa asignada a un usuario |
| GET | `/{id}/puntajes` | Puntajes (filtro opcional `carrera`) |
| GET | `/{idEncuesta}/promedio-carrera` | Promedio por carrera |
| GET | `/{idEncuesta}/kpi` | KPIs de la encuesta |
| GET | `/{idEncuesta}/reporte-completo` | Reporte completo |
| GET | `/reporte-global` | Reporte global (filtro opcional `carrera`) |

### Respuestas — `/api/respuestas`

| Método | Ruta | Descripción |
|---|---|---|
| POST | `/` | Registrar respuestas |
| POST | `/examen` | Registrar las respuestas de un examen |
| GET | `/respondidas/{idUsuario}` | Encuestas ya respondidas por un usuario |
| GET | `/usuario/{idUsuario}` | Respuestas de un usuario |
| GET | `/{id}` | Respuestas de una encuesta |
| GET | `/exportar/{idEncuesta}` | Respuestas en bruto para exportar a Excel (filtros opcionales `facultad`, `escuelaProfesional`) |

### Dashboard — `/api/dashboard`

| Método | Ruta | Descripción |
|---|---|---|
| GET | `/` | KPIs, distribución de sentimiento, ranking de preguntas y comparativo por escuela. Filtros opcionales: `idEncuesta`, `facultad` |

## Pruebas

Ejecutar todas las pruebas:

```bash
./mvnw test
```

Ejecutar solo las pruebas del dashboard:

```bash
./mvnw test -Dtest='DashboardServiceTest,DashboardControllerTest'
```

### Pruebas incluidas

Se usan JUnit 5, Mockito y MockMvc; no requieren base de datos.

| Clase | Qué verifica |
|---|---|
| `DashboardServiceTest` | Dashboard vacío sin respuestas, error 404 si la encuesta no existe, filtro por encuesta, cálculo de KPIs, comparativo por escuela, distribución de sentimiento y ranking de preguntas, filtro por facultad sin distinguir tildes ni mayúsculas, y caso sin escalas válidas |
| `DashboardControllerTest` | Respuesta con y sin filtros, y error 400 cuando `facultad` viene en blanco |

> `SistemaEncuestasApplicationTests` levanta el contexto completo de Spring y necesita una base MySQL disponible.
