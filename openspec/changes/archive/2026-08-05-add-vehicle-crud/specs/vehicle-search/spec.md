## ADDED Requirements

### Requirement: Sistem SHALL menyediakan panel filter pada halaman monitoring

Sistem SHALL menampilkan panel filter pada halaman monitoring (`index.html`) yang berisi input No Registrasi, input Nama Pemilik, tombol Search, dan tombol Add.

#### Scenario: Panel filter tersedia
- **WHEN** pengguna membuka halaman monitoring
- **THEN** tampil input No Registrasi, input Nama Pemilik, tombol Search, dan tombol Add di atas tabel data

#### Scenario: Tombol Add membuka form mode add
- **WHEN** pengguna menekan tombol Add
- **THEN** browser berpindah ke `form.html?mode=add` dengan seluruh field kosong

### Requirement: Sistem SHALL mencari data dengan partial match yang case-insensitive pada `noRegistrasi` dan `namaPemilik`

Sistem SHALL mencocokkan filter `noRegistrasi` dan `namaPemilik` secara partial (mengandung) dan case-insensitive. Kedua filter dikirim ke `GET /api/vehicles?noRegistrasi=&namaPemilik=&page=&size=` dan MUST digabung dengan operator AND; filter yang kosong MUST diabaikan.

#### Scenario: Filter partial match pada No Registrasi
- **WHEN** pengguna mengisi No Registrasi dengan `7763` lalu menekan Search
- **THEN** tabel menampilkan baris `B-7763-TXY`

#### Scenario: Filter case-insensitive pada Nama Pemilik
- **WHEN** pengguna mengisi Nama Pemilik dengan `messi` lalu menekan Search
- **THEN** tabel menampilkan baris milik `Lionel Messi`

#### Scenario: Kedua filter digabung dengan AND
- **WHEN** pengguna mengisi No Registrasi dengan `B-` dan Nama Pemilik dengan `Ronaldo` lalu menekan Search
- **THEN** tabel hanya menampilkan baris yang memenuhi kedua kondisi sekaligus, yaitu `B-1678-BDG` milik Cristiano Ronaldo

#### Scenario: Filter kosong menampilkan semua data
- **WHEN** pengguna menekan Search dengan kedua input filter dibiarkan kosong
- **THEN** sistem menampilkan seluruh data kendaraan sesuai pagination yang berlaku

#### Scenario: Salah satu filter kosong
- **WHEN** pengguna mengisi hanya Nama Pemilik dengan `Honda` dan mengosongkan No Registrasi
- **THEN** sistem mengabaikan filter yang kosong dan hanya menyaring berdasarkan Nama Pemilik

#### Scenario: Parameter filter tidak dikirim sama sekali
- **WHEN** klien memanggil `GET /api/vehicles` tanpa menyertakan parameter `noRegistrasi` maupun `namaPemilik`
- **THEN** sistem memperlakukan kedua parameter yang bernilai null sebagai string kosong, lalu mengembalikan seluruh data dengan pagination normal, bukan hasil kosong

### Requirement: Sistem SHALL menampilkan hasil pencarian dalam tabel monitoring dengan kolom yang telah ditentukan

Sistem SHALL menampilkan hasil pencarian dalam tabel dengan kolom berurutan: No, No Registrasi, Nama Pemilik, Merk Kendaraan, Tahun Pembuatan, Kapasitas, Warna, Bahan Bakar, dan Action. Kolom Kapasitas MUST ditampilkan dengan sufiks ` cc`, dan kolom Action MUST berisi tautan Detail, Edit, serta Delete.

#### Scenario: Kolom tabel sesuai urutan
- **WHEN** pengguna membuka halaman monitoring
- **THEN** header tabel menampilkan kolom No, No Registrasi, Nama Pemilik, Merk Kendaraan, Tahun Pembuatan, Kapasitas, Warna, Bahan Bakar, dan Action secara berurutan

#### Scenario: Kapasitas ditampilkan dengan sufiks cc
- **WHEN** sebuah kendaraan memiliki `kapasitasSilinder` bernilai `150`
- **THEN** kolom Kapasitas menampilkan `150 cc`, sementara form tetap menampilkan angka polos `150`

#### Scenario: Kapasitas kosong
- **WHEN** sebuah kendaraan memiliki `kapasitasSilinder` bernilai null
- **THEN** kolom Kapasitas dibiarkan kosong tanpa menampilkan sufiks `cc`

#### Scenario: Kolom Action berisi tiga tautan
- **WHEN** tabel menampilkan sebuah baris data
- **THEN** kolom Action pada baris tersebut berisi tautan Detail, Edit, dan Delete yang masing-masing menyasar `noRegistrasi` baris itu

### Requirement: Sistem SHALL menomori baris tabel dengan nomor urut tampilan, bukan primary key

Sistem SHALL mengisi kolom No dengan nomor urut tampilan yang dihitung dari halaman aktif dan ukuran halaman. Nomor tersebut MUST NOT dipakai sebagai identitas data; identitas baris tetap `noRegistrasi`.

#### Scenario: Penomoran pada halaman pertama
- **WHEN** pengguna melihat halaman pertama dengan ukuran 10 baris
- **THEN** kolom No berisi 1 sampai 10 secara berurutan dari atas ke bawah

#### Scenario: Penomoran berlanjut di halaman berikutnya
- **WHEN** pengguna berpindah ke halaman kedua dengan ukuran 10 baris
- **THEN** baris pertama pada halaman tersebut bernomor 11, bukan 1

#### Scenario: Nomor urut bukan primary key
- **WHEN** kolom No menampilkan nilai `1`
- **THEN** nilai tersebut tidak dipakai sebagai identitas data; tautan Detail, Edit, dan Delete tetap memakai `noRegistrasi`

### Requirement: Sistem SHALL mengurutkan hasil pencarian berdasarkan `noRegistrasi` ascending secara default

Sistem SHALL menerapkan urutan default `noRegistrasi` ascending lewat `Sort` pada `Pageable` di sisi server. Urutan MUST bersifat deterministik agar pagination tidak menghasilkan baris ganda maupun baris yang terlewat.

#### Scenario: Urutan default pada halaman monitoring
- **WHEN** pengguna membuka halaman monitoring tanpa filter
- **THEN** baris ditampilkan terurut menaik berdasarkan `noRegistrasi`, dimulai dari `B-1678-BDG`

#### Scenario: Urutan konsisten antar pemanggilan
- **WHEN** klien memanggil `GET /api/vehicles` dua kali berturut-turut tanpa perubahan data
- **THEN** urutan baris pada kedua response identik

#### Scenario: Tidak ada baris ganda atau terlewat antar halaman
- **WHEN** pengguna menelusuri seluruh halaman hasil pencarian dari halaman pertama sampai terakhir
- **THEN** setiap baris data muncul tepat satu kali dan tidak ada baris yang terlewat

### Requirement: Sistem SHALL menerapkan pagination server-side dengan default 10 baris per halaman

Sistem SHALL menerapkan pagination di sisi server dengan ukuran halaman default 10 baris. Endpoint `GET /api/vehicles` menerima parameter `page` dan `size`, dan MUST mengembalikan hanya data satu halaman beserta informasi total data dan total halaman.

#### Scenario: Ukuran halaman default
- **WHEN** klien memanggil `GET /api/vehicles` tanpa parameter `page` dan `size`
- **THEN** sistem mengembalikan halaman pertama berisi maksimal 10 baris beserta informasi total data dan total halaman

#### Scenario: Pemotongan data dilakukan di server
- **WHEN** database berisi 25 baris dan klien meminta `page=0&size=10`
- **THEN** response hanya memuat 10 baris, bukan seluruh 25 baris yang dipotong di sisi browser

#### Scenario: Berpindah halaman
- **WHEN** pengguna menekan kontrol halaman 2 pada tabel monitoring
- **THEN** sistem memanggil ulang API dengan `page=1` dan tabel menampilkan baris ke-11 hingga ke-20

#### Scenario: Filter dipertahankan saat berpindah halaman
- **WHEN** pengguna melakukan pencarian lalu berpindah ke halaman berikutnya
- **THEN** nilai filter No Registrasi dan Nama Pemilik ikut dikirim ulang, sehingga hasil tetap tersaring

#### Scenario: Pencarian mengembalikan hasil ke halaman pertama
- **WHEN** pengguna sedang berada di halaman 2 lalu menekan Search dengan filter baru
- **THEN** sistem menampilkan hasil mulai dari halaman pertama

### Requirement: Sistem SHALL menampilkan pesan "Data tidak ditemukan" saat hasil pencarian kosong

Sistem SHALL menampilkan satu baris pesan `Data tidak ditemukan` di dalam tabel ketika hasil pencarian tidak memuat data apa pun. Pesan tersebut MUST hilang begitu pencarian berikutnya menghasilkan data.

#### Scenario: Hasil pencarian kosong
- **WHEN** pengguna mencari No Registrasi `ZZZZ` yang tidak cocok dengan data mana pun
- **THEN** tabel menampilkan satu baris pesan `Data tidak ditemukan` dan tidak menampilkan baris data

#### Scenario: Kontrol pagination saat hasil kosong
- **WHEN** hasil pencarian kosong
- **THEN** kontrol pagination tidak menawarkan halaman berikutnya

#### Scenario: Pesan hilang setelah pencarian menghasilkan data
- **WHEN** pengguna mengosongkan filter lalu menekan Search setelah sebelumnya melihat pesan `Data tidak ditemukan`
- **THEN** pesan tersebut hilang dan tabel kembali menampilkan baris data
