package com.example.cliniccare.pagination;

public class PaginationQuery {
    public int page;
    public int size;
    public String sortBy;
    public String order;

    public PaginationQuery() {}

    public PaginationQuery(int page, int size, String sortBy, String order) {
        this.page = page;
        this.size = size;
        this.sortBy = sortBy;
        this.order = order;
    }
}
