package com.bohdeveloper.rexiacloud;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Prueba de humo de la Fase 0: el contexto de Spring Boot arranca y conecta
 * de verdad contra un Postgres real levantado por Testcontainers (no un
 * Postgres embebido/en memoria) - mismo criterio que ApplicationContextSmokeIT
 * en rexia-clasico contra el Oracle de Docker. Requiere Docker en marcha.
 */
@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class RexiaCloudApplicationSmokeIT {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("rexia_cloud")
            .withUsername("rexia_cloud")
            .withPassword("rexia_cloud_dev_2026");

    @DynamicPropertySource
    static void datasourceProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Test
    void elContextoArrancaYConectaConPostgresReal() {
        assertThat(postgres.isRunning()).isTrue();
    }
}
