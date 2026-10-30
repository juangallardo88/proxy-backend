package com.ucc.aiproxy.config;

import com.ucc.aiproxy.proxy.QuotaProxyService;
import com.ucc.aiproxy.proxy.RateLimitProxyService;
import com.ucc.aiproxy.service.AIGenerationService;
import com.ucc.aiproxy.service.MockAIGenerationService;
import com.ucc.aiproxy.store.UsageStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.time.ZoneId;

/**
 * Aquí se arma la cadena de proxies:
 *   Cliente -> RateLimitProxyService -> QuotaProxyService -> MockAIGenerationService
 * El controlador solo conoce la interfaz AIGenerationService.
 */
@Configuration
public class AppConfig {

    @Bean
    public Clock clock(@Value("${app.timezone:America/Bogota}") String timezone) {
        return Clock.system(ZoneId.of(timezone));
    }

    @Bean
    public UsageStore usageStore(Clock clock) {
        return new UsageStore(clock);
    }

    @Bean
    public AIGenerationService aiGenerationService(UsageStore store) {
        AIGenerationService real = new MockAIGenerationService();
        AIGenerationService quota = new QuotaProxyService(real, store);
        return new RateLimitProxyService(quota, store);
    }
}
