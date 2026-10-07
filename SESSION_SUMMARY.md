# 📝 Rangkuman Sesi Pengembangan (NodeWave Enterprise Portal)

**Terakhir Diperbarui:** 7 Oktober 2026  
**Workspace:** `/media/rereypc/Local Disk G/xampp/htdocs/Project_Nodewave/backend/springboot`  

---

## 📌 Status Riwayat Percakapan
Seluruh riwayat chat percakapan sesi ini tersimpan secara permanen dan otomatis di Antigravity IDE. File ringkasan ini dapat dijadikan referensi cepat kapan saja.

---

## 🏛️ 1. Arsitektur Proyek: Package-by-Feature (Vertical Slice Architecture)

Direktori proyek: `backend/springboot/`

### Struktur Slice Berdasarkan Role:
```text
backend/springboot/
├── pom.xml                                     # Spring Boot 3.3.4, Java 21, JPA, Security, JJWT, Springdoc Swagger
├── src/main/resources/
│   ├── application.properties                  # Konfigurasi default (H2 in-memory, Swagger, JWT)
│   └── application-postgres.properties         # Konfigurasi PostgreSQL (opsional)
└── src/main/java/com/nodewave/portal/
    ├── PortalApplication.java                  # Main entrypoint
    │
    ├── core/                                   # Shared Domain & Cross-Cutting
    │   ├── config/                             # OpenApiConfig (JWT Bearer definition & Swagger Metadata)
    │   ├── entity/                             # JPA Entities (User, Project, Task, TaskDependency, AuditLog)
    │   ├── repository/                         # Repositories (JpaSpecificationExecutor, Custom Queries)
    │   ├── security/                           # JwtAuthenticationFilter, UserPrincipal, SecurityConfig
    │   └── seeder/                             # DataSeeder (Inisialisasi 5 User, 1 Project, 6 Tasks, 2 Dependencies)
    │
    ├── exception/                              # GlobalExceptionHandler (400, 401, 403, 404, 409, 500)
    │
    └── feature/                                # 🍕 VERTICAL SLICES (Package-by-Feature)
        ├── auth/                               # Slice Autentikasi (POST /api/auth/login, GET /api/me)
        ├── pm/                                 # Slice Product Manager (Projects, Dependencies, Standup Summary)
        ├── internal/                           # Slice Internal Engineer (Update Task, Upload Attachment)
        ├── client/                             # Slice Client (Masked Client Task Views)
        ├── task/                               # Slice Core Task Query (Multi-keyword Search, Metrics, Masking)
        └── common/                             # Slice Common (Healthcheck, Root Info)
```

---

## 🔐 2. Kredensial Akun Pengujian (Pre-seeded)

Password default untuk seluruh akun: **`password123`**

| Peran (Role) | Email | Nama / Departemen | Hak Akses & Pembatasan Khusus |
|---|---|---|---|
| **PM** | `pm@nodewave.id` | Budi (Project Manager) | Mengatur dependensi tugas, melihat standup summary, **dilarang menandai task `DONE`** (`403 Forbidden`). |
| **INTERNAL** | `designer@nodewave.id` | Rian (UI/UX Designer) | Mengubah status tugas miliknya sendiri (`403` jika mengedit tugas orang lain). |
| **INTERNAL** | `frontend@nodewave.id` | Rerey (Frontend Engineer) | Mengubah tugas Frontend miliknya, upload attachment, terkena inter-task blocking jika prasyarat belum `DONE`. |
| **INTERNAL** | `backend@nodewave.id` | Joko (Backend Engineer) | Mengubah tugas Backend miliknya, upload attachment. |
| **CLIENT** | `client@nodewave.id` | PT Mitra Sukses (Client) | Hanya melihat task dengan `isClientVisible=true`. Detail assignee, dependensi, dan attachment otomatis disembunyikan. |

---

## 📑 3. Interactive Testing dengan Swagger UI (OpenAPI 3)

- **URL Swagger UI:** **`http://localhost:8080/swagger-ui.html`**
- **OpenAPI 3 Spec:** `http://localhost:8080/v3/api-docs`

### Cara Testing di Swagger UI:
1. Buka browser: `http://localhost:8080/swagger-ui.html`.
2. Buka tag **`1. Feature Slice: Auth`** -> `POST /api/auth/login`.
3. Klik **Try it out**, masukkan email (contoh: `pm@nodewave.id`) dan password (`password123`), lalu klik **Execute**.
4. Salin kode `"token"` dari response JSON.
5. Klik tombol hijau **Authorize 🔓** di bagian kanan atas Swagger UI.
6. Tempelkan token ke dalam kotak input (tanpa kata `Bearer `), lalu klik **Authorize** -> **Close**.
7. Ikon gembok berubah menjadi **🔒**, Anda dapat menguji seluruh endpoint lainnya secara langsung.

---

## 🧪 4. Aturan Bisnis & Testing Skenario Khusus

1. **State Restriction (PM Dilarang DONE):**
   - Kirim `{"status": "DONE"}` menggunakan token PM -> **`403 Forbidden`**:  
     `"Product Manager tidak dapat menandai task sebagai DONE"`.
2. **ABAC (Isolasi Antar Engineer):**
   - Engineer hanya bisa mengubah status tugas miliknya sendiri. Mengedit tugas orang lain -> **`403 Forbidden`**:  
     `"Akses Ditolak: Kamu hanya bisa mengubah status tugas milikmu sendiri."`.
3. **Inter-task Dependency Blocker:**
   - Tugas dengan prasyarat (*dependsOn*) tidak dapat diubah ke `IN_PROGRESS` atau `DONE` jika prasyarat belum `DONE` -> **`400 Bad Request`**:  
     `"Task is BLOCKED. Prerequisites must be DONE first."`.
4. **Optimistic Locking:**
   - Jika `version` yang dikirim tidak cocok dengan versi data saat ini -> **`409 Conflict`**:  
     `"Conflict 409: Data has been modified by another user. Please refresh."`.
5. **Client Data Masking:**
   - Permintaan `GET /api/tasks` dengan token role `CLIENT` secara otomatis menyembunyikan kolom assignee, dependensi, dan attachment, serta hanya mengembalikan tugas dengan `isClientVisible: true`.
6. **Daily Standup Auto-Summary:**
   - Endpoint `GET /api/tasks/standup/summary` menghitung data `AuditLog` 24 jam terakhir secara real-time dan mengelompokkan data per departemen (`UIUX`, `FRONTEND`, `BACKEND`).

---

## 🎨 5. Status Warna File di IntelliJ IDEA

Bila nama file di Project Explorer IntelliJ IDEA berwarna **merah / cokelat kemerahan**:
- **Penyebab:** Indikator Git untuk file baru yang belum di-stage (**Untracked / Unversioned**), bukan error Java.
- **Solusi:**
  ```bash
  cd "/media/rereypc/Local Disk G/xampp/htdocs/Project_Nodewave/backend"
  git add springboot/
  ```
  Warna akan berubah menjadi **Hijau** (Staged), dan kembali normal (**Putih/Abu-abu**) setelah di-commit.

---

## 🚀 6. Cara Menjalankan Aplikasi

```bash
cd "/media/rereypc/Local Disk G/xampp/htdocs/Project_Nodewave/backend/springboot"

# Mode H2 in-memory (Default, langsung jalan tanpa setup database)
mvn spring-boot:run

# Mode PostgreSQL (opsional)
mvn spring-boot:run -Dspring-boot.run.profiles=postgres
```
