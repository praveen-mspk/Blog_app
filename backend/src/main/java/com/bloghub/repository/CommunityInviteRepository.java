package com.bloghub.repository;

import com.bloghub.entity.CommunityInvite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CommunityInviteRepository extends JpaRepository<CommunityInvite, Long> {
    Optional<CommunityInvite> findByInviteCode(String inviteCode);
    List<CommunityInvite> findByCommunityId(Long communityId);
}
