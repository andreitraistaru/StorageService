package com.andreitraistaru.filestorage.utils;

import com.andreitraistaru.filestorage.exceptions.InvalidRegexpException;
import com.andreitraistaru.filestorage.exceptions.InvalidStorageItemNameException;
import lombok.extern.log4j.Log4j2;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

@Log4j2
public class Validators {
    public static void validateStorageItemName(String storageItemName) throws InvalidStorageItemNameException {
        if (storageItemName == null ||
                storageItemName.isBlank() ||
                storageItemName.length() > 64 ||
                !storageItemName.matches("[a-zA-Z0-9_-]+")) {
            throw new InvalidStorageItemNameException();
        }
    }

    public static void validateRegexp(String regexp) throws InvalidRegexpException {
        if (regexp == null || regexp.isBlank()) {
            throw new InvalidRegexpException();
        }

        try {
            Pattern.compile(regexp);
        } catch (PatternSyntaxException ignored) {
            throw new InvalidRegexpException();
        }
    }
}
