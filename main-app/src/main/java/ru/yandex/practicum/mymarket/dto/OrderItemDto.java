package ru.yandex.practicum.mymarket.dto;

public class OrderItemDto {

    private Long id;
    private Long itemId;
    private int count;
    private Long price;      // добавляем
    private String title;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getItemId() {
        return itemId;
    }

    public void setItemId(Long itemId) {
        this.itemId = itemId;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public Long getPrice() { return price; }  // ← геттер
    public void setPrice(Long price) { this.price = price;}
    public String getTitle() { return title; }  // ← геттер
    public void setTitle(String title) { this.title = title;}

}

