package ru.yandex.practicum.mymarket.model;

public class PagingWrapperReactive {
    private final int pageSize;
    private final int pageNumber;
    private final long totalElements;
    private final int totalPages;

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
}
