package id.ac.ui.cs.advprog.bidmartcatalogueservice.dto;

import id.ac.ui.cs.advprog.bidmartcatalogueservice.model.ListingStatus;
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
    private ListingStatus status;
}
