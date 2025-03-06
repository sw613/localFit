package com.example.LocalFit.signupMeeting.repository;

import com.example.LocalFit.signupMeeting.entity.SignupMeeting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SignupMeetingRepository extends JpaRepository<SignupMeeting, Long> {
    List<SignupMeeting> findByUserIdAndMeeting_Id(Long userId, Long meetingId);

    List<SignupMeeting> findByUser_Id(Long userId);

    boolean existsByUser_idAndMeeting_id(Long userId, Long meetingId);

    @Query("SELECT s From SignupMeeting s WHERE s.meeting.user.id = :userId AND s.isAgree = FALSE")
    List<SignupMeeting> findMeetingListByUserId(Long userId);

    List<SignupMeeting> findByMeeting_Id(Long meetingId);

    void deleteByMeeting_IdAndUser_Id(Long meetingId, Long userId);
}
