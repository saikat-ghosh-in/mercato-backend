package com.mercato.Schedular;

import com.mercato.Entity.EcommUser;
import com.mercato.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(
        prefix = "mercato.account.cleanup",
        name = "enabled",
        havingValue = "true",
        matchIfMissing = true
)
public class UserCleanupScheduler {

    private final UserRepository userRepository;

    @Value("${mercato.account.cleanup.retention-days:7}")
    private int retentionDays;

    /**
     * Runs daily at 2 AM UTC (configurable via cron expression)
     * Permanently deletes users who have been deactivated for more than configured retention days
     */
    @Scheduled(cron = "${mercato.account.cleanup.cron:0 0 2 * * *}")
    @Transactional
    public void cleanupDeactivatedUsers() {
        log.info("Starting scheduled cleanup of deactivated users (retention: {} days)", retentionDays);

        List<EcommUser> usersWithMissingDeactivationDate = userRepository
                .findByEnabledFalseAndDeactivatedAtIsNull();

        if (!usersWithMissingDeactivationDate.isEmpty()) {
            log.warn("Found {} users with enabled=false but deactivatedAt=null. Setting deactivatedAt to current time.",
                    usersWithMissingDeactivationDate.size());

            usersWithMissingDeactivationDate.forEach(user -> {
                user.setDeactivatedAt(Instant.now());
                userRepository.save(user);
                log.info("Fixed deactivatedAt for user: {} (userId: {})",
                        user.getUsername(), user.getUserId());
            });
        }

        Instant cutoffDate = Instant.now().minus(retentionDays, ChronoUnit.DAYS);

        List<EcommUser> usersToDelete = userRepository
                .findByEnabledFalseAndDeactivatedAtBefore(cutoffDate);

        log.info("Found {} deactivated users eligible for deletion (deactivated > {} days ago)",
                usersToDelete.size(), retentionDays);

        usersToDelete.forEach(user -> {
            log.info("Permanently deleting user: {} with userId:{} (deactivated at: {})",
                    user.getUsername(), user.getUserId(), user.getDeactivatedAt());
            userRepository.delete(user);
        });

        log.info("Cleanup completed. Deleted {} users", usersToDelete.size());
    }
}