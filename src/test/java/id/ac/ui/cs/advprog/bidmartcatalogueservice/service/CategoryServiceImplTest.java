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
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CategoryServiceImplTest {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryServiceImpl categoryService;

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
