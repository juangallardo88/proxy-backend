package com.ucc.aiproxy;

import com.ucc.aiproxy.domain.Plan;
import com.ucc.aiproxy.domain.UserAccount;
import com.ucc.aiproxy.dto.GenerationRequest;
import com.ucc.aiproxy.dto.GenerationResponse;
import com.ucc.aiproxy.exception.QuotaExceededException;
import com.ucc.aiproxy.exception.RateLimitExceededException;
import com.ucc.aiproxy.proxy.QuotaProxyService;
import com.ucc.aiproxy.proxy.RateLimitProxyService;
import com.ucc.aiproxy.service.AIGenerationService;
import com.ucc.aiproxy.service.MockAIGenerationService;
import com.ucc.aiproxy.store.UsageStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.*;

class ProxyChainTest {

    private UsageStore store;
    private AIGenerationService chain;

    @BeforeEach
    void setUp() {
        store = new UsageStore(Clock.system(ZoneId.of("America/Bogota")));
        AIGenerationService real = new MockAIGenerationService(0); // sin espera en pruebas
        chain = new RateLimitProxyService(new QuotaProxyService(real, store), store);
    }

    @Test
    void generaYDescuentaTokens() {
        GenerationResponse r = chain.generate(new GenerationRequest("u1", "Hola, explícame el patrón proxy"));
        assertNotNull(r.getText());
        assertTrue(r.getTotalTokens() > 0);
        assertEquals(r.getTotalTokens(), store.getOrCreate("u1").getTokensUsedThisMonth());
        assertEquals("FREE", r.getPlan());
        assertEquals(9, r.getRequestsRemainingThisMinute());
    }

    @Test
    void freeBloqueaLaRequest11Con429() {
        for (int i = 0; i < 10; i++) {
            chain.generate(new GenerationRequest("u2", "prompt " + i));
        }
        RateLimitExceededException ex = assertThrows(RateLimitExceededException.class,
                () -> chain.generate(new GenerationRequest("u2", "uno más")));
        assertTrue(ex.getRetryAfterSeconds() >= 1 && ex.getRetryAfterSeconds() <= 60);
    }

    @Test
    void resetDelMinutoPermiteVolverAEnviar() {
        for (int i = 0; i < 10; i++) {
            chain.generate(new GenerationRequest("u3", "p"));
        }
        store.getOrCreate("u3").resetMinute();
        assertDoesNotThrow(() -> chain.generate(new GenerationRequest("u3", "p")));
    }

    @Test
    void cuotaAgotadaDevuelve402() {
        UserAccount acc = store.getOrCreate("u4");
        acc.registerUsage(Plan.FREE.getMonthlyTokens(), store.today());
        assertThrows(QuotaExceededException.class,
                () -> chain.generate(new GenerationRequest("u4", "hola")));
    }

    @Test
    void enterpriseEsIlimitado() {
        store.getOrCreate("u5").setPlan(Plan.ENTERPRISE);
        for (int i = 0; i < 100; i++) {
            chain.generate(new GenerationRequest("u5", "p"));
        }
        assertEquals(-1, store.getOrCreate("u5").getRemainingTokens());
    }
}
