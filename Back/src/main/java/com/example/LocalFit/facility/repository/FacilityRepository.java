package com.example.LocalFit.facility.repository;

import com.example.LocalFit.facility.entity.Facility;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface FacilityRepository extends JpaRepository<Facility, Long> {
    List<Facility> findByGroundCategory(String groundCategory);
    Page<Facility> findByGroundCategory(String groundCategory, Pageable pageable);

    @Query("SELECT f FROM Facility f WHERE f.groundCategory NOT IN ('축구장', '야구장', '테니스장', '족구장', '풋살장')")
    Page<Facility> findByGroundCategoryNotIn(Pageable pageable);

    @Query("SELECT f FROM Facility f WHERE f.groundCategory NOT IN ('축구장', '야구장', '테니스장', '족구장', '풋살장') AND f.areaName = :areaName")
    Page<Facility> findByGroundCategoryNotInAndNameAndAreaName(String areaName, Pageable pageable);

    @Query("SELECT f FROM Facility f WHERE f.groundCategory = :groundCategory AND :areaName = ''")
    Page<Facility> findByGroundCategoryAndAreaNameIsNull(String groundCategory, String areaName, Pageable pageable);

    @Query("SELECT f FROM Facility f WHERE f.groundCategory = :groundCategory AND f.areaName = :areaName")
    Page<Facility> findByGroundCategoryAndAreaName(String groundCategory, String areaName, Pageable pageable);


    @Query("SELECT f.areaName FROM Facility f WHERE f.areaName IS NOT NULL GROUP BY f.areaName ORDER BY f.areaName ASC")
    List<String> findAllAreaName();

    @Query("SELECT f FROM Facility f WHERE f.id IN " +
            "(SELECT m.facility.id FROM Meeting m " +
            "GROUP BY m.facility.id " +
            "ORDER BY COUNT(m.id) DESC)")
    List<Facility> findTopFacilitiesByMeetingCount(Pageable pageable);


    @Query("SELECT MIN(f.id) FROM Facility f")
    Long findMinId();

    @Query("SELECT MAX(f.id) FROM Facility f")
    Long findMaxId();

    @Query("SELECT f.id FROM Facility f WHERE f.id BETWEEN :startId AND :endId ORDER BY f.id ASC")
    List<Long> findFacilityIdsByRange(@Param("startId") Long startId, @Param("endId") Long endId);


    @Query(value = "SELECT f.facility_id AS id, f.name AS name, f.max_class_name AS maxClassName, f.ground_category AS groundCategory, f.area_name AS areaName, f.place_name AS placeName " +
            "FROM facility f " +
            "WHERE f.facility_id = :facilityId",
            nativeQuery = true)
    Map<String, Object> findFacilityIndexingInfoRaw(@Param("facilityId") Long facilityId);

    @Query("SELECT f FROM Facility f WHERE " +
            "(:groundCategory IS NULL OR f.groundCategory = :groundCategory) AND " +
            "(:areaName IS NULL OR f.areaName = :areaName) AND " +
            "(COALESCE(:search, '') = '' OR LOWER(f.name) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Facility> findByGroundCategoryAndAreaNameAndSearch(
            @Param("groundCategory") String groundCategory,
            @Param("areaName") String areaName,
            @Param("search") String search,
            Pageable pageable);


    @Query("SELECT f FROM Facility f WHERE " +
            "(:groundCategory IS NULL OR f.groundCategory = :groundCategory) AND " +
            "(:search IS NULL OR LOWER(f.name) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Facility> findByGroundCategoryAndSearch(
            @Param("groundCategory") String groundCategory,
            @Param("search") String search,
            Pageable pageable);

    //커밋용 더미
    
    
    // 시설명 데이터 중복제거   
    @Query(value = """
    	    SELECT * FROM (
    	        SELECT *, ROW_NUMBER() OVER (PARTITION BY place_name ORDER BY facility_id DESC) AS rn
    	        FROM facility
    	    ) AS temp
    	    WHERE rn = 1
    	""", nativeQuery = true)
    List<Facility> findDistinctFacilities();    
   
    List<Facility> findByPlaceName(String placeName);

}
