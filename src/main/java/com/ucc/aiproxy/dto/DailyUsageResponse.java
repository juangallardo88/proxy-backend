package com.ucc.aiproxy.dto;

/** Uso de un día para la gráfica de barras. */
public record DailyUsageResponse(String date, String dayLabel, long tokens, int requests) {
}
