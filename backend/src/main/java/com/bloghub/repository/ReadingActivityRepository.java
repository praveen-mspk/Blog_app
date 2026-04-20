package com.bloghub.repository;

import com.bloghub.entity.ReadingActivity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ReadingActivityRepository extends JpaRepository<ReadingActivity, Long> {

    List<ReadingActivity> findByUserIdAndReadAtAfter(Long userId, LocalDateTime date);

    @Query("SELECT r FROM ReadingActivity r WHERE r.user.id = :userId ORDER BY r.readAt DESC")
    List<ReadingActivity> findAllByUserIdOrderByReadAtDesc(Long userId);

    @Query("SELECT COUNT(DISTINCT r.post.id) FROM ReadingActivity r WHERE r.user.id = :userId")
    Long countDistinctPostsByUserId(Long userId);
    
}