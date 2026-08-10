package com.kendaraan.exception;

import com.kendaraan.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Satu-satunya tempat penerjemahan exception menjadi response HTTP,
 * sehingga seluruh error API memakai struktur body yang seragam.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Kegagalan Bean Validation pada DTO request.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleBeanValidation(MethodArgumentNotValidException ex,
                                                              HttpServletRequest request) {
        Map<String, String> fieldErrors = new LinkedHashMap<>();
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            // Bila satu field punya lebih dari satu pelanggaran, pesan pertama
            // yang dipakai agar tampilan per field tetap ringkas.
            fieldErrors.putIfAbsent(fieldError.getField(), fieldError.getDefaultMessage());
        }
        return build(HttpStatus.BAD_REQUEST, "Validasi gagal", request, fieldErrors);
    }

    /**
     * Kegagalan validasi yang diperiksa di service, misalnya batas atas
     * tahunPembuatan. Sengaja dipetakan ke bentuk yang sama dengan kegagalan
     * Bean Validation agar frontend tidak perlu membedakan sumber error.
     */
    @ExceptionHandler(FieldValidationException.class)
    public ResponseEntity<ErrorResponse> handleFieldValidation(FieldValidationException ex,
                                                               HttpServletRequest request) {
        return build(HttpStatus.BAD_REQUEST, "Validasi gagal", request, ex.getErrors());
    }

    /**
     * Body request tidak bisa dibaca, misalnya JSON rusak atau tahunPembuatan
     * diisi teks yang bukan angka. Ditangani di sini agar tetap memakai format
     * error yang sama, bukan halaman error bawaan Spring.
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleUnreadableBody(HttpMessageNotReadableException ex,
                                                              HttpServletRequest request) {
        log.warn("Request body tidak dapat dibaca: {}", ex.getMessage());
        return build(HttpStatus.BAD_REQUEST,
                "Format data yang dikirim tidak valid, pastikan field numerik diisi angka",
                request, null);
    }

    @ExceptionHandler(DataNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(DataNotFoundException ex,
                                                        HttpServletRequest request) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage(), request, null);
    }

    @ExceptionHandler(DuplicateDataException.class)
    public ResponseEntity<ErrorResponse> handleDuplicate(DuplicateDataException ex,
                                                         HttpServletRequest request) {
        return build(HttpStatus.CONFLICT, ex.getMessage(), request, null);
    }

    /**
     * URL yang tidak cocok dengan controller mana pun akan berakhir di resource
     * handler statis, yang sejak Spring Boot 3.2 melempar NoResourceFoundException.
     * Tanpa handler ini, exception tersebut tertangkap oleh jaring terakhir
     * Exception.class dan berubah menjadi HTTP 500, padahal yang benar 404.
     */
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoResource(NoResourceFoundException ex,
                                                          HttpServletRequest request) {
        return build(HttpStatus.NOT_FOUND,
                "Halaman atau resource tidak ditemukan", request, null);
    }

    /**
     * Jaring terakhir. Tetap dipertahankan agar kegagalan tak terduga tidak
     * bocor sebagai halaman error bawaan Spring, tapi seluruh kasus yang sudah
     * punya arti sendiri ditangani oleh handler yang lebih spesifik di atas.
     * Spring selalu memilih handler paling spesifik lebih dulu.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleOther(Exception ex, HttpServletRequest request) {
        log.error("Terjadi kesalahan tak terduga", ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR,
                "Terjadi kesalahan pada server", request, null);
    }

    private ResponseEntity<ErrorResponse> build(HttpStatus status, String message,
                                                HttpServletRequest request,
                                                Map<String, String> fieldErrors) {
        ErrorResponse body = new ErrorResponse(
                status.value(), status.getReasonPhrase(), message, request.getRequestURI());
        if (fieldErrors != null && !fieldErrors.isEmpty()) {
            body.setFieldErrors(fieldErrors);
        }
        return ResponseEntity.status(status).body(body);
    }
}
