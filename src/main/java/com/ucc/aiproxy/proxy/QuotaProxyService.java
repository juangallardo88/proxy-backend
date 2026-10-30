package com.ucc.aiproxy.proxy;

import com.ucc.aiproxy.domain.UserAccount;
import com.ucc.aiproxy.dto.GenerationRequest;
import com.ucc.aiproxy.dto.GenerationResponse;
import com.ucc.aiproxy.exception.QuotaExceededException;
import com.ucc.aiproxy.service.AIGenerationService;
import com.ucc.aiproxy.service.TokenEstimator;
import com.ucc.aiproxy.store.UsageStore;

/**
 * Proxy 2 de la cadena: controla la cuota mensual de tokens.
 * Antes de llamar al servicio real verifica que al usuario le queden tokens
 * para el prompt; si no, lanza QuotaExceededException (402 Payment Required).
 * Después de la generación descuenta los tokens consumidos y registra el uso diario.
 */
public class QuotaProxyService implements AIGenerationService {

    private static final System.Logger LOG = System.getLogger(QuotaProxyService.class.getName());

    private final AIGenerationService realService;
    private final UsageStore store;

    public QuotaProxyService(AIGenerationService realService, UsageStore store) {
        this.realService = realService;
        this.store = store;
    }

    @Override
    public GenerationResponse generate(GenerationRequest request) {
        UserAccount account = store.getOrCreate(request.getUserId());
        long estimatedPromptTokens = TokenEstimator.estimate(request.getPrompt());

        boolean exhausted = account.getRemainingTokens() == 0;
        if (exhausted || !account.hasTokensFor(estimatedPromptTokens)) {
            LOG.log(System.Logger.Level.WARNING, "[QuotaProxy] {0} sin cuota: usados {1} de {2}",
                    account.getUserId(), account.getTokensUsedThisMonth(), account.getPlan().getMonthlyTokens());
            throw new QuotaExceededException(account.getPlan().name(), account.getTokensUsedThisMonth(),
                    account.getPlan().getMonthlyTokens(), estimatedPromptTokens);
        }

        GenerationResponse response = realService.generate(request);

        // Registro del consumo: se descuentan los tokens del prompt y de la respuesta.
        account.registerUsage(response.getTotalTokens(), store.today());
        LOG.log(System.Logger.Level.INFO, "[QuotaProxy] {0} consumió {1} tokens. Total del mes: {2}",
                account.getUserId(), response.getTotalTokens(), account.getTokensUsedThisMonth());

        response.setPlan(account.getPlan().name());
        response.setTokensUsedThisMonth(account.getTokensUsedThisMonth());
        response.setTokensRemaining(account.getRemainingTokens());
        return response;
    }
}
