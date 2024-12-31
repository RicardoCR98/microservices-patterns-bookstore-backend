package com.microservice.books.repositories;

import com.microservice.books.model.Category;
import com.microservice.books.model.Genre;
import com.microservice.books.model.Product;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface ProductRepository extends MongoRepository<Product, String> {
    List<Product> findByCategory(Category category);
    List<Product> findByAuthor(String author);
    List<Product> findBySalePriceBetween(BigDecimal minPrice, BigDecimal maxPrice);
    List<Product> findByGenre(Genre genre);
    List<Product> findByIsAvailable(Boolean isAvailable);
    List<Product> findByUserId(Long userId);
}
