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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CategoryServiceImplTest {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryServiceImpl categoryService;

    @Test
    void createCategoryTrimsNameAndReturnsSavedRootCategory() {
        Category saved = Category.builder().id(1L).name("Elektronik").build();
        when(categoryRepository.save(any(Category.class))).thenReturn(saved);

        var response = categoryService.createCategory("  Elektronik  ", null);

        assertEquals(1L, response.id());
        assertEquals("Elektronik", response.name());
        assertNull(response.parentId());
        assertTrue(response.children().isEmpty());
    }

    @Test
    void createCategoryAttachesExistingParent() {
        Category parent = Category.builder().id(1L).name("Elektronik").build();
        Category saved = Category.builder().id(2L).name("Handphone").parent(parent).build();
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(parent));
        when(categoryRepository.save(any(Category.class))).thenReturn(saved);

        var response = categoryService.createCategory("Handphone", 1L);

        assertEquals(2L, response.id());
        assertEquals(1L, response.parentId());
    }

    @Test
    void createCategoryRejectsMissingParent() {
        when(categoryRepository.findById(99L)).thenReturn(Optional.empty());

        IllegalArgumentException error = assertThrows(
                IllegalArgumentException.class,
                () -> categoryService.createCategory("Missing parent", 99L)
        );

        assertEquals("Parent category not found", error.getMessage());
    }

    @Test
    void getCategoryTreeSortsRootsAndBuildsNestedChildren() {
        Category fashion = Category.builder().id(1L).name("Fashion").build();
        Category elektronik = Category.builder().id(2L).name("Elektronik").build();
        Category handphone = Category.builder().id(3L).name("Handphone").parent(elektronik).build();
        Category smartphone = Category.builder().id(4L).name("Smartphone").parent(handphone).build();
        when(categoryRepository.findAll()).thenReturn(List.of(fashion, smartphone, handphone, elektronik));

        var tree = categoryService.getCategoryTree();

        assertEquals(List.of("Elektronik", "Fashion"), tree.stream().map(category -> category.name()).toList());
        assertEquals("Handphone", tree.get(0).children().get(0).name());
        assertEquals("Smartphone", tree.get(0).children().get(0).children().get(0).name());
    }

    @Test
    void collectDescendantCategoryIdsReturnsEmptyForNullOrMissingCategory() {
        when(categoryRepository.findById(42L)).thenReturn(Optional.empty());

        assertTrue(categoryService.collectDescendantCategoryIds(null).isEmpty());
        assertTrue(categoryService.collectDescendantCategoryIds(42L).isEmpty());
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
