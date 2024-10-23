package com.example.cliniccare.dto;

public class PaginationDTO {
    public int page;
    public int size;
    public String sortBy;
    public String order;

    public PaginationDTO() {}

    public PaginationDTO(int page, int size, String sortBy, String order) {
        this.page = page;
        this.size = size;
        this.sortBy = sortBy;
        this.order = order;
    }
}
