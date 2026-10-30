package com.ucc.aiproxy.service;

import com.ucc.aiproxy.dto.GenerationRequest;
import com.ucc.aiproxy.dto.GenerationResponse;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Objeto real (RealSubject): simula un modelo de IA que genera texto.
 * Tarda 1200 ms y devuelve un texto aleatorio de una lista predefinida.
 */
public class MockAIGenerationService implements AIGenerationService {

    static final long SIMULATED_LATENCY_MS = 1200;

    private static final List<String> RESPUESTAS = List.of(
            "El patrón Proxy coloca un intermediario entre el cliente y el objeto real para controlar cómo y cuándo se accede a él.",
            "Una buena práctica al diseñar APIs es limitar la cantidad de peticiones por usuario para proteger los recursos del servidor.",
            "Los modelos de lenguaje dividen el texto en tokens; en español, un token equivale aproximadamente a cuatro caracteres.",
            "La carga diferida permite crear objetos costosos solo cuando realmente se necesitan, mejorando el rendimiento inicial.",
            "Encadenar proxies permite separar responsabilidades: uno controla la frecuencia de uso y otro controla el consumo total.",
            "Un código de estado 429 indica que el cliente envió demasiadas peticiones en poco tiempo y debe esperar antes de reintentar.",
            "Un código de estado 402 indica que se requiere un pago; aquí lo usamos cuando el usuario agota la cuota de su plan.",
            "Spring Boot facilita crear servicios REST con poca configuración gracias a sus valores por defecto y a la inyección de dependencias."
    );

    private final long latencyMs;

    public MockAIGenerationService() {
        this(SIMULATED_LATENCY_MS);
    }

    /** Constructor para pruebas, permite reducir la espera. */
    public MockAIGenerationService(long latencyMs) {
        this.latencyMs = latencyMs;
    }

    @Override
    public GenerationResponse generate(GenerationRequest request) {
        long start = System.currentTimeMillis();
        try {
            Thread.sleep(latencyMs);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("La generación fue interrumpida", e);
        }

        String text = RESPUESTAS.get(ThreadLocalRandom.current().nextInt(RESPUESTAS.size()));
        long promptTokens = TokenEstimator.estimate(request.getPrompt());
        long completionTokens = TokenEstimator.estimate(text);
        long elapsed = System.currentTimeMillis() - start;
        return new GenerationResponse(text, promptTokens, completionTokens, elapsed);
    }
}
