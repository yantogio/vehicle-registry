package com.kendaraan.config;

import com.kendaraan.entity.Kendaraan;
import com.kendaraan.repository.KendaraanRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Menyisipkan lima baris data awal HANYA bila tabel masih kosong.
 *
 * Pendekatan ini dipilih menggantikan data.sql dengan MERGE INTO: dengan MERGE,
 * baris seed yang sudah dihapus atau diubah pengguna akan muncul kembali setiap
 * restart, sehingga fitur delete terlihat tidak bekerja.
 *
 * Untuk mengembalikan data ke kondisi awal: hentikan aplikasi, hapus direktori
 * ./data, lalu jalankan ulang aplikasi.
 */
@Component
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final KendaraanRepository repository;

    public DataSeeder(KendaraanRepository repository) {
        this.repository = repository;
    }

    @Override
    public void run(String... args) {
        if (repository.count() != 0) {
            log.info("Seed data dilewati, tabel kendaraan sudah berisi data.");
            return;
        }

        repository.saveAll(List.of(
                new Kendaraan("B-7763-TXY", "Lionel Messi",
                        "Jl. Achmad Yani No 89 Jakarta Pusat",
                        "Honda PCX", 2018, 150, "Hitam", "Bensin"),
                new Kendaraan("B-1678-BDG", "Cristiano Ronaldo",
                        "Jl. Merdeka No 12 Bandung",
                        "Honda Vario", 2020, 150, "Merah", "Bensin"),
                new Kendaraan("B-3461-UPQ", "Bambang Pamungkas",
                        "Jl. Diponegoro No 45 Semarang",
                        "Honda Beat", 2019, 125, "Biru", "Bensin"),
                new Kendaraan("B-3110-BGT", "Natasha Romanov",
                        "Jl. Sudirman No 7 Jakarta Selatan",
                        "Honda Scoopy", 2020, 125, "Hitam", "Bensin"),
                new Kendaraan("B-7829-TYP", "Entis Siti Jubaidah",
                        "Jl. Pahlawan No 23 Bekasi",
                        "Honda Beat", 2019, 125, "Merah", "Bensin")));

        log.info("Seed data berhasil disisipkan: 5 baris kendaraan.");
    }
}
