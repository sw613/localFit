package com.example.LocalFit.category.service;

import com.example.LocalFit.category.entity.Category;
import com.example.LocalFit.category.entity.CategoryRequestDto;
import com.example.LocalFit.category.entity.CategoryResponseDto;
import com.example.LocalFit.category.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    @Transactional
    public CategoryResponseDto createCategory(CategoryRequestDto categoryRequestDto) {

        // 중복 체크
        List<Category> findCategories = categoryRepository.findByName(categoryRequestDto.getName());

        // 카테고리 생성
        if (findCategories.isEmpty()) {

            Category category = Category.builder()
                    .name(categoryRequestDto.getName())
                    .build();

            categoryRepository.save(category);
            return category.categoryToCategoryResponseDto();
        }

        // 존재한다면 name가 unique이기에 첫번째 요소 리턴
        return findCategories.get(0).categoryToCategoryResponseDto();
    }

    @Transactional
    public CategoryResponseDto updateCategory(Long categoryId, CategoryRequestDto categoryRequestDto) {

        // 존재하는지 체크
        Category findCategory = categoryRepository.findById(categoryId).orElseThrow(() -> new NoSuchElementException("Not Found Category"));

        // 카테고리 수정
        findCategory.updateName(categoryRequestDto.getName());

        // 카테고리 저장
        categoryRepository.save(findCategory);

        return findCategory.categoryToCategoryResponseDto();
    }



}
