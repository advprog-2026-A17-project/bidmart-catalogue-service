package id.ac.ui.cs.advprog.bidmartcatalogueservice.util;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.regex.Pattern;

public class ImageUrlValidator {

    /** Aligns with frontend SellPage MAX_IMAGE_BYTES (~600KB file → ~800k base64 chars). */
    public static final int MAX_DATA_URL_LENGTH = 900_000;

    private static final Pattern IMAGE_DATA_URL_PATTERN = Pattern.compile(
            "^data:image/(png|jpe?g|webp);base64,[A-Za-z0-9+/=_-]+$",
            Pattern.CASE_INSENSITIVE
    );

    private ImageUrlValidator() {
        // utility class
    }

    public static boolean isValidImageUrl(String url) {
        if (url == null || url.isBlank()) {
            return true; // imageUrl bersifat opsional
        }
        String trimmedUrl = url.trim();
        if (ListingPresentation.EMBEDDED_IMAGE_PLACEHOLDER.equals(trimmedUrl)) {
            return true;
        }
        if (IMAGE_DATA_URL_PATTERN.matcher(trimmedUrl).matches()) {
            return trimmedUrl.length() <= MAX_DATA_URL_LENGTH;
        }
        try {
            URI uri = new URI(trimmedUrl);
            String scheme = uri.getScheme();
            return ("http".equalsIgnoreCase(scheme) || "https".equalsIgnoreCase(scheme))
                    && uri.getHost() != null
                    && !uri.getHost().isBlank();
        } catch (URISyntaxException exception) {
            return false;
        }
    }
}
