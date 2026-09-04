package lk.icbt.clinic.service;

import lk.icbt.clinic.dao.StaffDao;
import lk.icbt.clinic.exception.AuthenticationFailedException;
import lk.icbt.clinic.model.Staff;
import lk.icbt.clinic.model.StaffRole;
import lk.icbt.clinic.util.PasswordHasher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private StaffDao staffDao;

    private AuthService service;

    @BeforeEach
    void setUp() {
        service = new AuthService(staffDao);
    }

    private Staff staffWithPassword(String plaintext) {
        return new Staff("reception", PasswordHasher.hash(plaintext), "Nimali Perera", StaffRole.RECEPTIONIST);
    }

    @Test
    @DisplayName("authenticates a member of staff with the correct password")
    void authenticatesValidCredentials() {
        when(staffDao.findByUsernameAndActive("reception")).thenReturn(Optional.of(staffWithPassword("Recept@123")));

        Staff authenticated = service.authenticate("reception", "Recept@123");

        assertThat(authenticated.getUsername()).isEqualTo("reception");
        assertThat(authenticated.getRole()).isEqualTo(StaffRole.RECEPTIONIST);
    }

    @Test
    @DisplayName("rejects a wrong password")
    void rejectsWrongPassword() {
        when(staffDao.findByUsernameAndActive("reception")).thenReturn(Optional.of(staffWithPassword("Recept@123")));

        assertThatThrownBy(() -> service.authenticate("reception", "wrong"))
                .isInstanceOf(AuthenticationFailedException.class);
    }

    @Test
    @DisplayName("rejects an unknown username")
    void rejectsUnknownUser() {
        when(staffDao.findByUsernameAndActive("ghost")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.authenticate("ghost", "anything"))
                .isInstanceOf(AuthenticationFailedException.class);
    }

    @Test
    @DisplayName("gives the same message whether the user or the password was wrong")
    void doesNotRevealWhetherUsernameExists() {
        when(staffDao.findByUsernameAndActive("reception")).thenReturn(Optional.of(staffWithPassword("Recept@123")));
        when(staffDao.findByUsernameAndActive("ghost")).thenReturn(Optional.empty());

        String wrongPassword = catchMessage(() -> service.authenticate("reception", "nope"));
        String unknownUser = catchMessage(() -> service.authenticate("ghost", "nope"));

        assertThat(wrongPassword).isEqualTo(unknownUser);
    }

    @Test
    @DisplayName("a blank password is rejected without consulting the database")
    void rejectsBlankPassword() {
        assertThatThrownBy(() -> service.authenticate("reception", "  "))
                .isInstanceOf(AuthenticationFailedException.class);
    }

    private String catchMessage(Runnable action) {
        try {
            action.run();
            throw new AssertionError("expected authentication to fail");
        } catch (AuthenticationFailedException e) {
            return e.getMessage();
        }
    }
}
