# plan.md — REXIA Cloud

> Plan de trabajo vivo (Spec-Driven Development). Contexto y metodología en spec.md.
> Reglas: nada se implementa sin su punto aquí · al terminar se marca [x] con fecha ·
> las tareas grandes se desglosan en fases antes de empezar.

## Estado actual (2026-09-28)

| Fase | Estado |
|---|---|
| Fase 0 · Andamiaje | ✅ cerrada (2026-09-28) |
| Fase 1 · Primer agregado y primer caso de uso (TDD) | ⏳ pendiente — dominio por decidir |
| Fase 2 · Persistencia real detrás del puerto | ⏳ pendiente |
| Fase 3 · AWS simulado (LocalStack: S3/SQS) | ⏳ pendiente |
| Fase 4 · ¿Integración con rexia-clasico? | ⏳ por decidir |
| Fase 5 · CI y cierre | ⏳ pendiente |

**Pendiente inmediato:** decidir con Borja el dominio de la Fase 1 (ver spec.md §1 y §3-D8: empezar de cero vs. tomar el rol de "registro nacional simulado" que prevé `rexia-clasico` en su Fase 5). Hasta entonces, no se implementa nada de negocio.

## Fase 0 · Andamiaje (cerrada — la montó Claude completa, igual que en rexia-clasico)

- [x] (2026-09-28) Decisión de abrir proyecto hermano en vez de migrar rexia-clasico — ver spec.md D1-D2
- [x] (2026-09-28) `pom.xml` independiente: Spring Boot 3.2.5, Java 21, sin ser módulo del reactor de `rexia`
- [x] (2026-09-28) Paquetes hexagonales vacíos con `package-info.java` explicando cada capa: `domain`, `application/port/{in,out}`, `application/service`, `infrastructure/adapter/{in/rest,out/persistence}`, `infrastructure/config`
- [x] (2026-09-28) `EstadoController` (`GET /api/estado`) como smoke check manual, paralelo al `GET /` de rexia-clasico
- [x] (2026-09-28) `application.yml` (puerto 8081, datasource Postgres 5433, actuator health)
- [x] (2026-09-28) `docker-compose.yml` propio: Postgres 16 (5433) + LocalStack (4566)
- [x] (2026-09-28) `HexagonalArchitectureTest` (ArchUnit): domain/application no pueden depender de infrastructure ni de Spring — verificado en verde con JDK 21
- [x] (2026-09-28) `RexiaCloudApplicationSmokeIT` (Testcontainers + Postgres real) — escrito y compila; **ejecución pendiente** de la primera vez que Borja abra Docker Desktop (no estaba en marcha en esta sesión)
- [x] (2026-09-28) `spec.md`, `plan.md`, `README.md`, `CLAUDE.md` de este proyecto
- [x] (2026-09-28) `mvn compile` verificado con JDK 21 del sistema (`C:\Program Files\Java\jdk-21.0.9`), sin tocar el JDK 11 de rexia-clasico

**Verificable:** `mvn compile` construye sin errores y `mvn test -Dtest=HexagonalArchitectureTest` pasa — confirmado el 2026-09-28. **Falta verificar** (Docker Desktop abierto): `docker compose up -d` en esta carpeta + `mvn test -Dtest=RexiaCloudApplicationSmokeIT` + `mvn spring-boot:run` y `curl http://localhost:8081/api/estado`.

**Fase 0 cerrada (2026-09-28).**

## Fase 1 · Primer agregado y primer caso de uso

Modo mentor estricto desde aquí (ver spec.md §6) — Borja implementa.

- [ ] Decidir el dominio de partida con Borja (spec.md §1/§3-D8)
- [ ] Primer value object o entidad de dominio, con test unitario escrito antes que la clase (TDD)
- [ ] Primer puerto de entrada (`application/port/in`) y su caso de uso
- [ ] Primer adaptador REST que lo expone
- [ ] Repositorio en memoria (adaptador out "falso") para no acoplar el caso de uso a Postgres todavía

**Verificable:** un test de caso de uso pasa sin tocar Spring ni la base de datos; el endpoint REST responde usando ese caso de uso.

## Fase 2 · Persistencia real detrás del puerto

- [ ] Entidad JPA (anotada) distinta del modelo de dominio, con su propio mapper
- [ ] Adaptador de persistencia que implementa el puerto out sobre Postgres
- [ ] Sustituir el repositorio en memoria por el real sin tocar el caso de uso

**Verificable:** el mismo test de caso de uso de la Fase 1 sigue en verde; un test de integración con Testcontainers cubre el adaptador de persistencia.

## Fase 3 · AWS simulado (LocalStack)

- [ ] Un caso de uso que publique un evento (SQS) o guarde un fichero (S3) contra LocalStack
- [ ] Adaptador de salida correspondiente, detrás de su propio puerto

**Verificable:** el evento/fichero aparece en LocalStack tras invocar el caso de uso, verificado con un test de integración.

## Fase 4 · ¿Integración con rexia-clasico?

Por decidir — ver spec.md §1 y D-candidato del "registro nacional simulado" (Fase 5 de rexia-clasico).

- [ ] Decisión explícita: ¿`rexia-cloud` pasa a jugar ese rol, o quedan totalmente independientes?

## Fase 5 · CI y cierre

- [ ] GitHub Actions (`mvn -f rexia-cloud/pom.xml verify`)
- [ ] README con lo aprendido y decisiones técnicas

## Histórico de fases completadas

- **Fase 0 · Andamiaje** — cerrada 2026-09-28.
