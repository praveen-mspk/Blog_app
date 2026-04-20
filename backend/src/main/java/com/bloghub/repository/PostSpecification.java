package com.bloghub.repository;

import com.bloghub.entity.Post;
import org.springframework.data.jpa.domain.Specification;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;

public class PostSpecification {

    public static Specification<Post> filterPosts(Long categoryId, Integer month, Integer year, String status) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (status != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), Post.Status.valueOf(status)));
            }

            if (categoryId != null) {
                predicates.add(criteriaBuilder.equal(root.get("mainCategory").get("id"), categoryId));
            }

            if (month != null) {
                predicates.add(criteriaBuilder.equal(criteriaBuilder.function("MONTH", Integer.class, root.get("createdAt")), month));
            }

            if (year != null) {
                predicates.add(criteriaBuilder.equal(criteriaBuilder.function("YEAR", Integer.class, root.get("createdAt")), year));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
