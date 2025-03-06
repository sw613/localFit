package com.example.LocalFit.search.service;

import com.example.LocalFit.search.dto.ElasticsearchDTO;
import com.example.LocalFit.search.entity.PartialIndexing;
import com.example.LocalFit.search.utils.CsvToBulkApiConverter;
import com.example.LocalFit.search.utils.S3Utils;
import com.example.LocalFit.search.repository.IndexingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.analysis.*;
import co.elastic.clients.elasticsearch._types.mapping.TypeMapping;
import co.elastic.clients.elasticsearch.core.*;
import co.elastic.clients.elasticsearch.core.bulk.BulkOperation;
import co.elastic.clients.elasticsearch.indices.*;
import co.elastic.clients.elasticsearch.snapshot.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.data.redis.core.RedisTemplate;

import java.io.File;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

@Slf4j
@Service
@RequiredArgsConstructor
public class IndexingService {
    private final S3Utils s3Utils;
    private final CsvToBulkApiConverter csvToBulkApiConverter;
    private final ElasticsearchClient elasticsearchClient;
    private final ObjectMapper objectMapper;
    private final IndexingRepository indexingRepository;
    private final RedisTemplate<String, String> redisTemplate;

    private static final int BULK_SIZE = 2000;
    private static final String INDEX_NAME = "event-data";
    private static final String ALIAS_NAME = "current";
    private static final String SNAPSHOT_STORAGE_NAME = "elastic-search-s3";
    private static final String SNAPSHOT_S3_BUCKET = "elastic-search-s3";
    private static final String SNAPSHOT_NAME = "snapshot-1";
    private static final String INITIALIZATION_KEY = "elasticsearch_initialization_date";


    // 인덱스 생성
    private void createIndexWithEdgeNgram() {
        try {
            IndexSettings indexSettings = new IndexSettings.Builder()
                    .numberOfShards("5")
                    .numberOfReplicas("1")
                    .analysis(createAnalysis())
                    .index(new IndexSettings.Builder().maxNgramDiff(29).build())
                    .build();

            CreateIndexRequest createIndexRequest = new CreateIndexRequest.Builder()
                    .index(INDEX_NAME)
                    .aliases(ALIAS_NAME, new Alias.Builder().isWriteIndex(false).build())
                    .settings(indexSettings)
                    .mappings(createMappings())
                    .build();

            CreateIndexResponse createIndexResponse = elasticsearchClient.indices().create(createIndexRequest);
            log.info("Edge Ngram 설정을 포함한 인덱스가 생성되었습니다: {}", createIndexResponse.index());
        } catch (Exception e) {
            log.error("Edge Ngram 설정을 포함한 인덱스 생성에 실패했습니다: {}", e.getMessage(), e);
        }
    }

    // elasticsearch에 사용할 매핑을 생성
    private TypeMapping createMappings() {
        return new TypeMapping.Builder()
                .properties("HashTag", p -> p.text(t -> t.analyzer("ngram_analyzer").searchAnalyzer("remove_whitespace_analyzer")))
                .build();
    }

    private IndexSettingsAnalysis createAnalysis() {
        return new IndexSettingsAnalysis.Builder()
                .charFilter("remove_whitespace", new CharFilter.Builder().definition(def -> def.patternReplace(r -> r.pattern("\\s+").replacement(""))).build())
                .charFilter("remove_special_characters", new CharFilter.Builder().definition(def -> def.patternReplace(r -> r.pattern("[^\\w\\d]+").replacement(""))).build())
                .tokenizer("ngram_tokenizer", new Tokenizer.Builder().definition(def -> def.ngram(new NGramTokenizer.Builder().minGram(1).maxGram(30).tokenChars(TokenChar.Letter, TokenChar.Digit).build())).build())
                .analyzer("ngram_analyzer", new Analyzer.Builder().custom(custom -> custom.tokenizer("ngram_tokenizer").filter("lowercase").charFilter("remove_whitespace")).build())
                .analyzer("remove_whitespace_analyzer", new Analyzer.Builder().custom(custom -> custom.tokenizer("standard").charFilter("remove_whitespace")).build())
                .build();
    }

    public void handleFullIndexing(String payload) {
        initializeIndexing(); // 초기화 작업

        processPayload(payload);
    }

    private void initializeIndexing() {
        String today = LocalDateTime.now().toString(); // 배포시엔LocalDate.now().toString();
        String lastDate = redisTemplate.opsForValue().get(INITIALIZATION_KEY);

        if (lastDate == null || !lastDate.equals(today)) {
            synchronized (this) {
                lastDate = redisTemplate.opsForValue().get(INITIALIZATION_KEY);
                if (lastDate == null || !lastDate.equals(today)) {
                    try {
                        registerS3Repository(); // S3 저장소 설정
                        deleteExistingSnapshot(); // 기존 스냅샷 삭제
                        backupCurrentData(); // 현재 상태를 스냅샷으로 S3에 저장
                        deleteExistingData(); // 기존 데이터 삭제
                        createIndexIfNotExist(); // 인덱스 생성

                        redisTemplate.opsForValue().set(INITIALIZATION_KEY, today);
                        log.info("Redis 기반 인덱싱 초기화 작업이 완료되었습니다.");
                    } catch (Exception e) {
                        log.error("초기화 작업 중 오류 발생: {}", e.getMessage(), e);
                        throw new IllegalStateException("인덱싱 초기화에 실패했습니다.", e);
                    }
                } else {
                    log.info("오늘은 이미 초기화 작업이 Redis에 의해 처리되었습니다.");
                }
            }
        } else {
            log.info("오늘은 이미 초기화 작업이 Redis에 의해 처리되었습니다.");
        }
    }

    public void processPayload(String payload) {

        String[] s3UrlList = payload.split(",");

        for (String s3Url : s3UrlList) {
            String downloadPath = s3Utils.downloadFileWithRetry(s3Url);

            try (Stream<String> bulkJsonStream = csvToBulkApiConverter.convertCsvToBulkJsonStream(downloadPath, INDEX_NAME)) {
                insertDataToElasticsearch(bulkJsonStream); // Elasticsearch에 데이터 삽입
            } catch (Exception e) {
                log.error("전체 색인 처리 중 오류 발생: {}", e.getMessage(), e);
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
            // 스냅샷이 존재하는지 확인
            GetSnapshotRequest getSnapshotRequest = new GetSnapshotRequest.Builder()
                    .repository(SNAPSHOT_STORAGE_NAME)
                    .snapshot(SNAPSHOT_NAME)  // 확인할 스냅샷 이름
                    .build();

            // 스냅샷이 존재하는지 확인
            GetSnapshotResponse getSnapshotResponse = elasticsearchClient.snapshot().get(getSnapshotRequest);

            // 스냅샷이 존재하면 삭제
            if (!getSnapshotResponse.snapshots().isEmpty()) {
                DeleteSnapshotRequest deleteSnapshotRequest = new DeleteSnapshotRequest.Builder()
                        .repository(SNAPSHOT_STORAGE_NAME)
                        .snapshot(SNAPSHOT_NAME)  // 삭제할 스냅샷 이름
                        .build();

                elasticsearchClient.snapshot().delete(deleteSnapshotRequest);
                log.info("기존 스냅샷이 삭제되었습니다.");
            } else {
                log.info("삭제할 스냅샷이 존재하지 않습니다.");
            }

        } catch (Exception e) {
            log.error("스냅샷 삭제 중 오류 발생: {}", e.getMessage(), e);
        }
    }

    private void backupCurrentData() {
        try {
            GetIndexRequest getIndexRequest = new GetIndexRequest.Builder()
                    .index(INDEX_NAME)  // 확인할 인덱스 이름
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
            log.info("현재 상태의 스냅샷이 생성되어 백업되었습니다.");
        } catch (Exception e) {
            log.error("스냅샷 생성 중 오류 발생: {}", e.getMessage(), e);

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
                processBatch(currentBatch); // 벌크 데이터 처리
                currentBatch.clear();
            }
        });
        if (!currentBatch.isEmpty()) {
            processBatch(currentBatch); // 마지막 배치 처리
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
                log.info("Bulk API 요청 성공, 배치 크기: {}", batch.size());
            }
        } catch (Exception e) {
            log.error("벌크 요청 실패, 배치 크기: {}", batch.size(), e);
            throw new RuntimeException("Bulk 삽입 실패", e);
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

    public void handlePartialIndexingCreate(Map<String, Object> map) {
        try {
            String tag = (String) map.get("HashtagId");

            // PartialIndexing 객체 생성 및 operationType 설정
            PartialIndexing partialIndexing = objectMapper.convertValue(map, PartialIndexing.class);
            partialIndexing.setIndexed(true);

            indexingRepository.save(partialIndexing)
                    .doOnSuccess(saved -> {
                        try {
                            // 중복 방지
                            SearchRequest searchRequest = SearchRequest.of(builder ->
                                    builder.index(INDEX_NAME)
                                            .query(q -> q.term(t -> t.field("HashtagId").value(tag))));

                            SearchResponse<Map> searchResponse = elasticsearchClient.search(searchRequest, Map.class);

                            if (!searchResponse.hits().hits().isEmpty()) {
                                // 업데이트 TODO 해시태그가 업데이트 될 일이 있나?
                                String documentId = searchResponse.hits().hits().get(0).id();
                                IndexRequest<ElasticsearchDTO> updateRequest = IndexRequest.of(updateBuilder ->
                                        updateBuilder.index(INDEX_NAME)
                                                .id(documentId)
                                                .document(objectMapper.convertValue(partialIndexing, ElasticsearchDTO.class)));
                                IndexResponse updateResponse = elasticsearchClient.index(updateRequest);
                                log.info("Elasticsearch 문서 업데이트 완료: {}, 결과: {}", tag, updateResponse.result());
                            } else {
                                // 문서가 존재하지 않으면 새로 생성
                                ElasticsearchDTO elasticsearchDTO = objectMapper.convertValue(partialIndexing, ElasticsearchDTO.class);
                                IndexRequest<ElasticsearchDTO> createRequest = IndexRequest.of(createBuilder ->
                                        createBuilder.index(INDEX_NAME).document(elasticsearchDTO));
                                IndexResponse createResponse = elasticsearchClient.index(createRequest);
                                log.info("Elasticsearch에 문서가 생성되었습니다: {}, 결과: {}", tag, createResponse.result());
                            }
                        } catch (Exception e) {
                            log.error("Elasticsearch 색인 실패: {}", e.getMessage());
                            saved.setIndexed(false);
                            indexingRepository.save(saved).subscribe();
                        }
                    })
                    .doOnError(e -> log.error("MongoDB 저장 실패: {}", e.getMessage()))
                    .subscribe();
        } catch (Exception e) {
            log.error("문서 생성 실패: {}", e.getMessage(), e);
            throw new RuntimeException("문서 생성 실패", e);
        }
    }


}
