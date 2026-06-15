# Requirements Document

## Introduction

This document describes the requirements for the REST endpoint of the **Dashboard de Satisfacción de Egresados** in the `sistema-encuestas` system (Spring Boot / Java).

The goal is to expose a single endpoint `GET /api/dashboard` that consolidates four sections of analytical data: main KPIs, comparison by professional school, sentiment distribution, and quality ranking by question. The survey filter (by `idEncuesta`) and faculty filter (by `facultad` name) are optional; without filters, the endpoint returns the aggregate of all surveys and all graduates.

The system calculates the indicators by processing existing entities: `Encuesta`, `Dimension`, `Item` (only `ESCALA` type for averages), `Respuesta`, `Usuario`, and `TipoItem`.

---

## Glossary

- **Dashboard_Service**: Service component responsible for calculating and aggregating all dashboard indicators.
- **Dashboard_Controller**: REST controller that exposes the `GET /api/dashboard` endpoint.
- **Encuesta**: Entity representing a satisfaction survey. Identified by `idEncuesta`.
- **Dimension**: Thematic grouping of items within a survey. Identified by `idDimension` and has `codigo` and `nombre`.
- **Item**: Survey question. Each item belongs to a `Dimension` and has a `TipoItem`.
- **TipoItem**: Enumeration with values `ESCALA`, `BOOLEANO`, `FECHA`, `TEXTO`. Only `ESCALA` items (integer values 1–5 stored as text in `Respuesta.valor`) participate in average and satisfaction calculations.
- **Respuesta**: Record of a `Usuario`'s answer to an `Item`. Contains the field `valor` (text) and `fecha`.
- **Usuario**: Graduate who responds to a survey. Contains `escuelaProfesional` and `facultad` as classification fields.
- **Promedio_Satisfaccion**: Arithmetic mean of the numeric values of all `ESCALA` responses applicable to the active filter, on a 1–5 scale.
- **Egresado_Encuestado**: Unique `Usuario` who has at least one `Respuesta` registered in the filtered data set.
- **Tasa_Participacion**: Percentage of graduates who responded to at least one survey relative to the total registered `Usuario` in the system.
- **Categoria_Sentimiento**: Classification of an individual graduate's average: `Muy Satisfecho` [4.5–5.0], `Satisfecho` [3.5–4.5), `Neutral` [2.5–3.5), `Insatisfecho` [1.5–2.5), `Muy Insatisfecho` [1.0–1.5).
- **Estado_Item**: Classification of an item's average: `Excelente` (≥ 4.5), `Bueno` (≥ 3.5 and < 4.5), `Regular` (≥ 2.5 and < 3.5), `Crítico` (< 2.5).
- **Mejor_Escuela**: Professional school group with the highest `Promedio_Satisfaccion` in the filtered set.
- **Peor_Escuela**: Professional school group with the lowest `Promedio_Satisfaccion` in the filtered set.
- **Filtro_Encuesta**: Optional query parameter `idEncuesta` (Integer) that restricts data to a specific survey.
- **Filtro_Facultad**: Optional query parameter `facultad` (String, non-blank) that restricts data to graduates whose `Usuario.facultad` matches the provided value case-insensitively.

---

## Requirements

### Requirement 1: Dashboard Endpoint with Optional Filters

**User Story:** As a system administrator, I want to query a REST endpoint `GET /api/dashboard` with optional survey and faculty filters, so that I can obtain all analytical dashboard data in a single request.

#### Acceptance Criteria

1. THE `Dashboard_Controller` SHALL expose the endpoint `GET /api/dashboard` accepting the optional query parameters `idEncuesta` (Integer) and `facultad` (String).
2. WHEN the parameter `idEncuesta` is provided, THE `Dashboard_Service` SHALL restrict all calculations to `Respuesta` records whose `Item` belongs to a `Dimension` that belongs to the `Encuesta` with that `idEncuesta` (traversal: `Respuesta → Item → Dimension → Encuesta`).
3. WHEN the parameter `facultad` is provided, THE `Dashboard_Service` SHALL restrict all calculations to `Respuesta` records whose associated `Usuario.facultad` matches the provided value case-insensitively (using `LOWER()` comparison or equivalent).
4. WHEN both parameters `idEncuesta` and `facultad` are provided, THE `Dashboard_Service` SHALL apply both filters simultaneously (intersection of criteria 2 and 3).
5. WHEN no parameters are provided, THE `Dashboard_Service` SHALL calculate indicators over all `Respuesta` records registered in the system.
6. THE `Dashboard_Controller` SHALL return HTTP `200 OK` with a JSON body upon successful completion.
7. IF the provided `idEncuesta` does not exist in the database, THEN THE `Dashboard_Controller` SHALL return HTTP `404 Not Found` with a message indicating that no `Encuesta` exists with the provided `idEncuesta`.
8. IF the `facultad` parameter is provided but is blank or contains only whitespace, THEN THE `Dashboard_Controller` SHALL return HTTP `400 Bad Request` with a message indicating that `facultad` must be a non-blank string.
9. IF the `idEncuesta` parameter is provided but cannot be parsed as an integer, THEN THE `Dashboard_Controller` SHALL return HTTP `400 Bad Request` with a message indicating that `idEncuesta` must be an integer.
10. IF the set of `Respuesta` records resulting from applying the filters is empty, THEN THE `Dashboard_Controller` SHALL return HTTP `200 OK` with a response object where all numeric indicators are `0` or `0.0` and all lists are empty arrays.

---

### Requirement 2: Main Dashboard KPIs

**User Story:** As an administrator, I want to see the main satisfaction KPIs so that I can quickly evaluate the general state of graduate surveys.

#### Acceptance Criteria

1. THE `Dashboard_Service` SHALL calculate **total responses** as the count of all `Respuesta` records in the filtered set, regardless of `TipoItem`.
2. THE `Dashboard_Service` SHALL calculate **surveyed graduates** as the count of distinct `Usuario` who have at least one `Respuesta` in the filtered set.
3. THE `Dashboard_Service` SHALL calculate **satisfaction average** as the arithmetic mean of numeric values of `Respuesta` records whose `Item.tipo` is `ESCALA`, rounded to two decimal places using half-up rounding. WHEN a `Respuesta.valor` for an `ESCALA` item cannot be parsed as an integer in [1,5], THEN that record SHALL be silently excluded from the average calculation.
4. THE `Dashboard_Service` SHALL identify **mejor escuela** as the `escuelaProfesional` group (as defined in Requirement 3) with the highest `Promedio_Satisfaccion` in the filtered set. IF two or more school groups share the highest average, THE `Dashboard_Service` SHALL select the one that comes first alphabetically (ascending).
5. THE `Dashboard_Service` SHALL identify **peor escuela** as the `escuelaProfesional` group with the lowest `Promedio_Satisfaccion` in the filtered set. IF two or more school groups share the lowest average, THE `Dashboard_Service` SHALL select the one that comes first alphabetically (ascending).
6. THE `Dashboard_Service` SHALL calculate **participation rate** as the quotient of `Egresado_Encuestado` in the filtered set divided by the total registered `Usuario` in the system, expressed as a percentage rounded to two decimal places using half-up rounding. IF the total registered `Usuario` count is zero, THEN `tasaParticipacion` SHALL be `0.0`.
7. IF the filtered set contains no `ESCALA` responses, THEN THE `Dashboard_Service` SHALL return `0.0` for satisfaction average, `null` for `mejorEscuela` and `peorEscuela`. The `tasaParticipacion` SHALL still be calculated based on `Egresado_Encuestado` regardless of `ESCALA` responses.

---

### Requirement 3: Comparison by Professional School

**User Story:** As an administrator, I want to see the average satisfaction score broken down by professional school so that I can compare performance between schools.

#### Acceptance Criteria

1. THE `Dashboard_Service` SHALL group `Respuesta` records of type `ESCALA` in the filtered set (scoped by `idEncuesta` when provided, per Requirement 1 criterion 2) by the field `Usuario.escuelaProfesional`.
2. THE `Dashboard_Service` SHALL calculate the average satisfaction for each school group as the arithmetic mean of parseable integer `Respuesta.valor` values in [1,5], rounded to two decimal places using half-up rounding.
3. THE `Dashboard_Service` SHALL return the list of schools sorted from highest to lowest average. IF two school groups have the same average, THEN they SHALL be sorted alphabetically ascending by school name as a tie-breaking rule.
4. IF a `Usuario` has no value in `escuelaProfesional` (null or blank), THEN THE `Dashboard_Service` SHALL group their responses under the label `"SIN ESCUELA"`.
5. IF a school group has zero parseable `ESCALA` responses, THEN THE `Dashboard_Service` SHALL exclude that group from the result list.
6. WHEN the filtered set contains no `ESCALA` responses, THE `Dashboard_Service` SHALL return an empty list for the school comparison.

---

### Requirement 4: Sentiment Distribution

**User Story:** As an administrator, I want to see the percentage distribution of graduate sentiment so that I can understand the proportion of satisfaction and dissatisfaction.

#### Acceptance Criteria

1. WHEN the dashboard is requested, THE `Dashboard_Service` SHALL calculate the individual average of `ESCALA` responses for each `Egresado_Encuestado` in the filtered set. IF a graduate has no parseable `ESCALA` responses, THEN that graduate SHALL be excluded from the sentiment distribution calculation.
2. THE `Dashboard_Service` SHALL classify each included graduate into a `Categoria_Sentimiento` based on their individual average: `Muy Satisfecho` for averages in [4.5, 5.0], `Satisfecho` for [3.5, 4.5), `Neutral` for [2.5, 3.5), `Insatisfecho` for [1.5, 2.5), `Muy Insatisfecho` for [1.0, 1.5).
3. THE `Dashboard_Service` SHALL calculate the percentage of each `Categoria_Sentimiento` as the count of graduates in that category divided by the total included graduates multiplied by 100, rounded to two decimal places using half-up rounding (e.g., 1 out of 3 graduates = 33.33%).
4. THE `Dashboard_Service` SHALL always return all five sentiment categories in the response, with a percentage of `0.00` for those that have no graduates.
5. WHEN the total of included graduates is zero, THE `Dashboard_Service` SHALL return all five categories with a percentage of `0.00`.

---

### Requirement 5: Quality Ranking by Question

**User Story:** As an administrator, I want to see a ranking of survey questions with their average and quality status so that I can identify the best and worst evaluated items.

#### Acceptance Criteria

1. THE `Dashboard_Service` SHALL include in the ranking only `Item` records of type `ESCALA` that have at least one parseable response in the filtered set.
2. THE `Dashboard_Service` SHALL calculate the average of each `Item` as the arithmetic mean of the parseable integer values (in [1,5]) of its `Respuesta` records in the filtered set, rounded to two decimal places using half-up rounding. `Respuesta.valor` values that cannot be parsed as an integer in [1,5] SHALL be silently excluded from the calculation for that item.
3. THE `Dashboard_Service` SHALL assign the `codigo` of each ranking entry from the `Dimension.codigo` field of the `Dimension` to which the `Item` belongs. IF `Dimension.codigo` is null or blank, THEN the `codigo` SHALL be set to `"SIN_CODIGO"`.
4. THE `Dashboard_Service` SHALL assign the `Estado_Item` to each item based on its average: `Excelente` for average ≥ 4.5, `Bueno` for average ≥ 3.5 and < 4.5, `Regular` for average ≥ 2.5 and < 3.5, `Crítico` for average < 2.5.
5. THE `Dashboard_Service` SHALL return the list of items sorted from highest to lowest average. IF two items share the same average, THEN they SHALL be sorted alphabetically ascending by `Item.descripcion` as a tie-breaking rule.
6. WHEN the filtered set contains no parseable `ESCALA` responses, THE `Dashboard_Service` SHALL return an empty list for the ranking.

---

### Requirement 6: Dashboard JSON Response Structure

**User Story:** As a frontend developer, I want the endpoint to return all dashboard data in a single structured JSON object so that I can render the dashboard without additional calls.

#### Acceptance Criteria

1. THE `Dashboard_Controller` SHALL return a root JSON object with exactly the fields: `kpis`, `comparativoPorEscuela`, `distribucionSentimiento`, and `rankingCalidadPreguntas`.
2. THE `Dashboard_Controller` SHALL structure the `kpis` field with: `totalRespuestas` (Integer, ≥ 0), `egresadosEncuestados` (Integer, ≥ 0), `promedioSatisfaccion` (Double, range 0.0–5.0), `mejorEscuela` (String, nullable), `peorEscuela` (String, nullable), and `tasaParticipacion` (Double, range 0.0–100.0).
3. THE `Dashboard_Controller` SHALL structure each element of `comparativoPorEscuela` with: `escuelaProfesional` (String), `promedioSatisfaccion` (Double, range 1.0–5.0), and `totalRespuestas` (Integer, ≥ 0). The array SHALL be sorted from highest to lowest `promedioSatisfaccion`.
4. THE `Dashboard_Controller` SHALL structure each element of `distribucionSentimiento` with: `categoria` (String, one of `"Muy Satisfecho"`, `"Satisfecho"`, `"Neutral"`, `"Insatisfecho"`, `"Muy Insatisfecho"`) and `porcentaje` (Double, range 0.00–100.00). The sum of all `porcentaje` values SHALL equal 100.00 when at least one graduate is included; when no graduates are included all values SHALL be 0.00.
5. THE `Dashboard_Controller` SHALL structure each element of `rankingCalidadPreguntas` with: `codigo` (String), `descripcion` (String), `promedio` (Double, range 1.0–5.0), and `estado` (String, one of `"Excelente"`, `"Bueno"`, `"Regular"`, `"Crítico"`). The array SHALL be sorted from highest to lowest `promedio`.
6. IF no `Respuesta` records exist after applying the filters, THEN THE `Dashboard_Controller` SHALL return a `200 OK` response where `kpis.totalRespuestas` is `0`, `kpis.egresadosEncuestados` is `0`, `kpis.promedioSatisfaccion` is `0.0`, `kpis.mejorEscuela` is `null`, `kpis.peorEscuela` is `null`, `kpis.tasaParticipacion` is `0.0`, and all three list fields are empty arrays `[]`.
