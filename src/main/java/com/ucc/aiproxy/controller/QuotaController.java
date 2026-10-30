package com.ucc.aiproxy.controller;

import com.ucc.aiproxy.dto.QuotaStatusResponse;
import com.ucc.aiproxy.dto.UpgradeRequest;
import com.ucc.aiproxy.dto.UpgradeResponse;
import com.ucc.aiproxy.dto.UsageHistoryResponse;
import com.ucc.aiproxy.service.QuotaService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/quota")
public class QuotaController {

    private final QuotaService quotaService;

    public QuotaController(QuotaService quotaService) {
        this.quotaService = quotaService;
    }

    /** GET /api/quota/status — tokens usados, restantes, resetDate y plan actual. */
    @GetMapping("/status")
    public QuotaStatusResponse status(@RequestHeader(value = UserIdResolver.HEADER, required = false) String userHeader) {
        return quotaService.status(UserIdResolver.resolve(userHeader));
    }

    /** GET /api/quota/history — uso diario de los últimos 7 días. */
    @GetMapping("/history")
    public UsageHistoryResponse history(@RequestHeader(value = UserIdResolver.HEADER, required = false) String userHeader) {
        return quotaService.history(UserIdResolver.resolve(userHeader));
    }

    /** POST /api/quota/upgrade — upgrade de plan (FREE -> PRO) con pago simulado. */
    @PostMapping("/upgrade")
    public UpgradeResponse upgrade(@RequestHeader(value = UserIdResolver.HEADER, required = false) String userHeader,
                                   @RequestBody(required = false) UpgradeRequest request) {
        return quotaService.upgrade(UserIdResolver.resolve(userHeader), request);
    }
}
