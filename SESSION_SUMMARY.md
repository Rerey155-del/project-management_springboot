# 📝 Dokumentasi & Rangkuman Sesi Pengembangan (NodeWave Enterprise Portal)

**Terakhir Diperbarui:** 8 Oktober 2026  
**Workspace:** `/media/rereypc/Local Disk G/xampp/htdocs/springboot/springboot`  
**Tech Stack:** Java 21, Spring Boot 3.3.4, Spring Security, Spring Data JPA, H2 / PostgreSQL, JJWT 0.12.6, Springdoc OpenAPI 3.

---

## 📌 Status Riwayat Percakapan
Seluruh riwayat obrolan dan tanya-jawab telah tersimpan secara permanen di Antigravity IDE. File ini merangkum seluruh alur arsitektur, panduan eksekusi file, user flow, panduan testing Swagger, serta script presentasi untuk rekruter.

---

## 🏛️ 1. Arsitektur Proyek: Package-by-Feature (Vertical Slice Architecture)

Berbeda dari arsitektur *Layered* tradisional yang rawan coupling, proyek ini menggunakan **Vertical Slice Architecture** di mana kode dipisahkan berdasarkan fitur dan peran pengguna:

```text
src/main/java/com/nodewave/portal/
├── PortalApplication.java                      # Main Entrypoint (@SpringBootApplication)
│
├── core/                                       # Shared Infrastructure & Domain
│   ├── config/OpenApiConfig.java               # Konfigurasi Swagger UI (JWT Bearer definition)
│   ├── entity/                                 # JPA Entities: User, Project, Task, TaskDependency, AuditLog
│   ├── repository/                             # Repositories: UserRepository, TaskRepository, dll.
│   ├── security/                               # JwtTokenProvider, UserPrincipal, JwtAuthenticationFilter, SecurityConfig
│   └── seeder/DataSeeder.java                  # CommandLineRunner: Mengisi 5 User, 1 Project, 6 Tasks awal
│
├── exception/                                  # Centralized Exception Handling
│   ├── GlobalExceptionHandler.java             # @RestControllerAdvice (400, 401, 403, 404, 409, 500)
│   └── Custom Exceptions                       # TaskBlockedException, OptimisticLockConflictException, dll.
│
└── feature/                                    # 🍕 VERTICAL SLICES
    ├── auth/                                   # Login & User Profile (/api/auth/login, /api/me)
    ├── pm/                                     # Product Manager Slice (Projects, Dependencies, Standup Summary)
    ├── internal/                               # Internal Engineer Slice (Update Task, Upload Attachment)
    ├── client/                                 # Client Slice (Masked Client Task Views)
    ├── task/                                   # Task Query Slice (Multi-keyword Search, Metrics, Dynamic Masking)
    └── common/                                 # Common Slice (Healthcheck, Root Info)
```

---

## 🔐 2. Akun Pengujian & Matriks Hak Akses (Role-Based Access Control)

Password seluruh akun: **`password123`**

| Role | Email | Nama Akun | Aturan Bisnis & Validasi Khusus |
|---|---|---|---|
| **PM** | `pm@nodewave.id` | Budi (Project Manager) | Mengatur dependensi tugas, melihat standup summary, **dilarang menandai task `DONE`** (`403 Forbidden`). |
| **INTERNAL** | `designer@nodewave.id` | Rian (UI/UX Designer) | Mengunggah attachment desain, menyelesaikan Task 1 miliknya. |
| **INTERNAL** | `frontend@nodewave.id` | Rerey (Frontend Engineer) | Mengelola Task 4. **Terkunci (*Blocked*)** jika Task 1 belum `DONE`. Dilarang ubah task orang lain (`403`). Dilarang ubah deskripsi task (`403`). |
| **INTERNAL** | `backend@nodewave.id` | Joko (Backend Engineer) | Mengelola Task 2 & Task 3 miliknya sendiri. |
| **CLIENT** | `client@nodewave.id` | PT Mitra Sukses (Client) | Hanya melihat task dengan `isClientVisible: true`. Identitas engineer, avatar, dan attachment disembunyikan otomatis (Data Masking). |

---

## 🗺️ 3. User Flow (Alur Perjalanan Proyek di Dunia Nyata)

```text
┌─────────────────────────────────────────────────────────────────────────────┐
│                       ALUR PERJALANAN PROYEK DI DUNIA NYATA                 │
└─────────────────────────────────────────────────────────────────────────────┘

 [TAHAP 1] 👔 PRODUCT MANAGER (Budi)
   ├── 1. Login ke sistem.
   ├── 2. Mengatur aturan dependensi:
   │      "Tugas Frontend TERKUNCI sampai tugas Desain UI selesai!"
   └── 3. Memilih tugas mana yang boleh diintip Klien (Client-Visible).
   │
   ▼
 [TAHAP 2] ⚛️ FRONTEND ENGINEER (Rerey)
   └── Coba mulai tugas Frontend duluan...
       ❌ DITOLAK SISTEM! (Error 400: "Task is BLOCKED!")
          Alasan: Desain UI belum berstatus DONE.
   │
   ▼
 [TAHAP 3] 🎨 UI/UX DESIGNER (Rian)
   ├── 1. Mengubah status tugasnya jadi "IN_PROGRESS".
   ├── 2. Mengunggah bukti link desain Figma (Upload Attachment).
   └── 3. Menyelesaikan tugas desain menjadi "DONE".
   │
   ▼
 [TAHAP 4] ⚛️ FRONTEND ENGINEER (Mencoba Lagi)
   └── Coba ubah status tugas Frontend ke "IN_PROGRESS" lagi...
       ✅ BERHASIL! (Status 200 OK)
          Alasan: Karena syarat tugas Desain UI sudah resmi DONE!
   │
   ▼
 [TAHAP 5] 👔 REKAP HARIAN OLEH PM
   └── PM membuka menu Standup Summary:
       - Sistem otomatis merekap pekerjaan 24 jam terakhir.
       - PM dilarang klik tombol DONE (hanya engineer yang berhak).
   │
   ▼
 [TAHAP 6] 🏢 KLIEN (PT Mitra Sukses)
   └── Klien login dan membuka dashboard:
       - Hanya melihat ringkasan: "50% Selesai".
       - Nama engineer, email, dan file rahasia disembunyikan (Data Masking).
```

---

## 📦 4. Alur Eksekusi File (File-by-File Request Lifecycle)

Bagaimana sebuah HTTP Request diproses dari awal hingga menghasilkan raw JSON Response:

```text
 [1] CLIENT / SWAGGER UI
      │  Mengirim HTTP PATCH /api/tasks/{id}
      │  Membawa: Header Token JWT & Body JSON: {"status": "IN_PROGRESS", "version": 1}
      │
      ▼
 [2] JwtAuthenticationFilter.java  (🛡️ Pintu Gerbang Keamanan)
      │  - Membaca header Authorization: Bearer <token>
      │  - Validasi token via JwtTokenProvider.java
      │  - Bungkus User ID & Role ke objek UserPrincipal.java -> Simpan di SecurityContext
      │
      ▼
 [3] UpdateTaskRequest.java  (📋 DTO / Kontrak Data)
      │  - Deserialisasi JSON client ke objek Java record UpdateTaskRequest
      │  - Validasi tipe data dan constraints
      │
      ▼
 [4] InternalTaskController.java  (🚪 Controller)
      │  - Menerima request di method @PatchMapping("/tasks/{id}")
      │  - Ambil parameter id, DTO request, dan user login (UserPrincipal)
      │  - Meneruskan tugas ke Service: internalTaskService.updateTask(id, request, principal)
      │
      ▼
 [5] InternalTaskService.java  (🧠 Service Layer: Logika Bisnis & Validasi)
      │  ├── a. TaskRepository.java: Mengambil entitas Task.java dari database
      │  ├── b. Validasi ABAC: Apakah user adalah assignee dari task? (Jika bukan -> 403)
      │  ├── c. Validasi Role: PM dilarang set DONE, Engineer dilarang ubah deskripsi
      │  ├── d. Validasi Blocker: TaskDependencyRepository.java cek syarat tugas (Jika belum DONE -> 400)
      │  └── e. Validasi Optimistic Lock: Cek versi data saat ini (Jika tidak cocok -> 409)
      │
      ├── [JIKA VALIDASI GAGAL] ──► GlobalExceptionHandler.java (Ubah jadi JSON Error 400/403/409)
      │
      └── [JIKA SEMUA LOLOS]
           ├── Catat riwayat ke AuditLogRepository.java
           └── Simpan perubahan status ke TaskRepository.java
           │
           ▼
 [6] RAW JSON RESPONSE (200 OK)
      Jackson Converter menserialisasi hasil akhir menjadi JSON payload ke browser/client.
```

---

## 🎮 5. Panduan Pengujian di Swagger UI (`http://localhost:8080/swagger-ui.html`)

### A. Autentikasi Awal
1. Buka tag **`1. Feature Slice: Auth`** ➔ `POST /api/auth/login`.
2. Klik **`Try it out`** ➔ Masukkan email (`pm@nodewave.id`) dan password (`password123`) ➔ Klik **`Execute`**.
3. Salin teks `"token"` dari Response Body.
4. Klik tombol hijau **Authorize 🔓** di kanan atas Swagger ➔ Tempelkan token di kolom Value ➔ Klik **Authorize** ➔ **Close**.

### B. Menguji Update Status Task (200 OK)
1. Buka tag **`3. Feature Slice: Internal Engineer`** ➔ **`PATCH /api/tasks/{id}`**.
2. Klik **`Try it out`**.
3. Kolom **`id`**: Masukkan ID Task (contoh: `3e89c1b8-70e6-4d25-9b67-2d050490716a`).
   *(Catatan: Masukkan ID Task, BUKAN token JWT!).*
4. Kotak **`Request body`**:
   ```json
   {
     "status": "IN_PROGRESS",
     "version": 1
   }
   ```
5. Klik **`Execute`** ➔ Respon: **`200 OK`**.

### C. Menambah Dependensi Tugas (Khusus PM)
1. Buka tag **`2. Feature Slice: Product Manager (PM)`** ➔ **`POST /api/tasks/{id}/dependencies`**.
2. Kolom **`id`**: Masukkan ID task yang menunggu/terkunci (misal Task Frontend).
3. Kotak **`Request body`**:
   ```json
   {
     "dependsOnTaskId": "ID-task-syarat-yang-harus-selesai-duluan"
   }
   ```
4. Klik **`Execute`** ➔ Respon: **`200 OK`**.

---

## 🎤 6. Script Presentasi untuk Rekruter

Saat rekruter meminta Anda menjelaskan proyek ini, sampaikan naskah berikut:

> *"Proyek ini adalah **NodeWave Core Enterprise Portal** yang dibangun menggunakan **Spring Boot 3.3.4** dan **Java 21**. Alih-alih arsitektur layered tradisional, saya menerapkan **Vertical Slice Architecture (Package-by-Feature)** berdasarkan peran pengguna (PM, Internal Engineer, dan Client).*
> 
> *Sistem ini mengimplementasikan 4 keunggulan enterprise:*
> 1. * **Attribute-Based Access Control (ABAC):** Setiap engineer hanya dapat mengelola tugas miliknya sendiri.*
> 2. * **Inter-Task Blocker:** Menjamin tugas yang memiliki dependensi (seperti Frontend terhadap Desain UI) tidak dapat dimulai sebelum prasyaratnya berstatus DONE.*
> 3. * **Optimistic Concurrency Control:** Menggunakan anotasi JPA `@Version` untuk mendeteksi konflik data konkuren (HTTP 409 Conflict).*
> 4. * **API-Level Data Masking:** Saat akun Client mengakses endpoint, seluruh data identitas engineer dan attachment internal disaring langsung di tingkat DTO serializer untuk mencegah kebocoran data vendor.*
> 
> *Seluruh endpoint terdokumentasi dan dapat diuji langsung secara interaktif melalui Swagger UI OpenAPI 3."*
