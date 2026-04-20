package com.bloghub.config;

import com.bloghub.entity.MainCategory;
import com.bloghub.entity.SubCategory;
import com.bloghub.repository.MainCategoryRepository;
import com.bloghub.repository.SubCategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final MainCategoryRepository mainCategoryRepository;
    private final SubCategoryRepository subCategoryRepository;

    @Override
    public void run(String... args) {
        if (mainCategoryRepository.count() == 0) {
            Map<String, List<String>> categories = Map.of(
                "Technology & IT", List.of("Technology", "Programming", "Artificial Intelligence", "Data Science", "Cybersecurity", "Blockchain & Web3"),
                "Lifestyle & Personal", List.of("Lifestyle", "Health & Fitness", "Mental Health", "Productivity", "Self Improvement", "Relationships"),
                "Business & Career", List.of("Business", "Startups", "Finance", "Investing", "Marketing", "Entrepreneurship", "Career & Jobs"),
                "Creative & Media", List.of("Writing", "Design", "Photography", "Film & Cinema", "Music", "Art"),
                "Education & Knowledge", List.of("Education", "Science", "History", "Philosophy", "Psychology"),
                "Travel & Culture", List.of("Travel", "Food & Recipes", "Culture"),
                "Modern Trends", List.of("Remote Work", "Freelancing", "Digital Nomad", "Sustainability", "Climate Change", "Gaming", "Sports", "News & Politics")
            );

            categories.forEach((mainName, subs) -> {
                MainCategory main = mainCategoryRepository.save(MainCategory.builder().name(mainName).build());
                subs.forEach(subName -> {
                    subCategoryRepository.save(SubCategory.builder().name(subName).mainCategory(main).build());
                });
            });
        }
    }
}