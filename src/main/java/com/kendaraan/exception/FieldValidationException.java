package com.kendaraan.exception;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Kegagalan validasi yang tidak bisa dinyatakan lewat anotasi Bean Validation,
 * misalnya batas atas tahunPembuatan yang bergantung pada tahun berjalan.
 *
 * Membawa pasangan nama field dan pesannya, agar error handler terpusat dapat
 * menghasilkan HTTP 400 dengan struktur body yang identik dengan kegagalan
 * Bean Validation. Dengan begitu frontend tidak perlu membedakan sumber error.
 */
public class FieldValidationException extends RuntimeException {

    private final Map<String, String> errors;

    public FieldValidationException(String field, String message) {
        super(message);
        this.errors = new LinkedHashMap<>();
        this.errors.put(field, message);
    }

    public FieldValidationException(Map<String, String> errors) {
        super("Validasi gagal");
        this.errors = new LinkedHashMap<>(errors);
    }

    public Map<String, String> getErrors() {
        return errors;
    }
}
