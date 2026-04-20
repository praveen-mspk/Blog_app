package com.bloghub.repository;

import com.bloghub.entity.WriterPayout;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface WriterPayoutRepository extends JpaRepository<WriterPayout, Long> {
    List<WriterPayout> findByWriterIdOrderByPayoutDateDesc(Long writerId);
}