package ru.yandex.practicum.mymarket.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table(name = "carts")
public class Cart {
    @Id
    @Column("id")
    private Long id;

    public Cart() { }

    public Long getId() {
        return id;
    }

    public void setId(Long l) {
    }
}
