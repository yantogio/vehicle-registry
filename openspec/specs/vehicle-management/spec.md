# vehicle-management Specification

## Purpose
Mengelola data kendaraan secara menyeluruh: struktur penyimpanan, seed data awal, operasi CRUD lewat REST API, validasi input di sisi server, penanganan error terpusat, serta halaman form dengan mode add, edit, dan detail beserta konfirmasi hapus.

## Requirements

### Requirement: Sistem SHALL menyimpan data kendaraan pada tabel `kendaraan` dengan `noRegistrasi` sebagai primary key

Sistem SHALL menyimpan data kendaraan pada tabel `kendaraan` yang memuat delapan field, dengan `noRegistrasi` sebagai primary key. `noRegistrasi` bertipe String, wajib diisi, unik, dan tidak boleh diubah setelah tersimpan. `namaPemilik` bertipe String dan wajib diisi. Field lainnya bersifat opsional: `alamat` (VARCHAR(500)), `merkKendaraan` (String), `tahunPembuatan` (Integer), `kapasitasSilinder` (Integer), `warnaKendaraan` (String), dan `bahanBakar` (String).

#### Scenario: Struktur tabel terbentuk saat aplikasi start
- **WHEN** aplikasi dijalankan dan `schema.sql` dieksekusi dengan `CREATE TABLE IF NOT EXISTS`
- **THEN** tabel `kendaraan` tersedia dengan kedelapan kolom di atas dan `no_registrasi` sebagai PRIMARY KEY, dan isi tabel yang sudah ada tidak terhapus

#### Scenario: Menyimpan kendaraan hanya dengan field wajib
- **WHEN** klien mengirim `POST /api/vehicles` berisi hanya `noRegistrasi` dan `namaPemilik`
- **THEN** data tersimpan dan sistem mengembalikan HTTP 201 dengan field opsional bernilai null

### Requirement: Sistem SHALL menyediakan seed data awal berisi lima baris kendaraan

Sistem SHALL memuat lima baris seed data persis sesuai spesifikasi lewat kelas `DataSeeder` yang mengimplementasikan `CommandLineRunner`. Penyisipan MUST dilakukan hanya bila tabel masih kosong (`repository.count() == 0`), sehingga seeding tidak pernah menimpa atau memunculkan kembali data yang sudah disentuh pengguna. Alamat untuk `B-7763-TXY` MUST bernilai `Jl. Achmad Yani No 89 Jakarta Pusat`; alamat baris lain diisi nilai wajar.

#### Scenario: Seed data tersedia setelah aplikasi start
- **WHEN** aplikasi dijalankan pertama kali dan pengguna membuka halaman monitoring tanpa filter
- **THEN** tabel menampilkan lima baris: `B-7763-TXY` (Lionel Messi, Honda PCX, 2018, 150, Hitam, Bensin), `B-1678-BDG` (Cristiano Ronaldo, Honda Vario, 2020, 150, Merah, Bensin), `B-3461-UPQ` (Bambang Pamungkas, Honda Beat, 2019, 125, Biru, Bensin), `B-3110-BGT` (Natasha Romanov, Honda Scoopy, 2020, 125, Hitam, Bensin), dan `B-7829-TYP` (Entis Siti Jubaidah, Honda Beat, 2019, 125, Merah, Bensin)

#### Scenario: Seed data tidak terduplikasi saat restart
- **WHEN** aplikasi dimatikan lalu dijalankan ulang sementara tabel sudah berisi data
- **THEN** `DataSeeder` tidak menyisipkan apa pun karena `repository.count()` bukan nol, sehingga jumlah baris tidak bertambah dan tidak terjadi error primary key duplicate

#### Scenario: Data buatan pengguna bertahan setelah restart
- **WHEN** pengguna menambahkan satu data kendaraan baru, lalu aplikasi dimatikan dan dijalankan ulang
- **THEN** data baru tersebut masih ada di database dan tampil pada tabel monitoring

#### Scenario: Baris seed yang dihapus tidak muncul kembali
- **WHEN** pengguna menghapus salah satu baris seed lalu aplikasi dijalankan ulang
- **THEN** baris tersebut tetap terhapus, karena tabel masih berisi data lain sehingga `DataSeeder` tidak berjalan

#### Scenario: Perubahan pada baris seed tidak ditimpa saat restart
- **WHEN** pengguna mengubah `namaPemilik` pada salah satu baris seed lalu aplikasi dijalankan ulang
- **THEN** nilai hasil perubahan tetap dipertahankan dan tidak dikembalikan ke nilai seed awal

#### Scenario: Seeding berjalan kembali pada database kosong
- **WHEN** seluruh data dihapus atau direktori `data/` dihapus, lalu aplikasi dijalankan ulang
- **THEN** `DataSeeder` menyisipkan kembali kelima baris seed karena `repository.count()` bernilai nol

### Requirement: Sistem SHALL membuat data kendaraan baru melalui `POST /api/vehicles`

Sistem SHALL menerima DTO request JSON pada `POST /api/vehicles`, memvalidasinya di sisi server, lalu menyimpan entity baru. Sistem MUST menolak `noRegistrasi` yang sudah terdaftar dengan HTTP 409.

#### Scenario: Pembuatan data berhasil
- **WHEN** klien mengirim `POST /api/vehicles` dengan `noRegistrasi` yang belum terdaftar dan seluruh field valid
- **THEN** sistem menyimpan data, mengembalikan HTTP 201 beserta DTO response berisi data tersimpan

#### Scenario: `noRegistrasi` sudah terdaftar
- **WHEN** klien mengirim `POST /api/vehicles` dengan `noRegistrasi` yang sudah ada di database
- **THEN** sistem menolak penyimpanan dan mengembalikan HTTP 409 dengan pesan jelas yang menyebutkan `noRegistrasi` tersebut sudah terdaftar

### Requirement: Sistem SHALL menampilkan detail satu kendaraan melalui `GET /api/vehicles/{noRegistrasi}`

Sistem SHALL mengembalikan detail lengkap satu kendaraan berdasarkan `noRegistrasi` pada path, dan MUST mengembalikan HTTP 404 bila data tidak ditemukan.

#### Scenario: Data ditemukan
- **WHEN** klien mengirim `GET /api/vehicles/B-7763-TXY`
- **THEN** sistem mengembalikan HTTP 200 dengan DTO response berisi kedelapan field kendaraan tersebut

#### Scenario: Data tidak ditemukan
- **WHEN** klien mengirim `GET /api/vehicles/B-9999-ZZZ` yang tidak ada di database
- **THEN** sistem mengembalikan HTTP 404 dengan pesan bahwa data tidak ditemukan

### Requirement: Sistem SHALL mengubah data kendaraan melalui `PUT /api/vehicles/{noRegistrasi}`

Sistem SHALL mengizinkan perubahan seluruh field selain `noRegistrasi`. `noRegistrasi` MUST bersifat immutable setelah tersimpan, sehingga sistem selalu memakai nilai dari path dan mengabaikan nilai `noRegistrasi` pada body request.

#### Scenario: Perubahan data berhasil
- **WHEN** klien mengirim `PUT /api/vehicles/B-7763-TXY` dengan `namaPemilik` baru dan field lain valid
- **THEN** sistem menyimpan perubahan dan mengembalikan HTTP 200 dengan DTO response berisi data terbaru

#### Scenario: Upaya mengubah `noRegistrasi` saat edit
- **WHEN** klien mengirim `PUT /api/vehicles/B-7763-TXY` dengan body yang memuat `noRegistrasi` bernilai `B-0000-AAA`
- **THEN** sistem mengabaikan nilai `noRegistrasi` pada body, memakai nilai dari path (`B-7763-TXY`), dan tidak membuat baris baru

#### Scenario: Data yang diubah tidak ditemukan
- **WHEN** klien mengirim `PUT /api/vehicles/B-9999-ZZZ` yang tidak ada di database
- **THEN** sistem mengembalikan HTTP 404 dan tidak membuat data baru

### Requirement: Sistem SHALL menghapus data kendaraan melalui `DELETE /api/vehicles/{noRegistrasi}`

Sistem SHALL menghapus baris kendaraan secara permanen (hard delete) berdasarkan `noRegistrasi` pada path, dan MUST mengembalikan HTTP 404 bila data tidak ditemukan.

#### Scenario: Penghapusan berhasil
- **WHEN** klien mengirim `DELETE /api/vehicles/B-7829-TYP` yang ada di database
- **THEN** sistem menghapus baris tersebut dan mengembalikan HTTP 204 No Content tanpa body, mengikuti konvensi standar bahwa operasi hapus yang sukses tidak perlu mengembalikan representasi apa pun

#### Scenario: Data yang dihapus tidak ditemukan
- **WHEN** klien mengirim `DELETE /api/vehicles/B-9999-ZZZ` yang tidak ada di database
- **THEN** sistem mengembalikan HTTP 404 dengan pesan bahwa data tidak ditemukan

### Requirement: Sistem SHALL memvalidasi seluruh input di sisi server, bukan hanya lewat atribut HTML

Sistem SHALL memvalidasi seluruh input pada DTO request di sisi server; atribut HTML MUST NOT menjadi satu-satunya lapisan validasi. Aturan: `noRegistrasi` wajib; `namaPemilik` wajib; `tahunPembuatan` maksimal 4 digit dengan rentang 1900 sampai tahun berjalan; `kapasitasSilinder` harus bilangan positif; `warnaKendaraan` harus salah satu dari Merah, Hitam, Biru, atau Abu-Abu; `alamat`, `merkKendaraan`, dan `bahanBakar` bebas dan opsional.

#### Scenario: Field wajib kosong
- **WHEN** klien mengirim `POST /api/vehicles` dengan `noRegistrasi` kosong dan `namaPemilik` kosong
- **THEN** sistem mengembalikan HTTP 400 dengan daftar error per field yang menyebutkan `noRegistrasi` dan `namaPemilik` wajib diisi

#### Scenario: Tahun pembuatan lebih dari 4 digit
- **WHEN** klien mengirim `tahunPembuatan` bernilai `20255`
- **THEN** sistem mengembalikan HTTP 400 dengan pesan error pada field `tahunPembuatan`

#### Scenario: Tahun pembuatan di bawah batas bawah
- **WHEN** klien mengirim `tahunPembuatan` bernilai `1899`
- **THEN** sistem mengembalikan HTTP 400 dengan pesan error pada field `tahunPembuatan`

#### Scenario: Tahun pembuatan melebihi tahun berjalan
- **WHEN** klien mengirim `tahunPembuatan` yang melebihi tahun berjalan pada tanggal server
- **THEN** sistem mengembalikan HTTP 400 dengan pesan error pada field `tahunPembuatan`, dalam format response yang identik dengan error validasi lainnya, meskipun batas atas ini diperiksa di service dan bukan lewat anotasi

#### Scenario: Kapasitas silinder tidak positif
- **WHEN** klien mengirim `kapasitasSilinder` bernilai `0` atau `-150`
- **THEN** sistem mengembalikan HTTP 400 dengan pesan error pada field `kapasitasSilinder`

#### Scenario: Warna kendaraan di luar daftar yang diizinkan
- **WHEN** klien mengirim `warnaKendaraan` bernilai `Hijau`
- **THEN** sistem mengembalikan HTTP 400 dengan pesan error pada field `warnaKendaraan`

#### Scenario: Warna kendaraan dikosongkan
- **WHEN** klien mengirim `warnaKendaraan` bernilai null atau string kosong sementara field wajib lainnya valid
- **THEN** sistem menyimpan data tanpa error validasi, karena `warnaKendaraan` bersifat opsional

#### Scenario: Field opsional lain dikosongkan
- **WHEN** klien mengirim `alamat`, `merkKendaraan`, `tahunPembuatan`, `kapasitasSilinder`, dan `bahanBakar` dalam keadaan kosong
- **THEN** sistem menyimpan data tanpa error validasi dan field tersebut bernilai null

#### Scenario: Validasi tetap berlaku pada request yang melewati form
- **WHEN** request invalid dikirim langsung ke API tanpa melalui halaman HTML
- **THEN** sistem tetap menolak dengan HTTP 400 dan daftar error per field

### Requirement: Sistem SHALL menangani seluruh error API lewat satu handler terpusat dengan format response konsisten

Sistem SHALL menangani seluruh error API lewat satu kelas `@RestControllerAdvice` yang memetakan setiap jenis kegagalan ke status HTTP dan body error yang seragam. Setiap response error MUST memakai struktur body yang sama.

#### Scenario: Format error validasi
- **WHEN** terjadi kegagalan validasi
- **THEN** response HTTP 400 memuat daftar error per field, masing-masing berisi nama field dan pesannya

#### Scenario: Format error data tidak ditemukan
- **WHEN** operasi menyasar `noRegistrasi` yang tidak ada
- **THEN** response HTTP 404 memuat pesan error dalam struktur yang sama dengan error lain

#### Scenario: Format error duplikat primary key
- **WHEN** operasi create menyasar `noRegistrasi` yang sudah terdaftar
- **THEN** response HTTP 409 memuat pesan error dalam struktur yang sama dengan error lain

### Requirement: Sistem SHALL menyediakan halaman form tunggal dengan tiga mode add, edit, dan detail

Sistem SHALL menyediakan satu halaman `form.html` yang melayani mode add, edit, dan detail, dibedakan lewat query param `?mode=add|edit|detail&no={noRegistrasi}`. Layout dua kolom MUST mengikuti mockup: kolom kiri berisi No. Registrasi Kendaraan, Nama Pemilik, Merk Kendaraan, dan Alamat Pemilik Kendaraan (textarea); kolom kanan berisi Tahun Pembuatan, Kapasitas Silinder, Warna Kendaraan (dropdown), dan Bahan Bakar (text bebas).

#### Scenario: Mode add
- **WHEN** pengguna membuka `form.html?mode=add`
- **THEN** seluruh field kosong dan editable, serta tombol yang tampil adalah Simpan dan Kembali

#### Scenario: Mode edit
- **WHEN** pengguna membuka `form.html?mode=edit&no=B-7763-TXY`
- **THEN** seluruh field terisi data kendaraan tersebut, field No. Registrasi Kendaraan bersifat readonly, dan tombol yang tampil adalah Ubah dan Kembali

#### Scenario: Mode detail
- **WHEN** pengguna membuka `form.html?mode=detail&no=B-7763-TXY`
- **THEN** seluruh field terisi dan disabled/readonly, serta tombol yang tampil hanya Kembali

#### Scenario: Pesan validasi tampil per field
- **WHEN** pengguna menekan Simpan pada mode add dengan Nama Pemilik kosong dan server membalas HTTP 400
- **THEN** pesan error ditampilkan tepat di bawah field Nama Pemilik, bukan sebagai satu pesan global

#### Scenario: Warna kendaraan berupa dropdown, bahan bakar berupa text
- **WHEN** pengguna membuka form pada mode add
- **THEN** field Warna Kendaraan berupa dropdown berisi pilihan Merah, Hitam, Biru, dan Abu-Abu, sedangkan field Bahan Bakar berupa text input bebas

#### Scenario: Kembali ke halaman monitoring
- **WHEN** pengguna menekan tombol Kembali pada mode mana pun
- **THEN** browser berpindah ke halaman monitoring tanpa menyimpan perubahan apa pun

#### Scenario: Redirect setelah simpan berhasil pada mode add
- **WHEN** pengguna menekan Simpan pada mode add dengan data valid dan server membalas HTTP 201
- **THEN** halaman melakukan redirect ke `index.html`, dan data yang baru dibuat terlihat pada tabel monitoring

#### Scenario: Redirect setelah ubah berhasil pada mode edit
- **WHEN** pengguna menekan Ubah pada mode edit dengan data valid dan server membalas HTTP 200
- **THEN** halaman melakukan redirect ke `index.html`, dan perubahan terlihat pada tabel monitoring

#### Scenario: Tetap di halaman form saat submit gagal
- **WHEN** submit dibalas HTTP 400, 404, atau 409
- **THEN** halaman tidak melakukan redirect, dan pesan error ditampilkan agar pengguna dapat memperbaiki input

### Requirement: Sistem SHALL meminta konfirmasi lewat modal Bootstrap sebelum menghapus data

Sistem SHALL meminta konfirmasi lewat modal Bootstrap sebelum menghapus data, dan MUST NOT memakai dialog `confirm()` bawaan browser.

#### Scenario: Modal konfirmasi muncul
- **WHEN** pengguna menekan tautan Delete pada baris `B-7763-TXY`
- **THEN** modal Bootstrap tampil dengan teks persis `Anda yakin menghapus data B-7763-TXY ?` beserta tombol OK dan Batal

#### Scenario: Batal menghapus
- **WHEN** pengguna menekan tombol Batal pada modal konfirmasi
- **THEN** modal tertutup, tidak ada request DELETE yang dikirim, dan data tetap utuh di tabel

#### Scenario: Konfirmasi menghapus
- **WHEN** pengguna menekan tombol OK pada modal konfirmasi
- **THEN** sistem mengirim `DELETE /api/vehicles/{noRegistrasi}`, menutup modal, lalu me-refresh tabel monitoring tanpa baris yang dihapus

#### Scenario: Tidak memakai dialog bawaan browser
- **WHEN** pengguna menekan tautan Delete
- **THEN** yang muncul adalah modal Bootstrap di dalam halaman, bukan dialog `confirm()` bawaan browser
