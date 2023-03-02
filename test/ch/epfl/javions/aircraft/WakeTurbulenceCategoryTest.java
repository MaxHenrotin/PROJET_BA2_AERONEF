package ch.epfl.javions.aircraft;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WakeTurbulenceCategoryTest {
    @Test
    void ofWorks(){
        assertEquals(WakeTurbulenceCategory.LIGHT,WakeTurbulenceCategory.of("L"));

        assertEquals(WakeTurbulenceCategory.MEDIUM,WakeTurbulenceCategory.of("M"));

        assertEquals(WakeTurbulenceCategory.HEAVY,WakeTurbulenceCategory.of("H"));

        assertEquals(WakeTurbulenceCategory.UNKNOWN,WakeTurbulenceCategory.of("S"));

        assertEquals(WakeTurbulenceCategory.UNKNOWN,WakeTurbulenceCategory.of("!üo"));

        assertEquals(WakeTurbulenceCategory.UNKNOWN,WakeTurbulenceCategory.of("LMH"));
    }

}