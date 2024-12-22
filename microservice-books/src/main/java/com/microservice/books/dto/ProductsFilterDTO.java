package com.microservice.books.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductsFilterDTO {
    private String search;
    private String sort;
    private List<String> gender;
    private List<String> categories;
    private String price;
    private Integer rating;

}