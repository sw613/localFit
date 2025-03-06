package com.example.LocalFit.meeting.repository;

import com.example.LocalFit.facility.entity.Facility;
import com.example.LocalFit.meeting.entity.Meeting;
import com.example.LocalFit.meeting.entity.MeetingResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MeetingRepository extends JpaRepository<Meeting, Long> {
    Page<Meeting> findAll(Pageable pageable);

    List<Meeting> findByFacility(Facility facility);

    List<Meeting> findByUser_Id(Long userId);

    boolean existsMeetingsByUser_Id(Long userId);

    @Query("SELECT m FROM Meeting m WHERE m.id = :meetingId AND (m.numberPeopleMax - m.numberPeopleCur) >= 1")
    Optional<Meeting> findIfAvailable(Long meetingId);
}
