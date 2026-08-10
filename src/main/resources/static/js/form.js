/* Halaman form: satu file untuk tiga mode add, edit, dan detail.
   Mode ditentukan lewat query param ?mode=add|edit|detail&no={noRegistrasi}. */

$(function () {

    var API_URL = '/api/vehicles';
    var HALAMAN_MONITORING = 'index.html';

    // Daftar field yang dipetakan dua arah antara form dan DTO.
    var FIELDS = ['noRegistrasi', 'namaPemilik', 'merkKendaraan', 'alamat',
        'tahunPembuatan', 'kapasitasSilinder', 'warnaKendaraan', 'bahanBakar'];

    var JUDUL = {
        add: 'Tambah Data Kendaraan',
        edit: 'Edit Data Kendaraan',
        detail: 'Detail Data Kendaraan'
    };

    function paramUrl(nama) {
        return new URLSearchParams(window.location.search).get(nama);
    }

    var mode = paramUrl('mode');
    var noRegistrasiParam = paramUrl('no');

    function tampilkanPesanGlobal(pesan) {
        $('#pesanGlobal').text(pesan).removeClass('d-none');
    }

    function sembunyikanPesanGlobal() {
        $('#pesanGlobal').addClass('d-none').text('');
    }

    function bersihkanErrorField() {
        $('.pesan-error').text('');
        $('.form-control, .form-select').removeClass('is-invalid');
    }

    function tampilkanErrorField(fieldErrors) {
        $.each(fieldErrors, function (namaField, pesan) {
            var kotakPesan = $('#error-' + namaField);
            if (kotakPesan.length) {
                kotakPesan.text(pesan);
                $('#' + namaField).addClass('is-invalid');
            } else {
                // Field tidak dikenal di form: tampilkan sebagai pesan halaman
                // agar informasinya tidak hilang begitu saja.
                tampilkanPesanGlobal(namaField + ': ' + pesan);
            }
        });
    }

    /** Nilai null ditampilkan sebagai input kosong, bukan tulisan "null". */
    function nilaiTampil(nilai) {
        return (nilai === null || nilai === undefined) ? '' : nilai;
    }

    function isiForm(data) {
        $.each(FIELDS, function (i, field) {
            $('#' + field).val(nilaiTampil(data[field]));
        });
        // Dropdown warna ikut terpilih sesuai data. Bila nilainya null, val('')
        // membuat placeholder "Pilih Warna" yang tampil.
        $('#warnaKendaraan').val(nilaiTampil(data.warnaKendaraan));
    }

    function ambilDataForm() {
        var data = {};
        $.each(FIELDS, function (i, field) {
            var nilai = $.trim($('#' + field).val());
            if (field === 'tahunPembuatan' || field === 'kapasitasSilinder') {
                // Field numerik yang dikosongkan dikirim sebagai null, bukan
                // string kosong, agar tetap valid sebagai field opsional.
                data[field] = nilai === '' ? null : parseInt(nilai, 10);
            } else {
                data[field] = nilai === '' ? null : nilai;
            }
        });
        return data;
    }

    function kunciSeluruhField() {
        $('#areaForm').find('input, textarea, select').prop('disabled', true);
    }

    function tanganiGagalSubmit(xhr) {
        var respons = xhr.responseJSON;

        if (respons && respons.fieldErrors) {
            tampilkanErrorField(respons.fieldErrors);
            // Pesan ringkas di atas form agar kegagalan langsung terlihat
            // walau field yang bermasalah berada di bawah layar.
            tampilkanPesanGlobal('Periksa kembali isian yang ditandai merah.');
            return;
        }

        if (respons && respons.message) {
            tampilkanPesanGlobal(respons.message);
            return;
        }

        tampilkanPesanGlobal('Terjadi kesalahan saat menyimpan data, silakan coba lagi.');
    }

    function kembaliKeMonitoring() {
        window.location.href = HALAMAN_MONITORING;
    }

    function muatData(noRegistrasi, sesudahMuat) {
        $.ajax({
            url: API_URL + '/' + encodeURIComponent(noRegistrasi),
            method: 'GET',
            dataType: 'json'
        }).done(function (data) {
            isiForm(data);
            if (sesudahMuat) {
                sesudahMuat();
            }
        }).fail(function (xhr) {
            var pesan = (xhr.responseJSON && xhr.responseJSON.message)
                ? xhr.responseJSON.message
                : 'Gagal memuat data kendaraan.';
            tampilkanPesanGlobal(pesan);
            // Form dikunci agar pengguna tidak menyunting data yang gagal dimuat.
            kunciSeluruhField();
            $('#btnSimpan').addClass('d-none');
        });
    }

    function siapkanModeAdd() {
        $('#judulForm').text(JUDUL.add);
        $('#btnSimpan').text('Simpan');

        $('#formKendaraan').on('submit', function (e) {
            e.preventDefault();
            bersihkanErrorField();
            sembunyikanPesanGlobal();

            $.ajax({
                url: API_URL,
                method: 'POST',
                contentType: 'application/json',
                data: JSON.stringify(ambilDataForm())
            }).done(function () {
                kembaliKeMonitoring();
            }).fail(tanganiGagalSubmit);
        });
    }

    function siapkanModeEdit() {
        $('#judulForm').text(JUDUL.edit);
        $('#btnSimpan').text('Ubah');

        // readonly, BUKAN disabled: field disabled tidak ikut terkirim saat
        // submit, sedangkan @Valid pada PUT mewajibkan noRegistrasi ada di body.
        $('#noRegistrasi').prop('readonly', true).addClass('field-terkunci');

        muatData(noRegistrasiParam);

        $('#formKendaraan').on('submit', function (e) {
            e.preventDefault();
            bersihkanErrorField();
            sembunyikanPesanGlobal();

            $.ajax({
                url: API_URL + '/' + encodeURIComponent(noRegistrasiParam),
                method: 'PUT',
                contentType: 'application/json',
                data: JSON.stringify(ambilDataForm())
            }).done(function () {
                kembaliKeMonitoring();
            }).fail(tanganiGagalSubmit);
        });
    }

    function siapkanModeDetail() {
        $('#judulForm').text(JUDUL.detail);
        // Tidak ada submit pada mode ini, sehingga seluruh field boleh disabled.
        $('#btnSimpan').addClass('d-none');

        muatData(noRegistrasiParam, kunciSeluruhField);
    }

    /** Batas atas tahun mengikuti tahun berjalan, sejalan dengan validasi server. */
    function siapkanBatasTahun() {
        $('#tahunPembuatan').attr('max', new Date().getFullYear());
    }

    function mulai() {
        siapkanBatasTahun();
        $('#btnKembali').on('click', kembaliKeMonitoring);

        if (!JUDUL[mode]) {
            $('#judulForm').text('Data Kendaraan');
            tampilkanPesanGlobal(
                'Mode halaman tidak dikenali. Gunakan tautan Add, Detail, atau Edit ' +
                'dari halaman monitoring.');
            kunciSeluruhField();
            $('#btnSimpan').addClass('d-none');
            return;
        }

        if ((mode === 'edit' || mode === 'detail') && !noRegistrasiParam) {
            $('#judulForm').text(JUDUL[mode]);
            tampilkanPesanGlobal(
                'No registrasi kendaraan tidak disertakan, sehingga data tidak dapat dimuat. ' +
                'Kembali ke halaman monitoring lalu pilih data yang ingin dibuka.');
            kunciSeluruhField();
            $('#btnSimpan').addClass('d-none');
            return;
        }

        if (mode === 'add') {
            siapkanModeAdd();
        } else if (mode === 'edit') {
            siapkanModeEdit();
        } else {
            siapkanModeDetail();
        }
    }

    mulai();
});
