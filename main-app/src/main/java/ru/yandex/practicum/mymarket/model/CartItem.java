package ru.yandex.practicum.mymarket.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table(name = "cart_items")
public class CartItem {
    @Id
    private Long id;

    @Column("cart_id")
    private Long cartId;

    @Column("item_id")
    private Long itemId;

    @Column("count")
    private Integer count = 1;

    public CartItem() { }

    public CartItem(Long cartId, Long itemId, int count) {
        this.cartId = cartId;
        this.itemId = itemId;
        this.count = count;
    }

    public Long getId() { return id; }

    public Long getCartId() { return cartId; }
    public void setCartId(Long cartId) { this.cartId = cartId; }

    public Long getItemId() { return itemId; }
    public void setItemId(Long itemId) { this.itemId = itemId; }

    public Integer getCount() { return count; }
    public void setCount(Integer count) { this.count = count; }
}