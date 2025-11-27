package ru.yandex.practicum.mymarket;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import ru.yandex.practicum.mymarket.dto.OrderDto;
import ru.yandex.practicum.mymarket.service.CartService;
import ru.yandex.practicum.mymarket.service.OrderService;


import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@SpringBootTest
@AutoConfigureMockMvc
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OrderService orderService;
    @MockBean
    private CartService cartService;


    @Test
    void testViewAllOrders() throws Exception {
        List<OrderDto> orders = Arrays.asList(
                new OrderDto(1L, null, 100L),
                new OrderDto(2L, null, 200L)
        );
        when(orderService.findAllOrders()).thenReturn(orders);

        mockMvc.perform(MockMvcRequestBuilders.get("/orders"))
                .andExpect(status().isOk())
                .andExpect(view().name("orders"))
                .andExpect(model().attributeExists("orders"))
                .andExpect(model().attribute("orders", orders));


        verify(orderService, times(1)).findAllOrders();
    }

    @Test
    void testViewOrder_ExistingOrder() throws Exception {
        OrderDto orderDto = new OrderDto(1L, null, 100L);
        when(orderService.findOrder(1L)).thenReturn(Optional.of(orderDto));

        mockMvc.perform(MockMvcRequestBuilders.get("/orders/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("order"))
                .andExpect(model().attributeExists("order"))
                .andExpect(model().attribute("order", orderDto));

        verify(orderService, times(1)).findOrder(1L);
    }

    @Test
    void testViewOrder_OrderNotFound() throws Exception {
        when(orderService.findOrder(500L)).thenReturn(Optional.empty());


        mockMvc.perform(MockMvcRequestBuilders.get("/orders/500"))
                .andExpect(status().isNotFound());

        verify(orderService, times(1)).findOrder(500L);
    }
}

