package com.microservice.books.service;

import com.microservice.books.dto.ProductsFilterDTO;
import com.microservice.books.model.Product;
import com.microservice.books.repositories.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProductService {
    private static final Logger logger = LoggerFactory.getLogger(ProductService.class);

    private final ProductRepository productRepository;

    // Listar todos los productos (público)
    public List<Product> listAllProducts() {
        logger.info("Fetching all products");
        return productRepository.findAll();
    }

    // Filtrar productos (público)
    public List<Product> filterProducts(ProductsFilterDTO filter) {
        logger.info("Filtering products with filter: {}", filter);

        // Extraer valores del DTO
        Optional<String> search = Optional.ofNullable(filter.getSearch());
        Optional<String> sort = Optional.ofNullable(filter.getSort());
        Optional<List<String>> genders = Optional.ofNullable(filter.getGender());
        Optional<List<String>> categories = Optional.ofNullable(filter.getCategories());
        Optional<String> price = Optional.ofNullable(filter.getPrice());
        Optional<Integer> rating = Optional.ofNullable(filter.getRating());

        // Implementa la lógica del filtrado
        return productRepository.findAll().stream()
                .filter(product -> search.map(s -> product.getTitle().toLowerCase().contains(s.toLowerCase())).orElse(true))
                .filter(product -> genders.map(g -> g.contains(product.getGenre().toString())).orElse(true))
                .filter(product -> categories.map(c -> c.contains(product.getCategory().toString())).orElse(true))
                .filter(product -> price.map(p -> {
                    String[] range = p.split("-");
                    BigDecimal min = new BigDecimal(range[0]);
                    BigDecimal max = new BigDecimal(range[1]);
                    return product.getSalePrice().compareTo(min) >= 0 && product.getSalePrice().compareTo(max) <= 0;
                }).orElse(true))
                .filter(product -> rating.map(r -> product.getRating() >= r).orElse(true))
                .sorted((p1, p2) -> sort.map(s -> {
                    if ("bajo".equalsIgnoreCase(s)) {
                        return p1.getSalePrice().compareTo(p2.getSalePrice());
                    } else if ("alto".equalsIgnoreCase(s)) {
                        return p2.getSalePrice().compareTo(p1.getSalePrice());
                    } else if ("rating".equalsIgnoreCase(s)) {
                        return Double.compare(p2.getRating(), p1.getRating());
                    } else if ("nuevo".equalsIgnoreCase(s)) {
                        return p2.getCreatedAt().compareTo(p1.getCreatedAt());
                    } else if ("descuento".equalsIgnoreCase(s)) {
                        return p2.getOffer().compareTo(p1.getOffer());
                    }
                    return 0;
                }).orElse(0))
                .toList();
    }

    // Obtener detalles de un producto por ID (público)
    public Optional<Product> getProductDetails(String id) {
        logger.debug("Fetching product details for ID: {}", id);
        return productRepository.findById(id);
    }

    // Agregar un nuevo producto (requiere autenticación)
    public Product addProduct(Product product) {
        logger.info("Adding a new product for user ID: {}", product.getUserId());

        if (product.getId() == null || product.getId().isEmpty()) {
            product.setId(UUID.randomUUID().toString());
        }
        if (product.getSalePrice() != null && product.getOffer() != null) {
            product.setOfferPrice(product.getSalePrice(), product.getOffer());
        }
        if (product.getStockQuantity() != null) {
            product.setIsAvailable(product.getStockQuantity());
        }
        Product savedProduct = productRepository.save(product);
        logger.info("Product added successfully with ID: {}", savedProduct.getId());

        return savedProduct;
    }

    // Actualizar un producto existente (requiere autenticación)
    public Optional<Product> updateProduct(String id, Product updatedProduct, String authenticatedUserId) {
        logger.info("Updating product with ID: {} by user ID: {}", id, authenticatedUserId);

        return productRepository.findById(id)
                .filter(product -> product.getUserId().toString().equals(authenticatedUserId))
                .map(existingProduct -> {
                    updatedProduct.setId(existingProduct.getId());
                    updatedProduct.setUserId(existingProduct.getUserId());
                    return productRepository.save(updatedProduct);
                });
    }

    // Eliminar un producto por ID (requiere autenticación)
    public boolean deleteProduct(String id, String authenticatedUserId) {
        logger.info("Deleting product with ID: {} by user ID: {}", id, authenticatedUserId);

        return productRepository.findById(id)
                .filter(product -> product.getUserId().toString().equals(authenticatedUserId))
                .map(product -> {
                    productRepository.deleteById(id);
                    logger.info("Product deleted successfully with ID: {}", id);
                    return true;
                })
                .orElse(false);
    }

    // Obtener productos relacionados (público)
    public List<Product> getRelatedProducts(String id) {
        logger.debug("Fetching related products for ID: {}", id);
        return productRepository.findById(id)
                .map(product -> productRepository.findByCategory(product.getCategory()).stream()
                        .filter(relatedProduct -> !relatedProduct.getId().equals(id))
                        .toList())
                .orElse(List.of());
    }

    // Obtener libros de un usuario específico (requiere autenticación)
    public List<Product> getProductsByUserId(Long userId) {
        logger.debug("Fetching products for user ID: {}", userId);
        return productRepository.findByUserId(userId);
    }
}
