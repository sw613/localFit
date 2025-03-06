package com.example.LocalFit.category.controller;

import com.example.LocalFit.category.entity.CategoryRequestDto;
import com.example.LocalFit.category.entity.CategoryResponseDto;
import com.example.LocalFit.category.service.CategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/category")
@RequiredArgsConstructor
@Slf4j
public class CategoryApiController {

    private final CategoryService categoryService;

    @PostMapping("/create")
    public CategoryResponseDto createCategory(@RequestBody CategoryRequestDto categoryRequestDto) {
        return categoryService.createCategory(categoryRequestDto);
    }

    @PostMapping("/update/{categoryId}")
    public CategoryResponseDto updateCategory(@PathVariable Long categoryId, @RequestBody CategoryRequestDto categoryRequestDto) {
        return categoryService.updateCategory(categoryId, categoryRequestDto);
    }
}
