package com.jcaa.appcrudusers.domain.vo;

import com.jcaa.appcrudusers.domain.exception.ValidationException;

final class ValueValidation {
    private ValueValidation() {}

    static String required(String field, String value) {
        if (value == null || value.codePoints().allMatch(
                c -> Character.isWhitespace(c) || Character.isSpaceChar(c))) {
            throw new ValidationException(field, field + " es obligatorio.");
        }
        return value;
    }
}
