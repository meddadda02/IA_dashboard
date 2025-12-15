package org.example.ia_dashboard.controller;

import org.example.ia_dashboard.dto.FileAnalysisResponse;
import org.example.ia_dashboard.service.FileService;
import org.example.ia_dashboard.service.PythonAnalyzerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/files")
public class FileController {

    @Autowired
    private FileService fileService;

    @Autowired
    private PythonAnalyzerService pythonAnalyzerService;
    @Autowired
    private com.fasterxml.jackson.databind.ObjectMapper objectMapper;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadFile(@RequestParam("file") MultipartFile file, Authentication authentication) {
        System.out.println("[Java] Received upload request for file: " + file.getOriginalFilename());
        try {
            String username = authentication.getName();
            System.out.println("[Java] Authenticated user: " + username);
            org.example.ia_dashboard.Entity.File savedFile = fileService.store(file, username);

            java.util.Map<String, Object> response = new java.util.HashMap<>();
            response.put("message", "Uploaded the file successfully: " + file.getOriginalFilename());
            response.put("fileId", savedFile.getId());

            if (savedFile.getAnalysisResult() != null) {
                System.out.println("[Java] Raw analysis result from DB: " + savedFile.getAnalysisResult());
                try {
                    // Parse the JSON string back to an object so it's embedded properly
                    Object analysis = objectMapper.readValue(savedFile.getAnalysisResult(), Object.class);
                    response.put("analysis", analysis);
                } catch (Exception e) {
                    // Fallback if parsing fails
                    System.err.println("[Java] JSON parse error: " + e.getMessage());
                    response.put("analysis", savedFile.getAnalysisResult());
                }
            }

            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED)
                    .body(java.util.Collections.singletonMap("error",
                            "Could not upload the file: " + file.getOriginalFilename() + ". Error: " + e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getFile(@PathVariable Long id, Authentication authentication) {
        try {
            org.example.ia_dashboard.Entity.File file = fileService.getFile(id);
            // Check if user is authorized to view this file
            if (!file.getUser().getUsername().equals(authentication.getName())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied");
            }

            java.util.Map<String, Object> response = new java.util.HashMap<>();
            response.put("id", file.getId());
            response.put("name", file.getName());
            response.put("type", file.getType());

            if (file.getAnalysisResult() != null) {
                try {
                    Object analysis = objectMapper.readValue(file.getAnalysisResult(), Object.class);
                    response.put("analysis", analysis);
                } catch (Exception e) {
                    response.put("analysis", file.getAnalysisResult());
                }
            }

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("File not found");
        }
    }

    @GetMapping("/user")
    public ResponseEntity<java.util.List<java.util.Map<String, Object>>> getUserFiles(Authentication authentication) {
        java.util.List<org.example.ia_dashboard.Entity.File> files = fileService
                .getAllFilesByUser(authentication.getName());
        java.util.List<java.util.Map<String, Object>> response = files.stream().map(file -> {
            java.util.Map<String, Object> map = new java.util.HashMap<>();
            map.put("id", file.getId());
            map.put("name", file.getName());
            map.put("type", file.getType());
            // We don't send the full analysis here to keep it light, or we could
            return map;
        }).collect(java.util.stream.Collectors.toList());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/analyzer/status")
    public ResponseEntity<?> checkAnalyzerStatus() {
        boolean isOnline = pythonAnalyzerService.isPythonServiceOnline();
        return ResponseEntity.ok().body(new Object() {
            public boolean online = isOnline;
            public String message = isOnline ? "Python analyzer service is online"
                    : "Python analyzer service is offline";
        });
    }
}
