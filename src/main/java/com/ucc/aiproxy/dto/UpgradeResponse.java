package com.ucc.aiproxy.dto;

public record UpgradeResponse(
        String message,
        String previousPlan,
        String newPlan,
        String transactionId,
        String paymentMethod,
        String cardLast4,
        QuotaStatusResponse status
) {
}
