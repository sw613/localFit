package com.example.LocalFit.search.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import static com.amazonaws.services.s3.model.DeleteObjectsRequest.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class ElasticS3Service {

    private final AmazonS3 amazonS3;
    private final ConcurrentHashMap<String, AtomicLong> sequenceMap = new ConcurrentHashMap<>();

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

    private static final String BUCKET_NAME = "elastic-search-s3"; // 엘라스틱서치 데이터 저장용 버킷
    private static final String EVENT_INFO_LIST_FOLDER = "event_info_list";

    private static final String FACILITY_INFO_LIST_FOLDER = "facility_info_list"; // facility 저장


    public String uploadEventListInfoFile(File file) {
        ObjectMetadata objectMetadata = new ObjectMetadata();
        objectMetadata.setContentType("text/csv");
        objectMetadata.setContentEncoding("UTF-8");
        objectMetadata.setContentLength(file.length());

        int index = file.getName().lastIndexOf(".");
        String ext = (index > 0) ? file.getName().substring(index + 1) : "csv";

        long fileSequence = getNextSequence();

        // S3에 저장할 파일 키 날짜 + 순번
        String key = String.format(
                "%s/%s_%05d.%s",
                EVENT_INFO_LIST_FOLDER,
                DATE_FORMAT.format(new Date()),
                fileSequence,
                ext
        );
        try (FileInputStream fileInputStream = new FileInputStream(file)) {
            amazonS3.putObject(new PutObjectRequest(BUCKET_NAME, key, fileInputStream, objectMetadata));
        } catch (IOException e) {
            throw new RuntimeException("Failed to upload file", e);
        }

        return amazonS3.getUrl(BUCKET_NAME, key).toString();
    }

    private long getNextSequence() {
        String today = DATE_FORMAT.format(new Date());

        sequenceMap.putIfAbsent(today, new AtomicLong(0));
        return sequenceMap.get(today).incrementAndGet();
    }

    public List<String> getFiles() {
        List<String> fileKeys = new ArrayList<>();
        String continuationToken = null;

        do {
            ListObjectsV2Request request = new ListObjectsV2Request()
                    .withBucketName(BUCKET_NAME)
                    .withPrefix(EVENT_INFO_LIST_FOLDER)
                    .withContinuationToken(continuationToken);

            ListObjectsV2Result result = amazonS3.listObjectsV2(request);
            for (S3ObjectSummary objectSummary : result.getObjectSummaries()) {
                fileKeys.add(objectSummary.getKey());
            }

            continuationToken = result.getNextContinuationToken();
        } while (continuationToken != null);

        return fileKeys;
    }

    public void deleteAllElasticData() {
        ObjectListing objectListing = amazonS3.listObjects(BUCKET_NAME, EVENT_INFO_LIST_FOLDER);

        while (true) {
            List<DeleteObjectsRequest.KeyVersion> keysToDelete = new ArrayList<>();
            for (S3ObjectSummary summary : objectListing.getObjectSummaries()) {
                keysToDelete.add(new DeleteObjectsRequest.KeyVersion(summary.getKey()));
            }

            if (!keysToDelete.isEmpty()) {
                DeleteObjectsRequest deleteObjectsRequest = new DeleteObjectsRequest(BUCKET_NAME)
                        .withKeys(keysToDelete)
                        .withQuiet(true);
                amazonS3.deleteObjects(deleteObjectsRequest);
            }

            if (objectListing.isTruncated()) {
                objectListing = amazonS3.listNextBatchOfObjects(objectListing);
            } else {
                break;
            }
        }
    }

    // 아래부터 facility

    public String uploadFacilityListInfoFile(File file) {
        ObjectMetadata objectMetadata = new ObjectMetadata();
        objectMetadata.setContentType("text/csv");
        objectMetadata.setContentEncoding("UTF-8");
        objectMetadata.setContentLength(file.length());

        int index = file.getName().lastIndexOf(".");
        String ext = (index > 0) ? file.getName().substring(index + 1) : "csv";

        long fileSequence = getNextSequence();

        // S3에 저장할 파일 키 날짜 + 순번
        String key = String.format(
                "%s/%s_%05d.%s",
                FACILITY_INFO_LIST_FOLDER,
                DATE_FORMAT.format(new Date()),
                fileSequence,
                ext
        );
        try (FileInputStream fileInputStream = new FileInputStream(file)) {
            amazonS3.putObject(new PutObjectRequest(BUCKET_NAME, key, fileInputStream, objectMetadata));
        } catch (IOException e) {
            throw new RuntimeException("Failed to upload file", e);
        }

        return amazonS3.getUrl(BUCKET_NAME, key).toString();
    }

    public List<String> getFacilityFiles() {
        List<String> fileKeys = new ArrayList<>();
        String continuationToken = null;

        do {
            ListObjectsV2Request request = new ListObjectsV2Request()
                    .withBucketName(BUCKET_NAME)
                    .withPrefix(FACILITY_INFO_LIST_FOLDER)
                    .withContinuationToken(continuationToken);

            ListObjectsV2Result result = amazonS3.listObjectsV2(request);
            for (S3ObjectSummary objectSummary : result.getObjectSummaries()) {
                fileKeys.add(objectSummary.getKey());
            }

            continuationToken = result.getNextContinuationToken();
        } while (continuationToken != null);

        return fileKeys;
    }

    public void deleteAllFacilityElasticData() {
        ObjectListing objectListing = amazonS3.listObjects(BUCKET_NAME, FACILITY_INFO_LIST_FOLDER);

        while (true) {
            List<DeleteObjectsRequest.KeyVersion> keysToDelete = new ArrayList<>();
            for (S3ObjectSummary summary : objectListing.getObjectSummaries()) {
                keysToDelete.add(new DeleteObjectsRequest.KeyVersion(summary.getKey()));
            }

            if (!keysToDelete.isEmpty()) {
                DeleteObjectsRequest deleteObjectsRequest = new DeleteObjectsRequest(BUCKET_NAME)
                        .withKeys(keysToDelete)
                        .withQuiet(true);
                amazonS3.deleteObjects(deleteObjectsRequest);
            }

            if (objectListing.isTruncated()) {
                objectListing = amazonS3.listNextBatchOfObjects(objectListing);
            } else {
                break;
            }
        }
    }
}
