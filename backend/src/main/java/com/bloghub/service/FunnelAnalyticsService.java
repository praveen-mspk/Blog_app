package com.bloghub.service;

import com.bloghub.entity.User;
import com.bloghub.repository.PageViewRepository;
import com.bloghub.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FunnelAnalyticsService {

    private final PageViewRepository pageViewRepository;
    private final UserRepository userRepository;

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));
    }

    // A. Demographics Matrix
    public Map<String, Object> getDemographics() {
        Long writerId = getCurrentUser().getId();

        List<Object[]> countries = pageViewRepository.countByCountry(writerId);
        List<Object[]> cities = pageViewRepository.countByCity(writerId);
        long total = pageViewRepository.countTotalViews(writerId);

        List<Map<String, Object>> countryList = countries.stream().limit(10).map(row -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("name", row[0]);
            m.put("count", row[1]);
            m.put("percentage", total > 0 ? Math.round(((Long) row[1]) * 100.0 / total) : 0);
            return m;
        }).collect(Collectors.toList());

        List<Map<String, Object>> cityList = cities.stream().limit(8).map(row -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("name", row[0]);
            m.put("count", row[1]);
            return m;
        }).collect(Collectors.toList());

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("totalViews", total);
        result.put("countries", countryList);
        result.put("cities", cityList);
        return result;
    }

    // B. Device & Technology Matrix
    public Map<String, Object> getDeviceMatrix() {
        Long writerId = getCurrentUser().getId();
        long total = pageViewRepository.countTotalViews(writerId);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("deviceTypes", buildDistribution(pageViewRepository.countByDeviceType(writerId), total));
        result.put("operatingSystems", buildDistribution(pageViewRepository.countByOs(writerId), total));
        result.put("browsers", buildDistribution(pageViewRepository.countByBrowser(writerId), total));
        return result;
    }

    // C. Reader Journey Funnel
    public Map<String, Object> getReaderFunnel() {
        Long writerId = getCurrentUser().getId();

        long views = pageViewRepository.countTotalViews(writerId);
        long readers = pageViewRepository.countUniqueReaders(writerId);
        long engaged = pageViewRepository.countEngagedReaders(writerId);

        // Drop-off analysis
        List<Object[]> scrollBuckets = pageViewRepository.countByScrollDepthBucket(writerId);
        Map<String, Long> dropOff = new LinkedHashMap<>();
        long totalForDropoff = 0;
        for (Object[] row : scrollBuckets) {
            dropOff.put((String) row[0], (Long) row[1]);
            totalForDropoff += (Long) row[1];
        }

        Map<String, Object> dropOffPercentages = new LinkedHashMap<>();
        for (Map.Entry<String, Long> e : dropOff.entrySet()) {
            dropOffPercentages.put(e.getKey(), totalForDropoff > 0 ? Math.round(e.getValue() * 100.0 / totalForDropoff) : 0);
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("views", views);
        result.put("readers", readers);
        result.put("engaged", engaged);
        result.put("viewsToReaders", views > 0 ? Math.round(readers * 100.0 / views) : 0);
        result.put("readersToEngaged", readers > 0 ? Math.round(engaged * 100.0 / readers) : 0);
        result.put("dropOff", dropOffPercentages);
        return result;
    }

    // D. Time-Based Analytics
    public Map<String, Object> getTimePatterns() {
        Long writerId = getCurrentUser().getId();

        // Peak hours
        List<Object[]> hourly = pageViewRepository.countByHour(writerId);
        List<Map<String, Object>> peakHours = hourly.stream().limit(5).map(row -> {
            Map<String, Object> m = new LinkedHashMap<>();
            int hour = ((Number) row[0]).intValue();
            m.put("hour", String.format("%02d:00 - %02d:00", hour, (hour + 2) % 24));
            m.put("count", row[1]);
            return m;
        }).collect(Collectors.toList());

        // Active days
        String[] dayNames = {"", "Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};
        List<Object[]> daily = pageViewRepository.countByDayOfWeek(writerId);
        List<Map<String, Object>> activeDays = daily.stream().limit(5).map(row -> {
            Map<String, Object> m = new LinkedHashMap<>();
            int dayIdx = ((Number) row[0]).intValue();
            m.put("day", dayIdx >= 1 && dayIdx <= 7 ? dayNames[dayIdx] : "Unknown");
            m.put("count", row[1]);
            return m;
        }).collect(Collectors.toList());

        // Average duration
        Double avgDuration = pageViewRepository.avgReadDuration(writerId);

        // Duration by category
        List<Object[]> catDur = pageViewRepository.avgDurationByCategory(writerId);
        List<Map<String, Object>> categoryDurations = catDur.stream().map(row -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("category", row[0]);
            m.put("avgSeconds", Math.round((Double) row[1]));
            return m;
        }).collect(Collectors.toList());

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("peakHours", peakHours);
        result.put("activeDays", activeDays);
        result.put("avgDurationSeconds", avgDuration != null ? Math.round(avgDuration) : 0);
        result.put("categoryDurations", categoryDurations);
        return result;
    }

    // E. Reader Retention
    public Map<String, Object> getRetention() {
        Long writerId = getCurrentUser().getId();

        long totalReaders = pageViewRepository.countUniqueReaders(writerId);
        long returning = pageViewRepository.countReturningReaders(writerId);
        long newReaders = totalReaders - returning;

        long inactive30 = pageViewRepository.countInactiveReaders(writerId, LocalDateTime.now().minusDays(30));
        long inactive60 = pageViewRepository.countInactiveReaders(writerId, LocalDateTime.now().minusDays(60));
        long inactive90 = pageViewRepository.countInactiveReaders(writerId, LocalDateTime.now().minusDays(90));

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("totalReaders", totalReaders);
        result.put("returningReaders", returning);
        result.put("returningPercentage", totalReaders > 0 ? Math.round(returning * 100.0 / totalReaders) : 0);
        result.put("newReaders", newReaders);
        result.put("newPercentage", totalReaders > 0 ? Math.round(newReaders * 100.0 / totalReaders) : 0);
        result.put("inactive30Days", inactive30);
        result.put("inactive60Days", inactive60);
        result.put("inactive90Days", inactive90);
        return result;
    }

    // F. Recent Reader Activity (Detailed List)
    public List<Map<String, Object>> getRecentActivity() {
        Long writerId = getCurrentUser().getId();
        // Get last 50 entries
        List<com.bloghub.entity.PageView> recent = pageViewRepository.findRecentByWriter(writerId, org.springframework.data.domain.PageRequest.of(0, 50));

        return recent.stream().map(pv -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", pv.getId());
            m.put("readerName", pv.getUser() != null ? pv.getUser().getName() : "Anonymous");
            m.put("postTitle", pv.getPost().getTitle());
            m.put("location", pv.getCity() + ", " + pv.getCountry());
            m.put("device", pv.getDeviceType().toString());
            m.put("os", pv.getOs());
            m.put("browser", pv.getBrowser());
            m.put("duration", pv.getReadDurationSeconds());
            m.put("scroll", pv.getScrollDepth());
            m.put("time", pv.getViewedAt());
            return m;
        }).collect(Collectors.toList());
    }

    private List<Map<String, Object>> buildDistribution(List<Object[]> rows, long total) {
        return rows.stream().map(row -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("name", String.valueOf(row[0]));
            m.put("count", row[1]);
            m.put("percentage", total > 0 ? Math.round(((Long) row[1]) * 100.0 / total) : 0);
            return m;
        }).collect(Collectors.toList());
    }
}