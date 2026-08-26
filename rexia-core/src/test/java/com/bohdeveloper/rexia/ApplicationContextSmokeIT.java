package com.bohdeveloper.rexia;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.Connection;
import javax.sql.DataSource;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Prueba de humo de la Fase 0: confirma que el contexto raiz de Spring
 * (spring/applicationContext.xml) arranca y conecta de verdad con el Oracle
 * de docker-compose. No se ejecuta con "mvn test" (sufijo *IT, fuera del
 * patron por defecto de Surefire): requiere "docker compose up" antes.
 * Ejecutar con: mvn test -pl rexia-core -Dtest=ApplicationContextSmokeIT
 */
class ApplicationContextSmokeIT {

    private static ClassPathXmlApplicationContext context;

    @BeforeAll
    static void startContext() {
        context = new ClassPathXmlApplicationContext("spring/applicationContext.xml");
    }

    @AfterAll
    static void stopContext() {
        if (context != null) {
            context.close();
        }
    }

    @Test
    void elContextoDeSpringArrancaYConectaConOracle() throws Exception {
        DataSource dataSource = context.getBean(DataSource.class);
        assertNotNull(dataSource);

        try (Connection connection = dataSource.getConnection()) {
            assertTrue(connection.isValid(5));
        }
    }

    @Test
    void elEntityManagerFactorySeInicializa() {
        assertNotNull(context.getBean("entityManagerFactory"));
    }
}
