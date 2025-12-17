package ru.yandex.practicum.mymarket;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.ReactiveValueOperations;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import ru.yandex.practicum.mymarket.model.Item;
import ru.yandex.practicum.mymarket.model.ItemCache;
import ru.yandex.practicum.mymarket.service.ProductServiceImpl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

import static org.mockito.Mockito.*;

@Import(TestRedisConfig.class)
@SpringBootTest(
        properties = {
                "spring.r2dbc.url=r2dbc:h2:mem:///testdb;DB_CLOSE_DELAY=-1",
                "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration"
        }
)
@AutoConfigureWebTestClient
@ExtendWith(MockitoExtension.class)
@TestPropertySource(locations = "classpath:application-test.properties")
@ActiveProfiles("test")
public class ProductServiceIntegrationTest {

    @Autowired
    private DatabaseClient databaseClient;

    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    @Qualifier("itemRedisTemplate")
    ReactiveRedisTemplate<String, ItemCache> redisTemplate;

    @MockitoSpyBean
    ProductServiceImpl productService;

    @BeforeEach
    void setUp() {
        loadSqlScript("schema-test.sql");
        loadSqlScript("data-test.sql");
    }

    private void loadSqlScript(String scriptName) {
        try (var reader = new BufferedReader(new InputStreamReader(
                getClass().getResourceAsStream("/" + scriptName)))) {
            String sql = reader.lines().collect(Collectors.joining("\n"));
            databaseClient.sql(sql)
                    .fetch()
                    .rowsUpdated()
                    .block();
        } catch (IOException e) {
            throw new RuntimeException("Ошибка при загрузке SQL: " + scriptName, e);
        }
    }

    @Test
    void testCacheSerialization() {
        Item item = new Item(1L, "Test", "Desc", "img.jpg", 100L);
        ItemCache cache = productService.toCache(item).block();
        Item restored = productService.toDb(cache).block();
    }


    @Test
    void shouldReturnItemFromRedis() {
        ItemCache testCache = new ItemCache("123", "Cached Item", "Desc", "img.jpg", "100");
        String key = "product:123";

        ReactiveValueOperations<String, ItemCache> valueOps = mock(ReactiveValueOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(valueOps);
        when(valueOps.get(key)).thenReturn(Mono.just(testCache));

        StepVerifier.create(productService.findById(123L))
                .expectNextMatches(item ->
                        item.getTitle().equals("Cached Item") &&
                                item.getPrice() == 100L)
                .verifyComplete();
    }

    @Test
    void checkIds() {
        databaseClient.sql("SELECT id FROM items WHERE id IS NULL")
                .fetch()
                .rowsUpdated()
                .as(StepVerifier::create)
                .expectNext(0L)
                .verifyComplete();
    }

    @Test
    void debugController() {
        StepVerifier.create(
                        databaseClient.sql("SELECT * FROM items LIMIT 1")
                                .map(row -> new Item(
                                        row.get("id", Long.class),
                                        row.get("title", String.class),
                                        row.get("description", String.class),
                                        row.get("img_path", String.class),
                                        row.get("price", Long.class)
                                ))
                                .one()
                )
                .expectNextMatches(item -> {
                    return item.getId() != 0L;
                })
                .verifyComplete();
    }
}