package com.bloghub.controller;

import com.bloghub.entity.MainCategory;
import com.bloghub.entity.SubCategory;
import com.bloghub.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
public class CategoryController {
    private final CategoryService categoryService;

    @GetMapping("/main")
    public ResponseEntity<List<MainCategory>> getAllMainCategories() {
        return ResponseEntity.ok(categoryService.getAllMainCategories());
    }

    @GetMapping("/sub/{mainCategoryId}")
    public ResponseEntity<List<SubCategory>> getSubCategories(@PathVariable Long mainCategoryId) {
        return ResponseEntity.ok(categoryService.getSubCategoriesByMainCategory(mainCategoryId));
    }
}
