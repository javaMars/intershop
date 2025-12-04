package ru.yandex.practicum.mymarket;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.mymarket.controller.OrderController;
import ru.yandex.practicum.mymarket.dto.OrderDto;
import ru.yandex.practicum.mymarket.service.OrderService;

import java.util.List;

import static org.mockito.Mockito.*;

@WebMvcTest(OrderController.class)
@AutoConfigureMockMvc  // Включаем MockMvc для тестирования MVC
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean  // Мокируем сервис
    private OrderService orderService;

    @Test
    void testViewAllOrdersPage() throws Exception {
        // Подготавливаем тестовые данные
        List<OrderDto> orders = List.of(
                new OrderDto(1L, List.of(), 100L),
                new OrderDto(2L, List.of(), 200L)
        );

        // Мокируем сервис: возвращаем Flux → собираем в список
        when(orderService.findAllOrders())
                .thenReturn(Flux.fromIterable(orders));

        // Выполняем GET-запрос к /orders
        mockMvc.perform(MockMvcRequestBuilders.get("/orders"))
                // Проверяем статус
                .andExpect(MockMvcResultMatchers.status().isOk())
                // Проверяем, что рендерится шаблон "orders" (например, orders.html)
                .andExpect(MockMvcResultMatchers.view().name("orders"))
                // Проверяем, что в модель добавлен атрибут "items"
                .andExpect(MockMvcResultMatchers.model().attributeExists("items"))
                // Проверяем содержимое атрибута
                .andExpect(MockMvcResultMatchers.model().attribute("items", orders));

        // Проверяем, что сервис был вызван ровно 1 раз
        verify(orderService, times(1)).findAllOrders();
    }

    @Test
    void testViewOrderPage_ExistingOrder() throws Exception {
        // Тестовый DTO
        OrderDto orderDto = new OrderDto(1L, List.of(), 100L);

        // Мокируем сервис: при запросе ID=1 возвращаем Mono
        when(orderService.findOrder(1L))
                .thenReturn(Mono.just(orderDto));

        // Выполняем запрос к /orders/1
        mockMvc.perform(MockMvcRequestBuilders.get("/orders/1"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.view().name("order"))
                .andExpect(MockMvcResultMatchers.model().attributeExists("order"))
                .andExpect(MockMvcResultMatchers.model().attribute("order", orderDto));

        verify(orderService, times(1)).findOrder(1L);
    }

    @Test
    void testViewOrderPage_NotFound() throws Exception {
        // Мокируем: при запросе несуществующего ID возвращаем пустой Mono
        when(orderService.findOrder(500L))
                .thenReturn(Mono.empty());

        // Выполняем запрос — ожидаем 404 (можно вернуть шаблон 404 или статус)
        mockMvc.perform(MockMvcRequestBuilders.get("/orders/500"))
                .andExpect(MockMvcResultMatchers.status().isNotFound());

        verify(orderService, times(1)).findOrder(500L);
    }

    @Test
    void testViewOrderPage_ServiceError() throws Exception {
        // Мокируем ошибку сервиса
        when(orderService.findOrder(999L))
                .thenReturn(Mono.error(new RuntimeException("Service error")));


        // Ожидаем 500 Internal Server Error
        mockMvc.perform(MockMvcRequestBuilders.get("/orders/999"))
                .andExpect(MockMvcResultMatchers.status().isInternalServerError());


        verify(orderService, times(1)).findOrder(999L);
    }
}
