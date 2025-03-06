package com.example.LocalFit.hashtag.repository;

import com.example.LocalFit.hashtag.entity.Hashtag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface HashtagRepository extends JpaRepository<Hashtag, Long> {
    List<Hashtag> findByHashTag(String hashTag);

    @Query("SELECT MIN(h.id) FROM Hashtag h")
    Long findMinId();

    @Query("SELECT MAX(h.id) FROM Hashtag h")
    Long findMaxId();

    @Query("SELECT h.id FROM Hashtag h WHERE h.id BETWEEN :startId AND :endId ORDER BY h.id ASC")
    List<Long> findHashtagIdsByRange(@Param("startId") Long startId, @Param("endId") Long endId);


    @Query(value = "SELECT h.hashtag_id AS id, h.hash_tag AS hashtag " +
            "FROM hashtag h " +
            "WHERE h.hashtag_id = :hashtagId",
            nativeQuery = true)
    Map<String, Object> findHashtagIndexingInfoRaw(@Param("hashtagId") Long hashtagId);
}
