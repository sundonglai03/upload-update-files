package com.example.filestorage.storage;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import io.minio.BucketExistsArgs;
import io.minio.GetObjectArgs;
import io.minio.ListObjectsArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.Result;
import io.minio.messages.Item;

@Service
@ConditionalOnProperty(name = "storage.type", havingValue = "object")
public class ObjectStorageService implements StorageService {

  private final MinioClient minioClient;
  private final StorageProperties properties;

  public ObjectStorageService(StorageProperties properties) {
    this.properties = properties;
    this.minioClient = MinioClient.builder()
        .endpoint(properties.getEndpoint())
        .credentials(properties.getAccessKey(), properties.getSecretKey())
        .region(properties.getRegion())
        .build();
    init();
  }

  @Override
  public void init() {
    try {
      boolean exists = minioClient.bucketExists(BucketExistsArgs.builder().bucket(properties.getBucket()).build());
      if (!exists) {
        minioClient.makeBucket(io.minio.MakeBucketArgs.builder().bucket(properties.getBucket()).build());
      }
    } catch (Exception e) {
      throw new StorageException("Could not initialize object storage bucket.", e);
    }
  }

  @Override
  public void store(MultipartFile file) {
    if (file == null || file.isEmpty()) {
      throw new StorageException("Failed to store empty file.");
    }
    try {
      String filename = file.getOriginalFilename();
      if (filename == null || filename.isBlank()) {
        throw new StorageException("Filename cannot be empty.");
      }
      minioClient.putObject(
          io.minio.PutObjectArgs.builder()
              .bucket(properties.getBucket())
              .object(filename)
              .stream(new ByteArrayInputStream(file.getBytes()), file.getSize(), -1)
              .contentType(file.getContentType())
              .build());
    } catch (Exception e) {
      throw new StorageException("Failed to store file in object storage.", e);
    }
  }

  @Override
  public Stream<Path> loadAll() {
    try {
      List<Path> paths = new ArrayList<>();
      Iterable<Result<Item>> results = minioClient.listObjects(ListObjectsArgs.builder()
          .bucket(properties.getBucket())
          .build());
      for (Result<Item> result : results) {
        Item item = result.get();
        if (!item.isDir()) {
          paths.add(Paths.get(item.objectName()));
        }
      }
      return paths.stream();
    } catch (Exception e) {
      throw new StorageException("Failed to list files in object storage.", e);
    }
  }

  @Override
  public Path load(String filename) {
    return Paths.get(filename);
  }

  @Override
  public Resource loadAsResource(String filename) {
    try {
      byte[] bytes = minioClient.getObject(
          GetObjectArgs.builder().bucket(properties.getBucket()).object(filename).build()).readAllBytes();
      return new ByteArrayResource(bytes) {
        @Override
        public String getFilename() {
          return filename;
        }
      };
    } catch (Exception e) {
      throw new StorageFileNotFoundException("Could not read file: " + filename, e);
    }
  }

  @Override
  public void deleteAll() {
    try {
      for (Result<Item> result : minioClient.listObjects(ListObjectsArgs.builder().bucket(properties.getBucket()).build())) {
        Item item = result.get();
        if (!item.isDir()) {
          minioClient.removeObject(RemoveObjectArgs.builder().bucket(properties.getBucket()).object(item.objectName()).build());
        }
      }
    } catch (Exception e) {
      throw new StorageException("Failed to clear object storage bucket.", e);
    }
  }

  @Override
  public void delete(String filename) {
    try {
      minioClient.removeObject(RemoveObjectArgs.builder().bucket(properties.getBucket()).object(filename).build());
    } catch (Exception e) {
      throw new StorageException("Failed to delete file: " + filename, e);
    }
  }
}
