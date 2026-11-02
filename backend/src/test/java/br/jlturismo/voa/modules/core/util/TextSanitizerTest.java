package br.jlturismo.voa.modules.core.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TextSanitizerTest {

    @Test
    void removeTagsHtml() {
        assertEquals("João", TextSanitizer.plainText("<b>João</b>"));
    }

    @Test
    void rejeitaVazio() {
        assertThrows(IllegalArgumentException.class, () -> TextSanitizer.plainTextRequired("  ", "Nome"));
    }
}
