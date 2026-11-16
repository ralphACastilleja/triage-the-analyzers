package team.projectpulse.system;

import org.junit.jupiter.api.Test;
import team.projectpulse.ram.document.template.DocumentTemplate;
import team.projectpulse.ram.requirement.dto.ArtifactLinkViewDto;
import team.projectpulse.ram.requirement.dto.ArtifactTraceability;
import team.projectpulse.rubric.dto.CriterionDto;
import team.projectpulse.rubric.dto.RubricDto;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TransferCollectionOwnershipTest {

    @Test
    void transferListsCannotBeChangedThroughEitherAlias() {
        List<ArtifactLinkViewDto> incoming = new ArrayList<>();
        incoming.add(null);
        ArtifactTraceability dto = new ArtifactTraceability(1L, incoming, null);

        incoming.clear();

        assertEquals(1, dto.incoming().size());
        assertNull(dto.incoming().getFirst());
        assertNull(dto.outgoing());
        assertThrows(UnsupportedOperationException.class, () -> dto.incoming().clear());
    }

    @Test
    void transferSetsKeepNullMembersWithoutSharingCollectionState() {
        Set<CriterionDto> criteria = new LinkedHashSet<>();
        criteria.add(null);
        RubricDto dto = new RubricDto(1, "Rubric", criteria, 2);

        criteria.clear();

        assertEquals(1, dto.criteria().size());
        assertTrue(dto.criteria().contains(null));
        assertThrows(UnsupportedOperationException.class, () -> dto.criteria().clear());
    }

    @Test
    void templateOwnsItsListAndNormalizesAbsentSections() {
        DocumentTemplate template = new DocumentTemplate();
        List<DocumentTemplate.SectionTemplate> sections = new ArrayList<>();
        sections.add(new DocumentTemplate.SectionTemplate());
        template.setSections(sections);

        sections.clear();
        template.getSections().clear();

        assertEquals(1, template.getSections().size());
        template.setSections(null);
        assertTrue(template.getSections().isEmpty());
    }
}
