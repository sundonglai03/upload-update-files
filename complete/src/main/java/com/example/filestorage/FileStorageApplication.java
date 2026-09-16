package com.example.filestorage;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import com.example.filestorage.storage.StorageProperties;
import com.example.filestorage.storage.StorageService;

@SpringBootApplication
@EnableConfigurationProperties(StorageProperties.class)
public class FileStorageApplication {

  public static void main(String[] args) {
    SpringApplication.run(FileStorageApplication.class, args);
  }

  @Bean
  CommandLineRunner init(StorageService storageService) {
    return args -> storageService.init();
  }
}
