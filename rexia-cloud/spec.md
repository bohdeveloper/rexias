# spec.md — REXIA Cloud

> Documento vivo de especificación (Spec-Driven Development).
> Memoria de este proyecto: qué es, cómo está construido, decisiones y metodología.
> El trabajo pendiente vive en plan.md (que incluye el histórico resumido de lo completado).
> Regla: toda decisión nueva de producto/arquitectura se registra aquí en la misma sesión.

## 1. Qué es REXIA Cloud

Proyecto **hermano** de `rexia-clasico` (la raíz de este mismo repositorio), no una sustitución. Vive en `rexia-cloud/`, con su propio `pom.xml` — no es un módulo del reactor Maven de `rexia`, es un proyecto Maven independiente que se compila y despliega por separado.

**Por qué existe** — Una oferta de trabajo pedía un perfil distinto al que demuestra `rexia-clasico`: Java 21+, Spring Boot, microservicios, arquitectura hexagonal, SOLID, patrones de diseño, TDD, DDD, Clean Code, y conocimiento de AWS. `rexia-clasico` existe a propósito para el perfil contrario (consultoras del sector público gallego con Spring clásico, ver spec.md raíz §1) — sus decisiones D1-D8 no se tocan. En vez de forzar el perfil moderno dentro de ese proyecto, se abre este como sandbox independiente, en el mismo repo, para poder aprender y demostrar ambos.

**Relación con rexia-clasico**
- Mismo repositorio Git, cero acoplamiento de build.
- Ritmos independientes: sin secuencia obligatoria entre los dos (decisión de Borja, 2026-09-28 — ver spec.md raíz, actualización de §7).
- Candidato natural a futuro, **no decidido todavía**: `rexia-clasico` ya prevé en su Fase 5 un "microservicio simulado del registro nacional" (REIAC) al que sincronizarse vía REST. `rexia-cloud` encaja ahí por diseño (mismo dominio, arquitectura moderna, ya dibujado como caja aparte en el diagrama de arquitectura de spec.md raíz §2). Se decide explícitamente si y cuando `rexia-clasico` llegue a esa fase — no se asume ahora.

**Dominio de partida** — Deliberadamente sin decidir todavía. La Fase 0 es andamiaje puro (estructura, build, Docker, arquitectura hexagonal vacía) sin entidades de negocio, igual que se hizo en `rexia-clasico`. El primer agregado de dominio se define en la Fase 1, con Borja, en modo mentor.

## 2. Stack y arquitectura

| Capa | Tecnología |
|---|---|
| Lenguaje | Java 21 (LTS) |
| Framework | Spring Boot 3.2.x |
| Persistencia | Spring Data JPA + Hibernate (anotaciones, sin XML — al contrario que rexia-clasico, aquí sí es idiomático) |
| Base de datos | PostgreSQL 16 en Docker |
| Arquitectura | Hexagonal (puertos y adaptadores) |
| AWS (simulado) | LocalStack (S3, SQS) en Docker |
| Tests | JUnit 5 + AssertJ + Mockito (vía `spring-boot-starter-test`) + Testcontainers (Postgres real en los tests de integración) + ArchUnit (reglas de arquitectura como test) |
| Build | Maven (proyecto único, sin multi-módulo por ahora) |
| Contenedores | Docker Compose (`rexia-cloud/docker-compose.yml`: Postgres + LocalStack) |

**Arquitectura hexagonal — paquetes** (`com.bohdeveloper.rexiacloud`)

```
com.bohdeveloper.rexiacloud
├── domain/                          Entidades, value objects, reglas de negocio puras.
│                                    Sin Spring, sin JPA. Vacío en Fase 0 a propósito.
├── application/
│   ├── port/in/                     Casos de uso (interfaces) — driving ports
│   ├── port/out/                    Lo que la aplicación necesita del exterior — driven ports
│   └── service/                     Implementación de los casos de uso
└── infrastructure/
    ├── adapter/in/rest/             Controladores REST (implementan port/in indirectamente,
    │                                lo invocan)
    ├── adapter/out/persistence/     Repositorios JPA (implementan port/out)
    └── config/                      Beans de Spring, wiring
```

`domain` y `application` no pueden depender de `infrastructure` ni de `org.springframework.*` — no es una convención de palabra, `HexagonalArchitectureTest` (ArchUnit) lo comprueba en cada build y rompe el test si se viola.

## 3. Decisiones de arquitectura tomadas

- **D1 (2026-09-28) — Proyecto hermano, no migración.** Se descarta migrar `rexia-clasico` a Java 21/Boot/hexagonal porque sus decisiones D1-D8 (spec.md raíz) se tomaron a propósito para otro objetivo (perfil de consultoras gallegas, calcar el proyecto de Ocaso). Se abre `rexia-cloud` como proyecto independiente en vez de reescribir el existente.
- **D2 (2026-09-28) — No se mueve `rexia-clasico` de sitio.** El `.gitignore` raíz documenta que el workspace de Eclipse **es** la carpeta raíz del repo (`.metadata/`, `Servers/` ahí). Mover `rexia-core`/`rexia-web` a una subcarpeta habría roto el Server/JRE ya registrados en Eclipse sin necesidad. `rexia-cloud` se añade como carpeta hermana nueva; `rexia-clasico` sigue en la raíz tal cual estaba.
- **D3 (2026-09-28) — PostgreSQL en vez de Oracle.** Decisión deliberada de no repetir la misma base de datos que `rexia-clasico`: Postgres es más representativo del mundo cloud/AWS (RDS, Aurora) que se quiere demostrar aquí, y da la oportunidad de aprender un motor distinto.
- **D4 (2026-09-28) — Hexagonal enforzado con ArchUnit desde la Fase 0.** En vez de confiar solo en la disciplina de dónde se coloca cada clase, `HexagonalArchitectureTest` falla el build si `domain` o `application` acaban dependiendo de `infrastructure` o de Spring. Se decide meterlo ya en el andamiaje (no esperar a que la tentación de "total es solo esta vez" aparezca en la Fase 1).
- **D5 (2026-09-28) — LocalStack para AWS, no una cuenta real.** Simula S3/SQS en Docker sin coste ni credenciales reales; suficiente para aprender y demostrar el patrón de integración con AWS. Se reconsidera si en algún momento hace falta probar contra AWS real.
- **D6 (2026-09-28) — Puertos elegidos para no chocar con `rexia-clasico`.** App en 8081 (Tomcat de rexia-clasico ya usa 8080), Postgres en 5433 (Oracle de rexia-clasico ya usa 1522, y 5433 evita además un Postgres local si Borja llega a instalar uno), LocalStack en 4566 (puerto por defecto, sin conflicto conocido).
- **D7 (2026-09-28) — Sin límite de horas semanales fijo.** Se abandona el límite de 10h/semana que sí regía para `rexia-clasico` (spec.md raíz §7, ahora actualizado): Borja avanza en cada proyecto según le apetezca, sin ritmo ni secuencia obligatoria entre los dos.
- **D8 (2026-09-28) — Dominio de negocio pendiente de decidir.** La Fase 0 no incluye ninguna entidad ni caso de uso real a propósito, igual que en `rexia-clasico`. Se decide con Borja al arrancar la Fase 1, evaluando entre empezar desde cero o partir del rol de "registro nacional simulado" que menciona §1.

## 4. Convenciones

- Nombres de clases y métodos en inglés; comentarios técnicos en castellano solo donde el "por qué" no es obvio (mismo criterio que `rexia-clasico`, ver spec.md raíz §7).
- TDD desde la Fase 1: test antes que producción en `domain` y `application`. `RexiaCloudApplicationSmokeIT` ya fija el precedente de "conecta contra infraestructura real" en vez de mockear la base de datos en la prueba de arranque.
- DTOs (o `record`) para entrada/salida de los adaptadores REST; nunca exponer entidades JPA directamente.
- Un caso de uso por clase de servicio de aplicación (Single Responsibility también a nivel de puerto).

## 5. Entorno de desarrollo

- JDK 21 del sistema (`C:\Program Files\Java\jdk-21.0.9`) — el mismo que ya estaba instalado; no se toca el JDK 11 dedicado a `rexia-clasico` ni el JRE 17 de Eclipse.
- Docker Compose propio: `rexia-cloud/docker-compose.yml` levanta Postgres (5433) y LocalStack (4566), independiente del `docker-compose.yml` raíz (Oracle de `rexia-clasico`).
- App en el puerto 8081 vía `mvn spring-boot:run` (servidor embebido, sin Tomcat externo ni Eclipse — a diferencia de `rexia-clasico`).

**Verificado el 2026-09-28:** `mvn compile` construye el proyecto con JDK 21; `HexagonalArchitectureTest` (ArchUnit) pasa. **Pendiente de verificar** la primera vez que Borja tenga Docker Desktop abierto: `docker compose up -d` en `rexia-cloud/` y `mvn test -Dtest=RexiaCloudApplicationSmokeIT` (Testcontainers, conecta contra un Postgres real) — no se pudo ejecutar en esta sesión porque Docker no estaba en marcha.

## 6. Metodología de desarrollo

Misma disciplina que `rexia-clasico` (ver spec.md raíz §6, no se duplica aquí): Spec-Driven Development con este `spec.md`/`plan.md`, y **modo mentor desde la Fase 1** — Claude explica, Borja implementa. La Fase 0 (este andamiaje) es la única excepción, igual que allí.

## 7. Documentos del proyecto

| Documento | Rol |
|---|---|
| `spec.md` | Este documento |
| `plan.md` | Trabajo pendiente por fases |
| `README.md` | Escaparate de este proyecto dentro del repo |
| `CLAUDE.md` | Reglas de trabajo específicas de esta carpeta |

Ver también `spec.md` y `README.md` en la raíz del repo para el proyecto hermano `rexia-clasico` y el porqué de tener los dos.
