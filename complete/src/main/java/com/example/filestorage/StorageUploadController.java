package com.example.filestorage;

import java.util.stream.Collectors;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.filestorage.storage.StorageService;

@Controller
public class StorageUploadController {

  private final StorageService storageService;

  public StorageUploadController(StorageService storageService) {
    this.storageService = storageService;
  }

  @GetMapping("/")
  public String index(Model model) {
    model.addAttribute("files", storageService.loadAll().map(
        path -> MvcUriComponentsBuilder.fromMethodName(
            StorageDownloadController.class,
            "downloadFile",
            path.getFileName().toString())
            .build()
            .toUri()
            .toString())
        .collect(Collectors.toList()));

    return "uploadForm";
  }

  @PostMapping("/upload")
  public String handleFileUpload(@RequestParam("file") MultipartFile file,
      RedirectAttributes redirectAttributes) {

    storageService.store(file);
    redirectAttributes.addFlashAttribute("message",
        "You successfully uploaded " + file.getOriginalFilename() + "!");

    return "redirect:/";
  }

  @PostMapping("/files/{filename}/delete")
  public String deleteFile(@PathVariable String filename,
      RedirectAttributes redirectAttributes) {
    storageService.delete(filename);
    redirectAttributes.addFlashAttribute("message",
        "You successfully deleted " + filename + "!");
    return "redirect:/";
  }
}
