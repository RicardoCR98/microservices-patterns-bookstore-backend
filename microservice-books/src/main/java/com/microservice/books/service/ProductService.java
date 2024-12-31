package com.microservice.books.service;

import com.microservice.books.dto.ProductsFilterDTO;
import com.microservice.books.model.Product;
import com.microservice.books.repositories.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
@Service
@RequiredArgsConstructor
public class ProductService {
    private final ProductRepository productRepository;

    // Listar todos los productos
    public List<Product> listAllProducts() {
        return productRepository.findAll();
    }

    // Filtrar productos según los parámetros proporcionados
    public List<Product> filterProducts(ProductsFilterDTO filter) {
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
                    } else if("nuevo".equalsIgnoreCase(s)){
                        return p2.getCreatedAt().compareTo(p1.getCreatedAt());
                    } else if("descuento".equalsIgnoreCase(s)){
                        return p2.getOffer().compareTo(p1.getOffer());
                    }
                    return 0;
                }).orElse(0))
                .toList();
    }

    // Obtener detalles de un producto por ID
    public Optional<Product> getProductDetails(String id) {
        return productRepository.findById(id);
    }

    // Agregar un nuevo producto
    public Product addProduct(Product product) {
        if (product.getId() == null || product.getId().isEmpty()) {
            product.setId(UUID.randomUUID().toString());
        }
        if (product.getSalePrice() != null && product.getOffer() != null) {
            product.setOfferPrice(product.getSalePrice(), product.getOffer());
        }
        if(product.getStockQuantity() != null){
            product.setIsAvailable(product.getStockQuantity());
        }
        return productRepository.save(product);
    }

    // Actualizar un producto existente
    public Optional<Product> updateProduct(String id, Product updatedProduct) {
        return productRepository.findById(id).map(existingProduct -> {
            updatedProduct.setId(existingProduct.getId());
            return productRepository.save(updatedProduct);
        });
    }

    // Eliminar un producto por ID
    public boolean deleteProduct(String id) {
        if (productRepository.existsById(id)) {
            productRepository.deleteById(id);
            return true;
        }
        return false;
    }

    // Obtener productos relacionados (por ejemplo, misma categoría o género)
    public List<Product> getRelatedProducts(String id) {
        return productRepository.findById(id)
                .map(product -> productRepository.findByCategory(product.getCategory()).stream()
                        .filter(relatedProduct -> !relatedProduct.getId().equals(id))
                        .toList())
                .orElse(List.of());
    }
    // Nuevo metodo: obtener libros por userId
    public List<Product> getProductsByUserId(Long userId) {
        return productRepository.findByUserId(userId);
    }
}
