package com.ucc.aiproxy.service;

import com.ucc.aiproxy.dto.GenerationRequest;
import com.ucc.aiproxy.dto.GenerationResponse;

/**
 * Interfaz común (Subject del patrón Proxy).
 * La implementan el servicio real y los dos proxies, así el cliente
 * no sabe si habla con el objeto real o con un proxy.
 */
public interface AIGenerationService {
    GenerationResponse generate(GenerationRequest request);
}
