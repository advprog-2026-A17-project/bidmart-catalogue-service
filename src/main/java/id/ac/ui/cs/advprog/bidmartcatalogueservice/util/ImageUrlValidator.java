package id.ac.ui.cs.advprog.bidmartcatalogueservice.util;

import java.util.regex.Pattern;

public class ImageUrlValidator {

    private static final Pattern URL_PATTERN = Pattern.compile(
            "^https?://[\\w.-]+(:\\d+)?(/[\\w./%+@&=~-]*)*$"
    );
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
        return URL_PATTERN.matcher(url).matches() || IMAGE_DATA_URL_PATTERN.matcher(url).matches();
    }
}
