package com.microservice.books.controller;

import com.microservice.books.dto.ProductsFilterDTO;
import com.microservice.books.model.Product;
import com.microservice.books.service.ProductService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {
    private static final Logger logger = LoggerFactory.getLogger(ProductController.class);
    private final ProductService productService;

    // Endpoint: Listar todos los productos (público)
    @GetMapping
    public ResponseEntity<List<Product>> getAllProducts() {
        logger.info("Fetching all products via API");
        List<Product> products = productService.listAllProducts();
        return ResponseEntity.ok(products);
    }

    // Endpoint: Filtrar productos (público)
    @PostMapping("/filter")
    public ResponseEntity<Map<String, List<Product>>> filterProducts(@RequestBody ProductsFilterDTO filter, HttpServletRequest request) {
        String authenticatedUserId = request.getUserPrincipal().getName();
        logger.info("Filtering products via API with filter: {} by user: {}", filter, authenticatedUserId);
        List<Product> filteredProducts = productService.filterProducts(filter);
        return ResponseEntity.ok(Map.of("data", filteredProducts));
    }

    // Endpoint: Obtener detalles de un producto por ID
    @GetMapping("/{id}")
    public ResponseEntity<Product> getProductById(@PathVariable String id) {
        logger.info("Fetching product details via API for ID: {}", id);
        return productService.getProductDetails(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Endpoint: Agregar un nuevo producto (requiere autenticación)
    @PostMapping
    public ResponseEntity<Product> addProduct(@RequestBody Product product, HttpServletRequest request) {
        String authenticatedUserId = request.getUserPrincipal().getName();
        product.setUserId(Long.valueOf(authenticatedUserId));
        logger.info("Adding a new product for user: {}", authenticatedUserId);
        Product savedProduct = productService.addProduct(product);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedProduct);
    }

    // Endpoint: Agregar un nuevo producto CON portada (requiere autenticación)
    @PostMapping(value = "/with-cover", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Product> addProductWithCover(
            @RequestPart("product") Product product,
            @RequestPart("cover") MultipartFile file,
            HttpServletRequest request
    ) {
        String authenticatedUserId = request.getUserPrincipal().getName();
        product.setUserId(Long.valueOf(authenticatedUserId));
        logger.info("Adding a new product with cover for user: {}", authenticatedUserId);
        try {
            if (!file.isEmpty()) {
                product.setCover(file.getBytes());
            }
            Product savedProduct = productService.addProduct(product);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedProduct);
        } catch (Exception e) {
            logger.error("Error adding product with cover: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Endpoint: Actualizar un producto (requiere autenticación)
    @PutMapping("/{id}")
    public ResponseEntity<Product> updateProduct(@PathVariable String id, @RequestBody Product updatedProduct, HttpServletRequest request) {
        String authenticatedUserId = request.getUserPrincipal().getName();
        logger.info("Updating product with ID: {} by user: {}", id, authenticatedUserId);

        return productService.updateProduct(id, updatedProduct, authenticatedUserId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(HttpStatus.FORBIDDEN).build());
    }

    // Endpoint: Actualizar la imagen de un producto existente (requiere autenticación)
    @PutMapping(value = "/{id}/cover", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Product> updateCover(
            @PathVariable String id,
            @RequestPart("cover") MultipartFile file,
            HttpServletRequest request
    ) {
        String authenticatedUserId = request.getUserPrincipal().getName();
        logger.info("Updating product cover for ID: {} by user: {}", id, authenticatedUserId);
        try {
            Optional<Product> optionalProduct = productService.getProductDetails(authenticatedUserId);
            if (optionalProduct.isEmpty()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            Product existingProduct = optionalProduct.get();
            if (!file.isEmpty()) {
                existingProduct.setCover(file.getBytes());
            }
            Product updatedProduct = productService.addProduct(existingProduct);
            return ResponseEntity.ok(updatedProduct);
        } catch (Exception e) {
            logger.error("Error updating product cover for ID: {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Endpoint: Eliminar un producto (requiere autenticación)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable String id, HttpServletRequest request) {
        String authenticatedUserId = request.getUserPrincipal().getName();
        logger.info("Deleting product with ID: {} by user: {}", id, authenticatedUserId);

        if (productService.deleteProduct(id, authenticatedUserId)) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }

    // Endpoint: Obtener productos relacionados (público)
    @GetMapping("/{id}/related")
    public ResponseEntity<List<Product>> getRelatedProducts(@PathVariable String id) {
        logger.info("Fetching related products via API for ID: {}", id);
        List<Product> relatedProducts = productService.getRelatedProducts(id);
        return ResponseEntity.ok(relatedProducts);
    }

    // Endpoint: Obtener todos los productos de un usuario específico (requiere autenticación)
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Product>> getProductsByUserId(@PathVariable Long userId) {
        logger.info("Fetching products via API for user ID: {}", userId);
        List<Product> userProducts = productService.getProductsByUserId(userId);
        return ResponseEntity.ok(userProducts);
    }
}
