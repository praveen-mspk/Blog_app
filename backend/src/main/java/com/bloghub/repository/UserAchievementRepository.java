package com.bloghub.repository;

import com.bloghub.entity.UserAchievement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserAchievementRepository extends JpaRepository<UserAchievement, Long> {

    List<UserAchievement> findByUserId(Long userId);
    
    boolean existsByUserIdAndAchievementType(Long userId, UserAchievement.AchievementType type);

}