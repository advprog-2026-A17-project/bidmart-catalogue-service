package id.ac.ui.cs.advprog.bidmartcatalogueservice.dto;

import java.util.List;

public record CategoryResponse(
        Long id,
        String name,
        Long parentId,
        List<CategoryResponse> children
) {
}
