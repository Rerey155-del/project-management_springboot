# NodeWave Core Enterprise Portal - Spring Boot Backend

Backend implementasi ulang NodeWave Technical Assessment menggunakan **Spring Boot 3 (Java 21)** dan **Maven** dengan arsitektur **Package-by-Feature (Vertical Slice Architecture)** berdasarkan peran pengguna (*Role-based*).

---

## 🏛️ Arsitektur: Package-by-Feature (Vertical Slice Architecture)

Berbeda dari arsitektur *Layered* tradisional (`controllers/`, `services/`, `models/`), proyek ini membagi domain berdasarkan **Feature Slices & Role**:

```text
com.nodewave.portal
├── core/                                # Shared Domain & Infrastructure
│   ├── entity/                          # JPA Entities (User, Project, Task, TaskDependency, AuditLog)
│   ├── repository/                      # Spring Data JPA Repositories
│   ├── security/                        # JWT Filter, UserPrincipal, SecurityConfig
│   └── seeder/                          # DataSeeder (Inisialisasi 5 User, 1 Project, 6 Tasks, Dependencies)
├── exception/                           # Global Exception Handler & Custom Exceptions
└── feature/                             # VERTICAL SLICES (Package-by-Feature)
    ├── auth/                            # Slice Autentikasi (Login, /api/me, JWT Token)
    ├── pm/                              # Slice Product Manager (Projects, Task Dependencies, Standup Summary)
    ├── internal/                        # Slice Internal Engineer (Task Status update, Attachment upload)
    ├── client/                          # Slice Client (Masked client tasks projection)
    ├── task/                            # Slice Task Query (GET /api/tasks, Multi-keyword Search, Metrics)
    └── common/                          # Slice Umum (Health check, Root info)
```

Setiap slice mengelompokkan Controller, Service, dan DTO-nya sendiri sehingga perubahan pada satu peran/fitur tidak menimbulkan efek samping (*side-effects*) pada slice lainnya.

---

## 🔐 Kredensial Akun Pengujian (Pre-seeded)

Password default untuk semua akun: **`password123`**

| Peran (Role) | Email | Nama / Departemen | Akses Khusus |
|---|---|---|---|
| **PM** | `pm@nodewave.id` | Budi (Project Manager) | Atur dependensi, lihat standup, dilarang set status `DONE` |
| **INTERNAL** | `designer@nodewave.id` | Rian (UI/UX Designer) | Update tugas UI/UX miliknya |
| **INTERNAL** | `frontend@nodewave.id` | Rerey (Frontend Engineer) | Update tugas Frontend miliknya, upload attachment |
| **INTERNAL** | `backend@nodewave.id` | Joko (Backend Engineer) | Update tugas Backend miliknya, upload attachment |
| **CLIENT** | `client@nodewave.id` | PT Mitra Sukses (Client) | Hanya melihat task `isClientVisible=true`, data engineer disembunyikan |

---

## 🚀 Panduan Menjalankan Aplikasi

### Persyaratan Sistem
- **Java 21** (JDK 21) terpasang (`java -version`)
- **Maven 3.8+** terpasang (`mvn -version`)

---

### Langkah 1: Masuk ke Direktori Spring Boot
Buka terminal dan navigasikan ke folder `springboot`:
```bash
cd "springboot"
# atau path absolut:
cd "/media/rereypc/Local Disk G/xampp/htdocs/Project_Nodewave/backend/springboot"
```

---

### Langkah 2: Menjalankan Aplikasi

#### Opsi A: Mode Default (Database In-Memory H2 - Zero Setup)
Aplikasi sudah dikonfigurasi dengan fallback database H2 in-memory. Anda **tidak perlu menginstal PostgreSQL** untuk langsung menguji aplikasi:
```bash
mvn spring-boot:run
```
> Server akan berjalan di: **`http://localhost:8080`**  
> Konsol H2 Database (opsional): **`http://localhost:8080/h2-console`** (JDBC URL: `jdbc:h2:mem:nodewave_db`, Username: `sa`, Password: *kosong*)

#### Opsi B: Menggunakan PostgreSQL (Lokal atau Neon Cloud)
Jika ingin menggunakan PostgreSQL aktif:
1. Buka file `src/main/resources/application-postgres.properties` dan sesuaikan URL koneksi database Anda:
   ```properties
   spring.datasource.url=jdbc:postgresql://localhost:5432/nodewave_portal
   spring.datasource.username=postgres
   spring.datasource.password=postgres
   ```
2. Jalankan dengan profile `postgres`:
   ```bash
   mvn spring-boot:run -Dspring-boot.run.profiles=postgres
   ```

---

## 📑 Interactive API Testing dengan Swagger UI / OpenAPI 3

Aplikasi telah dilengkapi dengan antarmuka interaktif **Swagger UI (OpenAPI 3)** yang mengelompokkan setiap endpoint sesuai **Vertical Slice Architecture** berbasis peran.

- **URL Swagger UI:** **`http://localhost:8080/swagger-ui.html`** (atau `http://localhost:8080/swagger-ui/index.html`)
- **OpenAPI JSON Spec:** `http://localhost:8080/v3/api-docs`

### 💡 Cara Testing Menggunakan Swagger UI:
1. Buka browser ke **`http://localhost:8080/swagger-ui.html`**.
2. Scroll ke tag **`1. Feature Slice: Auth`** -> Buka endpoint **`POST /api/auth/login`**.
3. Klik tombol **Try it out**, masukkan email (contoh: `pm@nodewave.id`) dan password (`password123`), lalu klik **Execute**.
4. Salin kode `"token"` dari response JSON.
5. Scroll ke bagian paling atas halaman Swagger UI, klik tombol hijau **Authorize 🔓** di sebelah kanan.
6. Tempelkan token yang tadi disalin ke dalam kotak input (cukup tokennya saja, tanpa menuliskan kata `Bearer `), lalu klik **Authorize** -> **Close**.
7. Sekarang gembok berubah menjadi terkunci (🔒). Anda dapat menguji seluruh endpoint di bawah slice **PM**, **Internal Engineer**, **Client**, maupun **Task Query** secara interaktif langsung dari browser!

---

## 🧪 Panduan Pengujian Endpoint di Postman

### 1. Healthcheck
- **Method:** `GET`
- **URL:** `http://localhost:8080/api/health`
- **Response:** `200 OK`

---

### 2. Login & Dapatkan Token
- **Method:** `POST`
- **URL:** `http://localhost:8080/api/auth/login`
- **Headers:** `Content-Type: application/json`
- **Body (raw JSON):**
  ```json
  {
    "email": "pm@nodewave.id",
    "password": "password123"
  }
  ```
- **Response:** Salin nilai `token` dari respons JSON untuk digunakan pada header `Authorization: Bearer <token>`.

---

### 3. Profil Pengguna Saat Ini
- **Method:** `GET`
- **URL:** `http://localhost:8080/api/me`
- **Headers:**
  - `Authorization: Bearer <token>`

---

### 4. Daftar Proyek
- **Method:** `GET`
- **URL:** `http://localhost:8080/api/projects`
- **Headers:**
  - `Authorization: Bearer <token>`

---

### 5. Daftar Tasks (Dengan Pencarian & Masking Klien)
- **Method:** `GET`
- **URL:** `http://localhost:8080/api/tasks`
- **Query Params:**
  - `search` (opsional): kata kunci pencarian (misal: `frontend UI`)
  - `status` (opsional): `TODO`, `IN_PROGRESS`, `BLOCKED`, `DONE`
  - `page` (opsional, default: `1`)
  - `limit` (opsional, default: `50`)
- **Headers:**
  - `Authorization: Bearer <token>`
- **Catatan Masking:**
  - Jika login sebagai **CLIENT**, kolom rahasia seperti `assignee`, internal notes, dan attachment disembunyikan otomatis, hanya tugas `isClientVisible: true` yang ditampilkan.
  - Jika login sebagai **PM** atau **INTERNAL**, struktur data lengkap ditampilkan.

---

### 6. Update Status Task (ABAC, Blocker, & Optimistic Locking)
- **Method:** `PATCH`
- **URL:** `http://localhost:8080/api/tasks/{taskId}`
- **Headers:**
  - `Authorization: Bearer <token>`
  - `Content-Type: application/json`
- **Body (raw JSON):**
  ```json
  {
    "status": "IN_PROGRESS",
    "description": "Deskripsi baru yang diperbarui",
    "version": 1
  }
  ```
- **Skenario Validasi:**
  - **403 Forbidden:** Jika PM mencoba mengubah status menjadi `DONE`.
  - **403 Forbidden:** Jika Engineer INTERNAL mencoba mengupdate tugas milik engineer lain.
  - **400 Bad Request:** Jika tugas memiliki prasyarat (*dependencies*) yang belum berstatus `DONE`.
  - **409 Conflict:** Jika `version` tidak sesuai karena telah diperbarui oleh pengguna lain (Optimistic Locking).

---

### 7. Upload Attachment Task
- **Method:** `PATCH`
- **URL:** `http://localhost:8080/api/tasks/{taskId}/attachment`
- **Headers:**
  - `Authorization: Bearer <token>`
  - `Content-Type: application/json`
- **Body (raw JSON):**
  ```json
  {
    "attachmentUrl": "https://example.com/screenshot-pr.png"
  }
  ```
- **Aturan:** Hanya tim INTERNAL yang dapat mengunggah pada tugas miliknya sendiri.

---

### 8. Tambah Dependensi Antar Tugas (Khusus PM)
- **Method:** `POST`
- **URL:** `http://localhost:8080/api/tasks/{taskId}/dependencies`
- **Headers:**
  - `Authorization: Bearer <pm-token>`
  - `Content-Type: application/json`
- **Body (raw JSON):**
  ```json
  {
    "dependsOnTaskId": "{prerequisiteTaskId}"
  }
  ```
- **Aturan:** Hanya PM yang memiliki akses (HTTP 403 jika role lain).

---

### 9. Daily Standup Auto-Summary
- **Method:** `GET`
- **URL:** `http://localhost:8080/api/tasks/standup/summary`
- **Headers:**
  - `Authorization: Bearer <token>`
- **Response:** Menghasilkan ringkasan tugas yang diselesaikan 24 jam terakhir dan tugas yang terblokir saat ini, dikelompokkan berdasarkan departemen (`UIUX`, `FRONTEND`, `BACKEND`).

---

## 🛠️ Build & Package Jar
Untuk membuat file JAR mandiri untuk deployment produksi:
```bash
mvn clean package -DskipTests
java -jar target/portal-backend-0.0.1-SNAPSHOT.jar
```
