package ru.yandex.practicum.mymarket.dto;

import java.util.List;

public class OrderDto {
    private long id;
    private List<Item> items;
    private long totalSum;

    public OrderDto(long id, List<Item> items, long totalSum) {
        this.id = id;
        this.items = items;
        this.totalSum = totalSum;
    }

    public long getId() { return id; }
    public List<Item> getItems() { return items; }
    public long getTotalSum() { return totalSum; }
}
