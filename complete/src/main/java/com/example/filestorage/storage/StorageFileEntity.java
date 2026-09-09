package com.example.filestorage.storage;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "storage_files")
public class StorageFileEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true)
  private String filename;

  @Column(name = "content_type")
  private String contentType;

  @Column(nullable = false)
  private Long size;

  @Lob
  @Column(nullable = false)
  private byte[] content;

  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  protected StorageFileEntity() {
  }

  public StorageFileEntity(String filename, String contentType, byte[] content) {
    this.filename = filename;
    this.contentType = contentType;
    this.content = content;
    this.size = (long) (content == null ? 0 : content.length);
  }

  @PrePersist
  protected void onCreate() {
    this.createdAt = LocalDateTime.now();
  }

  public Long getId() {
    return id;
  }

  public String getFilename() {
    return filename;
  }

  public void setFilename(String filename) {
    this.filename = filename;
  }

  public String getContentType() {
    return contentType;
  }

  public void setContentType(String contentType) {
    this.contentType = contentType;
  }

  public Long getSize() {
    return size;
  }

  public void setSize(Long size) {
    this.size = size;
  }

  public byte[] getContent() {
    return content;
  }

  public void setContent(byte[] content) {
    this.content = content;
    this.size = (long) (content == null ? 0 : content.length);
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }
}
