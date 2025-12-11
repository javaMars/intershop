package ru.yandex.practicum.mymarket;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import ru.yandex.practicum.mymarket.model.ItemCache;

import static org.mockito.Mockito.mock;

@TestConfiguration
@Profile("test")
public class TestRedisConfig {

    @Bean
    @Primary
    public ReactiveRedisConnectionFactory reactiveRedisConnectionFactory() {
        return mock(ReactiveRedisConnectionFactory.class);
    }

    @Bean("itemRedisTemplate")
    @Primary
    public ReactiveRedisTemplate<String, ItemCache> reactiveRedisTemplate() {
        StringRedisSerializer keySerializer = new StringRedisSerializer();
        Jackson2JsonRedisSerializer<ItemCache> valueSerializer = new Jackson2JsonRedisSerializer<>(ItemCache.class);

        RedisSerializationContext<String, ItemCache> serializationContext =
                RedisSerializationContext.<String, ItemCache>newSerializationContext(keySerializer)
                        .value(valueSerializer)
                        .build();

        return new ReactiveRedisTemplate<>(
                reactiveRedisConnectionFactory(),
                serializationContext
        );
    }

}