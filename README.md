# REXIA — Rexistro de Identificación Animal

Proyecto de aprendizaje y portfolio de Borja Olazabal Hernández: un registro autonómico de identificación de animales de compañía, inspirado en el REGIAC (Rexistro Galego de Identificación de Animais de Compañía) de la Xunta de Galicia, con el stack clásico de las consultoras del sector público (Spring sin Boot, Oracle, JSP + Tiles).

> Reimplementación original y desde cero. No contiene código, datos ni lógica de negocio de EJIE, Inetum, Bilbomática ni del REGIAC real. Todos los datos de demostración son ficticios.

## Estado actual

**Fase 0 · Andamiaje** — cerrada. Estructura Maven multi-módulo, Spring configurado (persistencia, MVC, Tiles, seguridad placeholder), Oracle XE en Docker con datos maestros, y `GET /` verificado end-to-end. Ver [plan.md](plan.md) para el detalle y las fases siguientes.

## Qué hace REXIA (visión del proyecto completo)

- Censo de animales identificados con microchip (15 dígitos, ISO 11784/11785), ligados a un titular con histórico completo.
- Veterinarios habilitados, con series de microchip asignadas sin solapamiento.
- Máquina de estados del animal: activo, perdido, en acogida, adoptado, baja...
- Cambios de titularidad, incidencias (pérdida, robo, hallazgo, defunción) y licencias de animales potencialmente peligrosos.
- Sincronización con un registro nacional simulado.
- Consulta pública por microchip **sin exponer datos personales** del titular (diseño orientado a protección de datos).
- Multilenguaje castellano / gallego.

El detalle completo del dominio, las decisiones de arquitectura y la metodología de trabajo viven en [spec.md](spec.md).

## Stack

| Capa | Tecnología |
|---|---|
| Lenguaje | Java 11 |
| Framework | Spring Framework 5.3.x, clásico (sin Boot) |
| Servidor de aplicaciones | Apache Tomcat 9 |
| Flujo de trámites | Spring Web Flow |
| Persistencia | Spring Data JPA + Hibernate (mapeo XML, sin `@Entity`) · JdbcTemplate para informes |
| Base de datos | Oracle XE 21c en Docker |
| Vista | JSP + JSTL + Tiles |
| Estilos | Bootstrap 5 + jQuery |
| Seguridad | Spring Security |
| Build | Maven (multi-módulo) |
| Contenedores | Docker Compose |

Detalle completo y decisiones tomadas: [spec.md §2-3](spec.md).

## Estructura del proyecto

```
rexia/
├── pom.xml                    # POM padre (gestión de dependencias/versiones)
├── rexia-core/                # Modelo, persistencia y servicios (jar)
│   └── src/main/
│       ├── java/com/bohdeveloper/rexia/
│       │   ├── model/{entity,dto,enums}/
│       │   ├── repository/
│       │   ├── service/{impl,estado}/
│       │   ├── integration/nacional/
│       │   ├── util/
│       │   └── exception/
│       └── resources/
│           ├── db.properties
│           ├── META-INF/{persistence.xml,orm.xml}
│           └── spring/applicationContext.xml
├── rexia-web/                 # Controladores y vista (war)
│   └── src/main/
│       ├── java/com/bohdeveloper/rexia/controller/{web,api}/
│       ├── resources/messages.properties
│       └── webapp/WEB-INF/
│           ├── web.xml
│           ├── spring/{servlet-context.xml,security-context.xml}
│           ├── tiles.xml
│           └── views/
├── docker-compose.yml         # Oracle XE 21c
├── docker/init/               # DDL + datos maestros (autoejecutado al levantar Oracle)
├── spec.md                    # Memoria del proyecto (dominio, arquitectura, decisiones)
├── plan.md                    # Plan de trabajo por fases
└── CLAUDE.md                  # Reglas de trabajo (modo mentor, SDD)
```

## Instalación y arranque

Requisitos: JDK 11, Maven, Docker Desktop, y opcionalmente Eclipse IDE for Enterprise Java and Web Developers con Tomcat 9 registrado.

```bash
# 1. Levantar Oracle XE (puerto 1522 en el host; ver spec.md si tienes un Oracle nativo en 1521)
docker compose up -d

# 2. Compilar y empaquetar
mvn clean package

# 3. Desplegar rexia-web/target/rexia.war en un Tomcat 9 (Eclipse, o manualmente)
#    y abrir http://localhost:8080/
```

Prueba de humo de la capa de persistencia contra el Oracle real de Docker:

```bash
mvn test -pl rexia-core -Dtest=ApplicationContextSmokeIT
```

## Documentación del proyecto

| Documento | Contenido |
|---|---|
| [spec.md](spec.md) | Qué es REXIA, dominio, arquitectura, decisiones tomadas, convenciones y metodología |
| [plan.md](plan.md) | Plan de trabajo vivo por fases, con histórico de lo completado |
| [CLAUDE.md](CLAUDE.md) | Reglas de trabajo para Claude Code (modo mentor, Spec-Driven Development) |

Este repositorio sigue **Spec-Driven Development**: `spec.md` y `plan.md` son la fuente de verdad del proyecto, por delante de este README.
