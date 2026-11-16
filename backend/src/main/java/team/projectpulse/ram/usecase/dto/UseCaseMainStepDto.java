package team.projectpulse.ram.usecase.dto;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public record UseCaseMainStepDto(
        Long id,
        String actor,
        String actionText,
        List<UseCaseExtensionDto> extensions) {
    // Snapshot collection structure; retain nulls for existing validation behavior.
    public UseCaseMainStepDto {
        extensions = extensions == null ? null : Collections.unmodifiableList(new ArrayList<>(extensions));
    }
}
