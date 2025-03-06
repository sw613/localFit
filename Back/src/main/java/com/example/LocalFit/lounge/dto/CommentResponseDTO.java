package com.example.LocalFit.lounge.dto;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.example.LocalFit.lounge.entity.Comment;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommentResponseDTO {
    private Long id;         
    private String content;  
    private String userNickName;
    private Long userId;
    private String userProfileImg;
    private List<CommentResponseDTO> replies;

    public CommentResponseDTO(Comment comment) {
        this.id = comment.getId();
        this.content = comment.getContent();
        this.userNickName = (comment.getUser() != null) 
            ? comment.getUser().getNickname() 
            : "알 수 없음";
        this.userId = comment.getUser().getId();
        
        // 없으면 기본이미지
        this.userProfileImg = comment.getUser().getUserImg() != null
                ? comment.getUser().getUserImg().getPath()
                : "https://localfitbucket.s3.ap-northeast-2.amazonaws.com/default_profile.png";
        
        if (comment.getReplies() != null) {
        	this.replies = comment.getReplies().stream()
        			.map(CommentResponseDTO::new)
        			.collect(Collectors.toList());
        }
        else {
        	this.replies = new ArrayList<>();
        }
    }
}
