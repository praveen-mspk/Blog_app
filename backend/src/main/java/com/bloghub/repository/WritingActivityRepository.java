package com.bloghub.repository;

import com.bloghub.entity.WritingActivity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface WritingActivityRepository extends JpaRepository<WritingActivity, Long> {

    List<WritingActivity> findByUserIdAndWrittenAtAfter(Long userId, LocalDateTime date);

    @Query("SELECT w FROM WritingActivity w WHERE w.user.id = :userId ORDER BY w.writtenAt DESC")
    List<WritingActivity> findAllByUserIdOrderByWrittenAtDesc(Long userId);

    @Query("SELECT SUM(w.wordsWritten) FROM WritingActivity w WHERE w.user.id = :userId")
    Long sumWordsWrittenByUserId(Long userId);

}