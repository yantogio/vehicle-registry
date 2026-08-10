package com.kendaraan.exception;

/**
 * Dilempar saat operasi menyasar noRegistrasi yang tidak ada di database.
 * Dipetakan ke HTTP 404 oleh error handler terpusat.
 */
public class DataNotFoundException extends RuntimeException {

    public DataNotFoundException(String message) {
        super(message);
    }
}
