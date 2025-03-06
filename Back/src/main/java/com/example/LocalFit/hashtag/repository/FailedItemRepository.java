package com.example.LocalFit.hashtag.repository;

import com.example.LocalFit.hashtag.entity.FailedItem;
import com.example.LocalFit.hashtag.entity.JobStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FailedItemRepository extends JpaRepository<FailedItem, Long> {
    List<FailedItem> findByStatus(JobStatus status);

    List<FailedItem> findAllByItemIdIn(List<Long> itemIds);
}
