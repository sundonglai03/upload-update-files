package com.example.filestorage.storage;

final class FilenamePolicy {

  private FilenamePolicy() {
  }

  static String requireSafe(String filename) {
    if (filename == null || filename.isBlank()) {
      throw new StorageException("Filename cannot be empty.");
    }
    String cleaned = filename.trim();
    if (cleaned.equals(".") || cleaned.equals("..")
        || cleaned.contains("/") || cleaned.contains("\\")
        || cleaned.chars().anyMatch(Character::isISOControl)) {
      throw new StorageException("Filename must not contain path separators or control characters.");
    }
    return cleaned;
  }
}
