package com.example.LocalFit.search.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.analysis.Analyzer;
import co.elastic.clients.elasticsearch._types.analysis.TokenChar;
import co.elastic.clients.elasticsearch._types.analysis.Tokenizer;
import co.elastic.clients.elasticsearch._types.mapping.TypeMapping;
import co.elastic.clients.elasticsearch.core.*;
import co.elastic.clients.elasticsearch.core.bulk.BulkOperation;
import co.elastic.clients.elasticsearch.indices.*;
import co.elastic.clients.elasticsearch.snapshot.*;
import com.example.LocalFit.search.utils.CsvToBulkApiConverter;
import com.example.LocalFit.search.utils.FacilityS3Utils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.io.File;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

@Slf4j
@Service
@RequiredArgsConstructor
public class FacilityIndexingService {

    private final FacilityS3Utils s3Utils;
    private final CsvToBulkApiConverter csvToBulkApiConverter;
    private final ElasticsearchClient elasticsearchClient;
    private final ObjectMapper objectMapper;
    private final RedisTemplate<String, String> redisTemplate;

    private static final int BULK_SIZE = 2000;
    private static final String INDEX_NAME = "sports-facilities";
    private static final String SNAPSHOT_STORAGE_NAME = "facility-snapshot-storage";

    private static final String SNAPSHOT_S3_BUCKET = "elastic-search-s3";
    private static final String SNAPSHOT_NAME = "facility-snapshot";
    private static final String INITIALIZATION_KEY = "facility_indexing_initialized";

    private void createIndexWithEdgeNgram() {
        try {
            IndexSettings indexSettings = new IndexSettings.Builder()
                    .numberOfShards("1")
                    .numberOfReplicas("1")
                    .analysis(createAnalysis())
                    .index(new IndexSettings.Builder().maxNgramDiff(20).build())
                    .build();

            CreateIndexRequest createIndexRequest = new CreateIndexRequest.Builder()
                    .index(INDEX_NAME)
                    .settings(indexSettings)
                    .mappings(createMappings())
                    .build();

            CreateIndexResponse createIndexResponse = elasticsearchClient.indices().create(createIndexRequest);
            log.info("체육 시설 인덱스가 생성되었습니다: {}", createIndexResponse.index());
        } catch (Exception e) {
            log.error("체육 시설 인덱스 생성 실패: {}", e.getMessage(), e);
        }
    }

    private TypeMapping createMappings() {
        return new TypeMapping.Builder()
                .properties("name", p -> p.text(t -> t.analyzer("edge_ngram_analyzer").searchAnalyzer("standard")))
                .properties("maxClassName", p -> p.keyword(k -> k))
                .properties("groundCategory", p -> p.keyword(k -> k))
                .properties("areaName", p -> p.keyword(k -> k))
                .properties("placeName", p -> p.keyword(k -> k))
                .build();
    }

    private IndexSettingsAnalysis createAnalysis() {
        return new IndexSettingsAnalysis.Builder()
                .tokenizer("edge_ngram_tokenizer", new Tokenizer.Builder()
                        .definition(def -> def.edgeNgram(e -> e.minGram(1).maxGram(20).tokenChars(TokenChar.Letter, TokenChar.Digit)))
                        .build())
                .analyzer("edge_ngram_analyzer", new Analyzer.Builder()
                        .custom(custom -> custom.tokenizer("edge_ngram_tokenizer").filter("lowercase"))
                        .build())
                .build();
    }

    public void handleFullIndexing(String payload) {
        initializeIndexing();

        processPayload(payload);
    }

    private void initializeIndexing() {
        String today = LocalDateTime.now().toString();
        String lastDate = redisTemplate.opsForValue().get(INITIALIZATION_KEY);

        if (lastDate == null || !lastDate.equals(today)) {
            synchronized (this) {
                lastDate = redisTemplate.opsForValue().get(INITIALIZATION_KEY);
                if (lastDate == null || !lastDate.equals(today)) {
                    try {
                        registerS3Repository();
                        deleteExistingSnapshot();
                        backupCurrentData();
                        deleteExistingData();
                        createIndexIfNotExist();

                        redisTemplate.opsForValue().set(INITIALIZATION_KEY, today);
                        log.info("체육 시설 인덱싱 초기화 완료.");
                    } catch (Exception e) {
                        log.error("초기화 작업 중 오류 발생: {}", e.getMessage(), e);
                        throw new IllegalStateException("인덱싱 초기화 실패.", e);
                    }
                }
            }
        }
    }


    private void processPayload(String payload) {
        String[] s3UrlList = payload.split(",");

        for (String s3Url : s3UrlList) {
            String downloadPath = s3Utils.downloadFileWithRetry(s3Url);

            try (Stream<String> bulkJsonStream = csvToBulkApiConverter.convertCsvToBulkJsonStream(downloadPath, INDEX_NAME)) {
                insertDataToElasticsearch(bulkJsonStream);
            } catch (Exception e) {
                log.error("체육 시설 전체 색인 중 오류 발생: {}", e.getMessage(), e);
            } finally {
                cleanUpDownloads(downloadPath);
            }
        }
    }

    private void cleanUpDownloads(String filePath) {
        File file = new File(filePath);

        if (file.exists() && file.isFile()) {
            boolean deleted = file.delete();
            if (deleted) {
                log.info("File deleted: {}", filePath);
            } else {
                log.warn("Failed to delete file: {}", filePath);
            }
        } else {
            log.warn("File not found or not a regular file: {}", filePath);
        }

        // downloads 디렉토리 삭제 시도 (폴더가 비어있을 경우만 삭제)
        String downloadDirPath = file.getParent();
        File downloadDir = new File(downloadDirPath);

        if (downloadDir.exists() && downloadDir.isDirectory()) {
            File[] remainingFiles = downloadDir.listFiles();
            if (remainingFiles != null && remainingFiles.length == 0) {
                boolean dirDeleted = downloadDir.delete();
                if (dirDeleted) {
                    log.info("Downloads directory deleted: {}", downloadDirPath);
                } else {
                    log.warn("Failed to delete downloads directory: {}", downloadDirPath);
                }
            }
        }
    }

    private CreateRepositoryResponse registerS3Repository() {
        try {
            Repository repository = new Repository.Builder()
                    .s3(builder -> builder
                            .settings(settings -> settings
                                    .bucket(SNAPSHOT_S3_BUCKET)
                                    .basePath("elasticsearch/snapshot")
                            ))
                    .build();

            CreateRepositoryRequest repositoryRequest = new CreateRepositoryRequest.Builder()
                    .repository(repository)
                    .name(SNAPSHOT_STORAGE_NAME)
                    .build();


            CreateRepositoryResponse response = elasticsearchClient.snapshot().createRepository(repositoryRequest);

            log.info("S3 저장소가 성공적으로 등록되었습니다.");

            return response;
        } catch (Exception e) {
            log.error("S3 저장소 등록에 실패했습니다: {}", e.getMessage(), e);
            throw new RuntimeException("S3 저장소 등록에 실패했습니다.");
        }
    }


    private void deleteExistingSnapshot() {
        try {
            GetSnapshotRequest getSnapshotRequest = new GetSnapshotRequest.Builder()
                    .repository(SNAPSHOT_STORAGE_NAME)
                    .snapshot(SNAPSHOT_NAME)
                    .build();

            GetSnapshotResponse snapshotResponse = elasticsearchClient.snapshot().get(getSnapshotRequest);

            if (!snapshotResponse.snapshots().isEmpty()) {
                DeleteSnapshotRequest deleteSnapshotRequest = new DeleteSnapshotRequest.Builder()
                        .repository(SNAPSHOT_STORAGE_NAME)
                        .snapshot(SNAPSHOT_NAME)
                        .build();

                elasticsearchClient.snapshot().delete(deleteSnapshotRequest);
                log.info("기존 스냅샷이 삭제되었습니다.");
            } else {
                log.info("삭제할 스냅샷이 없습니다. (이미 존재하지 않음)");
            }
        } catch (Exception e) {
            if (e.getMessage().contains("snapshot_missing_exception")) {
                log.warn("삭제할 스냅샷이 없음. 그냥 넘어감.");
            } else {
                log.error("스냅샷 삭제 중 오류 발생: {}", e.getMessage(), e);
            }
        }
    }


    private void backupCurrentData() {
        try {
            GetIndexRequest getIndexRequest = new GetIndexRequest.Builder()
                    .index(INDEX_NAME)
                    .build();

            try {
                elasticsearchClient.indices().get(getIndexRequest);
            } catch (Exception e) {
                log.error("백업할 인덱스가 존재하지 않습니다: {}", INDEX_NAME);
                return;
            }

            CreateSnapshotRequest snapshotRequest = new CreateSnapshotRequest.Builder()
                    .repository(SNAPSHOT_STORAGE_NAME)
                    .snapshot(SNAPSHOT_NAME)
                    .indices(INDEX_NAME)
                    .build();

            elasticsearchClient.snapshot().create(snapshotRequest);
            log.info("새로운 스냅샷이 생성되었습니다: {}", SNAPSHOT_NAME);
        } catch (Exception e) {
            if (e.getMessage().contains("snapshot_name_already_in_use_exception")) {
                log.warn("같은 이름의 스냅샷이 이미 존재. 새로운 이름으로 시도.");
            } else {
                log.error("스냅샷 생성 중 오류 발생: {}", e.getMessage(), e);
            }
        }
    }


    private void deleteExistingData() {
        try {
            SearchRequest searchRequest = new SearchRequest.Builder()
                    .index(INDEX_NAME)
                    .size(1)
                    .build();

            SearchResponse<Void> searchResponse = elasticsearchClient.search(searchRequest, Void.class);

            if (searchResponse.hits().total().value() > 0) {
                // 데이터가 있다면 삭제
                DeleteByQueryRequest deleteRequest = new DeleteByQueryRequest.Builder()
                        .index(INDEX_NAME)
                        .query(q -> q.matchAll(m -> m)) // 모든 데이터를 삭제
                        .build();

                elasticsearchClient.deleteByQuery(deleteRequest);
                log.info("기존 데이터가 삭제되었습니다.");
            } else {
                log.info("데이터가 존재하지 않아 삭제하지 않았습니다.");
            }
        } catch (Exception e) {
            log.error("기존 데이터 삭제 중 오류 발생: {}", e.getMessage(), e);
        }
    }

    private void createIndexIfNotExist() {
        try {
            // 인덱스 존재 확인
            GetIndexRequest getIndexRequest = new GetIndexRequest.Builder()
                    .index(INDEX_NAME)
                    .build();

            try {
                elasticsearchClient.indices().get(getIndexRequest);
                log.info("인덱스가 이미 존재합니다: {}", INDEX_NAME);
            } catch (Exception e) {
                log.info("인덱스가 존재하지 않아서 새로 생성합니다: {}", INDEX_NAME);
                createIndexWithEdgeNgram();  // 인덱스가 없다면 인덱스 생성
            }
        } catch (Exception e) {
            log.error("인덱스 생성 중 오류 발생: {}", e.getMessage(), e);
        }
    }

    private void insertDataToElasticsearch(Stream<String> bulkJsonStream) {
        List<String> currentBatch = new ArrayList<>();
        bulkJsonStream.forEach(jsonLine -> {
            currentBatch.add(jsonLine);
            if (currentBatch.size() >= BULK_SIZE) {
                processBatch(currentBatch);
                currentBatch.clear();
            }
        });
        if (!currentBatch.isEmpty()) {
            processBatch(currentBatch);
        }
    }
    private void processBatch(List<String> batch) {
        try {
            List<BulkOperation> operations = new ArrayList<>();
            for (String jsonLine : batch) {
                JsonNode jsonNode = objectMapper.readTree(jsonLine.split("\n")[1]);
                BulkOperation operation = BulkOperation.of(b -> b.index(i -> i.index(INDEX_NAME).document(jsonNode)));
                operations.add(operation);
            }

            BulkRequest bulkRequest = new BulkRequest.Builder().operations(operations).build();
            BulkResponse bulkResponse = elasticsearchClient.bulk(bulkRequest);

            if (bulkResponse.errors()) {
                bulkResponse.items().forEach(item -> {
                    if (item.error() != null) {
                        log.error("벌크 작업 실패: {}", item.error().reason());
                    }
                });
            } else {
                log.info("체육 시설 데이터 벌크 삽입 성공, 배치 크기: {}", batch.size());
            }
        } catch (Exception e) {
            log.error("체육 시설 벌크 요청 실패, 배치 크기: {}", batch.size(), e);
        }
    }

    private void transformJsonArrayField(JsonNode jsonNode, String fieldName) {
        if (jsonNode.has(fieldName) && jsonNode.get(fieldName).isTextual()) {
            try {
                String fieldString = jsonNode.get(fieldName).asText();
                JsonNode fieldArray = objectMapper.readTree(fieldString);
                ((ObjectNode) jsonNode).set(fieldName, fieldArray);
            } catch (Exception e) {
                log.error("{} 필드를 JSON 배열로 변환하는데 실패했습니다", fieldName, e);
            }
        }
    }



}
