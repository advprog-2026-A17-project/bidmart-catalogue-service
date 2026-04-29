package id.ac.ui.cs.advprog.bidmartcatalogueservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import id.ac.ui.cs.advprog.bidmartcatalogueservice.config.AuthInterceptor;
import id.ac.ui.cs.advprog.bidmartcatalogueservice.dto.CategoryCreateRequest;
import id.ac.ui.cs.advprog.bidmartcatalogueservice.dto.CategoryResponse;
import id.ac.ui.cs.advprog.bidmartcatalogueservice.service.CategoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CategoryController.class)
class CategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CategoryService categoryService;

    @MockBean
    private AuthInterceptor authInterceptor;

    @BeforeEach
    void setUp() throws Exception {
        when(authInterceptor.preHandle(any(), any(), any())).thenReturn(true);
    }

    @Test
    void createCategoryShouldReturnCreatedCategory() throws Exception {
        when(categoryService.createCategory("Electronics", null))
                .thenReturn(new CategoryResponse(1L, "Electronics", null, List.of()));

        CategoryCreateRequest request = new CategoryCreateRequest("Electronics", null);

        mockMvc.perform(post("/api/v1/catalogue/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Electronics"));
    }

    @Test
    void getCategoryTreeShouldReturnNestedCategories() throws Exception {
        CategoryResponse cameras = new CategoryResponse(2L, "Cameras", 1L, List.of());
        CategoryResponse electronics = new CategoryResponse(1L, "Electronics", null, List.of(cameras));
        when(categoryService.getCategoryTree()).thenReturn(List.of(electronics));

        mockMvc.perform(get("/api/v1/catalogue/categories/tree"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Electronics"))
                .andExpect(jsonPath("$[0].children[0].name").value("Cameras"));
    }
}
