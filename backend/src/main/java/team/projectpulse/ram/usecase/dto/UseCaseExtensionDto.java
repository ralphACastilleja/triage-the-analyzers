package team.projectpulse.ram.usecase.dto;

import team.projectpulse.ram.usecase.ExtensionExit;
import team.projectpulse.ram.usecase.ExtensionKind;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public record UseCaseExtensionDto(
        Long id,
        String conditionText,
        ExtensionKind kind,
        ExtensionExit exit,
        List<UseCaseExtensionStepDto> steps) {
    // Snapshot collection structure; retain nulls for existing validation behavior.
    public UseCaseExtensionDto {
        steps = steps == null ? null : Collections.unmodifiableList(new ArrayList<>(steps));
    }
}
