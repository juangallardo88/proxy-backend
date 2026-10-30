package com.ucc.aiproxy.scheduler;

import com.ucc.aiproxy.domain.UserAccount;
import com.ucc.aiproxy.store.UsageStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Tareas programadas:
 *  - Cada minuto (segundo 0) reinicia el contador de requests por minuto.
 *  - El día 1 de cada mes a las 00:00 reinicia la cuota mensual de tokens.
 */
@Component
public class QuotaResetScheduler {

    private static final Logger log = LoggerFactory.getLogger(QuotaResetScheduler.class);

    private final UsageStore store;

    public QuotaResetScheduler(UsageStore store) {
        this.store = store;
    }

    @Scheduled(cron = "0 * * * * *", zone = "${app.timezone:America/Bogota}")
    public void resetRateLimits() {
        store.all().forEach(UserAccount::resetMinute);
        log.debug("Rate limit reiniciado para {} usuarios", store.all().size());
    }

    @Scheduled(cron = "0 0 0 1 * *", zone = "${app.timezone:America/Bogota}")
    public void resetMonthlyQuotas() {
        store.all().forEach(UserAccount::resetMonth);
        log.info("Cuota mensual reiniciada para {} usuarios", store.all().size());
    }
}
