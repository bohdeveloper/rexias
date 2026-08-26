# spec.md — REXIA

> Documento vivo de especificación (Spec-Driven Development).
> Memoria del proyecto: qué es, cómo está construido, decisiones y metodología.
> El trabajo pendiente vive en plan.md (que incluye el histórico resumido de lo completado).
> Regla: toda decisión nueva de producto/arquitectura se registra aquí en la misma sesión.

## 1. Qué es REXIA

**REXIA — Rexistro de Identificación Animal.** Proyecto de aprendizaje y portfolio de Borja Olazabal Hernández. Sistema de gestión de un registro autonómico de identificación de animales de compañía: censo de animales con microchip, veterinarios habilitados, control de series de chips, cambios de titularidad, incidencias (pérdida, hallazgo, defunción), licencias de tenencia de animales potencialmente peligrosos (PPP) y potestad inspectora y sancionadora.

Inspirado en el REGIAC (Rexistro Galego de Identificación de Animais de Compañía), dependiente de la Consellería de Medio Ambiente de la Xunta y gestionado por el Consello Galego de Colexios Veterinarios. El modelo es equivalente al de otras comunidades autónomas.

**Por qué este dominio**
- Administración pública real con potestad de registro, certificación y sanción.
- Trazabilidad estricta: un microchip único e irrepetible ligado a un animal y a un titular, con histórico completo.
- Máquina de estados natural y con matices: perdido, recuperado, en acogida, adoptado, baja.
- Interoperabilidad obvia: el registro autonómico sincroniza con la red nacional REIAC, que a su vez conecta con redes europeas e internacionales.
- Obliga a tomarse en serio la protección de datos: la consulta pública de un microchip no puede exponer los datos personales del titular.
- Dominio cercano y comprensible: se explica en veinte segundos a cualquiera.
- En modernización activa: la Xunta acaba de habilitar la tramitación electrónica del REGIAC para los veterinarios habilitados.

**Por qué este stack**

Borja tiene cinco años de experiencia profesional con Java, Spring, Oracle, JSP y el framework UDA de EJIE, trabajando para el Gobierno Vasco. Sus otros proyectos personales usan stack moderno (Next.js, Docker, TypeScript), pero ninguno demuestra el stack por el que le van a contratar en las consultoras del sector público gallego (Coremain, Altia, Balidea, Plexus, Bahía Software).

El proyecto replica la arquitectura JEE clásica (Spring sin Boot, JSP + Tiles, Oracle) **sin usar UDA**: el plugin de generación y la infraestructura de despliegue de EJIE (WebLogic interno, XLNetS) no son accesibles fuera de su red corporativa (ver decisión D1 en §3), así que se monta a mano con Spring Framework puro. Borja se incorpora en paralelo a un proyecto con Spring clásico para la aseguradora Ocaso, así que REXIA también le sirve para practicar el mismo estilo de stack fuera del trabajo.

**Origen del código** — Reimplementación original y desde cero. No contiene código, datos, lógica de negocio ni documentación de EJIE, Inetum, Bilbomática ni del REGIAC real. Todos los datos de demostración son ficticios.

**Cómo se cuenta en una entrevista**

> «Monté un registro de identificación de animales de compañía, inspirado en el REGIAC de la Xunta, con el stack clásico de las consultoras del sector público: Spring (sin Boot), Oracle y JSP con Tiles. Gestiona la trazabilidad del microchip al titular con histórico completo, controla las series asignadas a cada veterinario habilitado, y sincroniza con un registro nacional simulado. La consulta pública está diseñada para no exponer datos personales. Está en GitHub y se levanta con Docker Compose.»

Preguntas que van a venir — tenerlas preparadas:
- ¿Por qué una máquina de estados y no un campo de estado?
- ¿Cómo garantizas que no se registran dos veces el mismo microchip?
- ¿Cómo evitas que un veterinario use series que no le corresponden?
- ¿Qué pasa si el registro nacional no responde al sincronizar?
- ¿Cómo diseñaste la consulta pública para cumplir el RGPD?
- ¿Por qué JdbcTemplate teniendo JPA?
- ¿Cómo calculas los plazos hábiles?

## 2. Stack y arquitectura

| Capa | Tecnología |
|---|---|
| Lenguaje | Java 11 (LTS) — namespace `javax.*`, compatible con una futura migración a Spring Boot 2.7 sin saltar aún a Jakarta EE |
| Framework | Spring Framework 5.3.x, clásico (sin Boot) — mismo estilo que el proyecto de Ocaso al que se incorpora Borja |
| Servidor de aplicaciones | Apache Tomcat 9 (local, registrado en Eclipse) |
| Flujo de trámites | Spring Web Flow |
| Persistencia | Spring Data JPA + Hibernate (sin anotaciones, XML de configuración) · JdbcTemplate para consultas complejas |
| Base de datos | Oracle XE 21c en Docker |
| Procedimientos | PL/SQL |
| Vista | JSP + JSTL + Tiles |
| Estilos | Bootstrap 5 + jQuery |
| Seguridad | Spring Security |
| Documentos | JasperReports o iText (PDF) |
| Tests | JUnit 5 + Mockito |
| Build | Maven (multi-módulo) |
| Contenedores | Docker Compose (app + Oracle + registro nacional simulado) |
| CI | GitHub Actions |
| Calidad | SonarCloud |

**Migración futura a Spring Boot** — Si más adelante conviene modernizar (por ejemplo si el proyecto de Ocaso migra, o por aprendizaje), el camino es: Spring 5.3 clásico → Spring Boot 2.7 (mismo namespace `javax`, mismo Spring 5) → Spring Boot 3 + Jakarta EE (salto mayor, con find & replace de imports `javax`→`jakarta`). Se documenta como decisión explícita cuando se tome, no antes.

**Arquitectura en capas**

```
┌──────────────────────────────────────────────────┐
│  PRESENTACIÓN                                     │
│  JSP + JSTL + Tiles · Bootstrap · jQuery          │
│  Controladores Spring MVC + API REST pública      │
├──────────────────────────────────────────────────┤
│  SERVICIO                                         │
│  Lógica de negocio · @Transactional               │
│  Máquina de estados · Validación de reglas        │
├──────────────────────────────────────────────────┤
│  ACCESO A DATOS                                   │
│  Repositorios JPA · JdbcTemplate para informes    │
├──────────────────────────────────────────────────┤
│  INTEGRACIÓN                                      │
│  Cliente REST hacia el registro nacional          │
└──────────────────────────────────────────────────┘
         │                          │
         ▼                          ▼
   ┌───────────┐         ┌─────────────────────────┐
   │ Oracle XE │         │ Registro nacional (mock) │
   │  Docker   │         │  microservicio           │
   └───────────┘         └─────────────────────────┘
```

**Módulos Maven** — `rexia` (pom padre) contiene dos módulos:
- `rexia-core` (jar): modelo, persistencia (JPA vía `orm.xml`, ver D7) y servicios — testable sin contenedor de servlets.
- `rexia-web` (war): controladores, configuración web (MVC/Tiles/Security/Web Flow) y las JSP.

**Estructura de paquetes** (`com.bohdeveloper.rexia`, repartida entre los dos módulos)

```
com.bohdeveloper.rexia
├── config/          Spring, seguridad, i18n, Tiles
├── controller/
│   ├── web/         Controladores MVC
│   └── api/         API REST (consulta pública de microchip)
├── service/
│   ├── impl/
│   └── estado/      Máquina de estados del animal
├── repository/      JPA + JdbcTemplate
├── model/
│   ├── entity/      Entidades JPA
│   ├── dto/         Objetos de transferencia
│   └── enums/       Estados, especies, tipos de incidencia
├── integration/
│   └── nacional/    Cliente REST + DTOs de sincronización
├── util/            Plazos hábiles, validación de chip, PDF
└── exception/       Manejo centralizado
```

**Modelo de dominio** (entidades principales)

- **TITULAR** (persona física o jurídica: DNI/NIF, domicilio, concello, contacto) tiene muchos **ANIMAL** (microchip de 15 dígitos ISO 11784/11785, especie, raza, sexo, capa, fecha de nacimiento, esterilizado, indicador PPP).
- **VETERINARIO HABILITADO** (nº colegiado, colexio, habilitación vigente) tiene muchas **SERIE DE MICROCHIPS** (rango numérico asignado) y muchas **IDENTIFICACIÓN** (acto de implantación: fecha, lugar, veterinario actuante).
- **CAMBIO DE TITULARIDAD** (titular anterior → nuevo titular, fecha, motivo).
- **INCIDENCIA** (pérdida, robo con denuncia, hallazgo, defunción).
- **LICENCIA PPP** (seguro de RC, certificado de aptitud, vigencia y renovación).
- **NÚCLEO ZOOLÓXICO** (protectora, criador, residencia) gestiona **ACOLLIDA → ADOPCIÓN**.
- **CAMPAÑA SANITARIA** (vacunación antirrábica) tiene **VACUNACIÓN**.
- **INSPECCIÓN → ACTA → EXPEDIENTE SANCIONADOR**.

**Máquina de estados del animal**

```
PENDENTE_IDENTIFICACIÓN
   ↓ un veterinario habilitado implanta el chip
ACTIVO ◄──────────────────────────────┐
   │                                   │
   ├─ comunicar pérdida ──► PERDIDO ───┤ recuperado
   │                                   │
   ├─ denuncia de robo ───► ROUBADO ───┤ recuperado
   │                                   │
   ├─ ingreso en protectora ──► EN_ACOLLIDA
   │                                │
   │                                ↓ adopción
   │                            ADOPTADO ─────┘ (nuevo titular, vuelve a ACTIVO)
   │
   ├─ traslado fuera de la comunidad ──► TRASLADADO
   │
   └─ certificado de defunción ────────► BAIXA
```

Transiciones especiales: retirada cautelar por resolución de inspección, cesión temporal, y reactivación de un animal dado de baja por error (exige justificación).

## 3. Decisiones de producto y reglas de negocio (invariantes)

**Reglas de negocio del dominio**
1. Validación del microchip: 15 dígitos numéricos, los tres primeros son el código de país (724 para España). Debe ser único en todo el sistema.
2. Series controladas: los rangos de microchips se asignan a veterinarios habilitados sin solapamiento. Un veterinario solo puede registrar identificaciones dentro de sus rangos.
3. Solo veterinarios habilitados con habilitación vigente pueden dar de alta una identificación.
4. Un animal, un titular activo. El histórico de titulares se conserva íntegro.
5. Plazo de comunicación: los cambios de titularidad, las bajas y las pérdidas tienen plazo en días hábiles desde el hecho.
6. Animales potencialmente peligrosos: exigen licencia vigente con seguro de responsabilidad civil y certificado de aptitud. La licencia caduca y hay que renovarla.
7. Obligación de identificación según especie y edad, con margen desde la adquisición.
8. **Protección de datos** (requisito de diseño real, no un adorno): la consulta pública por microchip devuelve si el animal está registrado y su estado, nunca los datos personales del titular. Si el animal consta como perdido, se ofrece un canal de contacto mediado, no el teléfono del dueño.
9. Multilenguaje castellano y gallego, con ficheros de recursos.

**Roles**

| Rol | Puede |
|---|---|
| VETERINARIO | Identificar animales · registrar vacunaciones · certificar defunciones · gestionar sus series de chips |
| TITULAR | Consultar sus animales · comunicar pérdida o cambio de domicilio · solicitar cambio de titularidad |
| NUCLEO_ZOOLOXICO | Gestionar animales en acogida · tramitar adopciones |
| INSPECTOR | Levantar actas · iniciar expedientes · ordenar retiradas cautelares |
| ADMIN | Asignar series · gestionar habilitaciones, campañas y maestros |

**Decisiones de arquitectura tomadas**

- **D1 (2026-08-26) — UDA descartado.** El framework EJIE (UDA 2.4.3) requiere para su instalación "oficial" Oracle WebLogic Server interno y autenticación XLNetS, ambos infraestructura corporativa del Gobierno Vasco no accesible externamente; el plugin de Eclipse (`udaPlugin`) tampoco tiene releases públicas. Se descarta UDA por completo y se replica la arquitectura JEE a mano con Spring Framework puro. Nunca se afirma que el proyecto use UDA.
- **D2 (2026-08-26) — Vista JSP + Tiles + Web Flow mantenida.** Pese a descartar UDA, se mantiene la vista clásica (no se simplifica a Thymeleaf) porque es el motivo original del proyecto: demostrar el stack que usan las consultoras del sector público gallego.
- **D3 (2026-08-26) — Spring clásico, no Boot.** Se elige Spring Framework 5.3.x con configuración XML (sin anotaciones) en vez de Spring Boot, porque el proyecto de Ocaso al que se incorpora Borja usa Spring clásico y el objetivo es practicar en paralelo el mismo estilo. Boot queda como posible migración futura (ver §2).
- **D4 (2026-08-26) — Java 11, namespace javax.** Se fija JDK 11 (no la 17/21 que ya tenía instaladas Borja en el sistema) porque es la versión con la que Spring 5.3 clásico y `javax.*` funcionan sin sorpresas de compatibilidad con librerías antiguas (CGLIB, Hibernate 5.x). El JDK 11 se registra en Eclipse como "Installed JRE" adicional, sin tocar el JDK del sistema (21) ni el JRE con el que corre el propio Eclipse (17).
- **D5 (2026-08-26) — Tomcat 9, WebLogic pospuesto.** Se usa Tomcat 9 (namespace `javax.*`, sin licencia) para el día a día por menor fricción y arranque más rápido. WebLogic 14c (gratis solo para desarrollo bajo licencia OTN) queda descartado por ahora — sin garantía de que Ocaso lo use y con más coste de instalación/arranque — pero se retoma si aporta valor concreto más adelante.
- **D6 (2026-08-26) — Git local, luego remoto el mismo día.** Se empezó solo local; más tarde esa misma sesión Borja pidió conectar el remoto ya creado en GitHub (`github.com/bohdeveloper/rexias`) y se hizo el primer push (commit `7cc6c09`, rama `main`).
- **D7 (2026-08-26) — Persistencia: JPA con mapeo XML (`orm.xml`), no `@Entity`.** Para cumplir literalmente "Spring Data JPA + Hibernate sin anotaciones, XML de configuración": `rexia-core/src/main/resources/META-INF/persistence.xml` declara la unidad de persistencia con `exclude-unlisted-classes=true` y `mapping-file=META-INF/orm.xml`; las entidades futuras (Fase 1+) se mapean en `orm.xml`, no con `@Entity`/`@Column`. El `EntityManagerFactory` se configura en `spring/applicationContext.xml` vía `LocalContainerEntityManagerFactoryBean` + HikariCP como pool de conexiones. Los repositorios sí pueden ser interfaces `JpaRepository` normales (Spring Data los proxya vía `<jpa:repositories>` en XML; eso no es una anotación de mapeo, es un mecanismo de proxy).
- **D8 (2026-08-26) — Seguridad Fase 0 = placeholder sin filtrar nada.** `security-context.xml` usa `<http pattern="/**" security="none"/>` (Spring Security ni siquiera intercepta las peticiones) en vez de un `<http>` con `permitAll()`, porque este último exige un `AuthenticationEntryPoint`/mecanismo de login configurado y falla al arrancar sin uno. La Fase 2 sustituye esto por autenticación real con los cinco roles.

## 4. Sistema de diseño y convenciones UI

No hay sistema de diseño propio más allá de Bootstrap 5 (con jQuery para interacción) montado sobre JSP + JSTL + Tiles como motor de layout. Se define con más detalle cuando arranque la Fase 1 (layout Tiles + navegación). Multilenguaje es/gl mediante ficheros de recursos Spring (`messages_es.properties`, `messages_gl.properties`), sin librerías de i18n adicionales.

## 5. Entorno de desarrollo y producción

**Cómo se corre en local**
- JDK 11 (Eclipse Temurin), instalado en `C:\eclipse\JDK11_temurin`, registrado en Eclipse como Installed JRE adicional (el JDK 21 del sistema y el JRE 17 con el que corre Eclipse no se tocan).
- Eclipse IDE for Enterprise Java and Web Developers.
- Apache Tomcat 9.0.121 registrado como servidor en Eclipse, con su JRE puesto explícitamente al JDK 11 (no al "Workspace default").
- Docker Desktop, para levantar Oracle XE 21c (y el microservicio simulado del registro nacional) vía `docker-compose.yml`.
- Maven (instalado en el sistema, `apache-maven-3.9.14`).
- Git local — sin remoto todavía.

Verificado el 2026-08-26: `java -version` bajo Eclipse → 11.0.32.1; Tomcat 9.0.121 arranca sobre ese JDK en el puerto 8080 sin errores; Docker Desktop 29.6.2 responde a `docker info`; `docker compose up` levanta Oracle XE 21c (imagen `gvenzl/oracle-xe:21-slim-faststart`) con el esquema `RXA_ESPECIE`/`RXA_RAZA`/`RXA_CONCELLO` y datos maestros cargados via `docker/init/01_maestros.sql`; el WAR completo (`mvn package`) se desplegó en un Tomcat 9 aislado y `GET /` devolvió 200 con el layout de Tiles compuesto correctamente, con Spring conectando de verdad contra ese Oracle.

**Nota:** la máquina de Borja ya tenía un Oracle XE 21c nativo instalado (Windows) escuchando en el puerto 1521. El Oracle de Docker se mapea al puerto **1522** del host (`docker-compose.yml`) para no chocar con él; `db.properties` apunta a `jdbc:oracle:thin:@//localhost:1522/XEPDB1`.

**Producción** — No hay despliegue productivo real previsto; es un proyecto de portfolio. `docker-compose.yml` hace de "producción simulada" local para la demo (app + Oracle + registro nacional mock).

**Deuda técnica aceptada deliberadamente**
- WebLogic no se monta por ahora (ver D5) — Tomcat cubre las necesidades de desarrollo.
- No hay migración a Jakarta EE / Spring Boot 3 planificada a corto plazo (ver D3, D4).

## 6. Metodología de desarrollo

### 6.1 Modo mentor (regla innegociable de este proyecto)

REXIA tiene un objetivo doble: construir una aplicación y que Borja aprenda construyéndola. El reparto de trabajo:

| Claude hace | Borja hace |
|---|---|
| Explicar el siguiente paso | Escribir el código |
| Enseñar el concepto que hay detrás | Ejecutar y depurar |
| Dar pistas y señalar la dirección | Tomar las decisiones de diseño |
| Revisar el código escrito | Preguntar cuando se atasca |
| Montar el andamiaje inicial (Fase 0) | Todo lo demás |

**Reglas de comportamiento**
- No se escribe código de implementación salvo que Borja lo pida explícitamente («hazlo tú», «escríbeme esta clase», «genérame el DAO»). Sin esa señal: se explica y se espera.
- Cada paso sigue el formato: **Qué toca ahora** (objetivo concreto) → **Por qué** (qué problema resuelve, cómo encaja en la arquitectura) → **El concepto** (explicación técnica) → **Pistas** (clases/anotaciones/métodos a investigar, sin dar la solución) → **Cómo verificar** (qué debe ocurrir cuando esté bien).
- Se para después de explicar un paso. No se encadenan varios pasos seguidos — Borja implementa y vuelve.
- Cuando Borja trae código, se revisa de verdad: qué está bien y qué no, con el porqué. No se reescribe, se sugiere.
- Si Borja pide implementar algo directamente, se hace, pero acompañado de comentarios técnicos en español en las partes complejas, y se explican después las decisiones tomadas.
- Si Borja se atasca dos veces en lo mismo, se cambia de estrategia: un ejemplo análogo de otro contexto, no la solución directa.
- No se adelanta trabajo («ya que estaba, te he creado también...»).
- **Excepción: la Fase 0** (estructura de proyecto, Maven, Docker Compose, configuración base) la monta Claude completa. A partir de la Fase 1, modo mentor estricto.

### 6.2 Antes de desarrollar
1. Leer spec.md y plan.md; confirmar que la tarea está en el plan (si no, añadirla primero).
2. Contrastar con los invariantes de §3; avisar si choca con alguna decisión ya tomada.
3. Explorar el código existente antes de asumir que hay que crear algo nuevo.
4. Buscar entidad/helper/servicio existente que ya cubra el concepto antes de crear uno nuevo.
5. Para UI nueva, considerar el agente `ux-ui-designer` si el alcance visual lo justifica.
6. Desglosar tareas grandes en fases dentro de plan.md.

### 6.3 Durante el desarrollo
- Seguir las convenciones de §7 (código, base de datos, Git).
- Validar reglas de negocio de §3 en la capa de servicio, no en el controlador.
- Reutilizar servicios/repositorios existentes antes de duplicar lógica.

### 6.4 Después de desarrollar
1. Compilar/testear lo tocado (`mvn -pl <módulo> -am test`).
2. `/code-review` sobre el diff antes de cerrar cualquier feature.
3. `/security-review` si toca autenticación, datos personales o endpoints públicos (especialmente la consulta pública de microchip, ver regla 8 de §3).
4. Verificar end-to-end desplegando en el Tomcat local y probando en el navegador.
5. Registrar: marcar plan.md con fecha; decisiones nuevas → spec.md §3; cambios de alcance/stack → README.md.
6. Commit solo cuando Borja lo pida, en el estilo de convenciones de Git de §7.
7. `/simplify` opcional al cerrar una fase.

### 6.5 Skills del proyecto y cuándo usarlas

| Skill | Cuándo |
|---|---|
| `spec-driven` | Al empezar una sesión si spec.md/plan.md han quedado desalineados con el código o el histórico de decisiones |
| `code-review` | Antes de cerrar cualquier feature, sobre el diff |
| `security-review` | Al tocar autenticación, roles, o la API pública de consulta de microchip |
| `simplify` | Opcional, al cerrar una fase |
| `ux-ui-designer` (agente) | Al diseñar layout Tiles/pantallas nuevas con alcance visual relevante |
| `run` | Para levantar y comprobar la app real en el navegador |

## 7. Convenciones

**Código**
- Nombres de clases y métodos en inglés; comentarios y mensajes de usuario en castellano.
- Comentarios técnicos en español solo en la lógica compleja, no en getters.
- Un servicio por agregado de dominio.
- DTOs para entrada y salida de controladores; nunca exponer entidades JPA directamente.
- Excepciones de negocio propias, manejadas de forma centralizada.

**Base de datos**
- Tablas en mayúsculas con prefijo del módulo: `RXA_TITULAR`, `RXA_ANIMAL`, `RXA_IDENTIFICACION`.
- Claves primarias por secuencia Oracle.
- Toda tabla lleva `FECHA_ALTA`, `USUARIO_ALTA`, `FECHA_MOD`, `USUARIO_MOD`.
- Los cambios de esquema van en scripts versionados, no en `ddl-auto`.
- Datos personales: identificar desde el diseño qué columnas lo son y quién puede leerlas.

**Git**
- Ramas por fase: `fase-1-maestros`, `fase-2-seguridad`...
- Commits en español, descriptivos, en imperativo.
- Pull request por fase aunque sea proyecto individual — para tener el historial.
- Al cerrar cada fase: comentarios técnicos en las partes complejas, commit + push, actualizar README con lo nuevo, marcar la fase como cerrada en plan.md.

**Ritmo y límites** — Máximo 10 horas semanales; la búsqueda de empleo es prioridad uno, este proyecto la apoya, no la sustituye. Si en la Fase 4 el calendario aprieta: cerrar el alcance ahí y saltar directamente a la 8 (Cierre). Un proyecto terminado con buen README vale más que uno ambicioso al sesenta por ciento.

## 8. Documentos del proyecto

| Documento | Rol |
|---|---|
| `spec.md` | Este documento. Memoria: qué es, cómo está construido, decisiones, metodología |
| `plan.md` | Trabajo pendiente por fases, con histórico resumido de lo completado |
| `README.md` | Escaparate público del repo |
| `CLAUDE.md` | Reglas de trabajo que carga Claude Code en cada sesión (apunta aquí para el conocimiento del proyecto) |
