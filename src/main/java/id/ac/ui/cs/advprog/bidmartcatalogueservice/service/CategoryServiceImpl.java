package id.ac.ui.cs.advprog.bidmartcatalogueservice.service;

import id.ac.ui.cs.advprog.bidmartcatalogueservice.dto.CategoryResponse;
import id.ac.ui.cs.advprog.bidmartcatalogueservice.model.Category;
import id.ac.ui.cs.advprog.bidmartcatalogueservice.repository.CategoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryServiceImpl(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Override
    @Transactional
    public CategoryResponse createCategory(String name, Long parentId) {
        Category parent = null;
        if (parentId != null) {
            parent = categoryRepository.findById(parentId)
                    .orElseThrow(() -> new IllegalArgumentException("Parent category not found"));
        }

        Category category = Category.builder()
                .name(name.trim())
                .parent(parent)
                .build();
        return toResponse(categoryRepository.save(category), List.of());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryResponse> getCategoryTree() {
        List<Category> categories = categoryRepository.findAll().stream()
                .sorted(Comparator.comparing(Category::getName))
                .toList();
        Map<Long, List<Category>> childrenByParentId = new HashMap<>();
        List<Category> roots = new ArrayList<>();

        for (Category category : categories) {
            Category parent = category.getParent();
            if (parent == null) {
                roots.add(category);
            } else {
                childrenByParentId.computeIfAbsent(parent.getId(), ignored -> new ArrayList<>()).add(category);
            }
        }

        return roots.stream()
                .map(category -> toTreeResponse(category, childrenByParentId))
                .toList();
    }

    private CategoryResponse toTreeResponse(Category category, Map<Long, List<Category>> childrenByParentId) {
        List<CategoryResponse> children = childrenByParentId
                .getOrDefault(category.getId(), List.of())
                .stream()
                .map(child -> toTreeResponse(child, childrenByParentId))
                .toList();
        return toResponse(category, children);
    }

    private CategoryResponse toResponse(Category category, List<CategoryResponse> children) {
        Long parentId = category.getParent() == null ? null : category.getParent().getId();
        return new CategoryResponse(category.getId(), category.getName(), parentId, children);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Long> collectDescendantCategoryIds(Long categoryId) {
        if (categoryId == null) {
            return List.of();
        }
        if (categoryRepository.findById(categoryId).isEmpty()) {
            return List.of();
        }
        List<Category> categories = categoryRepository.findAll();
        Map<Long, List<Category>> childrenByParentId = new HashMap<>();
        for (Category category : categories) {
            Category parent = category.getParent();
            if (parent != null) {
                childrenByParentId.computeIfAbsent(parent.getId(), ignored -> new ArrayList<>()).add(category);
            }
        }
        List<Long> ids = new ArrayList<>();
        collectDescendants(categoryId, childrenByParentId, ids);
        return ids;
    }

    private void collectDescendants(
            Long categoryId,
            Map<Long, List<Category>> childrenByParentId,
            List<Long> ids
    ) {
        ids.add(categoryId);
        for (Category child : childrenByParentId.getOrDefault(categoryId, List.of())) {
            collectDescendants(child.getId(), childrenByParentId, ids);
        }
    }
}
