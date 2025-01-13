
// ProductController.java
package com.microservice.books.controller;

import com.microservice.books.dto.ProductsFilterDTO;
import com.microservice.books.model.Product;
import com.microservice.books.service.ProductService;
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

    // Endpoint: Listar todos los productos
    @GetMapping
    public ResponseEntity<List<Product>> getAllProducts() {
        logger.info("Fetching all products via API");
        List<Product> products = productService.listAllProducts();
        return ResponseEntity.ok(products);
    }

    @PostMapping("/filter")
    public ResponseEntity<Map<String, List<Product>>> filterProducts(@RequestBody ProductsFilterDTO filter) {
        logger.info("Filtering products via API with filter: {}", filter);
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

    // Endpoint: Agregar un nuevo producto (SIN portada)
    @PostMapping
    public ResponseEntity<Product> addProduct(@RequestBody Product product) {
        logger.info("Adding a new product via API: {}", product);
        Product savedProduct = productService.addProduct(product);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedProduct);
    }

    // NUEVO: Agregar un nuevo producto CON portada (byte[])
    @PostMapping(value = "/with-cover", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Product> addProductWithCover(
            @RequestPart("product") Product product,
            @RequestPart("cover") MultipartFile file
    ) {
        logger.info("Adding a new product with cover via API: {}", product);
        try {
            // Convertir el archivo a bytes y asignarlo al producto
            if (!file.isEmpty()) {
                product.setCover(file.getBytes());
            }
            // Guardamos el producto usando el servicio
            Product savedProduct = productService.addProduct(product);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedProduct);
        } catch (Exception e) {
            logger.error("Error adding product with cover: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Endpoint: Actualizar un producto
    @PutMapping("/{id}")
    public ResponseEntity<Product> updateProduct(@PathVariable String id, @RequestBody Product updatedProduct) {
        logger.info("Updating product via API with ID: {}", id);
        return productService.updateProduct(id, updatedProduct)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // NUEVO Endpoint: Actualizar la imagen de un producto existente
    @PutMapping(value = "/{id}/cover", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Product> updateCover(
            @PathVariable String id,
            @RequestPart("cover") MultipartFile file
    ) {
        logger.info("Updating product cover via API for ID: {}", id);
        try {
            Optional<Product> optionalProduct = productService.getProductDetails(id);
            if (optionalProduct.isEmpty()) {
                logger.warn("Product not found for cover update with ID: {}", id);
                return ResponseEntity.notFound().build();
            }
            Product existingProduct = optionalProduct.get();
            if (!file.isEmpty()) {
                existingProduct.setCover(file.getBytes());
            }
            // Guardamos nuevamente el producto
            Product updatedProduct = productService.addProduct(existingProduct);
            logger.info("Product cover updated successfully for ID: {}", id);
            return ResponseEntity.ok(updatedProduct);
        } catch (Exception e) {
            logger.error("Error updating product cover for ID: {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Endpoint: Eliminar un producto
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable String id) {
        logger.info("Deleting product via API with ID: {}", id);
        if (productService.deleteProduct(id)) {
            logger.info("Product deleted successfully via API with ID: {}", id);
            return ResponseEntity.noContent().build();
        }
        logger.warn("Product not found for deletion via API with ID: {}", id);
        return ResponseEntity.notFound().build();
    }

    // Endpoint: Obtener productos relacionados
    @GetMapping("/{id}/related")
    public ResponseEntity<List<Product>> getRelatedProducts(@PathVariable String id) {
        logger.info("Fetching related products via API for ID: {}", id);
        List<Product> relatedProducts = productService.getRelatedProducts(id);
        return ResponseEntity.ok(relatedProducts);
    }

    // Obtener todos los libros de un usuario
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Product>> getProductsByUserId(@PathVariable Long userId) {
        logger.info("Fetching products via API for user ID: {}", userId);
        List<Product> userProducts = productService.getProductsByUserId(userId);
        return ResponseEntity.ok(userProducts);
    }
}
