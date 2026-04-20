package com.bloghub.repository;

import com.bloghub.entity.PageView;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PageViewRepository extends JpaRepository<PageView, Long> {

    // Demographics - country distribution
    @Query("SELECT pv.country, COUNT(pv) FROM PageView pv WHERE pv.post.author.id = :writerId AND pv.country IS NOT NULL GROUP BY pv.country ORDER BY COUNT(pv) DESC")
    List<Object[]> countByCountry(@Param("writerId") Long writerId);

    // Demographics - city distribution
    @Query("SELECT pv.city, COUNT(pv) FROM PageView pv WHERE pv.post.author.id = :writerId AND pv.city IS NOT NULL GROUP BY pv.city ORDER BY COUNT(pv) DESC")
    List<Object[]> countByCity(@Param("writerId") Long writerId);

    // Device type distribution
    @Query("SELECT pv.deviceType, COUNT(pv) FROM PageView pv WHERE pv.post.author.id = :writerId GROUP BY pv.deviceType ORDER BY COUNT(pv) DESC")
    List<Object[]> countByDeviceType(@Param("writerId") Long writerId);

    // OS distribution
    @Query("SELECT pv.os, COUNT(pv) FROM PageView pv WHERE pv.post.author.id = :writerId AND pv.os IS NOT NULL GROUP BY pv.os ORDER BY COUNT(pv) DESC")
    List<Object[]> countByOs(@Param("writerId") Long writerId);

    // Browser distribution
    @Query("SELECT pv.browser, COUNT(pv) FROM PageView pv WHERE pv.post.author.id = :writerId AND pv.browser IS NOT NULL GROUP BY pv.browser ORDER BY COUNT(pv) DESC")
    List<Object[]> countByBrowser(@Param("writerId") Long writerId);

    // Total views for writer
    @Query("SELECT COUNT(pv) FROM PageView pv WHERE pv.post.author.id = :writerId")
    long countTotalViews(@Param("writerId") Long writerId);

    // Unique readers (non-null user)
    @Query("SELECT COUNT(DISTINCT pv.user.id) FROM PageView pv WHERE pv.post.author.id = :writerId AND pv.user IS NOT NULL")
    long countUniqueReaders(@Param("writerId") Long writerId);

    // Engaged readers (scrollDepth >= 60)
    @Query("SELECT COUNT(DISTINCT pv.user.id) FROM PageView pv WHERE pv.post.author.id = :writerId AND pv.user IS NOT NULL AND pv.scrollDepth >= 60")
    long countEngagedReaders(@Param("writerId") Long writerId);

    // Hourly distribution - PostgreSQL native query
    @Query(value = "SELECT EXTRACT(HOUR FROM pv.viewed_at) AS hr, COUNT(*) AS cnt FROM page_views pv JOIN posts p ON p.id = pv.post_id WHERE p.author_id = :writerId GROUP BY hr ORDER BY cnt DESC", nativeQuery = true)
    List<Object[]> countByHour(@Param("writerId") Long writerId);

    // Day-of-week distribution - PostgreSQL native query (0=Sunday, 6=Saturday)
    @Query(value = "SELECT EXTRACT(DOW FROM pv.viewed_at) AS dow, COUNT(*) AS cnt FROM page_views pv JOIN posts p ON p.id = pv.post_id WHERE p.author_id = :writerId GROUP BY dow ORDER BY cnt DESC", nativeQuery = true)
    List<Object[]> countByDayOfWeek(@Param("writerId") Long writerId);

    // Average read duration
    @Query("SELECT AVG(pv.readDurationSeconds) FROM PageView pv WHERE pv.post.author.id = :writerId AND pv.readDurationSeconds > 0")
    Double avgReadDuration(@Param("writerId") Long writerId);

    // Scroll depth distribution - native query for PostgreSQL
    @Query(value = "SELECT CASE WHEN pv.scroll_depth < 25 THEN 'intro' WHEN pv.scroll_depth < 75 THEN 'mid' ELSE 'completed' END AS bucket, COUNT(*) AS cnt FROM page_views pv JOIN posts p ON p.id = pv.post_id WHERE p.author_id = :writerId GROUP BY bucket", nativeQuery = true)
    List<Object[]> countByScrollDepthBucket(@Param("writerId") Long writerId);

    // Returning readers - native query for PostgreSQL
    @Query(value = "SELECT COUNT(DISTINCT pv.user_id) FROM page_views pv JOIN posts p ON p.id = pv.post_id WHERE p.author_id = :writerId AND pv.user_id IS NOT NULL AND pv.user_id IN (SELECT pv2.user_id FROM page_views pv2 JOIN posts p2 ON p2.id = pv2.post_id WHERE p2.author_id = :writerId GROUP BY pv2.user_id HAVING COUNT(DISTINCT pv2.viewed_at::date) > 1)", nativeQuery = true)
    long countReturningReaders(@Param("writerId") Long writerId);

    // Churn: users inactive for N days - native query for PostgreSQL
    @Query(value = "SELECT COUNT(DISTINCT pv.user_id) FROM page_views pv JOIN posts p ON p.id = pv.post_id WHERE p.author_id = :writerId AND pv.user_id IS NOT NULL AND pv.user_id NOT IN (SELECT pv2.user_id FROM page_views pv2 JOIN posts p2 ON p2.id = pv2.post_id WHERE p2.author_id = :writerId AND pv2.viewed_at > :since AND pv2.user_id IS NOT NULL)", nativeQuery = true)
    long countInactiveReaders(@Param("writerId") Long writerId, @Param("since") LocalDateTime since);

    // Average duration by category
    @Query("SELECT pv.post.mainCategory.name, AVG(pv.readDurationSeconds) FROM PageView pv WHERE pv.post.author.id = :writerId AND pv.readDurationSeconds > 0 GROUP BY pv.post.mainCategory.name ORDER BY AVG(pv.readDurationSeconds) DESC")
    List<Object[]> avgDurationByCategory(@Param("writerId") Long writerId);

    // Recent activity - list of individual views
    @Query("SELECT pv FROM PageView pv WHERE pv.post.author.id = :writerId ORDER BY pv.viewedAt DESC")
    List<PageView> findRecentByWriter(@Param("writerId") Long writerId, org.springframework.data.domain.Pageable pageable);
}
