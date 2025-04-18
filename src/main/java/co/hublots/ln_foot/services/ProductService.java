package co.hublots.ln_foot.services;

import co.hublots.ln_foot.models.Product;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

public interface ProductService {
    List<Product> getAllProducts();

    Product getProductById(UUID id) throws NoSuchElementException;

    Product createProduct(Product product);

    Product updateProduct(UUID id, Product product);

    void deleteProduct(UUID id);
}
