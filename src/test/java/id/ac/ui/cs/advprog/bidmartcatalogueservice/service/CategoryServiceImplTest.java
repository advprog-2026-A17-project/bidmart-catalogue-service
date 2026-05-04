package id.ac.ui.cs.advprog.bidmartcatalogueservice.service;

import id.ac.ui.cs.advprog.bidmartcatalogueservice.model.Category;
import id.ac.ui.cs.advprog.bidmartcatalogueservice.repository.CategoryRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CategoryServiceImplTest {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryServiceImpl categoryService;

    @Test
    void createCategoryShouldAttachParentWhenParentIdExists() {
        Category parent = Category.builder().id(1L).name("Electronics").build();
        Category saved = Category.builder().id(2L).name("Cameras").parent(parent).build();

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(parent));
        when(categoryRepository.save(any(Category.class))).thenReturn(saved);

        var response = categoryService.createCategory("Cameras", 1L);

        assertEquals(2L, response.id());
        assertEquals("Cameras", response.name());
        assertEquals(1L, response.parentId());
    }

    @Test
    void getCategoryTreeShouldReturnOnlyRootCategoriesWithChildren() {
        Category root = Category.builder().id(1L).name("Electronics").build();
        Category child = Category.builder().id(2L).name("Cameras").parent(root).build();

        when(categoryRepository.findAll()).thenReturn(List.of(root, child));

        var tree = categoryService.getCategoryTree();

        assertEquals(1, tree.size());
        assertEquals("Electronics", tree.getFirst().name());
        assertEquals("Cameras", tree.getFirst().children().getFirst().name());
    }
}
