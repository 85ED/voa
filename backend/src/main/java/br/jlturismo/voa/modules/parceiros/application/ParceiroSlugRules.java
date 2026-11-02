package br.jlturismo.voa.modules.parceiros.application;

import java.util.Locale;
import java.util.regex.Pattern;

public final class ParceiroSlugRules {

    private static final Pattern SLUG_PATTERN = Pattern.compile("^[a-z0-9]+(?:-[a-z0-9]+)*$");
    private static final int MIN_LEN = 3;
    private static final int MAX_LEN = 64;

    private ParceiroSlugRules() {}

    public static String normalize(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new IllegalArgumentException("Slug é obrigatório");
        }
        String slug = raw.toLowerCase(Locale.ROOT).trim();
        slug = slug.replaceAll("[^a-z0-9\\s-]", "");
        slug = slug.replaceAll("\\s+", "-").replaceAll("-+", "-");
        if (slug.startsWith("-")) {
            slug = slug.substring(1);
        }
        if (slug.endsWith("-")) {
            slug = slug.substring(0, slug.length() - 1);
        }
        validate(slug);
        return slug;
    }

    public static String fromNome(String nome) {
        return normalize(nome == null ? "parceiro" : nome);
    }

    public static void validate(String slug) {
        if (slug.length() < MIN_LEN || slug.length() > MAX_LEN) {
            throw new IllegalArgumentException("Slug deve ter entre 3 e 64 caracteres");
        }
        if (!SLUG_PATTERN.matcher(slug).matches()) {
            throw new IllegalArgumentException("Slug inválido: use letras minúsculas, números e hífens");
        }
    }
}
