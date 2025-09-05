package com.example.workmanager.repository;

import com.example.workmanager.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
    Optional<User> findByEmail(String email);
    boolean existsByEmailAndIdNot(String email, Integer id);
    Optional<User> findById(Integer id);
}