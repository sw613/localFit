package com.example.LocalFit.search.repository;

import com.example.LocalFit.search.entity.PartialIndexing;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Mono;

public interface IndexingRepository extends ReactiveMongoRepository<PartialIndexing, String> {
    Mono<PartialIndexing> findByHashTagId(String hashtagId);
}
