package com.nodewave.portal.core.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        final String securitySchemeName = "BearerAuth";
        return new OpenAPI()
                .info(new Info()
                        .title("NodeWave Core Enterprise Portal API")
                        .version("1.0.0")
                        .description("API Documentation & Interactive Testing untuk Backend NodeWave dengan arsitektur **Package-by-Feature (Vertical Slice Architecture)** berdasarkan Role.\n\n" +
                                "### Kredensial Akun (Password semua akun: `password123`):\n" +
                                "- **PM**: `pm@nodewave.id` (Atur dependensi, standup summary, dilarang mark DONE)\n" +
                                "- **UI/UX Designer**: `designer@nodewave.id` (Update tugas UI/UX)\n" +
                                "- **Frontend Engineer**: `frontend@nodewave.id` (Update tugas Frontend, upload attachment)\n" +
                                "- **Backend Engineer**: `backend@nodewave.id` (Update tugas Backend, upload attachment)\n" +
                                "- **Client**: `client@nodewave.id` (Masked tasks, data engineer & attachment tersembunyi)\n\n" +
                                "### Cara Testing di Swagger:\n" +
                                "1. Jalankan endpoint **`POST /api/auth/login`** dengan salah satu akun di atas.\n" +
                                "2. Salin token JWT dari response.\n" +
                                "3. Klik tombol hijau **Authorize 🔓** di kanan atas Swagger UI, tempelkan token pada kotak value (tanpa kata 'Bearer '), lalu klik **Authorize**.\n" +
                                "4. Sekarang Anda dapat menguji seluruh endpoint role-based secara interaktif!")
                        .contact(new Contact().name("NodeWave Development Team").email("dev@nodewave.id"))
                )
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName,
                                new SecurityScheme()
                                        .name(securitySchemeName)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("Masukkan token JWT hasil login di sini.")
                        )
                );
    }
}
