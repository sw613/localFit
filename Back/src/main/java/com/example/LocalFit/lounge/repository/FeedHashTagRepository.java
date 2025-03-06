package com.example.LocalFit.lounge.repository;

import com.example.LocalFit.lounge.entity.FeedHashTag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface FeedHashTagRepository extends JpaRepository<FeedHashTag, Long> {

    List<FeedHashTag> findByHashtag(String hashtag);

    @Query("SELECT MIN(f.id) FROM FeedHashTag f")
    Long findMinId();

    @Query("SELECT MAX(f.id) FROM FeedHashTag f")
    Long findMaxId();

    @Query("SELECT f.id FROM FeedHashTag f WHERE f.id BETWEEN :startId AND :endId ORDER BY f.id ASC")
    List<Long> findHashtagIdsByRange(@Param("startId") Long startId, @Param("endId") Long endId);


    @Query(value = "SELECT f.feed_hashtag_id AS id, f.hashtag AS hashTag " +
            "FROM feed_hash_tag f " +
            "WHERE f.feed_hashtag_id = :hashtagId",
            nativeQuery = true)
    Map<String, Object> findHashtagIndexingInfoRaw(@Param("hashtagId") Long hashtagId);

}
