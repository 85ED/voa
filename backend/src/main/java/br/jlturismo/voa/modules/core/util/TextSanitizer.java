package br.jlturismo.voa.modules.core.util;

import java.util.regex.Pattern;

public final class TextSanitizer {

    private static final Pattern HTML_TAGS = Pattern.compile("<[^>]*>");

    private TextSanitizer() {}

    public static String plainText(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            return trimmed;
        }
        String withoutTags = HTML_TAGS.matcher(trimmed).replaceAll("");
        return withoutTags.replaceAll("[\\x00-\\x08\\x0B\\x0C\\x0E-\\x1F]", "");
    }

    public static String plainTextRequired(String value, String fieldName) {
        String sanitized = plainText(value);
        if (sanitized == null || sanitized.isBlank()) {
            throw new IllegalArgumentException(fieldName + " é obrigatório");
        }
        return sanitized;
    }
}
