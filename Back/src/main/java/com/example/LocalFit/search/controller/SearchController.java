package com.example.LocalFit.search.controller;

import com.example.LocalFit.search.dto.AutoCompleteFacilityRes;
import com.example.LocalFit.search.dto.AutoCompleteRes;
import com.example.LocalFit.search.service.SearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/v1/search")
@RequiredArgsConstructor
public class SearchController {

    private final SearchService searchService;
    @GetMapping("/hashtag/auto-complete")
    public Mono<List<AutoCompleteRes>> autoComplete(@RequestParam("query") String query) {
        return Mono.fromFuture(() -> CompletableFuture.supplyAsync(() -> searchService.autoComplete(query)));
    }

    @GetMapping("/facility/auto-complete")
    public Mono<List<AutoCompleteFacilityRes>> autoFacilityComplete(@RequestParam("query") String query) {
        return Mono.fromFuture(() -> CompletableFuture.supplyAsync(() -> searchService.autoCompleteFacility(query)));
    }
}
