package com.ucc.aiproxy.store;

import com.ucc.aiproxy.domain.Plan;
import com.ucc.aiproxy.domain.UserAccount;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Almacenamiento en memoria de las cuentas de usuario.
 * Al reiniciar el servidor los datos se pierden (es un servicio simulado).
 */
public class UsageStore {

    private final Map<String, UserAccount> accounts = new ConcurrentHashMap<>();
    private final Clock clock;

    public UsageStore(Clock clock) {
        this.clock = clock;
    }

    public UserAccount getOrCreate(String userId) {
        return accounts.computeIfAbsent(userId, id -> new UserAccount(id, Plan.FREE));
    }

    public Collection<UserAccount> all() {
        return accounts.values();
    }

    public ZoneId zone() {
        return clock.getZone();
    }

    public LocalDate today() {
        return LocalDate.now(clock);
    }

    public ZonedDateTime now() {
        return ZonedDateTime.now(clock);
    }

    /** Siguiente reinicio del rate limit: el inicio del próximo minuto. */
    public ZonedDateTime nextMinuteReset() {
        return now().truncatedTo(ChronoUnit.MINUTES).plusMinutes(1);
    }

    /** Segundos que faltan para el próximo reinicio del rate limit (mínimo 1). */
    public long secondsUntilMinuteReset() {
        long millis = java.time.Duration.between(now(), nextMinuteReset()).toMillis();
        return Math.max(1, (long) Math.ceil(millis / 1000.0));
    }

    /** Siguiente reinicio de la cuota: el día 1 del próximo mes a medianoche. */
    public ZonedDateTime nextMonthlyReset() {
        return now().with(TemporalAdjusters.firstDayOfNextMonth()).truncatedTo(ChronoUnit.DAYS);
    }
}
