package com.example.LocalFit.search.utils;

import com.example.LocalFit.facility.entity.FacilityIndexingInfo;
import com.example.LocalFit.facility.repository.FacilityRepository;
import com.example.LocalFit.hashtag.entity.Hashtag;
import com.example.LocalFit.hashtag.entity.HashtagIndexingInfo;
import com.example.LocalFit.hashtag.repository.FailedItemRepository;
import com.example.LocalFit.hashtag.repository.HashtagRepository;
import com.example.LocalFit.lounge.repository.FeedHashTagRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import com.opencsv.CSVWriter;

import java.io.*;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class CSVGenerator {
    private final HashtagRepository hashtagRepository;
    private final FeedHashTagRepository feedHashTagRepository;
    private final FacilityRepository facilityRepository;

    public File generateCsv(List<Long> hashtagIds, String filePath) throws IOException {
        try (OutputStreamWriter fileWriter = new OutputStreamWriter(new FileOutputStream(filePath));
             CSVWriter csvWriter = new CSVWriter(fileWriter)) {

            // 헤더 작성
            csvWriter.writeNext(new String[]{"HashtagId", "Hashtag"});

            // 데이터 작성 (for 루프에서 직접 변환)
            for (Long hashtagId : hashtagIds) {
                HashtagIndexingInfo hashtag = convertToDto(feedHashTagRepository.findHashtagIndexingInfoRaw(hashtagId));

                csvWriter.writeNext(new String[]{
                        String.valueOf(hashtag.getId()),
                        hashtag.getHashtag()
                });
            }
        }

        return new File(filePath);
    }


    private HashtagIndexingInfo convertToDto(Map<String, Object> map) {
        return new HashtagIndexingInfo(
                ((Long) map.get("id")),
                (String) map.get("hashtag")
        );
    }

    public File facilityGenerateCsv(List<Long> facilityList, String filePath) throws IOException {
        try (OutputStreamWriter fileWriter = new OutputStreamWriter(new FileOutputStream(filePath));
             CSVWriter csvWriter = new CSVWriter(fileWriter)) {

            // 헤더 작성
            csvWriter.writeNext(new String[]{"FacilityId", "Name", "MaxClassName", "GroundCategory", "AreaName", "PlaceName"});

            // 데이터 작성 (for 루프에서 직접 변환)
            for (Long facilityId :facilityList) {
                FacilityIndexingInfo facility = facilityConvertToDto(facilityRepository.findFacilityIndexingInfoRaw(facilityId));

                csvWriter.writeNext(new String[]{
                        String.valueOf(facility.getId()),
                        facility.getName(),
                        facility.getMaxClassName(),
                        facility.getGroundCategory(),
                        facility.getAreaName(),
                        facility.getPlaceName()
                });
            }
        }

        return new File(filePath);
    }

    private FacilityIndexingInfo facilityConvertToDto(Map<String, Object> map) {
        return new FacilityIndexingInfo(
                ((Long) map.get("id")),
                (String) map.get("name"),
                (String) map.get("maxClassName"),
                (String) map.get("groundCategory"),
                (String) map.get("areaName"),
                (String) map.get("placeName")
        );
    }

}
