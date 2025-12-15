package org.example.ia_dashboard.service;

import org.example.ia_dashboard.Entity.File;
import org.example.ia_dashboard.Entity.User;
import org.example.ia_dashboard.Repository.FileRepository;
import org.example.ia_dashboard.Repository.UserRepository;
import org.example.ia_dashboard.dto.FileAnalysisResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.List;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class FileService {

    @Autowired
    private FileRepository fileRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PythonAnalyzerService pythonAnalyzerService;

    @Autowired
    private ObjectMapper objectMapper;

    public File store(MultipartFile file, String username) throws IOException {
        String fileName = StringUtils.cleanPath(file.getOriginalFilename());

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));

        File fileEntity = File.builder()
                .name(fileName)
                .type(file.getContentType())
                .data(file.getBytes())
                .user(user)
                .build();

        // Sauvegarder le fichier en base de données
        File savedFile = fileRepository.save(fileEntity);

        System.out.println("[Java] Envoi du fichier à Python pour analyse...");
        FileAnalysisResponse analysisResponse = pythonAnalyzerService.analyzeFile(
                file,
                username,
                savedFile.getId());

        if (analysisResponse != null && analysisResponse.isSuccess()) {
            System.out.println("[Java] Analyse réussie: " + analysisResponse.getMessage());
            try {
                // Convertir le résultat de l'analyse en JSON string pour le stocker
                String analysisJson = objectMapper.writeValueAsString(analysisResponse.getAnalysis());
                savedFile.setAnalysisResult(analysisJson);

                // SUPPRESSION DU CONTENU DU FICHIER POUR LIBÉRER DE L'ESPACE
                savedFile.setData(null);

                savedFile = fileRepository.save(savedFile);
            } catch (Exception e) {
                System.err.println("[Java] Erreur lors de la conversion du résultat d'analyse: " + e.getMessage());
            }
        } else {
            String errorMsg = (analysisResponse != null) ? analysisResponse.getError()
                    : "Réponse nulle du service d'analyse";
            System.err.println("[Java] Échec de l'analyse: " + errorMsg);
            savedFile.setAnalysisResult("Error: " + errorMsg);
            // On garde le fichier en cas d'erreur pour pouvoir réessayer plus tard si
            // besoin
            savedFile = fileRepository.save(savedFile);
        }

        return savedFile;
    }

    public File getFile(Long id) {
        return fileRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("File not found with id " + id));
    }

    public List<File> getAllFilesByUser(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));
        return fileRepository.findByUserId(user.getId());
    }
}
