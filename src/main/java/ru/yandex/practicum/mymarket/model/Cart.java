package ru.yandex.practicum.mymarket.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table(name = "carts")
public class Cart {
    @Id
    private Long id;

    public long getId() {
        return id;
    }
}
