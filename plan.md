# plan.md — REXIA

> Plan de trabajo vivo (Spec-Driven Development). Contexto y metodología en spec.md.
> Reglas: nada se implementa sin su punto aquí · al terminar se marca [x] con fecha ·
> las tareas grandes se desglosan en fases antes de empezar.
> El detalle punto por punto de lo completado vive en el historial de git.

## Estado actual (2026-08-26)

| Fase | Estado |
|---|---|
| Fase 0 · Andamiaje | ✅ cerrada (2026-08-26) |
| Fase 1 · Maestros y titulares | ⏳ pendiente |
| Fase 2 · Seguridad y multilenguaje | ⏳ pendiente |
| Fase 3 · Identificación de animales | ⏳ pendiente |
| Fase 4 · Máquina de estados e incidencias | ⏳ pendiente |
| Fase 5 · Integración con el registro nacional | ⏳ pendiente |
| Fase 6 · Consulta pública y protección de datos | ⏳ pendiente |
| Fase 7 · Licencias PPP, documentos e informes | ⏳ pendiente |
| Fase 8 · Cierre | ⏳ pendiente |

**Pendiente inmediato:** arrancar la Fase 1 (entidades Titular, Veterinario, Centro veterinario) en modo mentor estricto — Borja implementa.

## Fase 0 · Andamiaje (en curso — la monta Claude completa)

- [x] (2026-08-26) Decisión de stack cerrada: UDA descartado, Spring Framework 5.3.x clásico sin Boot, Java 11, JSP+Tiles+Web Flow mantenidos, Tomcat 9 (WebLogic pospuesto), git solo local por ahora — ver spec.md §3 decisiones D1-D6
- [x] (2026-08-26) Entorno de desarrollo instalado y verificado: JDK 11 Temurin registrado en Eclipse, Eclipse IDE for Enterprise Java and Web Developers, Tomcat 9.0.121 arrancando sobre JDK 11 en el puerto 8080, Docker Desktop 29.6.2 funcionando
- [x] (2026-08-26) spec.md y plan.md generados (spec-driven bootstrap)
- [x] (2026-08-26) Estructura Maven multi-módulo: `rexia` (pom padre) + `rexia-core` (jar: modelo/persistencia/servicios) + `rexia-web` (war: controladores/vista) — ver spec.md §2 y decisiones D7-D8
- [x] (2026-08-26) `docker-compose.yml` con Oracle XE 21c (`gvenzl/oracle-xe:21-slim-faststart`, puerto host 1522 para no chocar con el Oracle nativo ya instalado)
- [x] (2026-08-26) Configuración base de Spring (XML: contexto raíz, MVC, JPA/Hikari), seguridad (placeholder `security="none"`) y Tiles (layout + fragments + home)
- [x] (2026-08-26) Script DDL inicial y datos maestros: `RXA_ESPECIE` (5), `RXA_RAZA` (4), `RXA_CONCELLO` (6) — `docker/init/01_maestros.sql`, autoejecutado por docker-compose
- [x] (2026-08-26) Verificación end-to-end: `mvn package` construye el WAR; `ApplicationContextSmokeIT` conecta de verdad con el Oracle de Docker; WAR desplegado en un Tomcat 9 aislado respondió `GET /` → 200 con Tiles componiendo el layout correctamente
- [x] (2026-08-26) README.md de presentación
- [x] (2026-08-26) `git init` local + primer commit (`7cc6c09`) + remoto conectado y pusheado a `github.com/bohdeveloper/rexias` (rama `main`)

**Verificable:** `docker compose up` levanta Oracle y la app responde en `/` — confirmado el 2026-08-26 (ver spec.md §5).

**Fase 0 cerrada (2026-08-26).**

## Fase 1 · Maestros y titulares

- [ ] Entidades: Titular, Veterinario, Centro veterinario
- [ ] CRUD completo de titulares y veterinarios
- [ ] Layout Tiles + navegación

**Verificable:** se da de alta un titular y un veterinario habilitado, y se listan con paginación.

## Fase 2 · Seguridad y multilenguaje

- [ ] Spring Security con los cinco roles (VETERINARIO, TITULAR, NUCLEO_ZOOLOXICO, INSPECTOR, ADMIN)
- [ ] Login y control de acceso por rol
- [ ] Ficheros de recursos es/gl y selector de idioma

**Verificable:** un titular solo ve sus propios animales; la interfaz cambia de idioma.

## Fase 3 · Identificación de animales

- [ ] Entidad Animal con validación del microchip (15 dígitos, país 724, unicidad)
- [ ] Series de microchips asignadas a veterinarios sin solapamiento
- [ ] Alta de identificación: solo veterinario habilitado y dentro de su rango
- [ ] Buscador con filtros (especie, concello, estado) y paginación en servidor

**Verificable:** un chip fuera del rango del veterinario se rechaza; un chip duplicado también.

## Fase 4 · Máquina de estados e incidencias

- [ ] Motor de transiciones con validación
- [ ] Pérdida, robo con denuncia, hallazgo, defunción
- [ ] Cambio de titularidad con histórico completo
- [ ] Auditoría de cambios (quién, qué, cuándo)
- [ ] Cómputo de plazos hábiles de comunicación

**Verificable:** no se puede pasar de BAIXA a ACTIVO sin justificación; el histórico de titulares es consultable.

> Si en esta fase el calendario aprieta: cerrar el alcance aquí y saltar directamente a la Fase 8 (ver spec.md §7, Ritmo y límites).

## Fase 5 · Integración con el registro nacional

- [ ] Microservicio simulado del registro nacional
- [ ] Cliente REST con timeout, reintentos y manejo de errores
- [ ] Sincronización de altas y cambios; reconciliación de discrepancias

**Verificable:** si el registro nacional no responde, el alta local no se corrompe y queda registrado el fallo para reintento.

## Fase 6 · Consulta pública y protección de datos

- [ ] API REST pública de consulta por microchip
- [ ] Respuesta sin datos personales: estado del animal y canal de contacto mediado
- [ ] Control de acceso por rol para la ficha completa
- [ ] Registro de accesos a datos sensibles

**Verificable:** la consulta pública nunca devuelve el DNI ni el teléfono del titular.

## Fase 7 · Licencias PPP, documentos e informes

- [ ] Licencias de animales potencialmente peligrosos con vigencia y renovación
- [ ] Alerta de licencias caducadas
- [ ] PDF de certificado de identificación y de acta de inspección
- [ ] Procedimiento PL/SQL para censo agregado por concello y especie

**Verificable:** el PDF se genera y el censo cuadra con los datos.

## Fase 8 · Cierre

- [ ] Tests de los servicios críticos
- [ ] GitHub Actions + SonarCloud
- [ ] README con capturas, diagramas y decisiones técnicas
- [ ] Datos de demo precargados (ficticios)

**Verificable:** un tercero clona el repo y lo levanta sin preguntar nada.

## Histórico de fases completadas

Aún no hay fases completadas — proyecto recién arrancado (2026-08-26).
