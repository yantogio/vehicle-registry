package com.kendaraan.exception;

/**
 * Dilempar saat operasi create menyasar noRegistrasi yang sudah terdaftar.
 * Dipetakan ke HTTP 409 oleh error handler terpusat.
 *
 * Dinamai DuplicateDataException (bukan DuplicateKeyException) agar tidak
 * tertukar saat import dengan org.springframework.dao.DuplicateKeyException.
 */
public class DuplicateDataException extends RuntimeException {

    public DuplicateDataException(String message) {
        super(message);
    }
}
