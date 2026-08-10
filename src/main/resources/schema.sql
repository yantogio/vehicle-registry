-- Struktur tabel kendaraan.
-- Memakai CREATE TABLE IF NOT EXISTS (bukan DROP lalu CREATE) agar data yang
-- sudah tersimpan tetap bertahan setiap kali aplikasi dijalankan ulang.
CREATE TABLE IF NOT EXISTS kendaraan (
    no_registrasi      VARCHAR(50)  NOT NULL,
    nama_pemilik       VARCHAR(100) NOT NULL,
    alamat             VARCHAR(500),
    merk_kendaraan     VARCHAR(100),
    tahun_pembuatan    INTEGER,
    kapasitas_silinder INTEGER,
    warna_kendaraan    VARCHAR(20),
    bahan_bakar        VARCHAR(50),
    CONSTRAINT pk_kendaraan PRIMARY KEY (no_registrasi)
);
