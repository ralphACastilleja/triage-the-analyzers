package team.projectpulse.notification.dto;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * One team's students who owe an item, as the dialog and the report pages group them.
 */
public record OwingTeamDto(Integer teamId,
                           String teamName,
                           List<OwingStudentDto> students) {
    // Snapshot collection structure; retain nulls for existing validation behavior.
    public OwingTeamDto {
        students = students == null ? null : Collections.unmodifiableList(new ArrayList<>(students));
    }
}
