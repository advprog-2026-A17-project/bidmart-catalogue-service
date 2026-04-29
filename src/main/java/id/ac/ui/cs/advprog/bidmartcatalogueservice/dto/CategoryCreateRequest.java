package id.ac.ui.cs.advprog.bidmartcatalogueservice.dto;

public record CategoryCreateRequest(
        String name,
        Long parentId
) {
}
