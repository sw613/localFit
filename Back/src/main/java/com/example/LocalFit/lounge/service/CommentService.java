package com.example.LocalFit.lounge.service;

import org.springframework.stereotype.Service;

import com.example.LocalFit.lounge.dto.CommentRequestDTO;
import com.example.LocalFit.lounge.entity.Comment;
import com.example.LocalFit.lounge.entity.Feed;
import com.example.LocalFit.lounge.repository.CommentRepository;
import com.example.LocalFit.user.entity.User;
import com.example.LocalFit.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CommentService {
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    
    public Comment createComment(CommentRequestDTO dto, Feed feed, User user) {
        Comment.CommentBuilder commentBuilder = Comment.builder()
                .content(dto.getContent())
                .feed(feed)
                .user(user);
        
        if (dto.getParentCommentId() != null) {
            Comment parentComment = commentRepository.findById(dto.getParentCommentId())
                    .orElseThrow(() -> new RuntimeException("부모 댓글을 찾을 수 없습니다."));
            commentBuilder.parent(parentComment);
        }
        
        Comment newComment = commentBuilder.build();
        return commentRepository.save(newComment);
    }
}
