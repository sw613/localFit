package com.example.LocalFit.search;

import com.example.LocalFit.search.service.ElasticS3Service;
import com.example.LocalFit.search.utils.CSVGenerator;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/csv")
public class CsvTestController {
    private final CSVGenerator csvGenerator;
    private final ElasticS3Service elasticS3Service;

    public CsvTestController(CSVGenerator csvGenerator, ElasticS3Service elasticS3Service) {
        this.csvGenerator = csvGenerator;
        this.elasticS3Service = elasticS3Service;
    }

    @GetMapping("/test")
    public String generateAndUploadCsv() throws IOException {
        List<Long> sampleData = List.of(1001L, 1002L, 1003L); // 더미 데이터
        String filePath = "test.csv";

        File csvFile = csvGenerator.generateCsv(sampleData, filePath);
        elasticS3Service.uploadEventListInfoFile(csvFile);

        return "CSV 파일이 S3에 업로드되었습니다: " + filePath;
    }
}
