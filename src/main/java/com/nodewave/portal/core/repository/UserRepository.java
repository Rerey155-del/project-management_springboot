package com.nodewave.portal.core.repository;

import com.nodewave.portal.core.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String> {

    Optional<User> findByEmailAndDeletedAtIsNull(String email);

    Optional<User> findByIdAndDeletedAtIsNull(String id);

    boolean existsByEmailAndDeletedAtIsNull(String email);
}
