package com.example.filestorage.storage;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.stream.Stream;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
@ConditionalOnProperty(name = "storage.type", havingValue = "local", matchIfMissing = true)
public class LocalFileStorageService implements StorageService {

  private final Path rootLocation;

  public LocalFileStorageService(StorageProperties properties) {
    if (properties.getLocation() == null || properties.getLocation().trim().isEmpty()) {
      throw new StorageException("File upload location can not be empty.");
    }
    this.rootLocation = Paths.get(properties.getLocation());
  }

  @Override
  public void store(MultipartFile file) {
    try {
      if (file == null || file.isEmpty()) {
        throw new StorageException("Failed to store empty file.");
      }
      String originalFilename = FilenamePolicy.requireSafe(file.getOriginalFilename());
      Path destinationFile = this.rootLocation.resolve(Paths.get(originalFilename))
          .normalize()
          .toAbsolutePath();
      if (!destinationFile.getParent().equals(this.rootLocation.toAbsolutePath())) {
        throw new StorageException("Cannot store file outside current directory.");
      }
      try (InputStream inputStream = file.getInputStream()) {
        Files.copy(inputStream, destinationFile, StandardCopyOption.REPLACE_EXISTING);
      }
    } catch (IOException e) {
      throw new StorageException("Failed to store file.", e);
    }
  }

  @Override
  public Stream<Path> loadAll() {
    try {
      return Files.walk(this.rootLocation, 1)
          .filter(path -> !path.equals(this.rootLocation))
          .map(this.rootLocation::relativize);
    } catch (IOException e) {
      throw new StorageException("Failed to read stored files", e);
    }
  }

  @Override
  public Path load(String filename) {
    return rootLocation.resolve(FilenamePolicy.requireSafe(filename));
  }

  @Override
  public Resource loadAsResource(String filename) {
    try {
      Path file = load(filename);
      Resource resource = new UrlResource(file.toUri());
      if (resource.exists() && resource.isReadable()) {
        return resource;
      }
      throw new StorageFileNotFoundException("Could not read file: " + filename);
    } catch (MalformedURLException e) {
      throw new StorageFileNotFoundException("Could not read file: " + filename, e);
    }
  }

  @Override
  public void deleteAll() {
    try {
      FileSystemUtils.deleteRecursively(rootLocation.toFile());
    } catch (Exception e) {
      throw new StorageException("Could not clear storage directory.", e);
    }
  }

  @Override
  public void delete(String filename) {
    Path target = rootLocation.resolve(FilenamePolicy.requireSafe(filename)).normalize().toAbsolutePath();
    if (target.getParent() != null && target.getParent().equals(rootLocation.toAbsolutePath())) {
      try {
        Files.deleteIfExists(target);
      } catch (IOException e) {
        throw new StorageException("Failed to delete file: " + filename, e);
      }
    }
  }

  @Override
  public void init() {
    try {
      Files.createDirectories(rootLocation);
    } catch (IOException e) {
      throw new StorageException("Could not initialize storage", e);
    }
  }
}
