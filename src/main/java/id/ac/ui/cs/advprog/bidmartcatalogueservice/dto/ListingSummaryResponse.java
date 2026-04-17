package id.ac.ui.cs.advprog.bidmartcatalogueservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ListingSummaryResponse {
    private String id;
    private String sellerId;
    private String status;
}
