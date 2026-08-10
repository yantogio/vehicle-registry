package com.kendaraan.controller;

import com.kendaraan.dto.KendaraanRequest;
import com.kendaraan.dto.KendaraanResponse;
import com.kendaraan.dto.PageResponse;
import com.kendaraan.service.KendaraanService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/vehicles")
public class KendaraanController {

    private final KendaraanService service;

    public KendaraanController(KendaraanService service) {
        this.service = service;
    }

    /**
     * Pencarian dengan filter opsional dan pagination server-side.
     * Filter yang tidak dikirim bernilai null dan dinormalkan di service,
     * sehingga pemanggilan tanpa parameter mengembalikan seluruh data.
     */
    @GetMapping
    public ResponseEntity<PageResponse<KendaraanResponse>> search(
            @RequestParam(required = false) String noRegistrasi,
            @RequestParam(required = false) String namaPemilik,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        return ResponseEntity.ok(service.search(noRegistrasi, namaPemilik, page, size));
    }

    @GetMapping("/{noRegistrasi}")
    public ResponseEntity<KendaraanResponse> detail(@PathVariable String noRegistrasi) {
        return ResponseEntity.ok(service.findByNoRegistrasi(noRegistrasi));
    }

    @PostMapping
    public ResponseEntity<KendaraanResponse> create(@Valid @RequestBody KendaraanRequest request) {
        KendaraanResponse response = service.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * noRegistrasi yang dipakai selalu berasal dari path. Nilai noRegistrasi
     * pada body request diabaikan oleh service, karena primary key bersifat
     * immutable setelah data tersimpan.
     */
    @PutMapping("/{noRegistrasi}")
    public ResponseEntity<KendaraanResponse> update(@PathVariable String noRegistrasi,
                                                    @Valid @RequestBody KendaraanRequest request) {
        return ResponseEntity.ok(service.update(noRegistrasi, request));
    }

    @DeleteMapping("/{noRegistrasi}")
    public ResponseEntity<Void> delete(@PathVariable String noRegistrasi) {
        service.delete(noRegistrasi);
        return ResponseEntity.noContent().build();
    }
}
