package com.example.LocalFit.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.LocalFit.user.entity.User;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);
    boolean existsByNickname(String nickname);
    boolean existsByNicknameAndIdNot(String nickname, Long id);

}
