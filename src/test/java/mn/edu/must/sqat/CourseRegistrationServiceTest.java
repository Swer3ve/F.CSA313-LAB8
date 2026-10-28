package mn.edu.must.sqat;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CourseRegistrationServiceTest {

    @Mock StudentRepository repository;
    @Mock EmailSender emailSender;

    CourseRegistrationService service;

    @BeforeEach
    void setUp() {
        service = new CourseRegistrationService(repository, emailSender);
    }

    private static Student student(boolean active, List<String> taken) {
        return new Student("S1", "s1@must.edu.mn", active, taken);
    }

    // A1
    @Test
    void register_activeStudentWithPrereqs_returnsOkSavesAndSendsEmail() {
        when(repository.findStudent("S1")).thenReturn(student(true, List.of("CS101")));
        when(repository.courseExists("CS201")).thenReturn(true);
        when(repository.prerequisites("CS201")).thenReturn(List.of("CS101"));

        assertEquals("OK", service.register("S1", "CS201"));

        verify(repository).saveRegistration("S1", "CS201");
        verify(emailSender).send(eq("s1@must.edu.mn"), anyString(), anyString());
    }

    // A2
    @Test
    void register_unknownStudent_returnsNoStudentAndSavesNothing() {
        // mock-ийн findStudent default нь null буцаадаг
        assertEquals("ERROR_NO_STUDENT", service.register("S1", "CS201"));

        verify(repository, never()).saveRegistration(anyString(), anyString());
        verify(emailSender, never()).send(anyString(), anyString(), anyString());
    }

    // A3
    @Test
    void register_inactiveStudent_returnsInactiveAndNeverTouchesEmail() {
        when(repository.findStudent("S1")).thenReturn(student(false, List.of()));

        assertEquals("ERROR_INACTIVE_STUDENT", service.register("S1", "CS201"));

        verifyNoInteractions(emailSender);
    }

    // A4
    @Test
    void register_missingPrerequisite_returnsPrerequisitesAndSavesNothing() {
        when(repository.findStudent("S1")).thenReturn(student(true, List.of()));
        when(repository.courseExists("CS201")).thenReturn(true);
        when(repository.prerequisites("CS201")).thenReturn(List.of("CS101"));

        assertEquals("ERROR_PREREQUISITES", service.register("S1", "CS201"));

        verify(repository, never()).saveRegistration(anyString(), anyString());
    }

    // A5
    @Test
    void register_emailSenderThrows_stillReturnsOkAndSaves() {
        when(repository.findStudent("S1")).thenReturn(student(true, List.of("CS101")));
        when(repository.courseExists("CS201")).thenReturn(true);
        when(repository.prerequisites("CS201")).thenReturn(List.of("CS101"));
        doThrow(new RuntimeException("SMTP down"))
                .when(emailSender).send(anyString(), anyString(), anyString());

        assertEquals("OK", service.register("S1", "CS201"));

        verify(repository).saveRegistration("S1", "CS201");
    }

    // A6: хоёр аргумент тус бүрийн null-ийг шалгана (мутант 3-ыг барихын тулд)
    @Test
    void register_nullArgument_throwsIllegalArgumentAndTouchesNothing() {
        assertThrows(IllegalArgumentException.class, () -> service.register(null, "CS201"));
        assertThrows(IllegalArgumentException.class, () -> service.register("S1", null));

        verifyNoInteractions(repository, emailSender);
    }

    // A7
    @Test
    void register_success_emailBodyContainsCourseCode() {
        when(repository.findStudent("S1")).thenReturn(student(true, List.of("CS101")));
        when(repository.courseExists("CS201")).thenReturn(true);
        when(repository.prerequisites("CS201")).thenReturn(List.of("CS101"));
        ArgumentCaptor<String> body = ArgumentCaptor.forClass(String.class);

        service.register("S1", "CS201");

        verify(emailSender).send(eq("s1@must.edu.mn"), anyString(), body.capture());
        assertTrue(body.getValue().contains("CS201"));
    }

    // A8: мөр бүр өөр stub ашиглах тул lenient()
    @ParameterizedTest
    @CsvSource({
        "ERROR_NO_STUDENT,false,true,true",
        "ERROR_INACTIVE_STUDENT,true,false,true",
        "ERROR_PREREQUISITES,true,true,false"
    })
    void register_errorCases_returnExpectedCode(
            String expected, boolean studentExists, boolean active, boolean prereqMet) {
        lenient().when(repository.findStudent("S1"))
                .thenReturn(studentExists ? student(active, prereqMet ? List.of("CS101") : List.of()) : null);
        lenient().when(repository.courseExists("CS201")).thenReturn(true);
        lenient().when(repository.prerequisites("CS201")).thenReturn(List.of("CS101"));

        assertEquals(expected, service.register("S1", "CS201"));

        verify(repository, never()).saveRegistration(anyString(), anyString());
    }
}