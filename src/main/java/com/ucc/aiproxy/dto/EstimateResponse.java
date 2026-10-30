package com.ucc.aiproxy.dto;

public record EstimateResponse(long promptTokens, long estimatedCompletionTokens, long estimatedTotalTokens) {
}
