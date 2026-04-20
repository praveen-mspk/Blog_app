package com.bloghub.repository;

import com.bloghub.entity.DiscussionReply;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DiscussionReplyRepository extends JpaRepository<DiscussionReply, Long> {
    List<DiscussionReply> findByDiscussionIdOrderByCreatedAtAsc(Long discussionId);
}