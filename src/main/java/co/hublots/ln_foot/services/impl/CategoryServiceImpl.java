package co.hublots.ln_foot.services.impl;

import co.hublots.ln_foot.models.Category;
import co.hublots.ln_foot.repositories.CategoryRepository;
import co.hublots.ln_foot.services.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

@Service
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    @Autowired
    public CategoryServiceImpl(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Override
    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    @Override
    public Category getCategoryById(UUID id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Category not found with id: " + id));
    }

    @Override
    public Category createCategory(Category category) {
        return categoryRepository.save(category);
    }

    @Override
    public Category updateCategory(UUID id, Category category) {
        Category existingCategory = categoryRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Category not found with id: " + id));
        Optional.of(category.getName()).ifPresent(existingCategory::setName);
        
        return categoryRepository.save(existingCategory);
    }

    @Override
    public void deleteCategory(UUID id) {
        categoryRepository.deleteById(id);
    }
} 