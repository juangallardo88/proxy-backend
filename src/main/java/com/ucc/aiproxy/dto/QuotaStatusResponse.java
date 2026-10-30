package com.ucc.aiproxy.dto;

/**
 * Estado de cuota de un usuario. Los valores -1 significan "ilimitado".
 */
public record QuotaStatusResponse(
        String userId,
        String plan,
        long tokensUsed,
        long tokensRemaining,
        long monthlyTokenLimit,
        String resetDate,
        int requestsThisMinute,
        int requestsPerMinuteLimit,
        int requestsRemainingThisMinute,
        long secondsUntilRateLimitReset,
        String rateLimitResetAt
) {
}
