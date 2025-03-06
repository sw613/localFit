package com.example.LocalFit.hashtag.service;

import com.example.LocalFit.hashtag.entity.Hashtag;
import com.example.LocalFit.hashtag.repository.HashtagRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class HashtagService {

    private final HashtagRepository hashtagRepository;

    public Hashtag findById(Long id){
        return hashtagRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("해당 entity 찾지 못함"));
    }

    @Transactional
    public List<Hashtag> createHashtag(List<String> hashtagStrList) {

        List<Hashtag> hashtagList = new ArrayList<>();

        for (int i = 0; i < hashtagStrList.size(); i++) {
            // 중복 체크
            List<Hashtag> findHashtags = hashtagRepository.findByHashTag(hashtagStrList.get(i));

            // 해시태그 생성
            if (findHashtags.isEmpty()) {
                Hashtag hashtag = Hashtag.builder()
                        .hashTag(hashtagStrList.get(i))
                        .build();

                hashtagRepository.save(hashtag);
                hashtagList.add(hashtag);
            } else {
                // 해시태그가 존재하면 찾은 해시태그 추가
                hashtagList.add(findHashtags.getFirst());
            }
        }

        return hashtagList;
    }

    @Transactional(readOnly = true)
    public Hashtag getHashtag(Long hashtagId) {
        return hashtagRepository.findById(hashtagId).orElseThrow(() -> new NoSuchElementException("Not Found Hashtag id : " + hashtagId));
    }

}
