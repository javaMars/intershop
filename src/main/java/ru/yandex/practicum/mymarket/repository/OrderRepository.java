package ru.yandex.practicum.mymarket.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.yandex.practicum.mymarket.dto.Item;
import ru.yandex.practicum.mymarket.dto.Order;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

}
