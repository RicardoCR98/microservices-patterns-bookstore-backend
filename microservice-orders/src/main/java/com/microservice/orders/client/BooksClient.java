package com.microservice.orders.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Map;

@FeignClient(name = "msvc-books")
public interface BooksClient {
    @GetMapping("/books/{id}")
    Map<String,Object> getBook(@PathVariable("id") String id);
}