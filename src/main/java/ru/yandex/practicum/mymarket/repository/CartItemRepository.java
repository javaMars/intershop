package ru.yandex.practicum.mymarket.repository;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.mymarket.model.*;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    Optional<CartItem> findByCartAndItem(Cart cart, Item item);

    List<CartItem> findByCart(Cart cart);

    @Transactional
    @Modifying
    @Query("delete from CartItem ci where ci.cart = :cart and ci.item = :item")
    void deleteByCartAndItem(@Param("cart") Cart cart, @Param("item") Item item);
}
