package com.example.filestorage.storage;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface StorageFileRepository extends JpaRepository<StorageFileEntity, Long> {

  Optional<StorageFileEntity> findByFilename(String filename);

  void deleteByFilename(String filename);

  List<StorageFileEntity> findAllByOrderByCreatedAtDesc();
}
