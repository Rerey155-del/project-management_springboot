package com.nodewave.portal.core.repository;

import com.nodewave.portal.core.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProjectRepository extends JpaRepository<Project, String> {

    List<Project> findAllByDeletedAtIsNullOrderByCreatedAtAsc();

    Optional<Project> findByIdAndDeletedAtIsNull(String id);

    Optional<Project> findFirstByDeletedAtIsNullOrderByCreatedAtAsc();
}
