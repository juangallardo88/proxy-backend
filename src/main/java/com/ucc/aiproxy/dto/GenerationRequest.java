package com.ucc.aiproxy.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Petición de generación. El userId no viene en el body: el controlador
 * lo toma del header X-User-Id y lo asigna aquí.
 */
public class GenerationRequest {

    @NotBlank(message = "El prompt no puede estar vacío")
    @Size(max = 4000, message = "El prompt no puede superar 4000 caracteres")
    private String prompt;

    private String userId;

    public GenerationRequest() {
    }

    public GenerationRequest(String userId, String prompt) {
        this.userId = userId;
        this.prompt = prompt;
    }

    public String getPrompt() { return prompt; }
    public void setPrompt(String prompt) { this.prompt = prompt; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
}
