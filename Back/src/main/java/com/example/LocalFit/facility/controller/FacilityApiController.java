package com.example.LocalFit.facility.controller;

import com.example.LocalFit.facility.entity.Facility;
import com.example.LocalFit.facility.entity.FacilityResponse;
import com.example.LocalFit.facility.service.FacilityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;

    @Slf4j
    @RestController
    @RequestMapping("/api/facility")
    @RequiredArgsConstructor
    public class FacilityApiController {

        private final FacilityService facilityService;

        @GetMapping("/list/{start}/{end}")
        public Mono<FacilityResponse> getList(@PathVariable Integer start, @PathVariable Integer end) {
            return facilityService.getData(start, end);
        }

        @GetMapping("/saveData")
        public Mono<Void> saveFacilities() {
            return facilityService.saveFacilities();
        }

        @GetMapping("/{facilityId}")
        public Facility findById(@PathVariable Long facilityId) {
            return facilityService.findById(facilityId);
        }

        @GetMapping("/listAll")
        public List<Facility> findAll() {
            return facilityService.findAll();
        }

        @GetMapping("/list/{groundCategory}")
        public List<Facility> findByGroundCategory(@PathVariable String groundCategory) {
            log.info("###### groundCategory = {}", groundCategory);

            return facilityService.findByGroundCategory(groundCategory);
        }

        @GetMapping("/list")
        public Page<Facility> findByGroundCategoryAndAreaName(@RequestParam(required = false) String groundCategory,
                                                              @RequestParam(required = false) String areaName,
                                                              @RequestParam(defaultValue = "0") int page,
                                                              @RequestParam(defaultValue = "6") int size
        ) {
            log.info("###### groundCategory = {}", groundCategory);
            log.info("###### areaName = {}", areaName);

            return facilityService.findByGroundCategoryAndAreaName(PageRequest.of(page, size), groundCategory, areaName);
        }

        @GetMapping("/list/main")
        public List<Facility> findTopFacilitiesByMeetingCount() {
            return facilityService.findTopFacilitiesByMeetingCount();
        }

        @GetMapping("/list/areaNames")
        public List<String> findAreaNames() {
            return facilityService.findAreaNames();
        }

        @GetMapping("/list/search")
        public Page<Facility> findFacilities(
                @RequestParam(required = false) String groundCategory,
                @RequestParam(required = false) String areaName,
                @RequestParam(required = false) String search,
                Pageable pageable) {
            return facilityService.findByGroundCategoryAndAreaName(groundCategory, areaName, search, pageable);
        }
        
        // 시설명 중복제거된 데이터 (운동맵에서 조회)
        @GetMapping("/allFacility")
        public List<Facility> geDtistrictFacility() {
        	return facilityService.geDtistrictFacility();
        }
        
        @GetMapping("/byPlaceName")
        public List<Facility> getSamePlaceName(@RequestParam String placeName) {
        	return facilityService.getSamePlaceName(placeName);
        }
    }
