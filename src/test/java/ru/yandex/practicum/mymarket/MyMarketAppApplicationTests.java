package ru.yandex.practicum.mymarket;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import ru.yandex.practicum.mymarket.service.ProductServiceImpl;

import java.sql.Connection;
import java.sql.DriverManager;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@SpringBootTest
@ActiveProfiles("test")
public class MyMarketAppApplicationTests {

    @MockBean
    private ProductServiceImpl productService;

    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16")
            .withDatabaseName("testdb")
            .withUsername("testuser")
            .withPassword("testpass");

    @BeforeAll
    static void startPostgres() {
        postgres.start();
    }

    @AfterAll
    static void stopPostgres() {
        postgres.stop();
    }

    @DynamicPropertySource
    static void overrideDataSourceProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Test
    void testConnection() {
        assertDoesNotThrow(() -> {
            String jdbcUrl = postgres.getJdbcUrl();
            String username = postgres.getUsername();
            String password = postgres.getPassword();

            try (Connection connection = DriverManager.getConnection(jdbcUrl, username, password)) {
                assertNotNull(connection);
            }
        });
    }
}
