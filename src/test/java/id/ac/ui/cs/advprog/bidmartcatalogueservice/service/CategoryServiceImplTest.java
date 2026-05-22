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
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
class CategoryServiceImplTest {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryServiceImpl categoryService;

    @Test
    void createCategory_WithoutParent() {
        Category saved = Category.builder().id(1L).name("Elektronik").build();
        when(categoryRepository.save(any(Category.class))).thenReturn(saved);

        var response = categoryService.createCategory("Elektronik", null);
        assertEquals(1L, response.id());
        assertEquals("Elektronik", response.name());
        assertNull(response.parentId());
    }

    @Test
    void createCategory_WithValidParent() {
        Category parent = Category.builder().id(1L).name("Elektronik").build();
        Category savedChild = Category.builder().id(2L).name("Laptop").parent(parent).build();
        
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(parent));
        when(categoryRepository.save(any(Category.class))).thenReturn(savedChild);

        var response = categoryService.createCategory("Laptop", 1L);
        assertEquals(2L, response.id());
        assertEquals(1L, response.parentId());
    }

    @Test
    void createCategory_WithInvalidParent_ThrowsException() {
        when(categoryRepository.findById(999L)).thenReturn(Optional.empty());

        IllegalArgumentException ex = org.junit.jupiter.api.Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> categoryService.createCategory("Laptop", 999L)
        );
        assertEquals("Parent category not found", ex.getMessage());
    }

    @Test
    void getCategoryTree_ReturnsCorrectHierarchy() {
        Category root1 = Category.builder().id(1L).name("B_Root").build();
        Category root2 = Category.builder().id(2L).name("A_Root").build();
        Category child1 = Category.builder().id(3L).name("Child_B").parent(root1).build();

        when(categoryRepository.findAll()).thenReturn(List.of(root1, root2, child1));

        var tree = categoryService.getCategoryTree();

        // Should be sorted by name: A_Root (2L), B_Root (1L)
        assertEquals(2, tree.size());
        assertEquals(2L, tree.get(0).id());
        assertEquals("A_Root", tree.get(0).name());
        assertTrue(tree.get(0).children().isEmpty());

        assertEquals(1L, tree.get(1).id());
        assertEquals("B_Root", tree.get(1).name());
        assertEquals(1, tree.get(1).children().size());
        assertEquals(3L, tree.get(1).children().get(0).id());
    }

    @Test
    void collectDescendantCategoryIds_NullId() {
        assertTrue(categoryService.collectDescendantCategoryIds(null).isEmpty());
    }

    @Test
    void collectDescendantCategoryIds_NotFound() {
        when(categoryRepository.findById(999L)).thenReturn(Optional.empty());
        assertTrue(categoryService.collectDescendantCategoryIds(999L).isEmpty());
    }

    @Test
    void collectDescendantCategoryIdsShouldIncludeChildren() {
        Category elektronik = Category.builder().id(1L).name("Elektronik").build();
        Category handphone = Category.builder().id(2L).name("Handphone").parent(elektronik).build();
        Category smartphone = Category.builder().id(3L).name("Smartphone").parent(handphone).build();

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(elektronik));
        when(categoryRepository.findAll()).thenReturn(List.of(elektronik, handphone, smartphone));

        List<Long> ids = categoryService.collectDescendantCategoryIds(1L);

        assertEquals(3, ids.size());
        assertTrue(ids.containsAll(List.of(1L, 2L, 3L)));
    }
}
