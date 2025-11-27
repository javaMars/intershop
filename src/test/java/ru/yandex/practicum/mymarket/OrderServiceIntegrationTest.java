package ru.yandex.practicum.mymarket;

import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import ru.yandex.practicum.mymarket.dto.OrderDto;
import ru.yandex.practicum.mymarket.model.Order;
import ru.yandex.practicum.mymarket.repository.OrderRepository;
import ru.yandex.practicum.mymarket.repository.ProductRepository;
import ru.yandex.practicum.mymarket.repository.CartRepository;
import ru.yandex.practicum.mymarket.service.CartServiceImpl;
import ru.yandex.practicum.mymarket.service.OrderServiceImpl;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class OrderServiceIntegrationTest {
    @Autowired
    private OrderServiceImpl orderServiceImpl;

    @Autowired
    private CartServiceImpl cartServiceImpl;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductRepository productRepository;


    @BeforeEach
    void setUp() {
        orderRepository.deleteAll();
        orderRepository.flush();
    }

    @Test
    @DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
    void testFindAllOrders_shouldReturnDtoList() {

        Order order1 = new Order();
        order1.setTotalSum(1000L);
        orderRepository.save(order1);

        Order order2 = new Order();
        order2.setTotalSum(2000L);
        orderRepository.save(order2);

        List<OrderDto> result = orderServiceImpl.findAllOrders();

        assertNotNull(result);
        assertEquals(2, result.size());

        // Проверка конвертации полей
        assertEquals(1000L, result.get(0).getTotalSum());
        assertEquals(2000L, result.get(1).getTotalSum());
    }
}