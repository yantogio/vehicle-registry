package com.kendaraan.repository;

import com.kendaraan.entity.Kendaraan;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface KendaraanRepository extends JpaRepository<Kendaraan, String> {

    /**
     * Pencarian partial match dan case-insensitive pada kedua filter sekaligus,
     * digabung dengan AND. Filter yang tidak diisi dikirim sebagai string kosong
     * oleh service, sehingga mencocokkan seluruh baris.
     */
    Page<Kendaraan> findByNoRegistrasiContainingIgnoreCaseAndNamaPemilikContainingIgnoreCase(
            String noRegistrasi, String namaPemilik, Pageable pageable);
}
