package com.example.LocalFit.lounge.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommentRequestDTO {
    private String content;
    //private String userEmail;
    private Long parentCommentId;	// 부모댓글이면 null
}