package co.hublots.ln_foot.services;

import co.hublots.ln_foot.models.Product;
import java.util.List;
import java.util.NoSuchElementException;


public interface ProductService {
    List<Product> getAllProducts();

    Product getProductById(String id) throws NoSuchElementException;

    Product createProduct(Product product);

    Product updateProduct(String id, Product product);

    void deleteProduct(String id);
}
