package org.example.ia_dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FileAnalysisResponse {
    private boolean success;
    private String message;
    private Map<String, Object> analysis;
    private String error;
}
