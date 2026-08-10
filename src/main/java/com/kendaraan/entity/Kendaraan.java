package com.kendaraan.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "kendaraan")
public class Kendaraan {

    /**
     * Primary key. Wajib, unik, dan tidak boleh diubah setelah tersimpan.
     */
    @Id
    @Column(name = "no_registrasi", nullable = false, length = 50)
    private String noRegistrasi;

    @Column(name = "nama_pemilik", nullable = false, length = 100)
    private String namaPemilik;

    @Column(name = "alamat", length = 500)
    private String alamat;

    @Column(name = "merk_kendaraan", length = 100)
    private String merkKendaraan;

    @Column(name = "tahun_pembuatan")
    private Integer tahunPembuatan;

    @Column(name = "kapasitas_silinder")
    private Integer kapasitasSilinder;

    @Column(name = "warna_kendaraan", length = 20)
    private String warnaKendaraan;

    @Column(name = "bahan_bakar", length = 50)
    private String bahanBakar;

    public Kendaraan() {
    }

    public Kendaraan(String noRegistrasi, String namaPemilik, String alamat, String merkKendaraan,
                     Integer tahunPembuatan, Integer kapasitasSilinder, String warnaKendaraan,
                     String bahanBakar) {
        this.noRegistrasi = noRegistrasi;
        this.namaPemilik = namaPemilik;
        this.alamat = alamat;
        this.merkKendaraan = merkKendaraan;
        this.tahunPembuatan = tahunPembuatan;
        this.kapasitasSilinder = kapasitasSilinder;
        this.warnaKendaraan = warnaKendaraan;
        this.bahanBakar = bahanBakar;
    }

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
