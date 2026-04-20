package com.bloghub.repository;

import com.bloghub.entity.Discussion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DiscussionRepository extends JpaRepository<Discussion, Long> {
    List<Discussion> findByCommunityIdOrderByCreatedAtDesc(Long communityId);
    List<Discussion> findByAuthorIdOrderByCreatedAtDesc(Long authorId);
}