package id.ac.ui.cs.advprog.bidmartcatalogueservice.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ImageUrlValidatorTest {

    @Test
    void testValidHttpUrl() {
        assertTrue(ImageUrlValidator.isValidImageUrl("http://example.com/image.jpg"));
    }

    @Test
    void testValidHttpsUrl() {
        assertTrue(ImageUrlValidator.isValidImageUrl("https://example.com/image.png"));
    }

    @Test
    void testValidUrlWithPort() {
        assertTrue(ImageUrlValidator.isValidImageUrl("http://localhost:8080/images/photo.jpg"));
    }

    @Test
    void testValidUrlWithSubdirectories() {
        assertTrue(ImageUrlValidator.isValidImageUrl("https://cdn.example.com/uploads/2026/05/image.webp"));
    }

    @Test
    void testNullUrl_IsValid() {
        assertTrue(ImageUrlValidator.isValidImageUrl(null));
    }

    @Test
    void testBlankUrl_IsValid() {
        assertTrue(ImageUrlValidator.isValidImageUrl(""));
        assertTrue(ImageUrlValidator.isValidImageUrl("   "));
    }

    @Test
    void testInvalidUrl_NoProtocol() {
        assertFalse(ImageUrlValidator.isValidImageUrl("example.com/image.jpg"));
    }

    @Test
    void testInvalidUrl_FtpProtocol() {
        assertFalse(ImageUrlValidator.isValidImageUrl("ftp://example.com/image.jpg"));
    }

    @Test
    void testInvalidUrl_RandomString() {
        assertFalse(ImageUrlValidator.isValidImageUrl("not-a-url"));
    }

    @Test
    void testInvalidUrl_JavascriptProtocol() {
        assertFalse(ImageUrlValidator.isValidImageUrl("javascript:alert(1)"));
    }
}
