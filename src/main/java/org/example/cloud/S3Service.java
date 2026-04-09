package org.example.cloud;

import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class S3Service {
    private final S3Client s3;
    private final String bucket;

    public S3Service(S3Client s3, String bucket) {
        this.s3 = s3;
        this.bucket = bucket;
    }

    public void upload(String key, byte[] data) {
        s3.putObject(PutObjectRequest.builder().bucket(bucket).key(key).build(), RequestBody.fromBytes(data));
    }

    public byte[] download(String key) throws S3Exception, InvalidObjectStateException, S3Exception, AwsServiceException, SdkClientException, IOException {
        return s3.getObject(GetObjectRequest.builder().bucket(bucket).key(key).build()).readAllBytes();
    }

    public void deleteObject(String key) {
        s3.deleteObject(DeleteObjectRequest.builder().bucket(bucket).key(key).build());
    }

    public List<String> listMetadataKeys() {
        return s3.listObjectsV2(ListObjectsV2Request.builder().bucket(bucket).prefix("metadata/").build())
                .contents().stream().map(S3Object::key).collect(Collectors.toList());
    }
}