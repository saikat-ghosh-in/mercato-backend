package com.mercato.Schedular;

import com.mercato.Service.CartReservationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CartReservationCleanupJob {

    private final CartReservationService cartReservationService;

    @Scheduled(fixedDelayString = "${cart.reservation.cleanup.interval.ms}", initialDelay = 900000)
    public void releaseExpiredCartReservations() {
        log.debug("Running cart reservation cleanup job");
        cartReservationService.releaseExpired();
    }
}
