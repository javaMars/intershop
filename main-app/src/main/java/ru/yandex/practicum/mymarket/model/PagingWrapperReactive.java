package ru.yandex.practicum.mymarket.model;

public class PagingWrapperReactive {
    private int pageSize;
    private int pageNumber;
    private long totalElements;
    private int totalPages;

    public PagingWrapperReactive() {}

    public PagingWrapperReactive(int pageSize, int pageNumber, long totalElements) {
        this.pageSize = pageSize;
        this.pageNumber = pageNumber;
        this.totalElements = totalElements;
        this.totalPages = (int) Math.ceil((double) totalElements / pageSize);
    }

    public int getPageSize() {
        return pageSize;
    }

    public int getPageCurrent() {
        return pageNumber + 1;
    }

    public int getPageNumber() {
        return pageNumber;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public long getTotalElements() {
        return totalElements;
    }

    public boolean hasPrevious() {
        return pageNumber > 0;
    }

    public boolean hasNext() {
        return pageNumber + 1 < totalPages;
    }
}