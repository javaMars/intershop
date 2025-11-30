package ru.yandex.practicum.mymarket.controller;

import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.mymarket.model.Item;
import ru.yandex.practicum.mymarket.model.PagingWrapperReactive;
import ru.yandex.practicum.mymarket.service.CartService;
import ru.yandex.practicum.mymarket.service.ProductService;

import java.util.*;

@Controller
@RequestMapping("/items")
public class ProductController {
    private final ProductService productService;
    private final CartService cartService;

    private static final Map<String, Sort> SORT_STRATEGIES = Map.of(
            "ALPHA", Sort.by("title").ascending(),
            "PRICE", Sort.by("price").ascending()
    );

    public ProductController(ProductService productService, CartService cartService) {
        this.productService = productService;
        this.cartService = cartService;
    }

    @GetMapping
    public Mono<String> viewItems(@RequestParam(required = false, defaultValue = "") String search,
                                  @RequestParam(required = false, defaultValue = "NO") String sort,
                                  @RequestParam(required = false, defaultValue = "0") int pageNumber,
                                  @RequestParam(required = false, defaultValue = "5") int pageSize,
                                  Model model) {

        int pageIndex = Math.max(pageNumber, 0);

        String normalizedSort = (sort != null) ? sort.trim().toUpperCase() : null;
        Sort sorting = (normalizedSort != null) ? SORT_STRATEGIES.get(normalizedSort) : null;

        Flux<Item> itemsFlux;
        if (search != null && !search.trim().isEmpty()) {
            itemsFlux = productService.findByTitle(search.trim());
        } else {
            itemsFlux = productService.findAll();
        }

        itemsFlux = itemsFlux.skip((long) pageIndex * pageSize)
                .take(pageSize);

        return itemsFlux.collectList()
                .flatMap(filteredItems -> {
                    List<List<Item>> itemsByGroup = new ArrayList<>();

                    for (int i = 0; i < filteredItems.size(); i += 3) {
                        List<Item> partItems = new ArrayList<>(filteredItems.subList(i, Math.min(i + 3, filteredItems.size())));
                        while (partItems.size() < 3) {
                            partItems.add(new Item(-1L, "", "", "", 0L, 0)); // заглушка
                        }
                        itemsByGroup.add(partItems);
                    }
                    return cartService.getItemCountsMap()
                            .map(itemCountMap -> {
                                model.addAttribute("items", itemsByGroup);
                                model.addAttribute("paging", new PagingWrapperReactive(filteredItems.size(), pageIndex, pageSize));
                                model.addAttribute("search", search != null ? search : "");
                                model.addAttribute("sort", sorting);
                                model.addAttribute("itemCountMap", itemCountMap);

                                return "items";
                            });
                });
    }

    @PostMapping
    public Mono<String> updateItemCount(
            @RequestParam Long id,
            @RequestParam(required = false, defaultValue = "") String search,
            @RequestParam(required = false, defaultValue = "NO") String sort,
            @RequestParam(required = false, defaultValue = "0") int pageNumber,
            @RequestParam(required = false, defaultValue = "5") int pageSize,
            @RequestParam String action) {

        return cartService.handleItemAction(id, action)
                        .then(Mono.just("redirect:/items?search=" + search +
                                "&sort=" + sort +
                                "pageNumber=" + pageNumber +
                                "pageSize=" + pageSize));
    }

    @GetMapping("/{id}")
    public Mono<String> viewItem(@PathVariable Long id, Model model){
        return productService.findById(id)
                        .switchIfEmpty(Mono.error( new ResponseStatusException(HttpStatus.NOT_FOUND, "Товар не найден")))
                .flatMap(item -> cartService.getItemCountInCart(id)
                                .defaultIfEmpty(0)
                                        .map(countInCart -> {
                                            model.addAttribute("item", item);
                                            model.addAttribute("countInCart", countInCart);

                                            return "item";
                                        }));
    }

    @PostMapping("/{id}")
    public Mono<String> updateItemCount(
            @PathVariable Long id,
            @RequestParam String action,
            Model model) {
        return cartService.handleItemAction(id, action)
                .then(productService.findById(id))
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Товар не найден")))
                .flatMap(item -> cartService.getItemCountInCart(id)
                        .defaultIfEmpty(0)
                        .map(countInCart -> {
                            model.addAttribute("item", item);
                            model.addAttribute("countInCart", countInCart);
                            return "item";
                        }));
    }
}