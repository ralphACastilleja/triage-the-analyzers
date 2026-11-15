package team.projectpulse.system;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Modifier;

import static org.junit.jupiter.api.Assertions.assertTrue;

class StatusCodeTest {

    @Test
    void constantsHolderDoesNotExposeAConstructor() throws NoSuchMethodException {
        assertTrue(Modifier.isPrivate(StatusCode.class.getDeclaredConstructor().getModifiers()),
                "StatusCode contains only constants and must not expose an instantiable API");
    }
}
