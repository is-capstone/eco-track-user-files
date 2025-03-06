package com.enzulode.file.integration.s3;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.transfer.s3.S3TransferManager;

import java.net.URI;

import static software.amazon.awssdk.transfer.s3.SizeConstant.MB;

@Configuration
public class S3Config {

  @Value("${spring.aws.s3.endpoint}") private String endpoint;
  @Value("${spring.aws.s3.access-key}") private String accessKey;
  @Value("${spring.aws.s3.secret-key}") private String secretKey;

  @Bean
  public S3Client s3Client() {
    var creds = AwsBasicCredentials.create(accessKey, secretKey);
    return S3Client.builder()
        .endpointOverride(URI.create(endpoint))
        .region(Region.of("ru-1"))
        .credentialsProvider(StaticCredentialsProvider.create(creds))
        .build();
  }

  @Bean
  public S3AsyncClient s3AsyncClient() {
    var creds = AwsBasicCredentials.create(accessKey, secretKey);
    return S3AsyncClient.builder()
        .endpointOverride(URI.create(endpoint))
        .region(Region.of("ru-1"))
        .credentialsProvider(StaticCredentialsProvider.create(creds))
        .multipartEnabled(true)
        .multipartConfiguration(mc -> mc
            .minimumPartSizeInBytes(50 * MB)
            .thresholdInBytes(0L)
            .apiCallBufferSizeInBytes(200 * MB)
        )
        .build();
  }

  @Bean
  public S3TransferManager s3TransferManager(S3AsyncClient client) {
    return S3TransferManager.builder()
        .s3Client(client)
        .build();
  }
}
