package com.ecommerce.service.impl;


import com.ecommerce.config.S3Config;
import com.ecommerce.entity.Product;
import com.ecommerce.repository.ProductRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.errors.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectAclRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.util.UUID;

@Slf4j
@Service
public class ImageService {

    private final S3Client s3Client;
    private final ProductRepository productRepository;

    public ImageService(S3Client s3Client, ProductRepository productRepository) {
        this.s3Client = s3Client;
        this.productRepository = productRepository;
    }

    @Value("${aws.s3.bucket-name}")
    private String bucketName;

    @Value("${aws.region}")
    private String region;

    public String uploadProductImage(Long productId, MultipartFile file) throws IOException{

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("product not found: " + productId));

        String originalFileName = file.getOriginalFilename();
        String extension = "";
        if (originalFileName != null && originalFileName.contains(".")){
            extension = originalFileName.substring(originalFileName.lastIndexOf("."));
        }

        String key = "products/" + productId + "/" + UUID.randomUUID() + extension;

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .contentType(file.getContentType())
                .build();

        log.info("Uploading image to S3: bucket={} key={}", bucketName, key);

        s3Client.putObject(putObjectRequest, RequestBody.fromBytes(file.getBytes()));

        String url = "https://" + bucketName + ".s3." + region + ".amazonaws.com/" + key;

        product.setImageUrl(url);
        productRepository.save(product);
        return url;

    }
}
