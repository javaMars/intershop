package ru.yandex.practicum.mymarket.dto;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private List<Item> items = new ArrayList<>();

    public Order(){}
    public Order(List<Item> items){
        this.items = List.copyOf(items);;
    }
    public Long getId() { return id; }
    public List<Item> getItems() { return items; }
}
