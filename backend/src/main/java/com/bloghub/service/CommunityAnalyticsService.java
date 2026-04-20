package com.bloghub.service;

import com.bloghub.entity.Community;
import com.bloghub.repository.CommunityMemberRepository;
import com.bloghub.repository.CommunityRepository;
import com.bloghub.repository.DiscussionReplyRepository;
import com.bloghub.repository.DiscussionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CommunityAnalyticsService {
    private final CommunityRepository communityRepository;
    private final CommunityMemberRepository memberRepository;
    private final DiscussionRepository discussionRepository;

    public Map<String, Object> getAnalytics(Long communityId) {
        Community community = communityRepository.findById(communityId).orElseThrow();
        long totalMembers = memberRepository.findByCommunityId(communityId).size();
        long totalDiscussions = discussionRepository.findByCommunityIdOrderByCreatedAtDesc(communityId).size();
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("communityName", community.getName());
        stats.put("totalMembers", totalMembers);
        stats.put("totalDiscussions", totalDiscussions);
        stats.put("activeToday", Math.max(0, totalMembers / 10)); // Simulated
        stats.put("averageResponseTime", "15 minutes"); // Simulated
        return stats;
    }
}
