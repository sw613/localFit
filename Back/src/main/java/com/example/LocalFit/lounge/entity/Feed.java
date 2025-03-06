package com.example.LocalFit.lounge.entity;

import java.util.ArrayList;
import java.util.List;

import com.example.LocalFit.global.BaseEntity;
import com.example.LocalFit.user.entity.User;
import com.fasterxml.jackson.annotation.JsonBackReference;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@NoArgsConstructor
@SuperBuilder
@Entity
public class Feed extends BaseEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "feed_id")
	private Long id;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", referencedColumnName = "user_id")
	private User user;
	
	private String description;
	
	private int view;
	
	@Builder.Default
	@OneToMany(mappedBy = "feed", cascade = CascadeType.ALL, orphanRemoval = true) 
	private List<Heart> hearts = new ArrayList<>();
	
	@Builder.Default
	@OneToMany(mappedBy = "feed", cascade = CascadeType.ALL, orphanRemoval = true) 
	private List<FeedHashTag> feedHashtags = new ArrayList<>();
	
	@Builder.Default
	@OneToMany(mappedBy = "feed", cascade = CascadeType.ALL, orphanRemoval = true) 
	private List<Comment> comments = new ArrayList<>();
	
	@Builder.Default
	@OneToMany(mappedBy = "feed", cascade = CascadeType.ALL, orphanRemoval = true) 
	private List<FeedImg> feedImgs = new ArrayList<>();
	
}
