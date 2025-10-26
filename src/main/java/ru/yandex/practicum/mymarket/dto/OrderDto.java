package ru.yandex.practicum.mymarket.dto;

import java.math.BigDecimal;
import java.util.List;

public class OrderDto {
    private long id;
    private List<ItemDto> items;
    private long totalSum;
    public OrderDto(){

    }
    public OrderDto(long id, List<ItemDto> items, long totalSum) {
        this.id = id;
        this.items = items;
        this.totalSum = totalSum;
    }

    public long getId() { return id; }
    public List<ItemDto> getItems() { return items; }
    public long getTotalSum() { return totalSum; }

    public void setId(Long id) {
        this.id = id;
    }

    public void setTotalSum(long totalSum) {
        this.totalSum = totalSum;
    }

    public void setItems(List<ItemDto> items) {
        this.items = items;
    }
}
