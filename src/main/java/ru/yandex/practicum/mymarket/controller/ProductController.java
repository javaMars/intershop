package ru.yandex.practicum.mymarket.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import ru.yandex.practicum.mymarket.dto.Item;
import ru.yandex.practicum.mymarket.dto.PagingWrapper;
import ru.yandex.practicum.mymarket.service.ProductService;

import java.util.*;

@Controller
public class ProductController {
    private final ProductService productService;

    private static final Map<String, Sort> SORT_STRATEGIES = Map.of(
            "ALPHA", Sort.by("title").ascending(),
            "PRICE", Sort.by("price").ascending()
    );

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping("/items")
    public String viewItems(@RequestParam(required = false, defaultValue = "") String search,
                           @RequestParam(required = false, defaultValue = "NO") String sort,
                           @RequestParam(required = false, defaultValue = "0") int pageNumber,
                           @RequestParam(required = false, defaultValue = "5") int pageSize,
                           Model model) {

        int pageIndex = Math.max(pageNumber, 0);

        String normalizedSort = (sort != null) ? sort.trim().toUpperCase() : null;
        Sort sorting = (normalizedSort != null) ? SORT_STRATEGIES.get(normalizedSort) : null;

        Pageable pageable = (sorting != null)
                ? PageRequest.of(pageIndex, pageSize, sorting)
                : PageRequest.of(pageIndex, pageSize);


        Page<Item> filteredItemsPage;
        if (search != null && !search.trim().isEmpty()) {
            filteredItemsPage = productService.findByTitle(search.trim(), pageable);
        } else {
            filteredItemsPage = productService.findAll(pageable);
        }

        List<Item> filteredItems = filteredItemsPage.getContent();

        // Разбиваем на списки по 3 штуки + заглушки, при необходимости
        List<List<Item>> itemsByGroup = new ArrayList<>();
        for (int i = 0; i < filteredItems.size(); i += 3) {
            List<Item> partItems = new ArrayList<>(filteredItems.subList(i, Math.min(i + 3, filteredItems.size())));
            while (partItems.size() < 3) {
                partItems.add(new Item(-1L, "", "", "", 0L, 0)); // заглушка
            }
            itemsByGroup.add(partItems);
        }

        model.addAttribute("items", itemsByGroup);
        model.addAttribute("paging", new PagingWrapper<>(filteredItemsPage));
        model.addAttribute("search", search != null ? search : "");
        model.addAttribute("sort", sort);

        return "items";
    }

    @PostMapping("/items")
    public String updateItemCount(
            @RequestParam Long id,
            @RequestParam(required = false, defaultValue = "") String search,
            @RequestParam(required = false, defaultValue = "NO") String sort,
            @RequestParam(required = false, defaultValue = "0") int pageNumber,
            @RequestParam(required = false, defaultValue = "5") int pageSize,
            @RequestParam String action) {

        productService.handleItemAction(id, action);

        return String.format("redirect:/items?search=%s&sort=%s&pageNumber=%d&pageSize=%d",
                search, sort, pageNumber, pageSize);
    }

    @GetMapping("/items/{id}")
    public String viewItem(@PathVariable Long id, Model model) throws IllegalArgumentException{
        Optional<Item> optionalItem = productService.findById(id);

        model.addAttribute("item", optionalItem.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Товар не найден")));
        return "item";
    }

    @PostMapping("/items/{id}")
    public String updateItemCount(
            @PathVariable Long id,
            @RequestParam String action,
            Model model) {

        productService.handleItemAction(id, action);

        Item item = productService.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Товар не найден"));

        model.addAttribute("item", item);
        return "item";
    }
}
