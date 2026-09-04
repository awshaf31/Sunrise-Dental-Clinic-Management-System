package lk.icbt.clinic.exception;

/** The supplied credentials did not identify an active member of staff. */
public class AuthenticationFailedException extends RuntimeException {
    public AuthenticationFailedException(String message) {
        super(message);
    }
}
