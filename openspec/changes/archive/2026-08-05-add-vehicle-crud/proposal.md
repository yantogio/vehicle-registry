## Why

Ini adalah technical test rekrutmen: membangun "Aplikasi Data Kendaraan", sebuah aplikasi CRUD data kendaraan bermotor dengan deadline kurang dari 24 jam. Yang dinilai bukan kecanggihan fitur, melainkan **ketelitian membaca requirement**, **kerapian layering**, dan **kelengkapan alur** — sehingga proposal ini sengaja mengunci scope seminimal mungkin sesuai spesifikasi, dan mencatat setiap asumsi secara eksplisit agar dapat ditelusuri penilai.

## What Changes

- Membuat aplikasi web CRUD data kendaraan bermotor dengan satu tabel `kendaraan` (primary key `noRegistrasi`).
- **Halaman Monitoring** (`index.html`): panel filter (No Registrasi, Nama Pemilik) + tombol Search dan Add, tabel data dengan kolom No (nomor urut tampilan), No Registrasi, Nama Pemilik, Merk Kendaraan, Tahun Pembuatan, Kapasitas, Warna, Bahan Bakar, Action (Detail / Edit / Delete).
- **Pencarian & pagination**: partial match, case-insensitive, kedua filter digabung dengan AND, filter kosong menampilkan semua data; pagination server-side default 10 baris per halaman; pesan "Data tidak ditemukan" saat hasil kosong.
- **Halaman Form** (`form.html`): satu file untuk tiga mode, dibedakan lewat query param `?mode=add|edit|detail&no={noRegistrasi}`, layout dua kolom sesuai mockup, dengan pesan validasi per field.
- **Hapus data** lewat modal Bootstrap berteks persis `Anda yakin menghapus data {noRegistrasi} ?` dengan tombol OK dan Batal — tanpa `confirm()` bawaan browser.
- **REST API JSON** lima endpoint (`GET` list, `GET` detail, `POST`, `PUT`, `DELETE`) dengan aturan error 409 (duplikat primary key), 404 (data tidak ditemukan), dan 400 (daftar error per field).
- **Seed data** 5 baris persis sesuai spesifikasi, disisipkan lewat kelas `DataSeeder` (`CommandLineRunner`) hanya bila tabel masih kosong; struktur tabel dibuat lewat `schema.sql`.

## Capabilities

### New Capabilities

- `vehicle-management`: create, read (detail), update, delete, validasi field, aturan primary key `noRegistrasi` (wajib, unik, immutable), serta seed data awal.
- `vehicle-search`: filter berdasarkan `noRegistrasi` dan `namaPemilik`, pagination server-side, penomoran urut tampilan, dan kondisi hasil pencarian kosong.

### Modified Capabilities

Tidak ada. Ini adalah aplikasi baru dari nol; belum ada spec existing di `openspec/specs/`.

## Asumsi & Klarifikasi Requirement

### Ambiguitas: Warna Kendaraan vs Bahan Bakar

Spesifikasi **bertentangan dengan mockup-nya sendiri**:

| Sumber | Warna Kendaraan | Bahan Bakar |
|---|---|---|
| Tabel requirement spesifikasi | Text | Dropdown, nilai `['Merah','Hitam','Biru','Abu-Abu']` |
| Mockup form | Dropdown ("Pilih Warna") | Text input, berisi `Bensin` |

**Keputusan: mengikuti MOCKUP.** Warna Kendaraan = dropdown dengan 4 nilai (Merah, Hitam, Biru, Abu-Abu); Bahan Bakar = text input bebas.

**Alasan:**

1. Nilai dropdown yang tertulis di tabel requirement — Merah, Hitam, Biru, Abu-Abu — jelas merupakan **nama warna**, bukan jenis bahan bakar. Isi datanya sendiri membuktikan bahwa label kolom pada tabel requirement tertukar.
2. Mockup menampilkan placeholder "Pilih Warna" pada field Warna Kendaraan (pola khas dropdown) dan nilai literal "Bensin" pada field Bahan Bakar (pola khas text input) — konsisten dengan pembacaan di poin 1.
3. Seed data yang diminta spesifikasi berisi Warna = Hitam/Merah/Biru (semuanya ada di daftar dropdown) dan Bahan Bakar = Bensin (tidak ada di daftar dropdown). Jika daftar itu dipasang pada Bahan Bakar, seed data yang diminta spesifikasi sendiri tidak akan bisa disimpan.

Ketiga bukti mengarah ke kesimpulan yang sama, sehingga tabel requirement dinilai sebagai salah ketik (kolom tertukar) dan mockup dipakai sebagai sumber kebenaran.

### Asumsi lain

- **Nomor urut tabel**: kolom "No" adalah nomor urut tampilan yang mengikuti halaman aktif (baris pertama halaman 2 dengan size 10 bernomor 11), bukan ID atau primary key.
- **Sufiks "cc"**: kolom Kapasitas ditampilkan sebagai `150 cc` di tabel monitoring, sedangkan form hanya menerima dan menampilkan angka polos (`150`). Sufiks murni urusan presentasi, bukan bagian data tersimpan.
- **Tahun berjalan**: batas atas `tahunPembuatan` dihitung dinamis dari tanggal server saat validasi, bukan angka yang di-hardcode. Karena anotasi Bean Validation hanya menerima nilai konstan, batas bawah dipasang lewat `@Min(1900)` sementara batas atas diperiksa di `KendaraanService` dan dilaporkan dalam format error yang identik dengan error Bean Validation.
- **Alamat seed data**: hanya `B-7763-TXY` yang alamatnya ditentukan spesifikasi (`Jl. Achmad Yani No 89 Jakarta Pusat`); alamat empat baris lainnya diisi nilai wajar buatan sendiri.
- **Field opsional**: semua field selain `noRegistrasi` dan `namaPemilik` boleh kosong/null, sesuai tabel requirement. Konsekuensinya, validasi `warnaKendaraan` meloloskan nilai kosong dan hanya menolak nilai di luar keempat pilihan.
- **Urutan tampilan data**: data diurutkan berdasarkan `noRegistrasi` ascending, diterapkan lewat `Sort` pada `Pageable` di sisi server. Urutan ini **berbeda dari urutan pada mockup spesifikasi**. Alasannya: spesifikasi tidak menyebutkan aturan pengurutan apa pun, sedangkan pagination membutuhkan urutan yang deterministik — tanpa `ORDER BY` eksplisit, database tidak menjamin urutan baris, sehingga baris yang sama bisa muncul di dua halaman atau terlewat sama sekali. Kolom timestamp yang bisa dipakai merekonstruksi urutan input sudah masuk Non-Goals.

## Keputusan Teknis

| Area | Keputusan | Alasan |
|---|---|---|
| Backend | Java 17, Spring Boot 3, Spring Web, Spring Data JPA (Hibernate), Bean Validation | Stack standar industri; Bean Validation memberi validasi server-side deklaratif tanpa kode manual |
| Database | H2 file-mode (`jdbc:h2:file:./data/kendaraan`) + `schema.sql` untuk struktur tabel dan `DataSeeder` untuk data awal; konfigurasi MySQL disertakan dalam bentuk komentar di `application.properties` | Penilai bisa menjalankan aplikasi tanpa memasang database apa pun, tapi tetap terlihat siap dipindah ke MySQL |
| Build | Maven | Konvensi umum Spring Boot; sekali `mvn spring-boot:run` |
| Frontend | HTML statis di `src/main/resources/static`, Bootstrap 5 + jQuery 3.7 dari CDN | Tanpa npm dan tanpa build tool, sehingga tidak ada langkah instalasi tambahan bagi penilai; Bootstrap memberi layout dua kolom dan modal siap pakai |
| Tanpa Thymeleaf / SPA | Halaman statis + REST JSON via jQuery AJAX | Memisahkan tegas frontend dan backend; menghindari waktu setup framework di deadline < 24 jam |
| Layering | Controller → Service → Repository → Entity, DTO request/response terpisah, satu `@RestControllerAdvice` | Kerapian layering adalah kriteria penilaian eksplisit; DTO mencegah entity bocor ke API; error handler terpusat menjamin format error konsisten |

## Non-Goals

Berikut sengaja **tidak** dikerjakan, karena tidak diminta spesifikasi dan berisiko memakan waktu pada deadline < 24 jam:

- Login / autentikasi
- Manajemen user & role
- Upload foto kendaraan
- Soft delete
- Audit trail / kolom timestamp (createdAt, updatedAt)
- Export Excel / PDF
- Multi-bahasa (i18n)
- Docker / containerisasi
- Migrasi database dengan Flyway atau Liquibase
- Unit test menyeluruh dan integration test; yang dikerjakan hanya unit test tingkat service untuk tiga kasus inti (create valid, create duplikat, pencarian data tidak ditemukan), dan itu pun bersifat opsional — dikerjakan hanya bila waktu tersisa setelah seluruh alur berfungsi. Verifikasi utama tetap smoke test manual seluruh alur.
- CI/CD pipeline
- Dark mode
- Validasi format plat nomor Indonesia (`noRegistrasi` cukup divalidasi wajib-diisi dan unik)

## Risiko & Mitigasi

| Risiko | Dampak | Mitigasi |
|---|---|---|
| Asumsi Warna vs Bahan Bakar ditolak penilai | Dianggap salah baca requirement | Asumsi ditulis eksplisit di proposal dan diulang di README, lengkap dengan tiga alasannya, sehingga terbaca sebagai keputusan sadar, bukan kelalaian |
| `noRegistrasi` sebagai natural primary key | `PUT` yang mengubah primary key akan membuat baris baru, bukan mengubah baris lama | Service menolak perubahan `noRegistrasi`; nilai di body request diabaikan dan `noRegistrasi` dari path yang dipakai; field dibuat readonly di form mode edit |
| Deadline < 24 jam | Fitur inti tidak selesai | Urutan tasks dibuat menaik: backend + REST dulu (nilai layering), baru UI; Non-Goals dikunci di depan agar tidak ada scope creep |
| Skrip inisialisasi dijalankan ulang setiap kali aplikasi start | Data pengguna hilang saat restart, seed data tergandakan, atau baris seed yang sudah dihapus muncul kembali sehingga fitur delete terlihat tidak bekerja | `schema.sql` memakai `CREATE TABLE IF NOT EXISTS` (bukan DROP lalu CREATE) sehingga tabel dan isinya bertahan; seeding dipindah dari `data.sql` ke kelas `DataSeeder` (`CommandLineRunner`) yang hanya menyisipkan bila `repository.count() == 0`, sehingga penghapusan dan perubahan oleh pengguna bersifat permanen; `ddl-auto` tetap `none` agar Hibernate tidak ikut campur |
| Validasi hanya di HTML (maxlength, required) | Data invalid tetap bisa masuk lewat request langsung | Bean Validation di DTO sisi server sebagai sumber kebenaran; atribut HTML hanya pemanis UX |
| Modal delete tertukar dengan `confirm()` bawaan | Requirement eksplisit dilanggar | Ditulis sebagai skenario spec tersendiri dan masuk checklist tes manual |

## Impact

- **Kode baru** (belum ada kode sama sekali di repo ini):
  - `pom.xml`, `src/main/java/...` (Application, Entity, Repository, Service, Controller, DTO, `DataSeeder`, Exception, `@RestControllerAdvice`)
  - `src/main/resources/application.properties` dan `schema.sql`
  - `src/main/resources/static/index.html`, `form.html`, dan file JS pendukung
  - `README.md` (cara menjalankan + daftar asumsi)
- **API baru**: lima endpoint di bawah prefix `/api/vehicles`.
- **Dependensi**: Spring Boot starter web, starter data-jpa, starter validation, starter test (untuk unit test service opsional), dan driver H2 (Maven); Bootstrap 5 dan jQuery 3.7 lewat CDN (runtime browser).
- **Artefak runtime**: direktori `./data/` berisi file database H2 — perlu masuk `.gitignore`.
- **Tidak ada** breaking change; tidak ada sistem existing yang terpengaruh.
