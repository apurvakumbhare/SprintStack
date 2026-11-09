package com.sprintstack.repository;

import com.sprintstack.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.List;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmailIgnoreCase(String email);
    boolean existsByEmailIgnoreCase(String email);
    boolean existsByEmailIgnoreCaseAndIdNot(String email,Long id);
    List<User> findTop20ByFullNameContainingIgnoreCaseOrEmailContainingIgnoreCase(String name, String email);
}
