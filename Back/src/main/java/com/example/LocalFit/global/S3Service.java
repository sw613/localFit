package com.example.LocalFit.global;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.*;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.amazonaws.services.s3.AmazonS3;

import lombok.RequiredArgsConstructor;

@Service
@Component
@RequiredArgsConstructor
public class S3Service {

    private final AmazonS3 amazonS3;

    private final String bucketName = "elastic-search-s3";

    public String upload(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("파일이 비어있습니다.");
        }

        String originalName = file.getOriginalFilename();
        String ext = "";
        if (originalName != null && originalName.contains(".")) {
            ext = originalName.substring(originalName.lastIndexOf("."));
        }
        String fileName = UUID.randomUUID().toString() + ext;

        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(file.getSize());
        metadata.setContentType(file.getContentType());

        try {
            amazonS3.putObject(new PutObjectRequest(bucketName, fileName, file.getInputStream(), metadata));
        } catch (IOException e) {
            throw new RuntimeException("파일 업로드 중 오류", e);
        }

        return amazonS3.getUrl(bucketName, fileName).toString();
    }


}
