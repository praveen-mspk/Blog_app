package com.bloghub.service;

import com.bloghub.dto.ActivityLogRequest;
import com.bloghub.dto.ReaderDashboardResponse;
import com.bloghub.dto.WriterDashboardResponse;
import com.bloghub.entity.Post;
import com.bloghub.entity.ReadingActivity;
import com.bloghub.entity.User;
import com.bloghub.entity.UserAchievement;
import com.bloghub.entity.WritingActivity;
import com.bloghub.repository.PostRepository;
import com.bloghub.repository.ReadingActivityRepository;
import com.bloghub.repository.UserAchievementRepository;
import com.bloghub.repository.UserRepository;
import com.bloghub.repository.WritingActivityRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ActivityService {

    private static final Logger log = LoggerFactory.getLogger(ActivityService.class);

    private final ReadingActivityRepository readingActivityRepository;
    private final WritingActivityRepository writingActivityRepository;
    private final UserAchievementRepository userAchievementRepository;
    private final UserRepository userRepository;
    private final PostRepository postRepository;

    public ActivityService(ReadingActivityRepository readingActivityRepository,
                           WritingActivityRepository writingActivityRepository,
                           UserAchievementRepository userAchievementRepository,
                           UserRepository userRepository,
                           PostRepository postRepository) {
        this.readingActivityRepository = readingActivityRepository;
        this.writingActivityRepository = writingActivityRepository;
        this.userAchievementRepository = userAchievementRepository;
        this.userRepository = userRepository;
        this.postRepository = postRepository;
    }

    public void logReadingActivity(Long userId, ActivityLogRequest request) {
        User user = userRepository.findById(userId).orElseThrow();
        Post post = request.getPostId() != null ? postRepository.findById(request.getPostId()).orElse(null) : null;
        
        ReadingActivity activity = ReadingActivity.builder()
                .user(user)
                .post(post)
                .durationMinutes(request.getValue() != null ? request.getValue() : 1)
                .build();
        readingActivityRepository.save(activity);
        log.info("Logged reading activity for user: {}, post: {}, duration: {}", userId, request.getPostId(), activity.getDurationMinutes());

        checkAndAwardAchievements(user);
    }

    public void logWritingActivity(Long userId, ActivityLogRequest request) {
        User user = userRepository.findById(userId).orElseThrow();
        Post post = request.getPostId() != null ? postRepository.findById(request.getPostId()).orElse(null) : null;

        WritingActivity activity = WritingActivity.builder()
                .user(user)
                .post(post)
                .wordsWritten(request.getValue() != null ? request.getValue() : 0)
                .build();
        writingActivityRepository.save(activity);
        log.info("Logged writing activity for user: {}, words: {}", userId, activity.getWordsWritten());

        checkAndAwardAchievements(user);
    }

    public ReaderDashboardResponse getReaderDashboard(Long userId) {
        List<ReadingActivity> activities = readingActivityRepository.findAllByUserIdOrderByReadAtDesc(userId);
        
        Map<String, Integer> heatmap = new HashMap<>();
        Set<LocalDate> uniqueDays = new HashSet<>();
        int totalMinutes = 0;

        for (ReadingActivity act : activities) {
            LocalDate date = act.getReadAt().toLocalDate();
            String dateStr = date.toString();
            heatmap.put(dateStr, heatmap.getOrDefault(dateStr, 0) + 1); // Group by articles read today
            uniqueDays.add(date);
            totalMinutes += act.getDurationMinutes() != null ? act.getDurationMinutes() : 1;
        }

        int currentStreak = calculateStreak(uniqueDays);
        Long totalArticles = readingActivityRepository.countDistinctPostsByUserId(userId);
        if (totalArticles == null) totalArticles = 0L;

        Set<String> achievements = userAchievementRepository.findByUserId(userId).stream()
                .map(a -> a.getAchievementType().name())
                .collect(Collectors.toSet());

        return ReaderDashboardResponse.builder()
                .currentStreak(currentStreak)
                .totalReadingDays(uniqueDays.size())
                .totalArticlesRead(totalArticles)
                .totalReadingMinutes(totalMinutes)
                .heatmapData(heatmap)
                .achievements(achievements)
                .build();
    }

    public WriterDashboardResponse getWriterDashboard(Long userId) {
        List<WritingActivity> activities = writingActivityRepository.findAllByUserIdOrderByWrittenAtDesc(userId);

        Map<String, Integer> heatmap = new HashMap<>();
        Set<LocalDate> uniqueDays = new HashSet<>();

        for (WritingActivity act : activities) {
            LocalDate date = act.getWrittenAt().toLocalDate();
            String dateStr = date.toString();
            heatmap.put(dateStr, heatmap.getOrDefault(dateStr, 0) + act.getWordsWritten());
            uniqueDays.add(date);
        }

        int currentStreak = calculateStreak(uniqueDays);
        Long totalWords = writingActivityRepository.sumWordsWrittenByUserId(userId);
        if (totalWords == null) totalWords = 0L;

        Long publishedArticles = postRepository.countByAuthorIdAndStatus(userId, Post.Status.PUBLISHED);

        Set<String> achievements = userAchievementRepository.findByUserId(userId).stream()
                .map(a -> a.getAchievementType().name())
                .collect(Collectors.toSet());

        return WriterDashboardResponse.builder()
                .currentStreak(currentStreak)
                .totalWritingDays(uniqueDays.size())
                .totalArticlesPublished(publishedArticles)
                .totalWordsWritten(totalWords)
                .heatmapData(heatmap)
                .achievements(achievements)
                .build();
    }

    private int calculateStreak(Set<LocalDate> activeDays) {
        if (activeDays.isEmpty()) return 0;
        int streak = 0;
        LocalDate today = LocalDate.now();
        if (!activeDays.contains(today) && !activeDays.contains(today.minusDays(1))) {
            return 0; // Lost streak
        }
        LocalDate checkDate = activeDays.contains(today) ? today : today.minusDays(1);
        while (activeDays.contains(checkDate)) {
            streak++;
            checkDate = checkDate.minusDays(1);
        }
        return streak;
    }

    private void checkAndAwardAchievements(User user) {
        List<ReadingActivity> reads = readingActivityRepository.findAllByUserIdOrderByReadAtDesc(user.getId());
        if (!reads.isEmpty() && !userAchievementRepository.existsByUserIdAndAchievementType(user.getId(), UserAchievement.AchievementType.FIRST_READ)) {
            userAchievementRepository.save(UserAchievement.builder().user(user).achievementType(UserAchievement.AchievementType.FIRST_READ).build());
        }
        List<WritingActivity> writes = writingActivityRepository.findAllByUserIdOrderByWrittenAtDesc(user.getId());
        if (!writes.isEmpty() && !userAchievementRepository.existsByUserIdAndAchievementType(user.getId(), UserAchievement.AchievementType.FIRST_DRAFT)) {
            userAchievementRepository.save(UserAchievement.builder().user(user).achievementType(UserAchievement.AchievementType.FIRST_DRAFT).build());
        }
    }
}