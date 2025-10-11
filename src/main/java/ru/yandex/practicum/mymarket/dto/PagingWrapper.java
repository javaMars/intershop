package ru.yandex.practicum.mymarket.dto;

import org.springframework.data.domain.Page;

public class PagingWrapper<T> {
    private final Page<T> page;

    public PagingWrapper(Page<T> page) {
        this.page = page;
    }

    public int getPageSize() {
        return page.getSize();
    }

    public int getPageCurrent() {
        return page.getNumber() + 1; // пользователю с 1
    }

    public int getPageNumber() {
        return page.getNumber();
    }

    public boolean isHasNext() {
        return page.hasNext();
    }

    public boolean isHasPrevious() {
        return page.hasPrevious();
    }

    public int getTotalPages() {
        return page.getTotalPages();
    }

    public long getTotalElements() {
        return page.getTotalElements();
    }
}
