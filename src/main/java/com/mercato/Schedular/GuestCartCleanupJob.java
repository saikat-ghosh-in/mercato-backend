package com.mercato.Schedular;

import com.mercato.Repository.CartRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Component
@RequiredArgsConstructor
public class GuestCartCleanupJob {

    private final CartRepository cartRepository;

    @Scheduled(cron = "0 0 2 * * *", zone = "UTC")
    @Transactional
    public void purgeAbandonedGuestCarts() {
        Instant cutoff = Instant.now().minus(8, ChronoUnit.DAYS);
        cartRepository.deleteStaleGuestCarts(cutoff);
    }
}
