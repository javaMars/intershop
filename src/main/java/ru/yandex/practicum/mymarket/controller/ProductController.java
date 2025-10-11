package ru.yandex.practicum.mymarket.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import ru.yandex.practicum.mymarket.dto.Item;
import ru.yandex.practicum.mymarket.dto.PagingWrapper;
import ru.yandex.practicum.mymarket.service.ProductServiceImpl;

import java.util.ArrayList;
import java.util.List;
import jakarta.servlet.http.HttpServletRequest;

@Controller
public class ProductController {
    private final ProductServiceImpl productService;

    public ProductController(ProductServiceImpl productService){
        this.productService = productService;
    }

    @GetMapping("/items")
    public String getItems(@RequestParam(required = false, defaultValue = "") String search,
                           @RequestParam(required = false, defaultValue = "NO") String sort,
                           @RequestParam(required = false, defaultValue = "1") int pageNumber,
                           @RequestParam(required = false, defaultValue = "5") int pageSize,
                           Model model) {

        int pageIndex = Math.max(pageNumber - 1, 0);

        Pageable pageable = switch (sort.toUpperCase()) {
            case "ALPHA" -> PageRequest.of(pageIndex, pageSize, Sort.by("title").ascending());
            case "PRICE" -> PageRequest.of(pageIndex, pageSize, Sort.by("price").ascending());
            default -> PageRequest.of(pageIndex, pageSize);
        };

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
            while (partItems.size() < 3 ){
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

    @GetMapping("/items/{id}")
    public String viewItem(@PathVariable Long id, HttpServletRequest request, Model model) throws IllegalArgumentException{
        Item item =
    }
}
