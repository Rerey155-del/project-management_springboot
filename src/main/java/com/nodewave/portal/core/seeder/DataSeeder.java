package com.nodewave.portal.core.seeder;

import com.nodewave.portal.core.entity.*;
import com.nodewave.portal.core.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final TaskRepository taskRepository;
    private final TaskDependencyRepository dependencyRepository;

    public DataSeeder(
            UserRepository userRepository,
            ProjectRepository projectRepository,
            TaskRepository taskRepository,
            TaskDependencyRepository dependencyRepository
    ) {
        this.userRepository = userRepository;
        this.projectRepository = projectRepository;
        this.taskRepository = taskRepository;
        this.dependencyRepository = dependencyRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (userRepository.count() > 0) {
            log.info("Database already seeded. Skipping initial seeding.");
            return;
        }

        log.info("Starting initial database seeding...");

        // 1. Seed Users
        User pm = userRepository.save(new User(
                "Budi (Project Manager)",
                "pm@nodewave.id",
                "password123",
                Role.PM,
                Department.NONE
        ));

        User designer = userRepository.save(new User(
                "Rian (UI/UX Designer)",
                "designer@nodewave.id",
                "password123",
                Role.INTERNAL,
                Department.UIUX
        ));

        User frontend = userRepository.save(new User(
                "Rerey (Frontend Engineer)",
                "frontend@nodewave.id",
                "password123",
                Role.INTERNAL,
                Department.FRONTEND
        ));

        User backend = userRepository.save(new User(
                "Joko (Backend Engineer)",
                "backend@nodewave.id",
                "password123",
                Role.INTERNAL,
                Department.BACKEND
        ));

        User client = userRepository.save(new User(
                "PT Mitra Sukses (Client)",
                "client@nodewave.id",
                "password123",
                Role.CLIENT,
                Department.NONE
        ));

        log.info("Seeded 5 users successfully.");

        // 2. Seed Project
        Project project = projectRepository.save(new Project(
                "NodeWave Core Enterprise Portal",
                "Sistem operasional backbone untuk manajemen proyek skala besar."
        ));

        log.info("Seeded default project: {}", project.getName());

        // 3. Seed 6 Tasks
        Task t1 = taskRepository.save(new Task(
                "Desain UI Wireframe & High-Fidelity",
                "Menyusun wireframe dashboard dan komponen design system.",
                project,
                designer,
                true
        ));

        Task t2 = taskRepository.save(new Task(
                "Setup Repository & Database",
                "Inisialisasi project Next.js, Hono, dan skema Prisma PostgreSQL.",
                project,
                backend,
                true
        ));

        Task t3 = taskRepository.save(new Task(
                "Pembuatan Endpoint API Auth",
                "Bikin fitur login, JWT middleware, dan RBAC untuk backend.",
                project,
                backend,
                false
        ));

        Task t4 = taskRepository.save(new Task(
                "Slicing Frontend & Integrasi State",
                "Mengimplementasikan UI ke Next.js menggunakan Tailwind dan Zustand.",
                project,
                frontend,
                true
        ));

        Task t5 = taskRepository.save(new Task(
                "Integrasi API Task Board",
                "Menyambungkan endpoint backend dengan React Query di frontend.",
                project,
                frontend,
                false
        ));

        Task t6 = taskRepository.save(new Task(
                "Testing & Bug Fixing Aplikasi",
                "Melakukan QA secara menyeluruh sebelum rilis ke Client.",
                project,
                frontend,
                true
        ));

        // 4. Seed Dependencies
        // Task 4 depends on Task 1
        dependencyRepository.save(new TaskDependency(t4, t1));
        // Task 5 depends on Task 3
        dependencyRepository.save(new TaskDependency(t5, t3));

        log.info("Seeded 6 tasks and 2 dependencies successfully.");
    }
}
