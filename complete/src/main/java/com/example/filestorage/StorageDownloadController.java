package com.example.filestorage;

import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import com.example.filestorage.storage.StorageFileNotFoundException;
import com.example.filestorage.storage.StorageService;

@Controller
public class StorageDownloadController {

  private final StorageService storageService;

  public StorageDownloadController(StorageService storageService) {
    this.storageService = storageService;
  }

  @GetMapping("/files/{filename:.+}")
  @ResponseBody
  public ResponseEntity<Resource> downloadFile(@PathVariable String filename) {
    Resource file = storageService.loadAsResource(filename);

    if (file == null) {
      return ResponseEntity.notFound().build();
    }

    return ResponseEntity.ok()
        .header(HttpHeaders.CONTENT_DISPOSITION,
            "attachment; filename=\"" + file.getFilename() + "\"")
        .body(file);
  }

  @ExceptionHandler(StorageFileNotFoundException.class)
  public ResponseEntity<?> handleStorageFileNotFound(StorageFileNotFoundException exc) {
    return ResponseEntity.notFound().build();
  }
}
