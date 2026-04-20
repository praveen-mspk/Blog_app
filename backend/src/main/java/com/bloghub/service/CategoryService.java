package com.bloghub.service;

import com.bloghub.entity.MainCategory;
import com.bloghub.entity.SubCategory;
import com.bloghub.repository.MainCategoryRepository;
import com.bloghub.repository.SubCategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {
    private final MainCategoryRepository mainCategoryRepository;
    private final SubCategoryRepository subCategoryRepository;

    public List<MainCategory> getAllMainCategories() {
        return mainCategoryRepository.findAll();
    }

    public List<SubCategory> getSubCategoriesByMainCategory(Long mainCategoryId) {
        return subCategoryRepository.findByMainCategoryId(mainCategoryId);
    }
}
