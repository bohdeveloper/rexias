# REXIA Cloud

Proyecto hermano de [rexia-clasico](../README.md) (raíz de este mismo repositorio): sandbox de arquitectura moderna — Java 21, Spring Boot, arquitectura hexagonal, DDD, TDD y AWS simulado — sobre el mismo universo de dominio que REXIA (registro de identificación de animales de compañía).

> **Por qué existe:** `rexia-clasico` demuestra a propósito el stack clásico de las consultoras del sector público gallego (Spring sin Boot, JSP, Oracle). Una oferta de trabajo distinta pedía Java 21+/Spring Boot/microservicios/hexagonal/DDD/TDD/AWS — un perfil que no tenía sentido forzar dentro de ese proyecto. Este es el sitio para ese perfil, sin tocar las decisiones ya tomadas en `rexia-clasico`.

## Estado actual

**Fase 0 · Andamiaje** — cerrada. Proyecto Spring Boot 3.2.5 / Java 21 independiente (no es módulo del reactor de `rexia`), arquitectura hexagonal con los paquetes vacíos y sus límites vigilados por ArchUnit, Docker Compose propio (Postgres + LocalStack), y un smoke test con Testcontainers. Ver [plan.md](plan.md) para el detalle y las fases siguientes.

## Arquitectura

```
com.bohdeveloper.rexiacloud
├── domain/                          Entidades y reglas de negocio puras (sin Spring)
├── application/
│   ├── port/in/                     Casos de uso (interfaces)
│   ├── port/out/                    Lo que la aplicación necesita del exterior
│   └── service/                     Implementación de los casos de uso
└── infrastructure/
    ├── adapter/in/rest/             Controladores REST
    ├── adapter/out/persistence/     Repositorios JPA
    └── config/                      Beans de Spring
```

`domain` y `application` no pueden depender de `infrastructure` ni de Spring — lo comprueba `HexagonalArchitectureTest` (ArchUnit) en cada build. Detalle completo y decisiones tomadas: [spec.md](spec.md).

## Stack

| Capa | Tecnología |
|---|---|
| Lenguaje | Java 21 |
| Framework | Spring Boot 3.2.x |
| Persistencia | Spring Data JPA + Hibernate |
| Base de datos | PostgreSQL 16 (Docker) |
| AWS (simulado) | LocalStack (S3, SQS) |
| Tests | JUnit 5 + AssertJ + Mockito + Testcontainers + ArchUnit |
| Build | Maven |

## Instalación y arranque

Requisitos: JDK 21, Maven, Docker Desktop.

```bash
# 1. Levantar Postgres + LocalStack (puertos 5433 y 4566, no chocan con rexia-clasico)
docker compose up -d

# 2. Compilar
mvn clean compile

# 3. Arrancar (servidor embebido en el 8081)
mvn spring-boot:run

# 4. Comprobar
curl http://localhost:8081/api/estado
curl http://localhost:8081/actuator/health
```

Tests de arquitectura (no requieren Docker):

```bash
mvn test -Dtest=HexagonalArchitectureTest
```

Smoke test de persistencia contra un Postgres real (Testcontainers, requiere Docker en marcha):

```bash
mvn test -Dtest=RexiaCloudApplicationSmokeIT
```

## Documentación del proyecto

| Documento | Contenido |
|---|---|
| [spec.md](spec.md) | Qué es, arquitectura, decisiones tomadas, convenciones y metodología |
| [plan.md](plan.md) | Plan de trabajo vivo por fases |
| [CLAUDE.md](CLAUDE.md) | Reglas de trabajo para Claude Code en esta carpeta |

Este proyecto sigue **Spec-Driven Development**: `spec.md` y `plan.md` son la fuente de verdad, por delante de este README.
