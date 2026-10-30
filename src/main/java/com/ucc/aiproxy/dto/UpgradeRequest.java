package com.ucc.aiproxy.dto;

/**
 * Petición de upgrade. El pago es simulado: el frontend envía el método
 * y los últimos 4 dígitos de la tarjeta de prueba solo para mostrarlos en el recibo.
 */
public class UpgradeRequest {
    private String targetPlan = "PRO";
    private String paymentMethod = "CARD";
    private String cardLast4;

    public String getTargetPlan() { return targetPlan; }
    public void setTargetPlan(String targetPlan) { this.targetPlan = targetPlan; }
    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
    public String getCardLast4() { return cardLast4; }
    public void setCardLast4(String cardLast4) { this.cardLast4 = cardLast4; }
}
