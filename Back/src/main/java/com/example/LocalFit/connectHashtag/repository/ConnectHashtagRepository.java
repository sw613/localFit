package com.example.LocalFit.connectHashtag.repository;

import com.example.LocalFit.connectHashtag.entity.ConnectHashtag;
import com.example.LocalFit.hashtag.entity.Hashtag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ConnectHashtagRepository extends JpaRepository<ConnectHashtag,Long> {
    List<ConnectHashtag> findByMeeting_Id(Long meetingId);

    void deleteByMeeting_Id(Long meetingId);

    long countByHashtag(Hashtag hashtag);
}
