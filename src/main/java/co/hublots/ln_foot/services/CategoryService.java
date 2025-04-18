package co.hublots.ln_foot.services;

import co.hublots.ln_foot.models.Category;
import java.util.List;
import java.util.UUID;

public interface CategoryService {
    List<Category> getAllCategories();

    Category getCategoryById(UUID id);

    Category createCategory(Category category);

    Category updateCategory(UUID id, Category category);

    void deleteCategory(UUID id);
}