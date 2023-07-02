package com.andreitraistaru.filestorage.utils;

import com.andreitraistaru.filestorage.exceptions.InvalidRegexpException;
import com.andreitraistaru.filestorage.exceptions.InvalidStorageItemNameException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ValidatorsTests {
    @Test
    @DisplayName("Storage item name is null")
    void validateStorageItemName_nullName() {
        assertThrows(InvalidStorageItemNameException.class, () -> Validators.validateStorageItemName(null));
    }

    @Test
    @DisplayName("Storage item name is empty")
    void validateStorageItemName_emptyName() {
        // Empty
        assertThrows(InvalidStorageItemNameException.class, () -> Validators.validateStorageItemName(""));
        // One whitespace
        assertThrows(InvalidStorageItemNameException.class, () -> Validators.validateStorageItemName(" "));
        // Multiple whitespaces
        assertThrows(InvalidStorageItemNameException.class, () -> Validators.validateStorageItemName("  "));
        // Combination of whitespaces and tabs
        assertThrows(InvalidStorageItemNameException.class, () -> Validators.validateStorageItemName("      "));
    }

    @Test
    @DisplayName("Storage item name's size check")
    void validateStorageItemName_longerName() {
        assertDoesNotThrow(() -> Validators.validateStorageItemName("x".repeat(64)));

        assertThrows(InvalidStorageItemNameException.class, () -> Validators.validateStorageItemName("x".repeat(65)));
    }

    @Test
    @DisplayName("Storage item name's character set check")
    void validateStorageItemName_invalidCharactersName() {
        assertDoesNotThrow(() -> Validators.validateStorageItemName("abc"));
        assertDoesNotThrow(() -> Validators.validateStorageItemName("ABC"));
        assertDoesNotThrow(() -> Validators.validateStorageItemName("012"));
        assertDoesNotThrow(() -> Validators.validateStorageItemName("---"));
        assertDoesNotThrow(() -> Validators.validateStorageItemName("___"));
        assertDoesNotThrow(() -> Validators.validateStorageItemName("abc-ABC-012"));

        assertThrows(InvalidStorageItemNameException.class, () -> Validators.validateStorageItemName("?"));
        // This is `—` instead of `-` so we expect to get an InvalidStorageItemNameException for it
        assertThrows(InvalidStorageItemNameException.class, () -> Validators.validateStorageItemName("—"));
        assertThrows(InvalidStorageItemNameException.class, () -> Validators.validateStorageItemName("abc!"));
        assertThrows(InvalidStorageItemNameException.class, () -> Validators.validateStorageItemName("abc~"));
    }

    @Test
    @DisplayName("Regexp is null")
    void validateRegexp_nullRegexp() {
        assertThrows(InvalidRegexpException.class, () -> Validators.validateRegexp(null));
    }

    @Test
    @DisplayName("Regexp is empty")
    void validateRegexp_emptyRegexp() {
        // Empty
        assertThrows(InvalidRegexpException.class, () -> Validators.validateRegexp(""));
        // One whitespace
        assertThrows(InvalidRegexpException.class, () -> Validators.validateRegexp(" "));
        // Multiple whitespaces
        assertThrows(InvalidRegexpException.class, () -> Validators.validateRegexp("  "));
        // Combination of whitespaces and tabs
        assertThrows(InvalidRegexpException.class, () -> Validators.validateRegexp("      "));
    }

    @Test
    @DisplayName("Regexp is invalid")
    void validateRegexp_invalidRegexp() {
        assertDoesNotThrow(() -> Validators.validateRegexp("abc"));
        assertDoesNotThrow(() -> Validators.validateRegexp("012"));
        assertDoesNotThrow(() -> Validators.validateRegexp("[a-z]"));
        assertDoesNotThrow(() -> Validators.validateRegexp("[a-zA-Z0-9_-]"));
        assertDoesNotThrow(() -> Validators.validateRegexp("[a-zA-Z0-9_-]+"));
        assertDoesNotThrow(() -> Validators.validateRegexp("test.+test"));
        assertDoesNotThrow(() -> Validators.validateRegexp(".*"));
        assertDoesNotThrow(() -> Validators.validateRegexp("^test"));
        assertDoesNotThrow(() -> Validators.validateRegexp("a|b"));

        assertThrows(InvalidRegexpException.class, () -> Validators.validateRegexp("**"));
        assertThrows(InvalidRegexpException.class, () -> Validators.validateRegexp("[0-[]"));
        assertThrows(InvalidRegexpException.class, () -> Validators.validateRegexp("[a-z"));
        assertThrows(InvalidRegexpException.class, () -> Validators.validateRegexp("\\"));
    }
}
