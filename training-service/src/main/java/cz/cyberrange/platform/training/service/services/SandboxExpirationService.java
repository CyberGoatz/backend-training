package cz.cyberrange.platform.training.service.services;

import cz.cyberrange.platform.training.api.exceptions.MicroserviceApiException;
import cz.cyberrange.platform.training.api.responses.SandboxLockInfo;
import cz.cyberrange.platform.training.persistence.model.TrainingRun;
import cz.cyberrange.platform.training.service.services.api.SandboxApiService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

/**
 * Expires training runs whose sandbox lock session has ended.
 */
@Service
public class SandboxExpirationService {

    private static final Logger LOG = LoggerFactory.getLogger(SandboxExpirationService.class);

    private final TrainingRunService trainingRunService;
    private final SandboxApiService sandboxApiService;
    private final boolean enabled;

    public SandboxExpirationService(TrainingRunService trainingRunService,
                                    SandboxApiService sandboxApiService,
                                    @Value("${training.sandbox.expiration.enabled:true}") boolean enabled) {
        this.trainingRunService = trainingRunService;
        this.sandboxApiService = sandboxApiService;
        this.enabled = enabled;
    }

    @Scheduled(fixedDelayString = "${training.sandbox.expiration.scan-delay-ms:60000}",
            initialDelayString = "${training.sandbox.expiration.initial-delay-ms:60000}")
    public void expireSandboxSessions() {
        if (!enabled) {
            return;
        }
        expireRunningRuns();
        cleanupExpiredRuns();
        cleanupFinishedRuns();
    }

    private void expireRunningRuns() {
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        for (TrainingRun trainingRun : trainingRunService.findRunningCloudTrainingRunsWithSandboxAllocation()) {
            Integer allocationUnitId = trainingRun.getSandboxInstanceAllocationId();
            try {
                SandboxLockInfo lock = sandboxApiService.getSandboxAllocationUnitLock(allocationUnitId);
                if (lock == null || lock.getExpiresAt() == null || lock.getExpiresAt().isAfter(now)) {
                    continue;
                }
                TrainingRun expiredRun = trainingRunService.expireTrainingRunSandbox(trainingRun);
                cleanupExpiredRun(expiredRun);
            } catch (MicroserviceApiException ex) {
                if (ex.getStatusCode() == HttpStatus.NOT_FOUND) {
                    LOG.warn("Sandbox lock is missing for running training run {} and allocation unit {}; expiring run.",
                            trainingRun.getId(), allocationUnitId);
                    TrainingRun expiredRun = trainingRunService.expireTrainingRunSandbox(trainingRun);
                    cleanupExpiredRun(expiredRun);
                    continue;
                }
                LOG.warn("Failed to process sandbox expiry for training run {} and allocation unit {}.",
                        trainingRun.getId(), allocationUnitId, ex);
            } catch (RuntimeException ex) {
                LOG.warn("Failed to process sandbox expiry for training run {} and allocation unit {}.",
                        trainingRun.getId(), allocationUnitId, ex);
            }
        }
    }

    private void cleanupExpiredRuns() {
        for (TrainingRun trainingRun : trainingRunService.findExpiredCloudTrainingRunsWithSandboxAllocation()) {
            cleanupTrainingRunSandbox(trainingRun);
        }
    }

    private void cleanupExpiredRun(TrainingRun trainingRun) {
        cleanupTrainingRunSandbox(trainingRun);
    }

    private void cleanupFinishedRuns() {
        for (TrainingRun trainingRun : trainingRunService.findFinishedCloudTrainingRunsWithSandboxAllocation()) {
            cleanupTrainingRunSandbox(trainingRun);
        }
    }

    private void cleanupTrainingRunSandbox(TrainingRun trainingRun) {
        Integer allocationUnitId = trainingRun.getSandboxInstanceAllocationId();
        if (allocationUnitId == null) {
            return;
        }
        try {
            sandboxApiService.cleanupSandboxAllocationUnit(allocationUnitId, true, true);
            trainingRunService.clearTrainingRunSandbox(trainingRun);
        } catch (MicroserviceApiException ex) {
            if (ex.getStatusCode() == HttpStatus.NOT_FOUND) {
                LOG.warn("Sandbox allocation unit {} for training run {} is already missing; clearing stale sandbox references.",
                        allocationUnitId, trainingRun.getId());
                trainingRunService.clearTrainingRunSandbox(trainingRun);
                return;
            }
            LOG.warn("Failed to cleanup sandbox allocation unit {} for training run {}.",
                    allocationUnitId, trainingRun.getId(), ex);
        }
    }
}
