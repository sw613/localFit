package com.example.LocalFit.search.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import com.example.LocalFit.connectHashtag.service.ConnectHashtagService;
import com.example.LocalFit.facility.entity.FacilityIndexingInfo;
import com.example.LocalFit.hashtag.entity.Hashtag;
import com.example.LocalFit.hashtag.service.HashtagService;
import com.example.LocalFit.lounge.entity.FeedHashTag;
import com.example.LocalFit.lounge.repository.FeedHashTagRepository;
import com.example.LocalFit.search.dto.AutoCompleteFacilityRes;
import com.example.LocalFit.search.dto.AutoCompleteRes;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class SearchService {
    private final ElasticsearchClient elasticsearchClient;
    private static final String INDEX_NAME = "event-data";
    private static final String FACILITY_INDEX = "sports-facilities";

    private final HashtagService hashtagService;

    private final FeedHashTagRepository feedHashTagRepository;

    private final ConnectHashtagService connectHashtagService;


    private boolean isNotEmpty(List<?> list) {
        return list != null && !list.isEmpty();
    }

    private List<FieldValue> toFieldValues(List<?> list) {
        return list.stream()
                .map(item -> FieldValue.of(f -> f.stringValue(item.toString())))
                .toList();
    }

    public List<AutoCompleteRes> autoComplete(String query) {
        try {
            SearchRequest searchRequest = new SearchRequest.Builder()
                    .index(INDEX_NAME)
                    .query(q -> q.wildcard(w -> w.field("Hashtag").value("*" + query + "*")))
                    .size(5)
                    .sort(s -> s.field(f -> f.field("_score").order(SortOrder.Desc)))
                    .build();

            SearchResponse<AutoCompleteRes> response = elasticsearchClient.search(searchRequest, AutoCompleteRes.class);

            /* 정확도 순 뭉게지는 문제 발생
            List<AutoCompleteRes> result = response.hits().hits().stream()
                    .map(hit -> {
                        AutoCompleteRes res = hit.source();
                        res.setSearchCount(getSearchCountForHashtag(res.getHashtagId()));
                        return res;
                    })
                    .collect(Collectors.toList());

             */

            return response.hits().hits().stream()
                    .map(hit -> {
                        AutoCompleteRes res = hit.source();
                        return res;
                    })
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("자동완성 쿼리 실행 중 오류 발생: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }
    /*
    private Long getSearchCountForHashtag(String hashtagId) {
        Long id = Long.parseLong(hashtagId);
        FeedHashTag feedHashTag = feedHashTagRepository.findById(id);
        return connectHashtagService.getCountHashTag(FeedHashTag);
    }*/

    public List<AutoCompleteFacilityRes> autoCompleteFacility(String query) {
        try {
            SearchRequest searchRequest = new SearchRequest.Builder()
                    .index(FACILITY_INDEX)
                    .query(q -> q.wildcard(w -> w.field("Name").value("*" + query + "*")))
                    .size(5)
                    .sort(s -> s.field(f -> f.field("_score").order(SortOrder.Desc)))
                    .build();

            SearchResponse<FacilityIndexingInfo> response = elasticsearchClient.search(searchRequest, FacilityIndexingInfo.class);

            return response.hits().hits().stream()
                    .map(Hit::source)
                    .map(facility -> new AutoCompleteFacilityRes(facility.getId().toString(), facility.getName()))
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("체육 시설 자동완성 쿼리 실행 중 오류 발생: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }


}
