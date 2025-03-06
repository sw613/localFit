package com.example.LocalFit.facility.service;

import com.example.LocalFit.facility.entity.Facility;
import com.example.LocalFit.facility.entity.FacilityResponse;
import com.example.LocalFit.facility.repository.FacilityRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.NoSuchElementException;

@Slf4j
@Service
@RequiredArgsConstructor
public class FacilityService {

    private final FacilityRepository facilityRepository;
    private final WebClient webClient;

    public Mono<FacilityResponse> getData(int start, int end) {
        return webClient.get()
                .uri("/{start}/{end}/", start, end)
                .retrieve()
                .bodyToMono(FacilityResponse.class);
    }

    // 체육시설 데이터 가져온후 DB에 저장
    @Transactional
    public Mono<Void> saveFacilities() {
        return Flux.just(new int[]{1, 1000}, new int[]{1001, 1500})
                .flatMap(range -> getData(range[0], range[1])) // api 호출
                .flatMap(facilityResponse -> {
                    List<Facility> facilities = facilityResponse.getTvYeyakCollect().getFacilities().stream()
                            .filter(facility -> "체육시설".equals(facility.getMaxClassName()))
                            .toList();

                    // DB 저장
                    return Mono.fromCallable(() -> facilityRepository.saveAll(facilities)).then();
                }).then();
    }

    @Transactional(readOnly = true)
    public List<Facility> findAll() {
        return facilityRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Facility findById(Long facilityId) {
        return facilityRepository.findById(facilityId).orElseThrow(() -> new NoSuchElementException("Not Found Facility id : " + facilityId));
    }

    @Transactional(readOnly = true)
    public List<Facility> findByGroundCategory(String groundCategory) {
        return facilityRepository.findByGroundCategory(groundCategory);
    }

    @Transactional(readOnly = true)
    public Page<Facility> findByGroundCategoryAndAreaName(Pageable pageable, String groundCategory, String areaName) {

        if (groundCategory.equals("기타")) {
            if (areaName.isBlank()) {
                return facilityRepository.findByGroundCategoryNotIn(pageable);
            } else {
                return facilityRepository.findByGroundCategoryNotInAndNameAndAreaName(areaName, pageable);
            }
        } else {
            if (areaName.isBlank()) {
                return facilityRepository.findByGroundCategoryAndAreaNameIsNull(groundCategory, areaName, pageable);
            } else {
                return facilityRepository.findByGroundCategoryAndAreaName(groundCategory, areaName, pageable);
            }
        }
    }

    @Transactional(readOnly = true)
    public List<Facility> findTopFacilitiesByMeetingCount() {
        return facilityRepository.findTopFacilitiesByMeetingCount(PageRequest.of(0, 4));
    }

    @Transactional(readOnly = true)
    public List<String> findAreaNames() {
        return facilityRepository.findAllAreaName();
    }

    public Page<Facility> findByGroundCategoryAndAreaName(String groundCategory, String areaName, String search, Pageable pageable) {
        boolean hasSearch = (search != null && !search.trim().isEmpty());
        boolean hasArea = (areaName != null && !areaName.trim().isEmpty());

        if (hasSearch && hasArea) {
            return facilityRepository.findByGroundCategoryAndAreaNameAndSearch(groundCategory, areaName, search, pageable);
        } else if (hasSearch) {
            return facilityRepository.findByGroundCategoryAndSearch(groundCategory, search, pageable);
        } else if (hasArea) {
            return facilityRepository.findByGroundCategoryAndAreaName(groundCategory, areaName, pageable);
        } else {
            return facilityRepository.findByGroundCategory(groundCategory, pageable);
        }
    }
    
    @Transactional(readOnly = true)
    public List<Facility> geDtistrictFacility() {
        return facilityRepository.findDistinctFacilities();
    }
    
    @Transactional(readOnly = true)
    public List<Facility> getSamePlaceName(@RequestParam String placeName) {
    	return facilityRepository.findByPlaceName(placeName);
    }
}
