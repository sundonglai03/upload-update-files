package com.example.filestorage.storage;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Stream;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@ConditionalOnProperty(name = "storage.type", havingValue = "database")
public class DatabaseStorageService implements StorageService {

  private final StorageFileRepository storageFileRepository;

  public DatabaseStorageService(StorageFileRepository storageFileRepository) {
    this.storageFileRepository = storageFileRepository;
  }

  @Override
  public void init() {
    // database table is created by Hibernate automatically
  }

  @Override
  @Transactional
  public void store(MultipartFile file) {
    if (file == null || file.isEmpty()) {
      throw new StorageException("Failed to store empty file.");
    }

    String originalFilename = FilenamePolicy.requireSafe(file.getOriginalFilename());

    try {
      byte[] content = file.getBytes();
      storageFileRepository.findByFilename(originalFilename)
          .ifPresentOrElse(
              existing -> {
                existing.setContent(content);
                existing.setContentType(file.getContentType());
                existing.setSize((long) content.length);
                storageFileRepository.save(existing);
              },
              () -> storageFileRepository.save(new StorageFileEntity(originalFilename, file.getContentType(), content))
          );
    } catch (Exception e) {
      throw new StorageException("Failed to store file in database.", e);
    }
  }

  @Override
  @Transactional(readOnly = true)
  public Stream<Path> loadAll() {
    List<StorageFileEntity> files = storageFileRepository.findAllByOrderByCreatedAtDesc();
    return files.stream().map(file -> Paths.get(file.getFilename()));
  }

  @Override
  public Path load(String filename) {
    return Paths.get(FilenamePolicy.requireSafe(filename));
  }

  @Override
  @Transactional(readOnly = true)
  public Resource loadAsResource(String filename) {
    String safeFilename = FilenamePolicy.requireSafe(filename);
    return storageFileRepository.findByFilename(safeFilename)
        .map(file -> new ByteArrayResource(file.getContent()) {
          @Override
          public String getFilename() {
            return file.getFilename();
          }
        })
        .orElseThrow(() -> new StorageFileNotFoundException("Could not read file: " + filename));
  }

  @Override
  @Transactional
  public void deleteAll() {
    storageFileRepository.deleteAll();
  }

  @Override
  @Transactional
  public void delete(String filename) {
    storageFileRepository.deleteByFilename(FilenamePolicy.requireSafe(filename));
  }
}
