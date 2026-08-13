# Vehicle Registry

Aplikasi CRUD data kendaraan bermotor: halaman monitoring dengan pencarian dan pagination,
form tambah/ubah/detail, serta penghapusan data dengan konfirmasi modal.

## Teknologi

| Lapisan | Teknologi |
|---|---|
| Backend | Java 17, Spring Boot 3.2, Spring Web, Spring Data JPA (Hibernate), Bean Validation |
| Database | H2 file-mode (`jdbc:h2:file:./data/kendaraan`), siap dipindah ke MySQL |
| Build | Maven (via Maven Wrapper) |
| Frontend | HTML statis, Bootstrap 5 dan jQuery 3.7 dari CDN |
| Komunikasi | REST JSON lewat jQuery AJAX |

Layering: **Controller → Service → Repository → Entity**, dengan DTO request/response terpisah
dan satu `@RestControllerAdvice` sebagai penangan error terpusat.

## Cara Menjalankan

**Prasyarat: hanya JDK 17.** Maven tidak perlu dipasang — Maven Wrapper mengunduhnya otomatis
saat pertama kali dijalankan.

Windows:

```
.\mvnw spring-boot:run
```

macOS / Linux:

```
./mvnw spring-boot:run
```

Bila `JAVA_HOME` di mesin Anda masih menunjuk ke JDK selain 17, arahkan dulu sebelum menjalankan
perintah di atas:

```powershell
# PowerShell
$env:JAVA_HOME = "C:\Program Files\Eclipse Adoptium\jdk-17.0.20.8-hotspot"
```

```cmd
REM Command Prompt
set "JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-17.0.20.8-hotspot"
```

Tunggu sampai muncul `Started Application in ... seconds`, lalu buka:

| Kegunaan | URL |
|---|---|
| Aplikasi (halaman monitoring) | http://localhost:8080/ |
| H2 console | http://localhost:8080/h2-console/ |

Kredensial H2 console: JDBC URL `jdbc:h2:file:./data/kendaraan`, user `sa`, password dikosongkan.

Hentikan aplikasi dengan `Ctrl+C`.

## Persistensi Data

Database H2 berjalan dalam mode file di `./data/kendaraan.mv.db`, sehingga **seluruh perubahan
Anda (tambah, ubah, hapus) bertahan setelah aplikasi dimatikan dan dijalankan ulang.**

Data awal disisipkan oleh kelas `DataSeeder` yang **hanya berjalan bila tabel masih kosong**
(`repository.count() == 0`). Konsekuensinya:

- Baris seed yang Anda hapus tidak akan muncul kembali saat restart.
- Perubahan pada baris seed tidak ditimpa saat restart.

Untuk mengembalikan data ke kondisi awal: hentikan aplikasi, hapus direktori `data/`,
lalu jalankan ulang aplikasi.

## Berpindah ke MySQL

Konfigurasi MySQL sudah disiapkan dalam bentuk komentar di
`src/main/resources/application.properties`. Langkahnya:

1. Nonaktifkan blok datasource H2 (beri tanda `#` pada barisnya).
2. Hapus tanda `#` pada blok MySQL di bagian bawah berkas, lalu sesuaikan nama database,
   username, dan password.
3. Aktifkan dependency `mysql-connector-j` di `pom.xml` (juga sudah tersedia sebagai komentar).
4. Sesuaikan tipe kolom pada `src/main/resources/schema.sql` bila diperlukan.

## REST API

Semua endpoint berada di bawah prefix `/api/vehicles`.

| Method | Endpoint | Keterangan |
|---|---|---|
| GET | `/api/vehicles?noRegistrasi=&namaPemilik=&page=&size=` | Pencarian + pagination. Semua parameter opsional; `page` default `0`, `size` default `10` |
| GET | `/api/vehicles/{noRegistrasi}` | Detail satu kendaraan |
| POST | `/api/vehicles` | Tambah data, balasan `201 Created` |
| PUT | `/api/vehicles/{noRegistrasi}` | Ubah data, balasan `200 OK` |
| DELETE | `/api/vehicles/{noRegistrasi}` | Hapus data, balasan `204 No Content` |

### Aturan status error

| Status | Kapan terjadi |
|---|---|
| `400 Bad Request` | Validasi gagal. Body memuat `fieldErrors` berisi pasangan nama field dan pesannya |
| `404 Not Found` | `noRegistrasi` tidak ditemukan pada GET detail, PUT, atau DELETE. Juga dipakai untuk URL yang tidak dikenali |
| `409 Conflict` | POST dengan `noRegistrasi` yang sudah terdaftar |
| `500 Internal Server Error` | Kegagalan tak terduga (jaring terakhir) |

Seluruh error memakai struktur body yang seragam:

```json
{
  "timestamp": "2026-08-05T11:47:15.746",
  "status": 400,
  "error": "Bad Request",
  "message": "Validasi gagal",
  "path": "/api/vehicles",
  "fieldErrors": {
    "namaPemilik": "Nama pemilik wajib diisi"
  }
}
```

`fieldErrors` hanya muncul pada kegagalan validasi.

### Aturan validasi

Validasi dijalankan **di sisi server**, bukan hanya lewat atribut HTML.

| Field | Aturan |
|---|---|
| `noRegistrasi` | Wajib, unik, primary key, tidak dapat diubah setelah tersimpan |
| `namaPemilik` | Wajib |
| `alamat` | Opsional, maksimal 500 karakter |
| `merkKendaraan` | Opsional |
| `tahunPembuatan` | Opsional, rentang 1900 s/d tahun berjalan |
| `kapasitasSilinder` | Opsional, harus angka positif |
| `warnaKendaraan` | Opsional, salah satu dari Merah, Hitam, Biru, Abu-Abu |
| `bahanBakar` | Opsional, teks bebas |

Pada operasi `PUT`, nilai `noRegistrasi` di body **diabaikan**; yang dipakai selalu nilai dari
path. Dengan begitu perubahan data tidak pernah membuat baris baru.

## Asumsi

### 1. Warna Kendaraan dropdown, Bahan Bakar teks bebas

Spesifikasi bertentangan dengan mockup-nya sendiri:

| Sumber | Warna Kendaraan | Bahan Bakar |
|---|---|---|
| Tabel requirement | Text | Dropdown `['Merah','Hitam','Biru','Abu-Abu']` |
| Mockup form | Dropdown ("Pilih Warna") | Text input berisi "Bensin" |

**Aplikasi ini mengikuti mockup:** Warna Kendaraan berupa dropdown 4 nilai, Bahan Bakar
berupa teks bebas. Alasannya:

1. Nilai dropdown pada tabel requirement — Merah, Hitam, Biru, Abu-Abu — jelas merupakan
   **nama warna**, bukan jenis bahan bakar. Isi datanya sendiri menunjukkan label kolomnya tertukar.
2. Mockup menampilkan placeholder "Pilih Warna" pada Warna Kendaraan (pola khas dropdown)
   dan nilai "Bensin" pada Bahan Bakar (pola khas text input).
3. Seed data yang diminta spesifikasi berisi Bahan Bakar = "Bensin", yang tidak ada di daftar
   dropdown tersebut. Jika daftar itu dipasang pada Bahan Bakar, seed data yang diminta
   spesifikasi itu sendiri tidak akan bisa disimpan.

### 2. Inkonsistensi warna B-7763-TXY

Spesifikasi menyebut warna kendaraan `B-7763-TXY` secara berbeda di dua tempat: tabel
Monitoring menulis **Hitam**, sedangkan mockup Edit menulis **Merah**. Seed data mengikuti
**tabel Monitoring**, karena tabel itulah yang memuat kelima baris data secara lengkap.

### 3. Nomor urut tampilan

Kolom "No" pada tabel monitoring adalah nomor urut tampilan yang dihitung dari halaman aktif
(`page * size + index + 1`), bukan ID maupun primary key. Baris pertama pada halaman kedua
bernomor 11. Tautan Detail, Edit, dan Delete tetap menyasar `noRegistrasi`.

### 4. Sufiks "cc" hanya presentasi

Kolom Kapasitas pada tabel monitoring ditampilkan sebagai `150 cc`, sedangkan pada form berupa
angka polos `150`. Sufiks tidak ikut tersimpan di database. Bila nilainya kosong, sufiks tidak
ditampilkan.

### 5. Tahun berjalan dihitung dinamis

Batas atas `tahunPembuatan` mengikuti tahun berjalan pada tanggal server, bukan angka
yang ditulis mati. Karena anotasi Bean Validation hanya menerima nilai konstan, batas bawah
dipasang lewat `@Min(1900)` sementara batas atas diperiksa di `KendaraanService` dan dilaporkan
dalam format error yang sama persis. Di sisi form, `maxlength` sengaja tidak dipakai pada input
Tahun Pembuatan karena browser mengabaikan atribut tersebut pada `type="number"`; batas 4 digit
dijaga lewat `min` dan `max`.

### 6. Urutan tampilan data

Data diurutkan berdasarkan `noRegistrasi` **ascending**, sehingga urutannya berbeda dari urutan
pada mockup spesifikasi. Spesifikasi tidak menyebutkan aturan pengurutan apa pun, sedangkan
pagination membutuhkan urutan yang deterministik — tanpa `ORDER BY` eksplisit, database tidak
menjamin urutan baris sehingga sebuah baris bisa muncul di dua halaman atau terlewat sama sekali.
Kolom timestamp yang bisa dipakai merekonstruksi urutan input tidak dibuat karena di luar scope.

### 7. Pagination dan teks jumlah data adalah penambahan yang disengaja

Mockup spesifikasi tidak menampilkan kontrol pagination sama sekali. Pagination server-side
(default 10 baris per halaman) beserta teks "Menampilkan X-Y dari Z data" tetap ditambahkan agar
tabel tetap terkendali saat jumlah data bertambah banyak.

### 8. Alamat seed data

Hanya alamat `B-7763-TXY` yang ditentukan spesifikasi (`Jl. Achmad Yani No 89 Jakarta Pusat`).
Alamat empat baris lainnya diisi nilai wajar buatan sendiri.

## Di Luar Scope

Tidak dikerjakan karena tidak diminta oleh spesifikasi: login/autentikasi, manajemen user & role,
upload foto kendaraan, soft delete, audit trail/timestamp, export Excel/PDF, multi-bahasa,
Docker, migrasi Flyway/Liquibase, CI/CD, dark mode, dan validasi format plat nomor Indonesia.

Pengujian otomatis dibatasi pada unit test tingkat service untuk tiga kasus inti
(lihat `src/test/java`); verifikasi selebihnya dilakukan lewat pengujian manual seluruh alur.

## Struktur Project

```
src/main/java/com/kendaraan/
├── Application.java                  entry point
├── controller/KendaraanController    REST endpoint
├── service/KendaraanService          logika bisnis, validasi tahun, aturan primary key
├── repository/KendaraanRepository    Spring Data JPA
├── entity/Kendaraan                  entity tabel kendaraan
├── dto/                              KendaraanRequest, KendaraanResponse, PageResponse, ErrorResponse
├── exception/                        exception khusus + GlobalExceptionHandler
└── config/DataSeeder                 data awal, hanya saat tabel kosong

src/main/resources/
├── application.properties            konfigurasi H2 aktif + MySQL sebagai komentar
├── schema.sql                        CREATE TABLE IF NOT EXISTS
└── static/
    ├── index.html                    halaman monitoring + modal hapus
    ├── form.html                     form add/edit/detail
    ├── css/app.css
    └── js/monitoring.js, js/form.js
```
