package com.eilco.messagerie.integration;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

/**
 * Classe de base pour tous les tests d'integration
 * Configure automatiquement H2 Database et le contexte Spring
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(locations = "classpath:application-test.properties")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@Transactional
public abstract class BaseIntegrationTest {

    /**
     * Methode executee avant chaque test
     * Peut etre surchargee dans les classes filles
     */
    @BeforeEach
    void baseSetUp() {
        // Configuration commune pour tous les tests
        System.out.println("=== Demarrage du test avec H2 Database ===");
    }
}
