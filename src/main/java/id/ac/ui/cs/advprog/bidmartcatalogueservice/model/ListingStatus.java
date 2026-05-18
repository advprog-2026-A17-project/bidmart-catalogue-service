package id.ac.ui.cs.advprog.bidmartcatalogueservice.model;

public enum ListingStatus {
    DRAFT,      // Listing baru dibuat, belum dipublish
    ACTIVE,     // Listing sudah dipublish dan bisa di-bid
    EXTENDED,   // Lelang diperpanjang karena anti-sniping
    CLOSED,     // Lelang ditutup, menunggu penetapan hasil
    WON,        // Lelang selesai, reserve terpenuhi
    UNSOLD,     // Lelang selesai, reserve tidak terpenuhi
    CANCELLED   // Listing dibatalkan oleh seller
}
