package com.decathlon.ara.v2.domain.enumeration;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum DeploymentValidationStatus {
    CREATED(DeploymentValidationProgress.INITIAL),
    PENDING(DeploymentValidationProgress.IN_PROGRESS),
    RUNNING(DeploymentValidationProgress.IN_PROGRESS),
    INCOMPLETE(DeploymentValidationProgress.FINISHED),
    WARNING(DeploymentValidationProgress.FINISHED),
    FAILURE(DeploymentValidationProgress.FINISHED),
    SUCCESS(DeploymentValidationProgress.FINISHED);

    private DeploymentValidationProgress progress;

    public enum DeploymentValidationProgress {
        INITIAL,
        IN_PROGRESS,
        FINISHED
    }
}
