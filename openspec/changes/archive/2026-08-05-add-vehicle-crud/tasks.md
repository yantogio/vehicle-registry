## 1. Setup Project & Konfigurasi Database

- [x] 1.1 Buat project Maven Spring Boot 3 dengan Java 17 dan struktur `src/main/java` + `src/main/resources`
- [x] 1.2 Tambahkan dependency di `pom.xml`: `spring-boot-starter-web`, `spring-boot-starter-data-jpa`, `spring-boot-starter-validation`, `spring-boot-starter-test` (scope test), dan driver H2
- [x] 1.3 Buat kelas `Application` dengan anotasi `@SpringBootApplication` sebagai entry point
- [x] 1.4 Konfigurasi `application.properties`: datasource H2 file-mode `jdbc:h2:file:./data/kendaraan`, `spring.jpa.hibernate.ddl-auto=none`, `spring.sql.init.mode=always` (kini hanya menjalankan `schema.sql`, karena `data.sql` ditiadakan dan seeding dipindah ke `DataSeeder`), dan aktifkan H2 console
- [x] 1.5 Tambahkan blok konfigurasi MySQL dalam bentuk komentar di `application.properties` (url, username, password, driver, dialect) agar siap dipindah
- [x] 1.6 Buat `src/main/resources/schema.sql` memakai `CREATE TABLE IF NOT EXISTS kendaraan` (bukan DROP lalu CREATE, agar data bertahan antar restart) dengan `no_registrasi` sebagai PRIMARY KEY dan `alamat` bertipe `VARCHAR(500)` (bukan TEXT, karena H2 versi 2 memperketat tipe data dan VARCHAR menghindari urusan mapping CLOB ke String)
- [x] 1.7 Tambahkan `.gitignore` yang mengabaikan `target/` dan `data/`
- [x] 1.8 Verifikasi `mvn spring-boot:run` berjalan dan aplikasi start tanpa error

## 2. Entity, Repository, Service, dan Seed Data

- [x] 2.1 Buat entity `Kendaraan` dengan `@Entity`, `@Table(name = "kendaraan")`, `@Id` pada `noRegistrasi`, dan pemetaan kolom snake_case untuk kedelapan field
- [x] 2.2 Buat `KendaraanRepository extends JpaRepository<Kendaraan, String>` dengan method pencarian partial match case-insensitive untuk `noRegistrasi` dan `namaPemilik` yang mendukung `Pageable`
- [x] 2.3 Buat DTO request `KendaraanRequest` berisi kedelapan field beserta anotasi Bean Validation: `@NotBlank` pada `noRegistrasi` dan `namaPemilik`, `@Min(1900)` pada `tahunPembuatan` (batas ATAS tidak dipasang di anotasi karena anotasi hanya menerima nilai konstan — lihat 2.7), `@Positive` pada `kapasitasSilinder`; validasi `warnaKendaraan` MELOLOSKAN null dan string kosong karena field ini opsional, dan hanya menolak nilai di luar daftar Merah, Hitam, Biru, Abu-Abu
- [x] 2.4 Buat DTO response `KendaraanResponse` dan DTO halaman `PageResponse` berisi `content`, `page`, `size`, `totalElements`, dan `totalPages`
- [x] 2.5 Buat `KendaraanService` dengan operasi `search`, `findByNoRegistrasi`, `create`, `update`, dan `delete`, termasuk mapper entity ↔ DTO
- [x] 2.6 Terapkan aturan primary key di service: `create` menolak `noRegistrasi` yang sudah ada; `update` memakai `noRegistrasi` dari path dan mengabaikan nilai `noRegistrasi` pada body
- [x] 2.7 Terapkan batas atas `tahunPembuatan` di `KendaraanService`: bandingkan dengan tahun berjalan yang dihitung dinamis dari tanggal server, lalu lempar error validasi yang dipetakan handler ke HTTP 400 dengan struktur body per field yang identik dengan error Bean Validation, sehingga frontend tidak perlu membedakan sumber error
- [x] 2.8 Normalkan parameter filter di `KendaraanService.search`: nilai `null` pada `noRegistrasi` dan `namaPemilik` diubah menjadi string kosong sebelum diteruskan ke repository, agar `GET /api/vehicles` tanpa parameter tidak mengembalikan hasil kosong
- [x] 2.9 Bangun `Pageable` dengan `Sort.by("noRegistrasi").ascending()` sebagai urutan default, agar pagination deterministik dan tidak menghasilkan baris ganda atau terlewat
- [x] 2.10 Buat kelas `DataSeeder implements CommandLineRunner` yang menyisipkan kelima baris seed persis sesuai proposal HANYA bila `repository.count() == 0`, dengan alamat `Jl. Achmad Yani No 89 Jakarta Pusat` untuk `B-7763-TXY` dan alamat wajar untuk empat baris lainnya — tidak ada `data.sql`, agar baris seed yang dihapus atau diubah pengguna tidak muncul kembali saat restart

## 3. REST Controller, Validasi, dan Error Handler

- [x] 3.1 Buat `KendaraanController` dengan `@RestController` dan `@RequestMapping("/api/vehicles")`
- [x] 3.2 Implementasikan `GET /api/vehicles` dengan parameter `noRegistrasi`, `namaPemilik`, `page` (default 0), dan `size` (default 10), mengembalikan `PageResponse`
- [x] 3.3 Implementasikan `GET /api/vehicles/{noRegistrasi}` yang mengembalikan detail satu kendaraan
- [x] 3.4 Implementasikan `POST /api/vehicles` dengan `@Valid` pada request body, mengembalikan HTTP 201
- [x] 3.5 Implementasikan `PUT /api/vehicles/{noRegistrasi}` dengan `@Valid`, mengembalikan HTTP 200
- [x] 3.6 Implementasikan `DELETE /api/vehicles/{noRegistrasi}`
- [x] 3.7 Buat exception khusus `DataNotFoundException` dan `DuplicateDataException` (dinamai `DuplicateDataException`, bukan `DuplicateKeyException`, agar tidak tertukar saat import dengan `org.springframework.dao.DuplicateKeyException` yang sudah ada di Spring)
- [x] 3.8 Buat satu `@RestControllerAdvice` yang memetakan `MethodArgumentNotValidException` → 400 dengan daftar error per field, `DataNotFoundException` → 404, `DuplicateDataException` → 409, dan exception lain → 500, semuanya dengan struktur body error yang seragam
- [x] 3.9 Uji kelima endpoint lewat H2 console dan HTTP client (browser atau curl), termasuk jalur error 400, 404, dan 409

## 4. Halaman Monitoring, Pencarian, dan Pagination

- [x] 4.1 Buat `src/main/resources/static/index.html` dengan Bootstrap 5 dan jQuery 3.7 dari CDN
- [x] 4.2 Buat panel filter berisi input No Registrasi, input Nama Pemilik, tombol Search, dan tombol Add
- [x] 4.3 Buat kerangka tabel dengan kolom No, No Registrasi, Nama Pemilik, Merk Kendaraan, Tahun Pembuatan, Kapasitas, Warna, Bahan Bakar, dan Action
- [x] 4.4 Implementasikan pemanggilan `GET /api/vehicles` via jQuery AJAX dan render baris tabel secara dinamis
- [x] 4.5 Render kolom Kapasitas dengan sufiks ` cc`, dan biarkan kosong bila nilainya null
- [x] 4.6 Render kolom No sebagai nomor urut tampilan yang dihitung dari `page * size + index + 1`
- [x] 4.7 Render kolom Action berisi tautan Detail, Edit, dan Delete yang menyasar `noRegistrasi` baris bersangkutan
- [x] 4.8 Implementasikan kontrol pagination yang mengirim ulang nilai filter aktif, dan reset ke halaman pertama setiap kali tombol Search ditekan
- [x] 4.9 Tampilkan satu baris pesan `Data tidak ditemukan` saat hasil kosong, dan pastikan pesan hilang ketika pencarian berikutnya menghasilkan data

## 5. Halaman Form Add, Edit, dan Detail

- [x] 5.1 Buat `src/main/resources/static/form.html` dengan layout dua kolom: kiri berisi No. Registrasi Kendaraan, Nama Pemilik, Merk Kendaraan, dan Alamat Pemilik Kendaraan (textarea); kanan berisi Tahun Pembuatan, Kapasitas Silinder, Warna Kendaraan (dropdown), dan Bahan Bakar (text bebas)
- [x] 5.2 Isi dropdown Warna Kendaraan dengan placeholder "Pilih Warna" dan pilihan Merah, Hitam, Biru, Abu-Abu
- [x] 5.3 Baca query param `mode` dan `no` dari URL untuk menentukan perilaku halaman
- [x] 5.4 Implementasikan mode `add`: semua field kosong dan editable, tombol Simpan dan Kembali, submit ke `POST /api/vehicles`, dan setelah menerima response 201 lakukan redirect kembali ke `index.html`
- [x] 5.5 Implementasikan mode `edit`: muat data via `GET /api/vehicles/{no}`, buat field No. Registrasi readonly, tampilkan tombol Ubah dan Kembali, submit ke `PUT /api/vehicles/{no}`, dan setelah menerima response 200 lakukan redirect kembali ke `index.html`
- [x] 5.6 Implementasikan mode `detail`: muat data, disable/readonly seluruh field, dan tampilkan hanya tombol Kembali
- [x] 5.7 Tambahkan atribut HTML pendukung UX pada field numerik: `type="number"` dengan `min="1900"` pada Tahun Pembuatan dan nilai `max` diisi tahun berjalan lewat JavaScript agar batas 4 digit tetap terjaga dan konsisten dengan validasi server — atribut `maxlength` sengaja TIDAK dipakai di sini karena browser mengabaikannya pada input bertipe number; seluruh atribut ini hanya pendukung UX, bukan satu-satunya lapisan validasi
- [x] 5.8 Tampilkan pesan validasi dari response HTTP 400 tepat di bawah field yang bersangkutan, dan bersihkan pesan lama setiap kali submit ulang
- [x] 5.9 Implementasikan tombol Kembali untuk berpindah ke halaman monitoring tanpa menyimpan perubahan
- [x] 5.10 Tangani response 409 pada mode add dan 404 pada mode edit/detail dengan pesan yang terbaca pengguna, dan pastikan halaman TIDAK melakukan redirect saat submit gagal (400, 404, atau 409)

## 6. Modal Konfirmasi Hapus

- [x] 6.1 Tambahkan markup modal Bootstrap pada `index.html` beserta tombol OK dan Batal
- [x] 6.2 Isi teks modal secara dinamis dengan format persis `Anda yakin menghapus data {noRegistrasi} ?`
- [x] 6.3 Hubungkan tautan Delete pada setiap baris agar membuka modal dengan `noRegistrasi` baris tersebut
- [x] 6.4 Implementasikan tombol Batal untuk menutup modal tanpa mengirim request apa pun
- [x] 6.5 Implementasikan tombol OK untuk memanggil `DELETE /api/vehicles/{noRegistrasi}`, menutup modal, lalu me-refresh tabel
- [x] 6.6 Pastikan tidak ada pemakaian `confirm()` bawaan browser di seluruh file JavaScript

## 7. README

- [x] 7.1 Tulis `README.md` berisi ringkasan aplikasi dan daftar teknologi yang dipakai
- [x] 7.2 Tulis langkah menjalankan aplikasi dengan Maven Wrapper sebagai perintah utama: `.\mvnw spring-boot:run` (Windows) dan `./mvnw spring-boot:run` (macOS/Linux); tegaskan prasyaratnya HANYA JDK 17 karena Maven diunduh otomatis oleh wrapper dan tidak perlu dipasang; sertakan URL akses aplikasi dan H2 console
- [x] 7.3 Dokumentasikan cara berpindah ke MySQL dengan menunjuk blok konfigurasi yang dikomentari di `application.properties`
- [x] 7.4 Cantumkan daftar endpoint REST beserta aturan status error 400, 404, dan 409
- [x] 7.5 Cantumkan bagian "Asumsi" yang memuat keputusan Warna Kendaraan sebagai dropdown dan Bahan Bakar sebagai text bebas, lengkap dengan alasannya
- [x] 7.6 Cantumkan asumsi lain: nomor urut tampilan, sufiks `cc` sebagai presentasi, tahun berjalan yang dinamis, dan alamat seed data buatan sendiri
- [x] 7.7 Cantumkan asumsi urutan tampilan: data diurutkan `noRegistrasi` ascending sehingga berbeda dari urutan pada mockup, karena spesifikasi tidak menyebutkan aturan pengurutan sedangkan pagination membutuhkan urutan deterministik
- [x] 7.8 Jelaskan perilaku persistensi H2 file-mode: seluruh perubahan pengguna (tambah, ubah, hapus) bertahan antar restart karena `DataSeeder` hanya berjalan saat tabel kosong; sebutkan pula cara mengembalikan data ke kondisi awal, yaitu menghapus direktori `data/` lalu menjalankan ulang aplikasi
- [x] 7.9 Cantumkan asumsi inkonsistensi warna B-7763-TXY: tabel Monitoring pada spesifikasi menyebut warna `Hitam` sedangkan mockup Edit menyebut `Merah`; seed data mengikuti tabel Monitoring karena tabel itulah yang memuat kelima baris data secara lengkap
- [x] 7.10 Catat bahwa pagination server-side dan teks jumlah data ("Menampilkan X-Y dari Z data") adalah penambahan yang disengaja, karena mockup spesifikasi tidak menampilkan kontrol pagination sama sekali; alasannya agar tabel tetap terkendali saat jumlah data bertambah banyak

## 8. Unit Test Service (opsional — kerjakan hanya bila waktu tersisa setelah seluruh alur berfungsi)

- [x] 8.1 Buat kelas `KendaraanServiceTest` di `src/test/java` menggunakan Mockito untuk mem-mock `KendaraanRepository`, tanpa menyalakan Spring context
- [x] 8.2 Test: `create` dengan data valid memanggil `repository.save` dan mengembalikan DTO response berisi data tersimpan
- [x] 8.3 Test: `create` dengan `noRegistrasi` yang sudah ada melempar `DuplicateDataException`
- [x] 8.4 Test: `findByNoRegistrasi` dengan `noRegistrasi` yang tidak ada melempar `DataNotFoundException`
- [x] 8.5 Jalankan `mvn test` dan pastikan ketiga test lulus

## 9. Tes Manual Seluruh Alur

- [x] 9.1 Jalankan aplikasi dari kondisi bersih (hapus direktori `data/`) dan verifikasi `DataSeeder` menyisipkan lima baris seed persis sesuai spesifikasi
- [x] 9.2 Restart aplikasi tanpa mengubah apa pun dan pastikan `DataSeeder` tidak berjalan lagi, sehingga jumlah baris tetap lima dan tidak muncul error primary key
- [x] 9.3 **Uji persistensi file-mode**: tambahkan satu data baru lewat UI, hapus salah satu baris seed, ubah `namaPemilik` pada baris seed lain, lalu matikan dan jalankan ulang aplikasi — pastikan data baru MASIH ADA, baris yang dihapus TIDAK muncul kembali, dan perubahan `namaPemilik` tidak ditimpa
- [x] 9.4 Panggil `GET /api/vehicles` tanpa parameter filter apa pun dan pastikan seluruh data tampil, bukan hasil kosong
- [x] 9.5 Uji pencarian: partial match, case-insensitive, kombinasi AND dua filter, filter kosong, dan hasil kosong yang menampilkan `Data tidak ditemukan`
- [x] 9.6 Uji pagination: ukuran default 10, perpindahan halaman, penomoran yang berlanjut, dan filter yang bertahan antar halaman
- [x] 9.7 Uji determinisme urutan: tambahkan data hingga melebihi satu halaman, telusuri seluruh halaman, dan pastikan tidak ada baris yang muncul dua kali maupun terlewat
- [x] 9.8 Uji alur Add: simpan data valid dan pastikan halaman redirect ke `index.html` dengan data baru terlihat di tabel; lalu uji penolakan field wajib kosong, tahun 5 digit, tahun di luar rentang, kapasitas negatif, dan `noRegistrasi` duplikat (409) — pada kasus gagal halaman harus tetap di form
- [x] 9.9 Uji field opsional: simpan data dengan Warna Kendaraan dibiarkan kosong dan pastikan tersimpan tanpa error validasi
- [x] 9.10 Uji alur Edit: ubah data, pastikan field No. Registrasi readonly, `noRegistrasi` tidak berubah setelah disimpan, dan halaman redirect ke `index.html` dengan perubahan terlihat
- [x] 9.11 Uji alur Detail: seluruh field terkunci dan hanya tombol Kembali yang tersedia
- [x] 9.12 Uji alur Delete: teks modal sesuai persis, tombol Batal tidak menghapus apa pun, tombol OK menghapus lalu tabel ter-refresh
- [x] 9.13 Uji API langsung tanpa melalui halaman HTML untuk membuktikan validasi berjalan di sisi server
- [x] 9.14 Verifikasi tampilan kolom Kapasitas bersufiks ` cc` di tabel dan berupa angka polos di form
