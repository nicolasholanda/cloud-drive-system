package com.nicolas.clouddrive.storage;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.BucketAlreadyOwnedByYouException;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Service
public class BlockStorageService {

    private final S3Client s3Client;
    private final String bucket;

    public BlockStorageService(S3Client s3Client, @Value("${cloud.aws.s3.bucket}") String bucket) {
        this.s3Client = s3Client;
        this.bucket = bucket;
    }

    @PostConstruct
    public void ensureBucketExists() {
        try {
            s3Client.createBucket(CreateBucketRequest.builder().bucket(bucket).build());
        } catch (BucketAlreadyOwnedByYouException ignored) {
        }
    }

    public void uploadBlock(String storageKey, byte[] content) {
        s3Client.putObject(
                PutObjectRequest.builder().bucket(bucket).key(storageKey).build(),
                RequestBody.fromBytes(content));
    }
}
