package com.kendaraan.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/**
 * DTO request untuk operasi create dan update.
 *
 * Catatan validasi tahunPembuatan: batas bawah dipasang lewat anotasi
 * {@code @Min(1900)}, sedangkan batas atas (tahun berjalan) diperiksa di
 * KendaraanService karena anotasi Bean Validation hanya menerima nilai konstan.
 */
public class KendaraanRequest {

    @NotBlank(message = "No registrasi kendaraan wajib diisi")
    @Size(max = 50, message = "No registrasi kendaraan maksimal 50 karakter")
    private String noRegistrasi;

    @NotBlank(message = "Nama pemilik wajib diisi")
    @Size(max = 100, message = "Nama pemilik maksimal 100 karakter")
    private String namaPemilik;

    @Size(max = 500, message = "Alamat maksimal 500 karakter")
    private String alamat;

    @Size(max = 100, message = "Merk kendaraan maksimal 100 karakter")
    private String merkKendaraan;

    @Min(value = 1900, message = "Tahun pembuatan minimal 1900")
    private Integer tahunPembuatan;

    @Positive(message = "Kapasitas silinder harus berupa angka positif")
    private Integer kapasitasSilinder;

    /**
     * Field opsional. Pola di bawah meloloskan string kosong, dan {@code @Pattern}
     * sendiri sudah meloloskan null, sehingga field ini tidak menjadi wajib.
     */
    @Pattern(regexp = "^(Merah|Hitam|Biru|Abu-Abu)?$",
            message = "Warna kendaraan harus salah satu dari: Merah, Hitam, Biru, Abu-Abu")
    private String warnaKendaraan;

    @Size(max = 50, message = "Bahan bakar maksimal 50 karakter")
    private String bahanBakar;

    public String getNoRegistrasi() {
        return noRegistrasi;
    }

    public void setNoRegistrasi(String noRegistrasi) {
        this.noRegistrasi = noRegistrasi;
    }

    public String getNamaPemilik() {
        return namaPemilik;
    }

    public void setNamaPemilik(String namaPemilik) {
        this.namaPemilik = namaPemilik;
    }

    public String getAlamat() {
        return alamat;
    }

    public void setAlamat(String alamat) {
        this.alamat = alamat;
    }

    public String getMerkKendaraan() {
        return merkKendaraan;
    }

    public void setMerkKendaraan(String merkKendaraan) {
        this.merkKendaraan = merkKendaraan;
    }

    public Integer getTahunPembuatan() {
        return tahunPembuatan;
    }

    public void setTahunPembuatan(Integer tahunPembuatan) {
        this.tahunPembuatan = tahunPembuatan;
    }

    public Integer getKapasitasSilinder() {
        return kapasitasSilinder;
    }

    public void setKapasitasSilinder(Integer kapasitasSilinder) {
        this.kapasitasSilinder = kapasitasSilinder;
    }

    public String getWarnaKendaraan() {
        return warnaKendaraan;
    }

    public void setWarnaKendaraan(String warnaKendaraan) {
        this.warnaKendaraan = warnaKendaraan;
    }

    public String getBahanBakar() {
        return bahanBakar;
    }

    public void setBahanBakar(String bahanBakar) {
        this.bahanBakar = bahanBakar;
    }
}
