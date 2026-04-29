package id.ac.ui.cs.advprog.bidmartcatalogueservice.service;

import id.ac.ui.cs.advprog.bidmartcatalogueservice.dto.CategoryResponse;

import java.util.List;

public interface CategoryService {
    CategoryResponse createCategory(String name, Long parentId);
    List<CategoryResponse> getCategoryTree();
}
