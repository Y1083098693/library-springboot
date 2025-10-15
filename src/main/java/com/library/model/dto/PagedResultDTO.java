package com.library.model.dto;

import java.util.List;

public class PagedResultDTO<T> {
    private List<T> items;
    private Long total;
    private int page;
    private int limit;
    private int pages;

    public PagedResultDTO(List<T> items, Long total, int page, int limit) {
        this.items = items;
        this.total = total;
        this.page = page;
        this.limit = limit;
        this.pages = total > 0 ? (int) Math.ceil((double) total / limit) : 0;
    }

    // getter和setter方法
    public List<T> getItems() {
        return items;
    }

    public void setItems(List<T> items) {
        this.items = items;
    }

    public Long getTotal() {
        return total;
    }

    public void setTotal(Long total) {
        this.total = total;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    public int getPages() {
        return pages;
    }

    public void setPages(int pages) {
        this.pages = pages;
    }
}