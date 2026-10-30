package com.ucc.aiproxy.dto;

import java.util.List;

public record UsageHistoryResponse(String userId, String plan, List<DailyUsageResponse> days, long totalTokens, int totalRequests) {
}
