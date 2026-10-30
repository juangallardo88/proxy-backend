package com.ucc.aiproxy.proxy;

import com.ucc.aiproxy.domain.UserAccount;
import com.ucc.aiproxy.dto.GenerationRequest;
import com.ucc.aiproxy.dto.GenerationResponse;
import com.ucc.aiproxy.exception.RateLimitExceededException;
import com.ucc.aiproxy.service.AIGenerationService;
import com.ucc.aiproxy.store.UsageStore;

/**
 * Proxy 1 de la cadena: controla cuántas requests por minuto hace cada usuario
 * según su plan. Si supera el límite lanza RateLimitExceededException, que el
 * manejador global convierte en 429 Too Many Requests con el header Retry-After.
 * Si pasa, delega en el siguiente eslabón (QuotaProxyService).
 */
public class RateLimitProxyService implements AIGenerationService {

    private static final System.Logger LOG = System.getLogger(RateLimitProxyService.class.getName());

    private final AIGenerationService next;
    private final UsageStore store;

    public RateLimitProxyService(AIGenerationService next, UsageStore store) {
        this.next = next;
        this.store = store;
    }

    @Override
    public GenerationResponse generate(GenerationRequest request) {
        UserAccount account = store.getOrCreate(request.getUserId());

        if (!account.tryConsumeRequest()) {
            long retryAfter = store.secondsUntilMinuteReset();
            LOG.log(System.Logger.Level.WARNING, "[RateLimitProxy] {0} bloqueado: {1} req/min (plan {2}). Retry-After={3}s",
                    account.getUserId(), account.getPlan().getRequestsPerMinute(), account.getPlan(), retryAfter);
            throw new RateLimitExceededException(account.getPlan().name(),
                    account.getPlan().getRequestsPerMinute(), retryAfter);
        }

        LOG.log(System.Logger.Level.INFO, "[RateLimitProxy] {0} request {1} de este minuto",
                account.getUserId(), account.getRequestsThisMinute());

        GenerationResponse response = next.generate(request);
        response.setRequestsRemainingThisMinute(account.getRemainingRequests());
        return response;
    }
}
