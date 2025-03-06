package com.example.LocalFit.lounge.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.LocalFit.lounge.entity.Comment;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    void deleteByUser_Id(Long userId);
}
