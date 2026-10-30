package com.ucc.aiproxy.controller;

import com.ucc.aiproxy.dto.EstimateRequest;
import com.ucc.aiproxy.dto.EstimateResponse;
import com.ucc.aiproxy.dto.GenerationRequest;
import com.ucc.aiproxy.dto.GenerationResponse;
import com.ucc.aiproxy.dto.QuotaStatusResponse;
import com.ucc.aiproxy.service.AIGenerationService;
import com.ucc.aiproxy.service.QuotaService;
import com.ucc.aiproxy.service.TokenEstimator;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ai")
public class AIController {

    private final AIGenerationService aiService; // es el RateLimitProxyService (inicio de la cadena)
    private final QuotaService quotaService;

    public AIController(AIGenerationService aiService, QuotaService quotaService) {
        this.aiService = aiService;
        this.quotaService = quotaService;
    }

    /** POST /api/ai/generate — pasa por la cadena completa de proxies. */
    @PostMapping("/generate")
    public ResponseEntity<GenerationResponse> generate(
            @RequestHeader(value = UserIdResolver.HEADER, required = false) String userHeader,
            @Valid @RequestBody GenerationRequest request) {

        String userId = UserIdResolver.resolve(userHeader);
        request.setUserId(userId);

        GenerationResponse response = aiService.generate(request);
        QuotaStatusResponse status = quotaService.status(userId);

        return ResponseEntity.ok()
                .header("X-RateLimit-Limit", String.valueOf(status.requestsPerMinuteLimit()))
                .header("X-RateLimit-Remaining", String.valueOf(status.requestsRemainingThisMinute()))
                .header("X-RateLimit-Reset", String.valueOf(status.secondsUntilRateLimitReset()))
                .body(response);
    }

    /** POST /api/ai/estimate — estimación de tokens antes de enviar (no consume cuota). */
    @PostMapping("/estimate")
    public EstimateResponse estimate(@RequestBody EstimateRequest request) {
        long prompt = TokenEstimator.estimate(request.getPrompt());
        long completion = prompt == 0 ? 0 : QuotaService.AVERAGE_COMPLETION_TOKENS;
        return new EstimateResponse(prompt, completion, prompt + completion);
    }
}
