package com.example.filestorage;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.springframework.boot.CommandLineRunner;

import com.example.filestorage.storage.StorageService;

class ApplicationStartupTests {

  @Test
  void startupInitializesStorageWithoutDeletingExistingData() throws Exception {
    StorageService storage = mock(StorageService.class);
    CommandLineRunner runner = new FileStorageApplication().init(storage);

    runner.run();

    verify(storage).init();
    verify(storage, never()).deleteAll();
  }
}
