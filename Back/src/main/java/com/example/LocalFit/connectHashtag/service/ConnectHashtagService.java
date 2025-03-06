package com.example.LocalFit.connectHashtag.service;

import com.example.LocalFit.connectHashtag.entity.ConnectHashtag;
import com.example.LocalFit.connectHashtag.entity.ConnectHashtagResponseDto;
import com.example.LocalFit.connectHashtag.repository.ConnectHashtagRepository;
import com.example.LocalFit.hashtag.entity.Hashtag;
import com.example.LocalFit.meeting.entity.Meeting;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@AllArgsConstructor
public class ConnectHashtagService {

    private final ConnectHashtagRepository connectHashtagRepository;


    @Transactional
    public List<ConnectHashtag> createConnectHashtag(Meeting meeting, List<Hashtag> hashtagList) {

        List<ConnectHashtag> connectHashtagList = new ArrayList<>();

        for (int i = 0; i < hashtagList.size(); i++) {
            // 해시태그 연결 만들기
            ConnectHashtag connectHashtag = ConnectHashtag.builder()
                    .meeting(meeting)
                    .hashtag(hashtagList.get(i))
                    .build();

            connectHashtagRepository.save(connectHashtag);

            connectHashtagList.add(connectHashtag);
        }

        return connectHashtagList;
    }

    @Transactional(readOnly = true)
    public List<ConnectHashtagResponseDto> findByMeetingId(Long meetingId) {
        return connectHashtagRepository.findByMeeting_Id(meetingId).stream().map(connectHashtag -> connectHashtag.connectHashtagToConnectHashtagResponseDto()).toList();
    }

    @Transactional
    public List<ConnectHashtag> updateConnectHashtag(Meeting meeting, List<Hashtag> hashtagList) {

        // meeting에 해당하는 connectHash를 찾아옴
        List<ConnectHashtag> findConnectHashtag = connectHashtagRepository.findByMeeting_Id(meeting.getId());

        /* 동일수가 아니면 조건이 까다로움 -> 최대 3개뿐이라서 다지우고 다시 생성하는게 좋을수도 있음
        // hashtagList에 없는 findConnectHashtag 항목 추출
        Set<Hashtag> hashtagsSet = Set.copyOf(hashtagList);
        List<ConnectHashtag> filteredConnectHashtag = findConnectHashtag.stream()
                .filter(connectHashtag -> !hashtagsSet.contains(connectHashtag.getHashtag().getHashTag()))
                .toList();

        // findConnectHashtag에 없는 hashtagList 항목 추출
        Set<String> connectHashtagSet = findConnectHashtag.stream().map(connectHashtag -> connectHashtag.getHashtag().getHashTag()).collect(Collectors.toSet());
        List<Hashtag> filteredHashtagList = hashtagList.stream()
                .filter(hashtag -> !connectHashtagSet.contains(hashtag))
                .toList();

        // findConnectHashtag 의 hashtag 업데이트
        for (int i = 0; i < filteredHashtagList.size(); i++) {
            filteredConnectHashtag.get(i).updateHashtag(filteredHashtagList.get(i));
        }
        */

        // 모든 연결 삭제 (최대 3회)
        for (int i = 0; i < findConnectHashtag.size(); i++) {
            deleteConnectHashtag(findConnectHashtag.get(i));
        }

        return createConnectHashtag(meeting, hashtagList);
    }

    @Transactional
    public void deleteConnectHashtag(ConnectHashtag connectHashtag) {
        connectHashtagRepository.delete(connectHashtag);
    }

    @Transactional
    public void deleteConnectHashtagByMeetingId(Long meetingId) {
        connectHashtagRepository.deleteByMeeting_Id(meetingId);
    }

    public Long getCountHashTag(Hashtag hashtag){
        return connectHashtagRepository.countByHashtag(hashtag);
    }
}
