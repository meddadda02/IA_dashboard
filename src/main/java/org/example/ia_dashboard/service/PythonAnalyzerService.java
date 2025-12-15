package org.example.ia_dashboard.service;

import org.example.ia_dashboard.dto.FileAnalysisResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import org.springframework.beans.factory.annotation.Autowired;

@Service
public class PythonAnalyzerService {

    @Value("${python.analyzer.url:http://localhost:8000}")
    private String pythonAnalyzerUrl;

    private final RestTemplate restTemplate;

    @Autowired
    public PythonAnalyzerService(com.fasterxml.jackson.databind.ObjectMapper objectMapper) {
        this.restTemplate = new RestTemplate();
        // Manually add the converter with our custom ObjectMapper
        // This avoids using the deprecated AbstractJackson2HttpMessageConverter
        // directly
        // and ensures we use the configured ObjectMapper
        org.springframework.http.converter.json.MappingJackson2HttpMessageConverter converter = new org.springframework.http.converter.json.MappingJackson2HttpMessageConverter(
                objectMapper);
        this.restTemplate.getMessageConverters().add(0, converter);
    }

    /**
     * Envoie un fichier au service Python pour analyse
     */
    public FileAnalysisResponse analyzeFile(MultipartFile file, String username, Long fileId) {
        try {
            // Vérifier que le service Python est en ligne
            if (!isPythonServiceOnline()) {
                return FileAnalysisResponse.builder()
                        .success(false)
                        .error("Le service d'analyse Python n'est pas disponible")
                        .build();
            }

            // Préparer la requête multipart
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            MultiValueMap<String, Object> requestBody = new LinkedMultiValueMap<>();

            // Ajouter le fichier
            ByteArrayResource fileResource = new ByteArrayResource(file.getBytes()) {
                @Override
                public String getFilename() {
                    return file.getOriginalFilename();
                }
            };
            requestBody.add("file", fileResource);

            // Ajouter les métadonnées
            requestBody.add("username", username);
            requestBody.add("fileId", fileId != null ? fileId.toString() : "");

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);

            // Envoyer la requête au service Python
            String url = pythonAnalyzerUrl + "/api/analyze";
            System.out.println("[Java] Envoi du fichier à Python: " + url);

            ResponseEntity<FileAnalysisResponse> response = restTemplate.postForEntity(
                    url,
                    requestEntity,
                    FileAnalysisResponse.class);

            System.out.println("[Java] Réponse de Python reçue: " + response.getStatusCode());
            FileAnalysisResponse body = response.getBody();
            if (body != null) {
                System.out.println("[Java] Body success: " + body.isSuccess());
                System.out.println("[Java] Analysis map present: " + (body.getAnalysis() != null));
                if (body.getAnalysis() != null) {
                    System.out.println("[Java] Analysis map size: " + body.getAnalysis().size());
                    System.out.println("[Java] Analysis keys: " + body.getAnalysis().keySet());
                }
            }
            return body;

        } catch (IOException e) {
            System.err.println("[Java] Erreur lors de la lecture du fichier: " + e.getMessage());
            return FileAnalysisResponse.builder()
                    .success(false)
                    .error("Erreur lors de la lecture du fichier: " + e.getMessage())
                    .build();
        } catch (Exception e) {
            System.err.println("[Java] Erreur lors de la communication avec Python: " + e.getMessage());
            return FileAnalysisResponse.builder()
                    .success(false)
                    .error("Erreur lors de la communication avec le service d'analyse: " + e.getMessage())
                    .build();
        }
    }

    /**
     * Vérifie que le service Python est en ligne
     */
    public boolean isPythonServiceOnline() {
        try {
            String url = pythonAnalyzerUrl + "/health";
            // Use exchange with Void.class to ignore the body and avoid parsing errors
            ResponseEntity<Void> response = restTemplate.exchange(url, HttpMethod.GET, null, Void.class);
            return response.getStatusCode() == HttpStatus.OK;
        } catch (Exception e) {
            System.err.println("[Java] Service Python non disponible: " + e.getMessage());
            return false;
        }
    }
}
