package com.example.LocalFit.category.entity;

import com.example.LocalFit.global.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Getter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Category extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "category_id")
    Long id;

    @Column(nullable = false, unique = true)
    String name;

    public void updateName(String name) {
        this.name = name;
    }

    public CategoryResponseDto categoryToCategoryResponseDto() {
        return new CategoryResponseDto(this.name);
    }
}
