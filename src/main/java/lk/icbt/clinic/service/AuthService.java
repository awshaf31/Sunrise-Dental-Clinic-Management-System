package lk.icbt.clinic.service;

import lk.icbt.clinic.dao.StaffDao;
import lk.icbt.clinic.exception.AuthenticationFailedException;
import lk.icbt.clinic.model.Staff;
import lk.icbt.clinic.util.PasswordHasher;

/**
 * Verifies staff credentials. Only authorised staff may use the system, so
 * every other operation depends on this having succeeded first.
 */
public class AuthService {

    /**
     * Deliberately identical for an unknown user and a wrong password.
     * Telling the two apart would let anyone with the login page enumerate
     * valid usernames one guess at a time.
     */
    private static final String FAILURE_MESSAGE = "Invalid username or password";

    private final StaffDao staffDao;

    public AuthService(StaffDao staffDao) {
        this.staffDao = staffDao;
    }

    public Staff authenticate(String username, String password) {
        if (username == null || username.isBlank() || password == null || password.isBlank()) {
            throw new AuthenticationFailedException(FAILURE_MESSAGE);
        }

        Staff staff = staffDao.findByUsernameAndActive(username.trim())
                .orElseThrow(() -> new AuthenticationFailedException(FAILURE_MESSAGE));

        if (!PasswordHasher.matches(password, staff.getPasswordHash())) {
            throw new AuthenticationFailedException(FAILURE_MESSAGE);
        }

        return staff;
    }
}
