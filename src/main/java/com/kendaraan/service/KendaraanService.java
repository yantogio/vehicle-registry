package com.kendaraan.service;

import com.kendaraan.dto.KendaraanRequest;
import com.kendaraan.dto.KendaraanResponse;
import com.kendaraan.dto.PageResponse;
import com.kendaraan.entity.Kendaraan;
import com.kendaraan.exception.DataNotFoundException;
import com.kendaraan.exception.DuplicateDataException;
import com.kendaraan.exception.FieldValidationException;
import com.kendaraan.repository.KendaraanRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Year;
import java.util.List;

@Service
public class KendaraanService {

    private static final int TAHUN_MINIMAL = 1900;

    private final KendaraanRepository repository;

    public KendaraanService(KendaraanRepository repository) {
        this.repository = repository;
    }

    /**
     * Pencarian dengan filter partial match case-insensitive, digabung AND,
     * lalu dipotong per halaman di sisi server.
     */
    @Transactional(readOnly = true)
    public PageResponse<KendaraanResponse> search(String noRegistrasi, String namaPemilik, int page, int size) {
        // Method turunan Spring Data tidak mencocokkan apa pun bila parameternya
        // null, sedangkan string kosong mencocokkan semua baris. Karena itu
        // filter yang tidak dikirim dinormalkan menjadi string kosong.
        String filterNoRegistrasi = normalize(noRegistrasi);
        String filterNamaPemilik = normalize(namaPemilik);

        Pageable pageable = buildPageable(page, size);
        Page<Kendaraan> hasil = repository
                .findByNoRegistrasiContainingIgnoreCaseAndNamaPemilikContainingIgnoreCase(
                        filterNoRegistrasi, filterNamaPemilik, pageable);

        List<KendaraanResponse> content = hasil.getContent().stream()
                .map(this::toResponse)
                .toList();

        return new PageResponse<>(
                content,
                hasil.getNumber(),
                hasil.getSize(),
                hasil.getTotalElements(),
                hasil.getTotalPages());
    }

    @Transactional(readOnly = true)
    public KendaraanResponse findByNoRegistrasi(String noRegistrasi) {
        Kendaraan kendaraan = repository.findById(noRegistrasi)
                .orElseThrow(() -> new DataNotFoundException(
                        "Data kendaraan dengan no registrasi " + noRegistrasi + " tidak ditemukan"));
        return toResponse(kendaraan);
    }

    @Transactional
    public KendaraanResponse create(KendaraanRequest request) {
        String noRegistrasi = request.getNoRegistrasi().trim();

        if (repository.existsById(noRegistrasi)) {
            throw new DuplicateDataException(
                    "No registrasi " + noRegistrasi + " sudah terdaftar, gunakan no registrasi lain");
        }

        validasiTahunPembuatan(request.getTahunPembuatan());

        Kendaraan kendaraan = new Kendaraan();
        kendaraan.setNoRegistrasi(noRegistrasi);
        applyRequest(kendaraan, request);

        return toResponse(repository.save(kendaraan));
    }

    /**
     * noRegistrasi bersifat immutable: nilai yang dipakai selalu berasal dari
     * path, sedangkan nilai noRegistrasi pada body request diabaikan. Dengan
     * begitu operasi update tidak pernah membuat baris baru.
     */
    @Transactional
    public KendaraanResponse update(String noRegistrasi, KendaraanRequest request) {
        Kendaraan kendaraan = repository.findById(noRegistrasi)
                .orElseThrow(() -> new DataNotFoundException(
                        "Data kendaraan dengan no registrasi " + noRegistrasi + " tidak ditemukan"));

        validasiTahunPembuatan(request.getTahunPembuatan());
        applyRequest(kendaraan, request);

        return toResponse(repository.save(kendaraan));
    }

    @Transactional
    public void delete(String noRegistrasi) {
        if (!repository.existsById(noRegistrasi)) {
            throw new DataNotFoundException(
                    "Data kendaraan dengan no registrasi " + noRegistrasi + " tidak ditemukan");
        }
        repository.deleteById(noRegistrasi);
    }

    /**
     * Urutan default noRegistrasi ascending. Tanpa urutan eksplisit, database
     * tidak menjamin urutan baris, sehingga pagination bisa menampilkan baris
     * yang sama di dua halaman atau melewatkannya sama sekali.
     */
    private Pageable buildPageable(int page, int size) {
        int halaman = Math.max(page, 0);
        int ukuran = size > 0 ? size : 10;
        return PageRequest.of(halaman, ukuran, Sort.by("noRegistrasi").ascending());
    }

    /**
     * Batas atas tahunPembuatan diperiksa di sini, bukan lewat anotasi, karena
     * anotasi Bean Validation hanya menerima nilai konstan sedangkan tahun
     * berjalan dihitung dinamis dari tanggal server.
     */
    private void validasiTahunPembuatan(Integer tahunPembuatan) {
        if (tahunPembuatan == null) {
            return;
        }
        int tahunBerjalan = Year.now().getValue();
        if (tahunPembuatan > tahunBerjalan) {
            throw new FieldValidationException("tahunPembuatan",
                    "Tahun pembuatan tidak boleh melebihi tahun berjalan (" + tahunBerjalan + ")");
        }
        if (tahunPembuatan < TAHUN_MINIMAL) {
            throw new FieldValidationException("tahunPembuatan",
                    "Tahun pembuatan minimal " + TAHUN_MINIMAL);
        }
    }

    private String normalize(String filter) {
        return filter == null ? "" : filter.trim();
    }

    private String trimToNull(String nilai) {
        if (nilai == null) {
            return null;
        }
        String hasil = nilai.trim();
        return hasil.isEmpty() ? null : hasil;
    }

    /**
     * Menyalin seluruh field yang boleh diubah dari request ke entity.
     * noRegistrasi sengaja tidak ikut disalin.
     */
    private void applyRequest(Kendaraan kendaraan, KendaraanRequest request) {
        kendaraan.setNamaPemilik(request.getNamaPemilik().trim());
        kendaraan.setAlamat(trimToNull(request.getAlamat()));
        kendaraan.setMerkKendaraan(trimToNull(request.getMerkKendaraan()));
        kendaraan.setTahunPembuatan(request.getTahunPembuatan());
        kendaraan.setKapasitasSilinder(request.getKapasitasSilinder());
        kendaraan.setWarnaKendaraan(trimToNull(request.getWarnaKendaraan()));
        kendaraan.setBahanBakar(trimToNull(request.getBahanBakar()));
    }

    private KendaraanResponse toResponse(Kendaraan kendaraan) {
        KendaraanResponse response = new KendaraanResponse();
        response.setNoRegistrasi(kendaraan.getNoRegistrasi());
        response.setNamaPemilik(kendaraan.getNamaPemilik());
        response.setAlamat(kendaraan.getAlamat());
        response.setMerkKendaraan(kendaraan.getMerkKendaraan());
        response.setTahunPembuatan(kendaraan.getTahunPembuatan());
        response.setKapasitasSilinder(kendaraan.getKapasitasSilinder());
        response.setWarnaKendaraan(kendaraan.getWarnaKendaraan());
        response.setBahanBakar(kendaraan.getBahanBakar());
        return response;
    }
}
