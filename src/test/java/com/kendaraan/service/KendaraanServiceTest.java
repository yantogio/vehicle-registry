package com.kendaraan.service;

import com.kendaraan.dto.KendaraanRequest;
import com.kendaraan.dto.KendaraanResponse;
import com.kendaraan.entity.Kendaraan;
import com.kendaraan.exception.DataNotFoundException;
import com.kendaraan.exception.DuplicateDataException;
import com.kendaraan.repository.KendaraanRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit test tingkat service. Repository di-mock dengan Mockito dan Spring context
 * sengaja tidak dinyalakan, sehingga test berjalan cepat dan hanya menguji
 * logika service.
 */
class KendaraanServiceTest {

    private KendaraanRepository repository;
    private KendaraanService service;

    @BeforeEach
    void setUp() {
        repository = mock(KendaraanRepository.class);
        service = new KendaraanService(repository);
    }

    private KendaraanRequest requestValid() {
        KendaraanRequest request = new KendaraanRequest();
        request.setNoRegistrasi("B-7763-TXY");
        request.setNamaPemilik("Lionel Messi");
        request.setAlamat("Jl. Achmad Yani No 89 Jakarta Pusat");
        request.setMerkKendaraan("Honda PCX");
        request.setTahunPembuatan(2018);
        request.setKapasitasSilinder(150);
        request.setWarnaKendaraan("Hitam");
        request.setBahanBakar("Bensin");
        return request;
    }

    @Test
    @DisplayName("create dengan data valid memanggil save dan mengembalikan data tersimpan")
    void createDenganDataValidTersimpan() {
        KendaraanRequest request = requestValid();
        when(repository.existsById("B-7763-TXY")).thenReturn(false);
        when(repository.save(any(Kendaraan.class))).thenAnswer(inv -> inv.getArgument(0));

        KendaraanResponse response = service.create(request);

        ArgumentCaptor<Kendaraan> tersimpan = ArgumentCaptor.forClass(Kendaraan.class);
        verify(repository).save(tersimpan.capture());

        assertThat(tersimpan.getValue().getNoRegistrasi()).isEqualTo("B-7763-TXY");
        assertThat(tersimpan.getValue().getNamaPemilik()).isEqualTo("Lionel Messi");
        assertThat(response.getNoRegistrasi()).isEqualTo("B-7763-TXY");
        assertThat(response.getMerkKendaraan()).isEqualTo("Honda PCX");
        assertThat(response.getWarnaKendaraan()).isEqualTo("Hitam");
    }

    @Test
    @DisplayName("create dengan noRegistrasi yang sudah ada melempar DuplicateDataException")
    void createDenganNoRegistrasiDuplikatDitolak() {
        KendaraanRequest request = requestValid();
        when(repository.existsById("B-7763-TXY")).thenReturn(true);

        assertThatThrownBy(() -> service.create(request))
                .isInstanceOf(DuplicateDataException.class)
                .hasMessageContaining("B-7763-TXY");

        verify(repository, never()).save(any(Kendaraan.class));
    }

    @Test
    @DisplayName("findByNoRegistrasi untuk data yang tidak ada melempar DataNotFoundException")
    void findByNoRegistrasiTidakDitemukan() {
        when(repository.findById("B-9999-ZZZ")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findByNoRegistrasi("B-9999-ZZZ"))
                .isInstanceOf(DataNotFoundException.class)
                .hasMessageContaining("B-9999-ZZZ");
    }
}
