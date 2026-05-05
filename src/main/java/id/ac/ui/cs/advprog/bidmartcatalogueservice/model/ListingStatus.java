package id.ac.ui.cs.advprog.bidmartcatalogueservice.model;

public enum ListingStatus {
    DRAFT,            // Listing baru dibuat, belum dipublish
    ACTIVE,           // Listing sudah dipublish dan bisa di-bid
    AUCTION_CREATED,  // Auction sudah dibuat untuk listing ini
    SOLD,             // Listing terjual (lelang selesai, ada pemenang)
    UNSOLD,           // Listing tidak terjual (lelang selesai, tidak ada pemenang)
    CANCELLED         // Listing dibatalkan oleh seller
}
