package id.ac.ui.cs.advprog.bidmartcatalogueservice.util;

import java.util.regex.Pattern;

public class ImageUrlValidator {

    private static final Pattern URL_PATTERN = Pattern.compile(
            "^https?://[\\w.-]+(:\\d+)?(/[\\w./%+@&=~-]*)*$"
    );

    private ImageUrlValidator() {
        // utility class
    }

    public static boolean isValidImageUrl(String url) {
        if (url == null || url.isBlank()) {
            return true; // imageUrl bersifat opsional
        }
        return URL_PATTERN.matcher(url).matches();
    }
}
