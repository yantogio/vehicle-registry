/* Halaman monitoring: pencarian, pagination server-side, dan render tabel. */

$(function () {

    var API_URL = '/api/vehicles';
    var UKURAN_HALAMAN = 10;

    // Filter yang sedang aktif. Disimpan terpisah dari isi input agar
    // perpindahan halaman memakai filter yang benar-benar sudah di-Search,
    // bukan teks yang baru diketik tapi belum ditekan Search.
    var filterAktif = {
        noRegistrasi: '',
        namaPemilik: ''
    };
    var halamanAktif = 0;

    function escapeHtml(nilai) {
        if (nilai === null || nilai === undefined) {
            return '';
        }
        return String(nilai)
            .replace(/&/g, '&amp;')
            .replace(/</g, '&lt;')
            .replace(/>/g, '&gt;')
            .replace(/"/g, '&quot;')
            .replace(/'/g, '&#39;');
    }

    function tampilkanKosong(nilai) {
        return (nilai === null || nilai === undefined || nilai === '') ? '' : escapeHtml(nilai);
    }

    // Kapasitas ditampilkan dengan sufiks " cc" di tabel, sementara di form
    // tetap angka polos. Bila nilainya kosong, sufiks tidak ikut ditampilkan.
    function formatKapasitas(nilai) {
        if (nilai === null || nilai === undefined || nilai === '') {
            return '';
        }
        return escapeHtml(nilai) + ' cc';
    }

    function muatData(halaman, sesudahMuat) {
        halamanAktif = halaman;

        $.ajax({
            url: API_URL,
            method: 'GET',
            dataType: 'json',
            data: {
                noRegistrasi: filterAktif.noRegistrasi,
                namaPemilik: filterAktif.namaPemilik,
                page: halaman,
                size: UKURAN_HALAMAN
            }
        }).done(function (hasil) {
            renderTabel(hasil);
            renderPagination(hasil);
            renderInfo(hasil);
            if (sesudahMuat) {
                sesudahMuat(hasil);
            }
        }).fail(function () {
            $('#tabelBody').html(
                '<tr><td colspan="9" class="baris-kosong">Gagal memuat data, silakan coba lagi.</td></tr>');
            $('#pagination').empty();
            $('#infoData').text('');
        });
    }

    function renderTabel(hasil) {
        var body = $('#tabelBody');
        body.empty();

        if (!hasil.content || hasil.content.length === 0) {
            body.append('<tr><td colspan="9" class="baris-kosong">Data tidak ditemukan</td></tr>');
            return;
        }

        $.each(hasil.content, function (index, item) {
            // Nomor urut tampilan, bukan primary key. Dihitung dari halaman
            // aktif agar penomoran berlanjut di halaman berikutnya.
            var nomorUrut = (hasil.page * hasil.size) + index + 1;
            var noRegistrasi = encodeURIComponent(item.noRegistrasi);

            var baris = '<tr>' +
                '<td class="sel-tengah">' + nomorUrut + '</td>' +
                '<td>' + tampilkanKosong(item.noRegistrasi) + '</td>' +
                '<td>' + tampilkanKosong(item.namaPemilik) + '</td>' +
                '<td>' + tampilkanKosong(item.merkKendaraan) + '</td>' +
                '<td class="sel-tengah">' + tampilkanKosong(item.tahunPembuatan) + '</td>' +
                '<td>' + formatKapasitas(item.kapasitasSilinder) + '</td>' +
                '<td>' + tampilkanKosong(item.warnaKendaraan) + '</td>' +
                '<td>' + tampilkanKosong(item.bahanBakar) + '</td>' +
                '<td class="sel-tengah">' +
                    '<a href="form.html?mode=detail&no=' + noRegistrasi + '" class="aksi-link aksi-detail">Detail</a>' +
                    '<a href="form.html?mode=edit&no=' + noRegistrasi + '" class="aksi-link aksi-edit">Edit</a>' +
                    '<a href="#" class="aksi-link aksi-delete" data-no="' + escapeHtml(item.noRegistrasi) + '">Delete</a>' +
                '</td>' +
                '</tr>';

            body.append(baris);
        });
    }

    function renderPagination(hasil) {
        var nav = $('#pagination');
        nav.empty();

        // Saat hasil kosong, kontrol pagination tidak menawarkan halaman apa pun.
        if (!hasil.totalPages || hasil.totalPages <= 1) {
            return;
        }

        function itemHalaman(label, targetHalaman, aktif, nonaktif) {
            var kelas = 'page-item';
            if (aktif) {
                kelas += ' active';
            }
            if (nonaktif) {
                kelas += ' disabled';
            }
            return '<li class="' + kelas + '">' +
                '<a class="page-link" href="#" data-halaman="' + targetHalaman + '">' + label + '</a></li>';
        }

        nav.append(itemHalaman('&laquo;', hasil.page - 1, false, hasil.page === 0));
        for (var i = 0; i < hasil.totalPages; i++) {
            nav.append(itemHalaman(i + 1, i, i === hasil.page, false));
        }
        nav.append(itemHalaman('&raquo;', hasil.page + 1, false, hasil.page >= hasil.totalPages - 1));
    }

    function renderInfo(hasil) {
        if (!hasil.totalElements) {
            $('#infoData').text('');
            return;
        }
        var dari = (hasil.page * hasil.size) + 1;
        var sampai = dari + hasil.content.length - 1;
        $('#infoData').text('Menampilkan ' + dari + '-' + sampai + ' dari ' + hasil.totalElements + ' data');
    }

    $('#btnSearch').on('click', function () {
        filterAktif.noRegistrasi = $.trim($('#filterNoRegistrasi').val());
        filterAktif.namaPemilik = $.trim($('#filterNamaPemilik').val());
        // Pencarian baru selalu dimulai dari halaman pertama.
        muatData(0);
    });

    // Menekan Enter di kolom filter sama dengan menekan tombol Search.
    $('#filterNoRegistrasi, #filterNamaPemilik').on('keypress', function (e) {
        if (e.which === 13) {
            $('#btnSearch').trigger('click');
        }
    });

    $('#btnAdd').on('click', function () {
        window.location.href = 'form.html?mode=add';
    });

    $('#pagination').on('click', 'a.page-link', function (e) {
        e.preventDefault();
        if ($(this).closest('li').hasClass('disabled') || $(this).closest('li').hasClass('active')) {
            return;
        }
        // Filter aktif ikut terkirim ulang, sehingga hasil tetap tersaring.
        muatData(parseInt($(this).data('halaman'), 10));
    });

    /* ---------- Hapus data ---------- */

    var modalHapus = new bootstrap.Modal(document.getElementById('modalHapus'));
    var noRegistrasiDihapus = null;

    function tampilkanPesanAksi(pesan, jenis) {
        $('#pesanAksi')
            .removeClass('d-none alert-success alert-danger')
            .addClass(jenis === 'gagal' ? 'alert-danger' : 'alert-success')
            .text(pesan);

        setTimeout(function () {
            $('#pesanAksi').addClass('d-none');
        }, 4000);
    }

    /**
     * Setelah penghapusan, tabel dimuat ulang pada halaman yang sedang dibuka
     * dengan filter yang masih aktif. Bila baris terakhir di halaman terakhir
     * yang dihapus, halaman itu menjadi kosong padahal data masih ada, sehingga
     * tampilan dimundurkan satu halaman.
     */
    function refreshSesudahHapus() {
        muatData(halamanAktif, function (hasil) {
            var halamanKosong = hasil.content.length === 0;
            var masihAdaData = hasil.totalElements > 0;

            if (halamanKosong && masihAdaData && halamanAktif > 0) {
                muatData(halamanAktif - 1);
            }
        });
    }

    $('#tabelBody').on('click', 'a.aksi-delete', function (e) {
        e.preventDefault();
        noRegistrasiDihapus = $(this).data('no');
        // Teks konfirmasi mengikuti mockup persis, termasuk spasi sebelum "?".
        $('#modalHapusLabel').text('Anda yakin menghapus data ' + noRegistrasiDihapus + ' ?');
        modalHapus.show();
    });

    $('#btnHapusOk').on('click', function () {
        if (!noRegistrasiDihapus) {
            return;
        }

        var target = noRegistrasiDihapus;
        $(this).prop('disabled', true);

        $.ajax({
            url: API_URL + '/' + encodeURIComponent(target),
            method: 'DELETE'
        }).done(function () {
            modalHapus.hide();
            tampilkanPesanAksi('Data ' + target + ' berhasil dihapus.', 'sukses');
            refreshSesudahHapus();
        }).fail(function (xhr) {
            // Modal tetap ditutup agar tidak menggantung terbuka saat gagal.
            modalHapus.hide();

            var pesan = (xhr.responseJSON && xhr.responseJSON.message)
                ? xhr.responseJSON.message
                : 'Gagal menghapus data, silakan coba lagi.';
            tampilkanPesanAksi(pesan, 'gagal');

            // Tabel tetap dimuat ulang, misalnya karena data sudah dihapus
            // dari tab lain sehingga baris tersebut memang perlu hilang.
            refreshSesudahHapus();
        }).always(function () {
            $('#btnHapusOk').prop('disabled', false);
            noRegistrasiDihapus = null;
        });
    });

    muatData(0);
});
